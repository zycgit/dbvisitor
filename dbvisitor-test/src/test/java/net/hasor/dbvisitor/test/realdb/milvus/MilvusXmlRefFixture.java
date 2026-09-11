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
import net.hasor.dbvisitor.test.realdb.milvus.material.MilvusXmlRefMapperDao;

/** Isolated native material for the shared @RefMapper contract, including two fixture orderings. */
final class MilvusXmlRefFixture implements AutoCloseable {
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
                        email VARCHAR(128), create_time VARCHAR(128),
                        v FLOAT_VECTOR(2), v_age FLOAT_VECTOR(2)
                    ) WITH (consistency_level=Strong)
                    """);
            for (String field : new String[] { "v", "v_age" }) {
                statement.executeUpdate("CREATE INDEX ref_" + field + " ON user_info(" + field + ") USING FLAT WITH (metric_type=L2)");
            }
            statement.executeUpdate("LOAD TABLE user_info");
        }
        return new JdbcTemplate(this.connection);
    }

    void insert(JdbcTemplate jdbc, Object[] values) throws SQLException {
        int id = ((Number) values[0]).intValue();
        int age = ((Number) values[2]).intValue();
        jdbc.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time, v, v_age) VALUES (?, ?, ?, ?, ?, ?, ?)",
                new Object[] { values[0], values[1], values[2], values[3], "2026-09-11 12:00:00",
                        new float[] { id, 0F }, new float[] { age, 0F } });
    }

    MilvusXmlRefMapperDao mapper() throws Exception {
        open();
        this.session = new Configuration().newSession(this.connection);
        // Load via @RefMapper itself, not by preloading the XML resource in the fixture.
        return this.session.createMapper(MilvusXmlRefMapperDao.class);
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
