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
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ColumnMappedUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ColumnNameUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ColumnValueUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ExplicitMappingUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.IgnoreOnMethodUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.IgnoreWithColumnUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.IgnoredEmailUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.InsertExcludedUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.MultipleIgnoreUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ReadOnlyEmailUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.UpdateExcludedUser;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;

/** Common annotation models mapped to a private native collection, using real Lambda operations. */
public final class NativeAnnotationMappingFixture implements AutoCloseable {
    private final String collection = "nxn_mapping_" + UUID.randomUUID().toString().replace("-", "");
    private Connection connection;
    private JdbcTemplate jdbc;
    private boolean mongo;
    private boolean created;

    public LambdaTemplate open(String environment) throws SQLException {
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

        MappingRegistry registry = new MappingRegistry();
        // @formatter:off
        Class<?>[] models = { UserInfo.class, ColumnMappedUser.class, ColumnNameUser.class, ColumnValueUser.class,
                InsertExcludedUser.class, UpdateExcludedUser.class, ReadOnlyEmailUser.class, IgnoredEmailUser.class,
                MultipleIgnoreUser.class, IgnoreWithColumnUser.class, IgnoreOnMethodUser.class, ExplicitMappingUser.class };
        // @formatter:on
        for (Class<?> model : models) {
            registry.loadEntityAsTable(model, this.collection);
        }
        return new LambdaTemplate(this.connection, registry, null);
    }

    public void delete(int id) throws SQLException {
        String command = this.mongo ? "test." + this.collection + ".remove({id: ?})"
                : "POST /" + this.collection + "/_delete_by_query {\"query\": {\"term\": {\"id\": ?}}}";
        this.jdbc.executeUpdate(command, new Object[] { id });
    }

    public void updateEmail(int id, String email) throws SQLException {
        if (this.mongo) {
            this.jdbc.executeUpdate("test." + this.collection + ".update({id: ?}, {$set: {email: ?}})", new Object[] { id, email });
        } else {
            String command = "POST /" + this.collection + """
                    /_update_by_query {
                      "query": {"term": {"id": ?}},
                      "script": {"source": "ctx._source.email = params.email", "params": {"email": ?}}
                    }
                    """;
            this.jdbc.executeUpdate(command, new Object[] { id, email });
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
