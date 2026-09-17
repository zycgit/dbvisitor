/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7.material;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import net.hasor.dbvisitor.dynamic.MacroRegistry;
import net.hasor.dbvisitor.dynamic.RuleRegistry;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;

/** Native Elasticsearch material with deterministic field types and search visibility. */
public final class ElasticMatrixFixture implements AutoCloseable {
    private final String          index = "nxn_matrix_" + UUID.randomUUID().toString().replace("-", "");
    private       Connection      connection;
    private       JdbcTemplate    jdbc;
    private       MappingRegistry registry;
    private       boolean         created;

    public JdbcTemplate open(String environment) throws SQLException {
        String properties = """
                {"properties": {"id": {"type": "integer"},"name": {"type": "keyword"},
                 "age": {"type": "integer"},"email": {"type": "keyword"},
                 "create_time": {"type": "date"}}}
                """;
        return open(environment, UserInfo.class, properties);
    }

    public JdbcTemplate open(String environment, Class<?> entityType, String properties) throws SQLException {
        if (connection != null) {
            return jdbc;
        }
        OneApiDataSourceManager.assumeCurrentDataSource(environment);
        connection = OneApiDataSourceManager.getConnection(environment);
        registry = new MappingRegistry();
        jdbc = new JdbcTemplate(connection, registry, null);
        String mapping = "es6".equals(environment) ? "{\"_doc\": " + properties + "}" : properties;
        jdbc.execute("PUT /" + index + " {\"mappings\": " + mapping + "}");
        created = true;
        registry.loadEntityAsTable(entityType, index);
        return jdbc;
    }

    public Connection connection() {
        return connection;
    }

    public MappingRegistry registry() {
        return registry;
    }

    public String index() {
        return index;
    }

    public LambdaTemplate lambdaTemplate() throws SQLException {
        return new LambdaTemplate(connection, registry, null);
    }

    public Session session() throws SQLException {
        Configuration configuration = new Configuration(registry, new MacroRegistry(), new RuleRegistry());
        return configuration.newSession(connection);
    }

    public void insert(int id, String name, Integer age, String email) throws SQLException {
        jdbc.executeUpdate("PUT /" + index + "/_doc/" + id + "\\?refresh=true " + """
                {"id": ?,"name": ?,"age": ?,"email": ?,"create_time": ?}
                """, new Object[] { id, name, age, email, new Date() });
    }

    /** Prepare iterator data in one request; write-operation tests still use their original paths. */
    public void seedUsers(int startId, String prefix, int count, int age) throws SQLException {
        if (count == 0) {
            return;
        }
        StringBuilder command = new StringBuilder("POST /" + index + "/_bulk\\?refresh=true [");
        List<Object> parameters = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                command.append(',');
            }
            command.append("{\"index\": {\"_type\": \"_doc\", \"_id\": ?}},").append("{\"id\": ?, \"name\": ?, \"age\": ?, \"email\": ?, \"create_time\": ?}");
            parameters.add(String.valueOf(startId + i));
            parameters.add(startId + i);
            parameters.add(prefix + (i + 1));
            parameters.add(age);
            parameters.add(null);
            parameters.add(new Date());
        }
        command.append(']');
        int inserted = jdbc.executeUpdate(command.toString(), parameters.toArray());
        if (inserted != count) {
            throw new SQLException("Iterator fixture expected " + count + " rows, but inserted " + inserted);
        }
    }

    public String select(String columns, String predicate, boolean ordered) {
        String query;
        switch (predicate) {
            case "id = ?":
                query = "{\"term\": {\"id\": ?}}";
                break;
            case "age = ?":
                query = "{\"term\": {\"age\": ?}}";
                break;
            case "age > ?":
                query = "{\"range\": {\"age\": {\"gt\": ?}}}";
                break;
            case "age BETWEEN ? AND ?":
                query = "{\"range\": {\"age\": {\"gte\": ?,\"lte\": ?}}}";
                break;
            case "id BETWEEN ? AND ?":
                query = "{\"range\": {\"id\": {\"gte\": ?,\"lte\": ?}}}";
                break;
            default:
                throw new IllegalArgumentException("Unknown result fixture predicate: " + predicate);
        }
        String fields = "*".equals(columns) ? "id,name,age,email,create_time" : columns;
        String projection = Arrays.stream(fields.split(",")).map(String::trim).map(field -> "\"" + field + "\"").collect(Collectors.joining(","));
        return "POST /" + index + "/_search {\"size\": 100,\"_source\": [" + projection + "],\"query\": " + query + (ordered ? ",\"sort\": [{\"id\": \"asc\"}]" : "") + "}";
    }

    @Override
    public void close() throws SQLException {
        try {
            if (created) {
                jdbc.execute("DELETE /" + index);
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
