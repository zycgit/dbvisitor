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
import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationJsonFieldMappingContractTest;
import net.hasor.dbvisitor.test.contract.material.model.types.SpecialJsonTypeEntity;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import org.junit.After;
import org.junit.Before;

/** Entity-field JSON conversion through Lambda and a private mapping registry. */
public abstract class NativeJsonFieldMappingSupport extends AnnotationJsonFieldMappingContractTest {
    private final String collection = "nxn_json_fields_" + UUID.randomUUID().toString().replace("-", "");
    private Connection connection;
    private boolean created;
    private boolean mongo;

    @Override
    @Before
    public void setup() throws SQLException {
        OneApiDataSourceManager.assumeCurrentDataSource(profile().env());
        this.connection = OneApiDataSourceManager.getConnection(profile().env());
        this.jdbcTemplate = new JdbcTemplate(this.connection);
        this.mongo = "mongo".equals(profile().env());
        if (this.mongo) {
            this.jdbcTemplate.execute("use test");
            this.jdbcTemplate.execute("db.createCollection('" + this.collection + "')");
        } else {
            this.jdbcTemplate.execute("PUT /" + this.collection);
        }
        this.created = true;
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityAsTable(SpecialJsonTypeEntity.class, this.collection);
        this.lambdaTemplate = new LambdaTemplate(this.connection, registry, null);
    }

    @Override
    protected SpecialJsonTypeEntity queryJson(int id) throws SQLException {
        if (!this.mongo) {
            this.jdbcTemplate.execute("POST /" + this.collection + "/_refresh");
        }
        return super.queryJson(id);
    }

    @After
    public void cleanupFixture() throws SQLException {
        try {
            if (this.created) {
                this.jdbcTemplate.execute(this.mongo ? "test." + this.collection + ".drop()" : "DELETE /" + this.collection);
            }
        } finally {
            if (this.connection != null) {
                this.connection.close();
            }
        }
    }
}
