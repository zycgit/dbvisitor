/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo.material;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;

/** Keeps literal entity collection names in a database owned by one test. */
public final class MongoNamingFixture implements AutoCloseable {
    private final String database = "nxn_names_" + UUID.randomUUID().toString().replace("-", "");
    private Connection connection;
    private JdbcTemplate jdbc;

    public JdbcTemplate open() throws SQLException {
        OneApiDataSourceManager.assumeCurrentDataSource("mongo");
        this.connection = OneApiDataSourceManager.getConnection("mongo");
        this.jdbc = new JdbcTemplate(this.connection);
        this.jdbc.execute("use " + this.database);
        create("user_info");
        this.jdbc.execute("db.user_info.createIndex({id: 1}, {unique: true})");
        return this.jdbc;
    }

    public LambdaTemplate lambda(Options options) throws SQLException {
        return new LambdaTemplate(this.connection, options);
    }

    public void create(String table) throws SQLException {
        this.jdbc.execute("db.createCollection('" + table + "')");
    }

    public void createStrictKeyCollection() throws SQLException {
        this.jdbc.execute("db.createCollection('user_strict_none', {validator: {$jsonSchema: {bsonType: 'object', "
                + "required: ['id'], properties: {id: {bsonType: 'int'}}}}})");
        this.jdbc.execute("db.user_strict_none.createIndex({id: 1}, {unique: true})");
    }

    public String insert(String table, String columns) {
        StringBuilder fields = new StringBuilder();
        for (String column : columns.split(",")) {
            if (fields.length() > 0) {
                fields.append(", ");
            }
            fields.append(column.trim()).append(": ?");
        }
        return "db." + table + ".insert({" + fields + "})";
    }

    @Override
    public void close() throws SQLException {
        if (this.connection != null) {
            try {
                this.jdbc.execute(this.database + ".dropDatabase()");
            } finally {
                this.connection.close();
            }
        }
    }
}
