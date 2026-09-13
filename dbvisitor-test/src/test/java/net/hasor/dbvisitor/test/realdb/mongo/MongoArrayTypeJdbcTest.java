/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;

import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.test.contract.feature.type.ArrayTypeJdbcCase;
import net.hasor.dbvisitor.test.contract.material.model.types.ArrayTypesAnnotationModel;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;

public class MongoArrayTypeJdbcTest extends ArrayTypeJdbcCase {
    private final String collection = "nxn_arrays_" + UUID.randomUUID().toString().replace("-", "");
    private Connection connection;
    private boolean created;

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        OneApiDataSourceManager.assumeCurrentDataSource(profile().env());
        this.connection = OneApiDataSourceManager.getConnection(profile().env());
        this.jdbcTemplate = new JdbcTemplate(this.connection);
        this.jdbcTemplate.execute("use test");
        this.jdbcTemplate.execute("db.createCollection('" + this.collection + "')");
        this.created = true;
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityAsTable(ArrayTypesAnnotationModel.class, this.collection);
        this.lambdaTemplate = new LambdaTemplate(this.connection, registry, null);
    }

    @Override
    protected String insertCommand(String table, String columns, String... parameters) {
        String[] names = columns.split(",");
        StringBuilder fields = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                fields.append(", ");
            }
            fields.append(names[i].trim()).append(": ").append(parameters[i]);
        }
        return command("insert({" + fields + "})");
    }

    @Override
    protected String selectCommand(String table, String column) {
        return select(column, "?");
    }

    @Override
    protected String selectNamedCommand(String table, String column) {
        return select(column, ":id");
    }

    @Override
    protected String updateArrayCommand() {
        return command("update({id: :id}, {$set: {int_array: :array}})");
    }

    @Override
    protected String countRangeCommand() {
        return command("count({id: {$gte: ?, $lte: ?}})");
    }

    private String select(String column, String parameter) {
        return command("find({id: " + parameter + "}, {_id: 0, " + column + ": 1})");
    }

    private String command(String operation) {
        return "test." + this.collection + "." + operation;
    }

    @After
    public void closeFixture() throws SQLException {
        try {
            if (this.created) {
                this.jdbcTemplate.execute(command("drop()"));
            }
        } finally {
            if (this.connection != null) {
                this.connection.close();
            }
        }
    }
}
