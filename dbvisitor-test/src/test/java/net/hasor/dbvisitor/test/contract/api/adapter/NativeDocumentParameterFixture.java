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
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterCommand;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;

/** Named native command material; no parsing or emulation of relational SQL. */
public final class NativeDocumentParameterFixture implements AutoCloseable {
    private final String collection = "nxn_params_" + UUID.randomUUID().toString().replace("-", "");
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
            String properties = "\"properties\": {\"id\": {\"type\": \"integer\"}, \"name\": {\"type\": \"keyword\"}, \"age\": {\"type\": \"integer\"}}";
            String mappings = "es6".equals(environment) ? "{\"_doc\": {" + properties + "}}" : "{" + properties + "}";
            this.jdbc.execute("PUT /" + this.collection + " {\"mappings\": " + mappings + "}");
        }
        this.created = true;
        return this.jdbc;
    }

    public String table() {
        return this.collection;
    }

    public String column(String name) {
        return this.mongo ? name : "\"" + name + "\"";
    }

    public String orderFragment() {
        return this.mongo ? "{age: -1}" : "[{\"age\": \"desc\"}]";
    }

    public String command(JdbcParameterCommand command) {
        switch (command) {
            case INSERT_POSITIONAL:
                return insert("?", "?", "?", "?", "?");
            case INSERT_COLON:
                return insert(":id", ":name", ":age", ":email", ":createTime");
            case INSERT_BRACE:
                return insert("#{id}", "#{name}", "#{age}", "#{email}", "#{createTime}");
            case INSERT_ARRAY_SOURCE:
                return insert(":arg0", ":arg1", ":arg2", ":arg3", ":arg4");
            default:
                return this.mongo ? mongoRead(command) : elasticRead(command);
        }
    }

    private String insert(String id, String name, String age, String email, String createdAt) {
        String document = "{\"id\": " + id + ", \"name\": " + name + ", \"age\": " + age + ", \"email\": " + email + ", \"create_time\": " + createdAt + "}";
        return this.mongo ? "test." + this.collection + ".insert(" + document + ")" : "POST /" + this.collection + "/_doc " + document;
    }

    private String mongoRead(JdbcParameterCommand command) {
        String source = "test." + this.collection;
        switch (command) {
            case SELECT_USER:
                return source + ".find({name: ?, age: {$gt: ?}}, {id: 1, name: 1, age: 1, email: 1})";
            case SELECT_NULL_ROW:
                return source + ".find({id: ?}, {name: 1, age: 1})";
            case SELECT_EMAIL_BY_ID:
                return source + ".find({id: ?}, {email: 1})";
            case SELECT_EMAIL_BY_NAME:
                return source + ".find({name: ?}, {email: 1})";
            case COUNT_BY_NAME_AGE:
                return source + ".count({name: :name, age: {$gt: :age}})";
            case COUNT_BY_ID_NAME:
                return source + ".count({id: #{id}, name: #{name}})";
            case COUNT_BY_NESTED:
                return source + ".count({id: :ids[0], name: :names[0], age: :user.info.age})";
            case SELECT_TEXT_VALUE:
                return "test.${tableName}.find({${column}: #{name}}, {${column}: 1, age: 1})";
            case COUNT_TEXT_VALUE:
                return "test.${tableName}.count({${column}: #{name}})";
            case SELECT_TEXT_ORDER:
                return "test.${tableName}.find({}, {${column}: 1, age: 1}).sort(${orderBy})";
            case COUNT_TEXT_COLUMN:
                return source + ".count({${column}: #{name}})";
            case COUNT_SOURCE_NAMES:
                return source + ".count({name: {$in: :names}, age: {$gt: :minAge}})";
            default:
                throw new IllegalArgumentException("No native fixture for " + command);
        }
    }

    private String elasticRead(JdbcParameterCommand command) {
        String source = "POST /" + this.collection;
        switch (command) {
            case SELECT_USER:
                return source + "/_search {\"query\": {\"bool\": {\"filter\": [{\"term\": {\"name\": ?}}, {\"range\": {\"age\": {\"gt\": ?}}}]}}, \"_source\": [\"id\", \"name\", \"age\", \"email\"]}";
            case SELECT_NULL_ROW:
                return source + "/_search {\"query\": {\"term\": {\"id\": ?}}, \"_source\": [\"name\", \"age\"]}";
            case SELECT_EMAIL_BY_ID:
                return source + "/_search {\"query\": {\"term\": {\"id\": ?}}, \"_source\": [\"email\"]}";
            case SELECT_EMAIL_BY_NAME:
                return source + "/_search {\"query\": {\"term\": {\"name\": ?}}, \"_source\": [\"email\"]}";
            case COUNT_BY_NAME_AGE:
                return source + "/_count {\"query\": {\"bool\": {\"filter\": [{\"term\": {\"name\": :name}}, {\"range\": {\"age\": {\"gt\": :age}}}]}}}";
            case COUNT_BY_ID_NAME:
                return source + "/_count {\"query\": {\"bool\": {\"filter\": [{\"term\": {\"id\": #{id}}}, {\"term\": {\"name\": #{name}}}]}}}";
            case COUNT_BY_NESTED:
                return source + "/_count {\"query\": {\"bool\": {\"filter\": [{\"term\": {\"id\": :ids[0]}}, {\"term\": {\"name\": :names[0]}}, {\"term\": {\"age\": :user.info.age}}]}}}";
            case SELECT_TEXT_VALUE:
                return "POST /${tableName}/_search {\"query\": {\"term\": {${column}: #{name}}}, \"_source\": [${column}, \"age\"]}";
            case COUNT_TEXT_VALUE:
                return "POST /${tableName}/_count {\"query\": {\"term\": {${column}: #{name}}}}";
            case SELECT_TEXT_ORDER:
                return "POST /${tableName}/_search {\"query\": {\"match_all\": {}}, \"_source\": [${column}, \"age\"], \"sort\": ${orderBy}}";
            case COUNT_TEXT_COLUMN:
                return source + "/_count {\"query\": {\"term\": {${column}: #{name}}}}";
            case COUNT_SOURCE_NAMES:
                return source + "/_count {\"query\": {\"bool\": {\"filter\": [{\"terms\": {\"name\": :names}}, {\"range\": {\"age\": {\"gt\": :minAge}}}]}}}";
            default:
                throw new IllegalArgumentException("No native fixture for " + command);
        }
    }

    public void refresh() throws SQLException {
        if (!this.mongo) {
            this.jdbc.execute("POST /" + this.collection + "/_refresh");
        }
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
