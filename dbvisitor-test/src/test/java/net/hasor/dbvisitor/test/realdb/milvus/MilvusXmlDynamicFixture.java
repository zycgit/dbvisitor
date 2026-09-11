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
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

/** Native row and ordering material for XML template contracts. */
final class MilvusXmlDynamicFixture implements AutoCloseable {
    private final MilvusDatabaseFixture database = new MilvusDatabaseFixture();
    private Connection connection;
    private Session session;

    JdbcTemplate open() throws SQLException {
        if (this.connection != null) {
            return new JdbcTemplate(this.connection);
        }
        this.connection = this.database.open();
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE user_info (
                        id INT64 PRIMARY KEY, name VARCHAR(128), age INT32,
                        email VARCHAR(128), create_time VARCHAR(128), v FLOAT_VECTOR(2)
                    ) WITH (consistency_level=Strong)
                    """);
            statement.executeUpdate("CREATE INDEX dynamic_v ON user_info(v) USING FLAT WITH (metric_type=L2)");
            statement.executeUpdate("LOAD TABLE user_info");
        }
        return new JdbcTemplate(this.connection);
    }

    void insert(JdbcTemplate jdbc, Object[] values) throws SQLException {
        int id = ((Number) values[0]).intValue();
        jdbc.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time, v) VALUES (?, ?, ?, ?, ?, ?)",
                new Object[] { values[0], values[1], values[2], values[3], "2026-09-11 12:00:00", new float[] { id, 0F } });
    }

    Session session(String resource) throws Exception {
        open();
        Configuration configuration = new Configuration();
        configuration.loadMapper(resource);
        this.session = configuration.newSession(this.connection);
        return this.session;
    }

    @Override
    public void close() throws Exception {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                try (Statement statement = this.connection.createStatement()) {
                    statement.executeUpdate("DROP TABLE IF EXISTS user_info");
                }
            }
        } finally {
            try {
                if (this.session != null) {
                    this.session.close();
                }
            } finally {
                this.database.close();
            }
        }
    }
}
