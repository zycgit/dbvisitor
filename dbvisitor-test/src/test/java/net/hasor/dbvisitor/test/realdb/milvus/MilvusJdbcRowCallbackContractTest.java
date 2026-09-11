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
import java.util.Arrays;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcRowCallbackContractTest;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;

/** Native Milvus fixtures for the unchanged shared result-mapping assertions. */
public class MilvusJdbcRowCallbackContractTest extends JdbcRowCallbackContractTest {
    private final String table = "dbv_mapping_" + UUID.randomUUID().toString().replace("-", "");
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
                        id INT64 PRIMARY KEY, name VARCHAR(128), age INT32,
                        email VARCHAR(128), v FLOAT_VECTOR(2)
                    ) WITH (consistency_level='Strong')
                    """.formatted(this.table));
            statement.executeUpdate("CREATE INDEX mapping_v ON " + this.table + "(v) USING FLAT WITH (metric_type=L2)");
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
    protected void insertUser(int id, String name, int age, String email) throws SQLException {
        // Distance from [0,0] gives the shared assertions their deterministic fixture order.
        jdbcTemplate.executeUpdate("INSERT INTO " + this.table + " (id, name, age, email, v) VALUES (?, ?, ?, ?, ?)",
                new Object[] { id, name, age, email, Arrays.asList((float) (id - baseId()), 0F) });
    }

    @Override
    protected String selectSql(String columns, String predicate, boolean ordered) {
        String sql = "SELECT " + columns + " FROM " + this.table + " WHERE " + predicate;
        if (ordered) {
            // The shared fixture has ten rows. This tests mapping, not scalar ORDER BY support.
            sql += " ORDER BY v <-> [0, 0] LIMIT 10 WITH (metric_type='L2')";
        }
        return sql;
    }
}
