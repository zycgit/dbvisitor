/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.dialect.provider;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.features.VectorSqlDialect;
import net.hasor.dbvisitor.lambda.core.MetricType;

/**
 * ES8 方言
 * @author 赵永春 (zyc@hasor.net)
 * @version 2026-02-03
 */
public class Elastic8Dialect extends AbstractElasticDialect implements VectorSqlDialect {
    public static final SqlDialect    DEFAULT  = new Elastic8Dialect();
    private final       List<KnnSort> knnSorts = new ArrayList<>();

    private static class KnnSort {
        String field;
        Object vector;
        String vectorTerm;
    }

    @Override
    public AbstractElasticDialect newBuilder() {
        return new Elastic8Dialect();
    }

    @Override
    protected String getSearchEndpoint() {
        return buildPath("_search");
    }

    @Override
    protected String getInsertEndpoint() {
        return buildPath("_doc");
    }

    @Override
    protected String getUpdateEndpoint() {
        return buildPath("_update_by_query");
    }

    @Override
    protected String getDeleteEndpoint() {
        return buildPath("_delete_by_query");
    }

    private String buildPath(String action) {
        StringBuilder sb = new StringBuilder();
        sb.append("/");
        sb.append(StringUtils.isBlank(this.index) ? "*" : this.index);
        if (StringUtils.isNotBlank(action)) {
            sb.append("/").append(action);
        }
        return sb.toString();
    }

    // --- VectorSqlDialect impl ---

    @Override
    public void addOrderByVector(String col, String colTerm, Object vector, String vectorTerm, MetricType metricType) {
        if (metricType != MetricType.L2 && metricType != MetricType.COSINE && metricType != MetricType.IP) {
            throw new UnsupportedOperationException("Elastic8Dialect native kNN only supports L2, COSINE, and IP. For other metrics, consider using Elastic7Dialect (script_score).");
        }

        KnnSort knn = new KnnSort();
        knn.field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
        knn.vector = vector;
        knn.vectorTerm = vectorTerm;
        this.knnSorts.add(knn);
    }

    @Override
    public BoundSql buildSelect(boolean useQualifier) throws SQLException {
        BoundSql boundSql = super.buildSelect(useQualifier);
        if (this.knnSorts.isEmpty()) {
            return boundSql;
        }

        List<Object> knnArgs = new ArrayList<>();
        StringBuilder knnJson = new StringBuilder();
        knnJson.append("\"knn\": [");
        for (int i = 0; i < this.knnSorts.size(); i++) {
            KnnSort s = this.knnSorts.get(i);
            if (i > 0) {
                knnJson.append(", ");
            }

            String val = formatKnnValue(s.vector, s.vectorTerm, knnArgs);

            knnJson.append("{");
            knnJson.append("\"field\": \"").append(s.field).append("\", ");
            knnJson.append("\"query_vector\": ").append(val).append(", ");
            knnJson.append("\"k\": 10, \"num_candidates\": 100");
            knnJson.append("}");
        }
        knnJson.append("], ");

        String originalSql = boundSql.getSqlString();
        int jsonStart = originalSql.indexOf('{');
        String newSql = originalSql.substring(0, jsonStart + 1) + knnJson.toString() + originalSql.substring(jsonStart + 1);

        List<Object> allArgs = new ArrayList<>(knnArgs);
        Collections.addAll(allArgs, boundSql.getArgs());

        return new BoundSql.BoundSqlObj(newSql, allArgs.toArray());
    }

    private String formatKnnValue(Object value, String valueTerm, List<Object> paramArgs) {
        if (StringUtils.isNotBlank(valueTerm)) {
            return valueTerm;
        }
        paramArgs.add(value);
        return "?";
    }

    @Override
    public void addConditionForVectorRange(ConditionLogic logic, String col, String colTerm, Object vector, String vectorTerm,//
            Object threshold, String thresholdTerm, MetricType metricType) {
        this.conditions.addSegment((delimited, dialect) -> {
            String field = StringUtils.isNotBlank(colTerm) ? colTerm : col;
            String vecVal = formatValue(vector, vectorTerm);
            String thrVal = formatValue(threshold, thresholdTerm);

            String scriptCode;
            switch (metricType) {
                case L2:
                    scriptCode = "l2norm(params.vector, '" + field + "') < " + thrVal;
                    break;
                case COSINE:
                    scriptCode = "(1 - cosineSimilarity(params.vector, '" + field + "')) < " + thrVal;
                    break;
                default:
                    throw new UnsupportedOperationException("Metric " + metricType + " not supported for range condition in Elastic8.");
            }

            return "{ \"script\": { \"script\": { \"source\": \"" + scriptCode + "\", \"params\": { \"vector\": " + vecVal + " } } } }";
        });
    }
}
