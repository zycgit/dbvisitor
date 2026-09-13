/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcTextParameterCase;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;

public class MilvusJdbcTextParameterTest extends JdbcTextParameterCase {
    private final String table = "dbv_params_" + UUID.randomUUID().toString().replace("-", "");
    private Connection connection;

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        String env = OneApiDataSourceManager.getDbDialect();
        Assume.assumeTrue("Milvus contract is not selected for " + env, profile().env().equals(env));
        this.connection = OneApiDataSourceManager.getConnection(profile().env());
        this.jdbcTemplate = new JdbcTemplate(this.connection);
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE %s (
                        id INT64 PRIMARY KEY, name VARCHAR(128), age INT32 NULL,
                        email VARCHAR(128) NULL, create_time VARCHAR(128), v FLOAT_VECTOR(2)
                    ) WITH (consistency_level='Strong')
                    """.formatted(this.table));
            statement.executeUpdate("CREATE INDEX params_v ON " + this.table + "(v) USING FLAT WITH (metric_type=L2)");
            statement.executeUpdate("LOAD TABLE " + this.table);
        }
    }

    @After
    public void cleanupCollection() throws SQLException {
        if (this.connection != null) {
            try (Connection closing = this.connection; Statement statement = closing.createStatement()) {
                statement.executeUpdate("DROP TABLE IF EXISTS " + this.table);
            }
        }
    }

    @Override
    protected String sql(String statement) {
        String sql = statement.replace("user_info", this.table);
        if (sql.startsWith("INSERT INTO ")) {
            // Add the required vector literal without changing any bound value or its position.
            sql = sql.replace("create_time)", "create_time, v)");
            sql = sql.substring(0, sql.length() - 1) + ", [1, 0])";
        }
        return sql;
    }

}
