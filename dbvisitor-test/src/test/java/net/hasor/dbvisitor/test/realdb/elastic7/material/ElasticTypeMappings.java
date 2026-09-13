/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7.material;

import java.util.LinkedHashMap;
import java.util.Map;
import java.sql.SQLException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

/** Explicit Elasticsearch field schemas for shared JDBC type contracts. */
public final class ElasticTypeMappings {
    private ElasticTypeMappings() {
    }

    public static Map<String, Object> binary() {
        return fields("binary", "binary_value", "varbinary_value", "blob_value");
    }

    public static Map<String, Object> time() {
        Map<String, Object> properties = fields("date", "date_value", "timestamp_value", "local_date_ts", "local_datetime_ts");
        // Elasticsearch has no time-only field type; preserve wall-clock values as HH:mm:ss text.
        properties.put("time_value", Map.of("type", "keyword"));
        properties.put("local_time_ts", Map.of("type", "keyword"));
        return properties;
    }

    public static Map<String, Object> arrays() {
        Map<String, Object> properties = fields("integer", "int_array", "array_no_annotation", "array_jdbc_type", "array_type_handler",
                "array_number_special", "array_full_annotated");
        properties.put("float_array", Map.of("type", "float"));
        properties.put("string_array", Map.of("type", "keyword"));
        return properties;
    }

    public static void addArrays(JdbcTemplate jdbc, String environment, String index) throws SQLException {
        String endpoint = "/" + index + "/_mapping" + ("es6".equals(environment) ? "/_doc" : "");
        try {
            String schema = new ObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValueAsString(Map.of("properties", arrays()));
            jdbc.execute("PUT " + endpoint + " " + schema);
        } catch (JsonProcessingException error) {
            throw new SQLException("Cannot serialize Elasticsearch array mapping", error);
        }
    }

    private static Map<String, Object> fields(String type, String... names) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("id", Map.of("type", "integer"));
        for (String name : names) {
            properties.put(name, Map.of("type", type));
        }
        return properties;
    }
}
