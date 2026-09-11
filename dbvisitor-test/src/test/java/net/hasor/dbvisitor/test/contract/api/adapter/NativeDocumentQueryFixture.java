/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.adapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCrudCommand;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;

/** Native command material for JdbcTemplate query-result contracts. */
public final class NativeDocumentQueryFixture implements AutoCloseable {
    private final String collection = "nxn_query_" + UUID.randomUUID().toString().replace("-", "");
    private Connection connection;
    private JdbcTemplate jdbc;
    private boolean mongo;
    private boolean created;

    public JdbcTemplate open(String environment) throws SQLException {
        OneApiDataSourceManager.assumeCurrentDataSource(environment);
        this.connection = OneApiDataSourceManager.getConnection(environment);
        this.jdbc = new JdbcTemplate(this.connection);
        this.mongo = "mongo".equals(environment);
        if (this.mongo) {
            this.jdbc.execute("use test");
            this.jdbc.execute("db.createCollection('" + this.collection + "')");
        } else {
            this.jdbc.execute("PUT /" + this.collection);
        }
        this.created = true;
        return this.jdbc;
    }

    public void insert(int id, String name, int age, String email, Date createdAt) throws SQLException {
        this.jdbc.executeUpdate(insertCommand(), new Object[] { id, name, age, email, createdAt });
    }

    public LambdaTemplate lambdaTemplate() throws SQLException {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityAsTable(UserInfo.class, this.collection);
        return new LambdaTemplate(this.connection, registry, null);
    }

    private String insertCommand() {
        String fields = "{id: ?, name: ?, age: ?, email: ?, create_time: ?}";
        return this.mongo ? "test." + this.collection + ".insert(" + fields + ")"
                : "POST /" + this.collection + "/_doc {\"id\": ?, \"name\": ?, \"age\": ?, \"email\": ?, \"create_time\": ?}";
    }

    public String crudCommand(JdbcCrudCommand command) throws SQLException {
        switch (command) {
            case INSERT:
                return insertCommand();
            case SELECT_NAME:
                return selectById("name", "?");
            case SELECT_AGE:
                return selectById("age", "?");
            case COUNT_BY_ID:
                if (this.mongo) {
                    return "test." + this.collection + ".count({id: ?})";
                }
                refresh();
                return "POST /" + this.collection + "/_count {\"query\": {\"term\": {\"id\": ?}}}";
            case UPDATE_AGE:
                if (this.mongo) {
                    return "test." + this.collection + ".update({id: #{arg1}}, {$set: {age: #{arg0}}})";
                }
                refresh();
                return "POST /" + this.collection + "/_update_by_query " + """
                        {"query": {"term": {"id": #{arg1}}},
                         "script": {"source": "ctx._source.age = params.age", "params": {"age": #{arg0}}}}
                        """;
            case DELETE:
                if (this.mongo) {
                    return "test." + this.collection + ".remove({id: ?})";
                }
                refresh();
                return "POST /" + this.collection + "/_delete_by_query {\"query\": {\"term\": {\"id\": ?}}}";
            default:
                throw new IllegalArgumentException("Unknown CRUD fixture command: " + command);
        }
    }

    public String selectById(String columns, String parameter) throws SQLException {
        String filter = this.mongo ? "{id: " + parameter + "}" : "{\"term\": {\"id\": " + parameter + "}}";
        return select(columns, filter, false);
    }

    public String selectRange(String columns, String lower, String upper, boolean ordered) throws SQLException {
        return select(columns, rangeFilter(lower, upper), ordered);
    }

    public String countRange(String lower, String upper) throws SQLException {
        String filter = rangeFilter(lower, upper);
        if (this.mongo) {
            return "test." + this.collection + ".count(" + filter + ")";
        }
        refresh();
        return "POST /" + this.collection + "/_count {\"query\": " + filter + "}";
    }

    private String rangeFilter(String lower, String upper) {
        return this.mongo ? "{id: {$gte: " + lower + ", $lte: " + upper + "}}"
                : "{\"range\": {\"id\": {\"gte\": " + lower + ", \"lte\": " + upper + "}}}";
    }

    private String select(String columns, String filter, boolean ordered) throws SQLException {
        List<String> fields = new ArrayList<>();
        for (String column : columns.split(",")) {
            fields.add("\"" + column.trim() + "\"" + (this.mongo ? ": 1" : ""));
        }
        if (this.mongo) {
            String command = "test." + this.collection + ".find(" + filter + ", {" + String.join(", ", fields) + "})";
            return ordered ? command + ".sort({id: 1})" : command;
        }
        refresh();
        String sort = ordered ? ", \"sort\": [{\"id\": \"asc\"}]" : "";
        return "POST /" + this.collection + "/_search {\"query\": " + filter + ", \"_source\": [" + String.join(", ", fields) + "]" + sort + "}";
    }

    private void refresh() throws SQLException {
        this.jdbc.execute("POST /" + this.collection + "/_refresh");
    }

    @Override
    public void close() throws SQLException {
        try {
            if (this.created) {
                this.jdbc.execute(this.mongo ? "test." + this.collection + ".drop()" : "DELETE /" + this.collection);
            }
        } finally {
            if (this.connection != null) {
                this.connection.close();
            }
        }
    }
}
