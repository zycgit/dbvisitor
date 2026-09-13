/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic;

import java.io.InputStream;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.dbvisitor.driver.AdapterCursor;
import net.hasor.dbvisitor.driver.AdapterMetadata;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;

/** Describes declared index mappings, never sampled document values. */
final class ElasticMetadata {
    private ElasticMetadata() {
    }

    static AdapterCursor tables(ElasticCmd client, ObjectMapper json, String catalog, String schema, String pattern, String[] types) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (!namespaceMatches(catalog, schema) || types != null && !Arrays.asList(types).contains("TABLE")) {
            return AdapterMetadata.tables(rows);
        }
        JsonNode mappings = mappings(client, json);
        for (String index : names(mappings)) {
            if (AdapterMetadata.matchesPattern(index, pattern)) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("TABLE_NAME", index);
                row.put("TABLE_TYPE", "TABLE");
                rows.add(row);
            }
        }
        return AdapterMetadata.tables(rows);
    }

    static AdapterCursor columns(ElasticCmd client, ObjectMapper json, String catalog, String schema, String table, String column) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (!namespaceMatches(catalog, schema)) {
            return AdapterMetadata.columns(rows);
        }
        JsonNode mappings = mappings(client, json);
        for (String index : names(mappings)) {
            if (!AdapterMetadata.matchesPattern(index, table)) {
                continue;
            }
            JsonNode mapping = mappings.path(index).path("mappings");
            Map<String, JsonNode> fields = new TreeMap<>();
            if (mapping.has("properties")) {
                fields(mapping.get("properties"), "", fields);
            } else {
                for (JsonNode typed : mapping) {
                    fields(typed.path("properties"), "", fields);
                }
            }
            int ordinal = 0;
            for (Map.Entry<String, JsonNode> field : fields.entrySet()) {
                ordinal++;
                if (!AdapterMetadata.matchesPattern(field.getKey(), column)) {
                    continue;
                }
                JsonNode definition = field.getValue();
                String type = definition.path("type").asText(definition.has("properties") ? "object" : "unknown");
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("TABLE_NAME", index);
                row.put("COLUMN_NAME", field.getKey());
                row.put("DATA_TYPE", jdbcType(type));
                row.put("TYPE_NAME", type);
                row.put("NULLABLE", DatabaseMetaData.columnNullableUnknown);
                row.put("ORDINAL_POSITION", ordinal);
                row.put("IS_NULLABLE", "");
                row.put("IS_AUTOINCREMENT", "NO");
                row.put("IS_GENERATEDCOLUMN", "NO");
                if (definition.has("dims")) {
                    row.put("REMARKS", "dims=" + definition.get("dims").asText());
                }
                rows.add(row);
            }
        }
        return AdapterMetadata.columns(rows);
    }

    private static boolean namespaceMatches(String catalog, String schema) {
        return (catalog == null || catalog.isEmpty()) && AdapterMetadata.matchesPattern("", schema);
    }

    private static JsonNode mappings(ElasticCmd client, ObjectMapper json) throws SQLException {
        try {
            Response response = client.getClient().performRequest(new Request("GET", "/_mapping"));
            int status = response.getStatusLine().getStatusCode();
            if (status >= 300) {
                throw new SQLException("Cannot read Elasticsearch index mappings: HTTP " + status, "E" + status);
            }
            try (InputStream input = response.getEntity().getContent()) {
                JsonNode result = json.readTree(input);
                if (result == null || !result.isObject()) {
                    throw new SQLException("Invalid Elasticsearch mapping response");
                }
                for (JsonNode index : result) {
                    if (!index.path("mappings").isObject()) {
                        throw new SQLException("Missing Elasticsearch index mappings");
                    }
                }
                return result;
            }
        } catch (ResponseException error) {
            throw new SQLException(error.getMessage(), "E" + error.getResponse().getStatusLine().getStatusCode(), error);
        } catch (IOException error) {
            throw new SQLException("Cannot read Elasticsearch index mappings", error);
        }
    }

    private static List<String> names(JsonNode object) {
        List<String> names = new ArrayList<>();
        object.fieldNames().forEachRemaining(names::add);
        Collections.sort(names);
        return names;
    }

    private static void fields(JsonNode properties, String prefix, Map<String, JsonNode> result) throws SQLException {
        for (String name : names(properties)) {
            JsonNode field = properties.get(name);
            String path = prefix + name;
            JsonNode previous = result.putIfAbsent(path, field);
            if (previous != null && !previous.equals(field)) {
                throw new SQLException("Conflicting Elasticsearch mapping types for field " + path);
            }
            fields(field.path("properties"), path + ".", result);
        }
    }

    private static int jdbcType(String type) {
        switch (type) {
            case "boolean": return Types.BOOLEAN;
            case "byte": return Types.TINYINT;
            case "short": return Types.SMALLINT;
            case "integer": return Types.INTEGER;
            case "long": return Types.BIGINT;
            case "half_float":
            case "float": return Types.REAL;
            case "double":
            case "scaled_float": return Types.DOUBLE;
            case "date":
            case "date_nanos": return Types.TIMESTAMP;
            case "binary": return Types.VARBINARY;
            case "text":
            case "keyword":
            case "constant_keyword":
            case "wildcard": return Types.VARCHAR;
            default: return Types.OTHER;
        }
    }
}
