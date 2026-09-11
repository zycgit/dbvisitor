/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands.admin;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.partition.request.LoadPartitionsReq;
import net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandKeys;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.LoadOptionContext;
import net.hasor.dbvisitor.adapter.milvus.parser.MilvusParser.LoadOptionsContext;
import net.hasor.dbvisitor.driver.AdapterRequest;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.parseLiteral;
import static net.hasor.dbvisitor.adapter.milvus.commands.MilvusCommandUtils.readName;

/** Validates SQL loading options before submitting either native request. */
final class MilvusLoadOptions {
    private Integer      numReplicas;
    private Boolean      refresh;
    private Boolean      skipLoadDynamicField;
    private List<String> loadFields;
    private List<String> resourceGroups;

    static MilvusLoadOptions read(LoadOptionsContext context, AtomicInteger args, AdapterRequest request) throws SQLException {
        MilvusLoadOptions options = new MilvusLoadOptions();
        if (context == null) {
            return options;
        }
        Set<String> names = new HashSet<>();
        for (LoadOptionContext option : context.loadOption()) {
            String key = readName(option.identifier()).toLowerCase(Locale.ROOT);
            if (!names.add(key)) {
                throw new SQLException("Duplicate LOAD option: " + key);
            }
            Object value = parseLiteral(option.literal(), args, request);
            switch (key) {
                case MilvusCommandKeys.NUM_REPLICAS:
                    options.numReplicas = replicas(value);
                    break;
                case MilvusCommandKeys.REFRESH:
                    options.refresh = bool(value, key);
                    break;
                case MilvusCommandKeys.SKIP_LOAD_DYNAMIC_FIELD:
                    options.skipLoadDynamicField = bool(value, key);
                    break;
                case MilvusCommandKeys.LOAD_FIELDS:
                    options.loadFields = names(value, key);
                    break;
                case MilvusCommandKeys.RESOURCE_GROUPS:
                    options.resourceGroups = names(value, key);
                    break;
                default:
                    throw new SQLException("Unknown LOAD option: " + key);
            }
        }
        return options;
    }

    void apply(LoadCollectionReq.LoadCollectionReqBuilder builder) {
        if (numReplicas != null) {
            builder.numReplicas(numReplicas);
        }
        if (refresh != null) {
            builder.refresh(refresh);
        }
        if (skipLoadDynamicField != null) {
            builder.skipLoadDynamicField(skipLoadDynamicField);
        }
        if (loadFields != null) {
            builder.loadFields(loadFields);
        }
        if (resourceGroups != null) {
            builder.resourceGroups(resourceGroups);
        }
    }

    void apply(LoadPartitionsReq.LoadPartitionsReqBuilder builder) {
        if (numReplicas != null) {
            builder.numReplicas(numReplicas);
        }
        if (refresh != null) {
            builder.refresh(refresh);
        }
        if (skipLoadDynamicField != null) {
            builder.skipLoadDynamicField(skipLoadDynamicField);
        }
        if (loadFields != null) {
            builder.loadFields(loadFields);
        }
        if (resourceGroups != null) {
            builder.resourceGroups(resourceGroups);
        }
    }

    private static int replicas(Object value) throws SQLException {
        try {
            if (value instanceof Number) {
                int count = new BigDecimal(value.toString()).intValueExact();
                if (count > 0) {
                    return count;
                }
            }
        } catch (ArithmeticException | NumberFormatException error) {
            throw new SQLException("num_replicas requires a positive INT32 integer.", error);
        }
        throw new SQLException("num_replicas requires a positive INT32 integer.");
    }

    private static boolean bool(Object value, String key) throws SQLException {
        if (!(value instanceof Boolean)) {
            throw new SQLException(key + " requires a boolean.");
        }
        return (Boolean) value;
    }

    private static List<String> names(Object value, String key) throws SQLException {
        if (value instanceof String) {
            try {
                List<String> decoded = new ArrayList<>();
                for (JsonElement item : JsonParser.parseString((String) value).getAsJsonArray()) {
                    if (!item.isJsonPrimitive() || !item.getAsJsonPrimitive().isString()) {
                        throw new SQLException(key + " requires a JSON array of strings.");
                    }
                    decoded.add(item.getAsString());
                }
                value = decoded;
            } catch (RuntimeException error) {
                throw new SQLException(key + " requires a JSON array of strings.", error);
            }
        }
        if (value instanceof String[]) {
            value = Arrays.asList((String[]) value);
        }
        if (!(value instanceof Iterable<?>)) {
            throw new SQLException(key + " requires a list of non-empty strings.");
        }
        List<String> result = new ArrayList<>();
        for (Object item : (Iterable<?>) value) {
            if (!(item instanceof String) || ((String) item).trim().isEmpty()) {
                throw new SQLException(key + " requires a list of non-empty strings.");
            }
            result.add((String) item);
        }
        return result;
    }
}
