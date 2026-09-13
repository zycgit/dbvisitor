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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.feature.schema.StandardSchemaCase;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import org.junit.After;
import org.junit.Before;

public class MongoStandardSchemaTest extends StandardSchemaCase {
    private final Map<String, List<String>> schema = new LinkedHashMap<>();
    private Connection connection;

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        OneApiDataSourceManager.assumeCurrentDataSource("mongo");
        this.connection = schemaConnection();
        this.jdbcTemplate = new JdbcTemplate(this.connection);
        String prefix = "nxn_schema_" + UUID.randomUUID().toString().replace("-", "") + "_";
        for (Map.Entry<String, List<String>> entry : super.standardSchema().entrySet()) {
            String name = prefix + entry.getKey();
            this.jdbcTemplate.execute("db.createCollection('" + name + "')");
            this.schema.put(name, entry.getValue());
        }
    }

    @Override
    protected Connection schemaConnection() throws SQLException {
        Connection result = OneApiDataSourceManager.getConnection("mongo");
        result.setCatalog("test");
        return result;
    }

    @Override
    protected Map<String, List<String>> standardSchema() {
        return this.schema;
    }

    @After
    public void closeFixture() throws SQLException {
        if (this.connection != null) {
            try {
                for (String collection : this.schema.keySet()) {
                    this.jdbcTemplate.execute("db." + collection + ".drop()");
                }
            } finally {
                this.connection.close();
            }
        }
    }
}
