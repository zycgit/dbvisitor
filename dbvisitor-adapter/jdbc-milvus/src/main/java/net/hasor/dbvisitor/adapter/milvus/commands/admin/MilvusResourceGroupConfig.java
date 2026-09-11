/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands.admin;
import java.sql.SQLException;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.milvus.common.resourcegroup.ResourceGroupConfig;

/** Canonical Protobuf JSON shared by CONFIG input and SHOW output. */
final class MilvusResourceGroupConfig {
    private static final Gson JSON = new Gson();

    private MilvusResourceGroupConfig() {
    }

    static JsonObject object(Object value) throws SQLException {
        try {
            JsonElement json;
            if (value instanceof String) {
                json = JsonParser.parseString((String) value);
            } else if (value instanceof Map || value instanceof JsonObject) {
                json = JSON.toJsonTree(value);
            } else {
                throw new SQLException("CONFIG requires a JSON object, Map, or JSON object string.");
            }
            if (!json.isJsonObject()) {
                throw new SQLException("CONFIG requires a JSON object.");
            }
            return json.getAsJsonObject();
        } catch (RuntimeException e) {
            throw new SQLException("Invalid resource-group CONFIG JSON.", e);
        }
    }

    static ResourceGroupConfig read(JsonObject object) throws SQLException {
        io.milvus.grpc.ResourceGroupConfig.Builder builder = io.milvus.grpc.ResourceGroupConfig.newBuilder();
        try {
            // The official parser validates names, nested shapes and integer ranges.
            JsonFormat.parser().merge(object.toString(), builder);
            return new ResourceGroupConfig(builder.build());
        } catch (InvalidProtocolBufferException e) {
            throw new SQLException("Invalid resource-group CONFIG: " + e.getMessage(), e);
        }
    }

    static String write(ResourceGroupConfig config) throws SQLException {
        if (config == null) {
            return null;
        }
        try {
            return JsonFormat.printer().includingDefaultValueFields().omittingInsignificantWhitespace().print(config.toGRPC());
        } catch (InvalidProtocolBufferException e) {
            throw new SQLException("Cannot serialize resource-group CONFIG.", e);
        }
    }
}
