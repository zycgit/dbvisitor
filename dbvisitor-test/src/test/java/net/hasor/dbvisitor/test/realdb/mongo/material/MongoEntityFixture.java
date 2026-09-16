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
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;

public final class MongoEntityFixture implements AutoCloseable {
    private final String        collection    = "nxn_entity_" + UUID.randomUUID().toString().replace("-", "");
    private final Configuration configuration = new Configuration();
    private       Connection    connection;
    private       JdbcTemplate  jdbc;

    public JdbcTemplate open() throws SQLException {
        return open(UserInfo.class, "{id: 1}");
    }

    public JdbcTemplate open(Class<?> entity, String uniqueFields) throws SQLException {
        OneApiDataSourceManager.assumeCurrentDataSource("mongo");
        this.connection = OneApiDataSourceManager.getConnection("mongo");
        this.configuration.getMappingRegistry().loadEntityAsTable(entity, this.collection);
        this.jdbc = this.configuration.newJdbc(this.connection);
        this.jdbc.execute("use test");
        this.jdbc.execute("db.createCollection('" + this.collection + "', {validator: {$jsonSchema: {bsonType: 'object', " + "properties: {name: {bsonType: ['string', 'null'], maxLength: 100}}}}})");
        this.jdbc.execute(command("createIndex(" + uniqueFields + ", {unique: true})"));
        return this.jdbc;
    }

    public Session session() throws SQLException {
        return this.configuration.newSession(this.connection);
    }

    public String command(String operation) {
        return "db." + this.collection + "." + operation;
    }

    public String table() {
        return this.collection;
    }

    public void mapEntity(Class<?> entity) throws SQLException {
        this.configuration.getMappingRegistry().loadEntityAsTable(entity, this.collection);
    }

    public LambdaTemplate lambda() throws SQLException {
        return this.configuration.newLambda(this.connection);
    }

    @Override
    public void close() throws SQLException {
        if (this.connection != null) {
            try {
                this.jdbc.execute("db." + this.collection + ".drop()");
            } finally {
                this.connection.close();
            }
        }
    }
}
