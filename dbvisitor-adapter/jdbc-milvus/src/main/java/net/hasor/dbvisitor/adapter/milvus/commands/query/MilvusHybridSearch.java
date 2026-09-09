package net.hasor.dbvisitor.adapter.milvus.commands.query;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.milvus.grpc.FieldSchema;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.IndexParam.MetricType;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.vector.request.AnnSearchReq;
import io.milvus.v2.service.vector.request.HybridSearchReq;
import io.milvus.v2.service.vector.request.ranker.RRFRanker;
import io.milvus.v2.service.vector.request.ranker.WeightedRanker;
import net.hasor.dbvisitor.adapter.milvus.MilvusCmd;
import net.hasor.dbvisitor.adapter.milvus.MilvusRequest;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusExpression.Filter;
import net.hasor.dbvisitor.adapter.milvus.mapping.MilvusVectorCodec;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.AnnClauseContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.HybridClauseContext;
import net.hasor.dbvisitor.driver.AdapterRequest;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.*;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusVector.readVectorValue;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusVector.vectorMetric;

/** Multiple ANN candidates, one server-ranked result group (NQ is always one). */
final class MilvusHybridSearch {
    private MilvusHybridSearch() {
    }

    record Candidate(String field, Object vector, MetricType metric, long limit, Map<String, Object> params) {
    }

    static List<Candidate> bind(HybridClauseContext clause, AtomicInteger args, AdapterRequest request) throws SQLException {
        List<Candidate> candidates = new ArrayList<>();
        if (clause == null)
            return candidates;
        for (AnnClauseContext ann : clause.annClause()) {
            String field = readName(ann.fieldName);
            Object vector = readVectorValue(ann.vectorValue(), args, request);
            long limit = readLimit(ann.limit, args, request);
            MetricType metric = vectorMetric(ann.distanceOperator());
            Map<String, Object> params = readProperties(args, request, ann.propertiesList());
            if (params.containsKey(MilvusCommandKeys.OFFSET))
                throw new SQLException("Hybrid candidates do not support OFFSET.");
            if (params.containsKey(MilvusCommandKeys.METRIC_TYPE) && !metric.name().equalsIgnoreCase(String.valueOf(params.get(MilvusCommandKeys.METRIC_TYPE)))) {
                throw new SQLException("Candidate " + MilvusCommandKeys.METRIC_TYPE + " must agree with its distance operator.");
            }
            params.put(MilvusCommandKeys.METRIC_TYPE, metric.name());
            candidates.add(new Candidate(field, vector, metric, limit, params));
        }
        return candidates;
    }

    static HybridSearchReq build(MilvusCmd cmd, String collection, String partition, Filter filter, List<String> outputs, Map<String, FieldSchema> fields, List<Candidate> candidates, long limit, long offset, Map<String, Object> properties, MilvusRequest request) throws SQLException {
        List<AnnSearchReq> searches = new ArrayList<>();
        for (Candidate candidate : candidates) {
            searches.add(AnnSearchReq.builder().vectorFieldName(candidate.field()).filter(filter.expression()).filterTemplateValues(filter.parameters()).limit(candidate.limit()).vectors(Collections.singletonList(MilvusVectorCodec.searchValue(fields.get(candidate.field()), candidate.vector(), candidate.metric()))).metricType(candidate.metric()).params(propertiesToJson(candidate.params())).build());
        }
        HybridSearchReq.HybridSearchReqBuilder builder = HybridSearchReq.builder().databaseName(cmd.getCatalog()).collectionName(collection).searchRequests(searches).limit(limit).offset(offset).outFields(outputs).ranker(ranker(properties, searches.size()));
        if (partition != null)
            builder.partitionNames(Collections.singletonList(partition));
        if (request.getConsistencyLevel() != null)
            builder.consistencyLevel(ConsistencyLevel.valueOf(request.getConsistencyLevel().name()));
        return builder.build();
    }

    private static CreateCollectionReq.Function ranker(Map<String, Object> properties, int candidates) throws SQLException {
        Map<String, Object> options = new LinkedHashMap<>(properties);
        String name = String.valueOf(options.remove(MilvusCommandKeys.RERANKER));
        CreateCollectionReq.Function result;
        if ("rrf".equalsIgnoreCase(name)) {
            RRFRanker.RRFRankerBuilder builder = RRFRanker.builder();
            if (options.containsKey(MilvusCommandKeys.RRF_K)) {
                long k = integerBound(options.remove(MilvusCommandKeys.RRF_K), "RRF " + MilvusCommandKeys.RRF_K, 1);
                if (k > Integer.MAX_VALUE)
                    throw new SQLException("RRF " + MilvusCommandKeys.RRF_K + " exceeds integer range.");
                builder.k((int) k);
            }
            result = builder.build();
        } else if ("weighted".equalsIgnoreCase(name)) {
            try {
                JsonArray input = JsonParser.parseString(String.valueOf(options.remove(MilvusCommandKeys.WEIGHTS))).getAsJsonArray();
                if (input.size() != candidates)
                    throw new IllegalArgumentException("one weight required per candidate");
                List<Float> weights = new ArrayList<>();
                for (com.google.gson.JsonElement item : input) {
                    float weight = item.getAsFloat();
                    if (!Float.isFinite(weight) || weight < 0 || weight > 1)
                        throw new IllegalArgumentException(MilvusCommandKeys.WEIGHTS + " must be in [0,1]");
                    weights.add(weight);
                }
                result = WeightedRanker.builder().weights(weights).build();
            } catch (RuntimeException e) {
                throw new SQLException("Weighted rerank requires " + MilvusCommandKeys.WEIGHTS + "='[0.7,0.3]' with one finite weight per candidate.", e);
            }
        } else {
            throw new SQLException("Hybrid Search requires WITH (" + MilvusCommandKeys.RERANKER + "='rrf'|'weighted', ...).");
        }
        if (!options.isEmpty())
            throw new SQLException("Unknown hybrid rerank options: " + options.keySet());
        return result;
    }
}
