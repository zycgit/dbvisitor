/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Date;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcScalarQueryCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

public class MilvusJdbcScalarQueryTest extends JdbcScalarQueryCase {
    private final MilvusDatabaseFixture fixture = new MilvusDatabaseFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        Connection connection = this.fixture.open();
        this.jdbcTemplate = new JdbcTemplate(connection);
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE user_info (
                        id INT64 PRIMARY KEY, name VARCHAR(128), age INT32,
                        email VARCHAR(128), create_time VARCHAR(128), v FLOAT_VECTOR(2)
                    ) WITH (consistency_level=Strong)
                    """);
            statement.executeUpdate("CREATE INDEX query_v ON user_info(v) USING FLAT WITH (metric_type=L2)");
            statement.executeUpdate("LOAD TABLE user_info");
        }
    }

    @Override
    protected void insertUser(int id, String name, int age, String email, Date createTime) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time, v) VALUES (?, ?, ?, ?, ?, ?)",
                new Object[] { id, name, age, email, createTime, new float[] { id - baseId(), 0F } });
    }

    @Override
    protected String selectSql(String columns, String predicate, boolean ordered) {
        String sql = "SELECT " + columns + " FROM user_info WHERE " + predicate;
        // Native vector distances provide the three-row fixture's deterministic order.
        return ordered ? sql + " ORDER BY v <-> [0, 0] LIMIT 3 WITH (metric_type=L2)" : sql;
    }

    @After
    public void cleanupFixture() throws SQLException {
        try {
            if (this.jdbcTemplate != null) {
                this.jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS user_info");
            }
        } finally {
            this.fixture.close();
        }
    }
}
