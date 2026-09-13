/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic;

import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.dbvisitor.driver.AdapterType;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

/** Mapping and conversions for one explicitly projected search result. */
final class ElasticFieldTypes {
    private final ObjectMapper mapper;
    private final Map<String, JsonNode> definitions = new LinkedHashMap<>();
    private final Set<String> normalizedDates = new HashSet<>();
    private final Set<String> scriptFields = new LinkedHashSet<>();

    private ElasticFieldTypes(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    static ElasticFieldTypes load(ElasticCmd cmd, ElasticOperation operation, ObjectMapper mapper, List<String> fields, Map<String, Object> body) throws Exception {
        ElasticFieldTypes types = new ElasticFieldTypes(mapper);
        Object scripts = body.get("script_fields");
        if (scripts instanceof Map) {
            for (Object alias : ((Map<?, ?>) scripts).keySet()) {
                types.scriptFields.add(String.valueOf(alias));
            }
        }
        List<String> sourceFields = new ArrayList<>(fields);
        sourceFields.removeAll(types.scriptFields);
        if (sourceFields.isEmpty()) {
            return types;
        }
        String path = operation.getQueryPath();
        String prefix = path.substring(0, path.lastIndexOf("/_search"));
        // The optional ES6 type segment is irrelevant to the index mapping endpoint.
        int slash = prefix.indexOf('/', 1);
        if (slash >= 0) {
            prefix = prefix.substring(0, slash);
        }
        Response response = cmd.getClient().performRequest(new Request("GET", prefix + "/_mapping"));
        try (InputStream input = response.getEntity().getContent()) {
            JsonNode root = mapper.readTree(input);
            if (root == null || !root.isObject()) {
                throw new SQLException("Invalid Elasticsearch mapping response");
            }
            Iterator<JsonNode> indices = root.elements();
            while (indices.hasNext()) {
                JsonNode mappings = indices.next().get("mappings");
                if (mappings == null || !mappings.isObject()) {
                    throw new SQLException("Elasticsearch mapping response is missing mappings");
                }
                if (mappings.has("properties")) {
                    types.collect(mappings.path("properties"), sourceFields);
                } else {
                    Iterator<JsonNode> documents = mappings.elements();
                    while (documents.hasNext()) {
                        JsonNode document = documents.next();
                        if (document.has("properties")) {
                            types.collect(document.path("properties"), sourceFields);
                        }
                    }
                }
            }
        }
        return types;
    }

    private void collect(JsonNode properties, List<String> fields) throws SQLException {
        for (String field : fields) {
            JsonNode definition = null;
            JsonNode current = properties;
            for (String part : field.split("\\.")) {
                definition = current.get(part);
                if (definition == null) {
                    break;
                }
                current = definition.path("properties");
            }
            if (definition == null) {
                continue;
            }
            JsonNode previous = definitions.get(field);
            if (previous != null && (!previous.path("type").equals(definition.path("type"))
                    || !previous.path("format").equals(definition.path("format"))
                    || !previous.path("doc_values").equals(definition.path("doc_values")))) {
                throw new SQLException("Conflicting Elasticsearch mappings for projected field '" + field + "'");
            }
            definitions.put(field, definition);
        }
    }

    /** Never replace a caller's existing docvalue format. */
    Map<String, Object> prepareBody(Map<String, Object> body) {
        Map<String, Object> prepared = new LinkedHashMap<>(body);
        Object existing = body.get("docvalue_fields");
        if (existing != null && !(existing instanceof Collection)) {
            return prepared;
        }
        List<Object> docValues = existing == null ? new ArrayList<>() : new ArrayList<>((Collection<?>) existing);
        for (Map.Entry<String, JsonNode> entry : definitions.entrySet()) {
            String field = entry.getKey();
            JsonNode definition = entry.getValue();
            if (!isDate(definition) || !definition.path("doc_values").asBoolean(true) || containsField(docValues, field)) {
                continue;
            }
            String format = "date_nanos".equals(definition.path("type").asText()) ? "strict_date_optional_time_nanos" : "strict_date_time";
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("field", field);
            request.put("format", format);
            docValues.add(request);
            normalizedDates.add(field);
        }
        if (!docValues.isEmpty()) {
            prepared.put("docvalue_fields", docValues);
        }
        return prepared;
    }

    private static boolean containsField(List<Object> values, String field) {
        for (Object value : values) {
            Object name = value instanceof Map ? ((Map<?, ?>) value).get("field") : value;
            // A wildcard request can already supply this field in a caller-selected format.
            if (name instanceof String && (field.equals(name) || ((String) name).contains("*") || ((String) name).contains("?"))) {
                return true;
            }
        }
        return false;
    }

    Object value(JsonNode hit, String field) throws SQLException {
        if (hit == null) {
            return null;
        }
        if (scriptFields.contains(field)) {
            JsonNode value = hit.path("fields").path(field);
            if (value.isArray()) {
                if (value.isEmpty()) {
                    return null;
                }
                if (value.size() == 1) {
                    value = value.get(0);
                }
            }
            return convert(value, null);
        }
        if ("_ID".equals(field)) {
            return hit.hasNonNull("_id") ? hit.get("_id").asText() : null;
        }
        if ("_DOC".equals(field)) {
            return hit.path("_source").toString();
        }
        JsonNode source = hit.path("_source");
        for (String part : field.split("\\.")) {
            source = source.path(part);
        }
        JsonNode definition = definitions.get(field);
        if (!source.isArray() && !source.isNull() && !source.isMissingNode() && normalizedDates.contains(field)) {
            JsonNode normalized = hit.path("fields").path(field);
            if (normalized.isArray() && normalized.size() == 1) {
                return dateValue(normalized.get(0), definition);
            }
        }
        return convert(source, definition);
    }

    private Object convert(JsonNode value, JsonNode definition) throws SQLException {
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        if (value.isArray()) {
            List<Object> values = new ArrayList<>();
            for (JsonNode item : value) {
                values.add(convert(item, definition));
            }
            return values;
        }
        if (definition != null && "binary".equals(definition.path("type").asText())) {
            try {
                return Base64.getDecoder().decode(value.asText());
            } catch (IllegalArgumentException error) {
                throw new SQLException("Invalid Base64 value in an Elasticsearch binary field", error);
            }
        }
        if (isDate(definition)) {
            return dateValue(value, definition);
        }
        if (definition != null && value.isNumber()) {
            switch (definition.path("type").asText()) {
                case "byte":
                    return (byte) value.intValue();
                case "short":
                    return (short) value.intValue();
                case "integer":
                    return value.intValue();
                case "long":
                    return value.longValue();
                case "float":
                    return value.floatValue();
                case "double":
                    return value.doubleValue();
                default:
                    break;
            }
        }
        return mapper.convertValue(value, Object.class);
    }

    private static Object dateValue(JsonNode value, JsonNode definition) {
        String format = definition.path("format").asText("strict_date_optional_time||epoch_millis");
        try {
            if (value.isNumber() && format.contains("epoch_millis")) {
                return Timestamp.from(Instant.ofEpochMilli(value.longValue()));
            }
            String text = value.asText();
            try {
                return Timestamp.from(OffsetDateTime.parse(text).toInstant());
            } catch (java.time.DateTimeException ignored) {
                return Timestamp.from(LocalDate.parse(text).atStartOfDay().toInstant(ZoneOffset.UTC));
            }
        } catch (RuntimeException unsupportedFormat) {
            // doc_values=false/custom source formats remain readable as their original JSON scalar.
            return value.isNumber() ? value.numberValue() : value.asText();
        }
    }

    String columnType(String field, Object value) {
        if (value instanceof List) {
            return AdapterType.Array;
        }
        JsonNode definition = definitions.get(field);
        if (value instanceof Timestamp || (value == null && isDate(definition) && normalizedDates.contains(field))) {
            return AdapterType.SqlTimestamp;
        }
        if (definition != null && "binary".equals(definition.path("type").asText())) {
            return AdapterType.Bytes;
        }
        return value instanceof Boolean ? AdapterType.Boolean : value instanceof Byte ? AdapterType.Byte
                : value instanceof Short ? AdapterType.Short : value instanceof Integer ? AdapterType.Int
                : value instanceof Long ? AdapterType.Long : value instanceof Float ? AdapterType.Float
                : value instanceof Number ? AdapterType.Double : AdapterType.String;
    }

    String elementType(String field) {
        JsonNode definition = definitions.get(field);
        if (definition == null) {
            return AdapterType.Unknown;
        }
        switch (definition.path("type").asText()) {
            case "byte": return AdapterType.Byte;
            case "short": return AdapterType.Short;
            case "integer": return AdapterType.Int;
            case "long": return AdapterType.Long;
            case "float": return AdapterType.Float;
            case "double": return AdapterType.Double;
            case "boolean": return AdapterType.Boolean;
            case "date":
            case "date_nanos": return AdapterType.SqlTimestamp;
            case "binary": return AdapterType.Bytes;
            default: return AdapterType.String;
        }
    }

    private static boolean isDate(JsonNode definition) {
        return definition != null && ("date".equals(definition.path("type").asText()) || "date_nanos".equals(definition.path("type").asText()));
    }
}
