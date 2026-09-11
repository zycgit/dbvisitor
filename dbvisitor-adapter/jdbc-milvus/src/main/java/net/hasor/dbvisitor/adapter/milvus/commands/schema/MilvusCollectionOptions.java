package net.hasor.dbvisitor.adapter.milvus.commands.schema;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.PropertiesListContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.PropertyContext;
import net.hasor.dbvisitor.driver.AdapterRequest;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.getIdentifier;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readProperties;

/** Typed CREATE TABLE options; schema fields and function parameters are read before these values. */
final class MilvusCollectionOptions {
    private MilvusCollectionOptions() {
    }

    static void apply(CreateCollectionReq.CreateCollectionReqBuilder builder, CreateCollectionReq.CollectionSchema schema, PropertiesListContext options, AtomicInteger args, AdapterRequest request) throws SQLException {
        long partitionKeys = schema.getFieldSchemaList().stream().filter(f -> Boolean.TRUE.equals(f.getIsPartitionKey())).count();
        long clusteringKeys = schema.getFieldSchemaList().stream().filter(f -> Boolean.TRUE.equals(f.getIsClusteringKey())).count();
        if (partitionKeys > 1 || clusteringKeys > 1) {
            throw new SQLException("A collection allows at most one partition key and one clustering key.");
        }
        if (options == null) {
            return;
        }
        Set<String> names = new HashSet<>();
        for (PropertyContext option : options.property()) {
            String name = getIdentifier(option.getChild(0).getText()).toLowerCase(Locale.ROOT);
            if (!names.add(name)) {
                throw new SQLException("Duplicate collection option: " + name);
            }
        }
        for (Map.Entry<String, Object> entry : readProperties(args, request, options).entrySet()) {
            String name = entry.getKey().toLowerCase(Locale.ROOT);
            Object value = entry.getValue();
            switch (name) {
                case MilvusCommandKeys.COLLECTION_CONSISTENCY_LEVEL:
                    try {
                        builder.consistencyLevel(ConsistencyLevel.valueOf(stringValue(name, value).toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException e) {
                        throw new SQLException("Unknown consistency_level: " + value, e);
                    }
                    break;
                case MilvusCommandKeys.NUM_PARTITIONS:
                    if (partitionKeys == 0) {
                        throw new SQLException("num_partitions requires a PARTITION KEY field.");
                    }
                    builder.numPartitions(positiveInteger(name, value));
                    break;
                case MilvusCommandKeys.NUM_SHARDS:
                    builder.numShards(positiveInteger(name, value));
                    break;
                case MilvusCommandKeys.DESCRIPTION:
                    builder.description(stringValue(name, value));
                    break;
                default:
                    throw new SQLException("Unknown collection option: " + name);
            }
        }
    }

    private static String stringValue(String name, Object value) throws SQLException {
        if (!(value instanceof String)) {
            throw new SQLException(name + " requires a non-null string.");
        }
        return (String) value;
    }

    static void append(StringBuilder sql, DescribeCollectionResp description) {
        List<String> options = new ArrayList<>();
        if (description.getConsistencyLevel() != null) {
            options.add(MilvusCommandKeys.COLLECTION_CONSISTENCY_LEVEL + "='" + description.getConsistencyLevel().name() + "'");
        }
        if (description.getShardsNum() != null && description.getShardsNum() > 0) {
            options.add(MilvusCommandKeys.NUM_SHARDS + "=" + description.getShardsNum());
        }
        boolean partitioned = description.getCollectionSchema().getFieldSchemaList().stream().anyMatch(field -> Boolean.TRUE.equals(field.getIsPartitionKey()));
        if (partitioned && description.getNumOfPartitions() != null && description.getNumOfPartitions() > 0) {
            options.add(MilvusCommandKeys.NUM_PARTITIONS + "=" + description.getNumOfPartitions());
        }
        if (description.getDescription() != null && !description.getDescription().isEmpty()) {
            options.add(MilvusCommandKeys.DESCRIPTION + "='" + description.getDescription().replace("'", "''") + "'");
        }
        if (!options.isEmpty()) {
            sql.append(" WITH (").append(String.join(", ", options)).append(')');
        }
    }

    private static int positiveInteger(String name, Object value) throws SQLException {
        if (value instanceof Number) {
            try {
                int number = new BigDecimal(value.toString()).intValueExact();
                if (number > 0) {
                    return number;
                }
            } catch (ArithmeticException | NumberFormatException e) {
                throw new SQLException(name + " requires a positive INT32 integer.", e);
            }
        }
        throw new SQLException(name + " requires a positive INT32 integer.");
    }
}
