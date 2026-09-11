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
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

/** Native statement material for the shared Session and BaseMapper API assertions. */
final class MilvusStatementFixture implements AutoCloseable {
    private final MilvusDatabaseFixture database = new MilvusDatabaseFixture();
    private Connection connection;
    private Session session;

    void open() throws SQLException {
        this.connection = this.database.open();
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE user_info (
                        id INT64 PRIMARY KEY, name VARCHAR(128) NULL, age INT32 NULL,
                        email VARCHAR(128) NULL, create_time VARCHAR(128) NULL, v FLOAT_VECTOR(2)
                    ) WITH (consistency_level=Strong)
                    """);
            statement.executeUpdate("""
                    CREATE TABLE user_order (
                        id INT64 PRIMARY KEY, user_id INT64, order_no VARCHAR(128),
                        amount DOUBLE, create_time VARCHAR(128) NULL, v FLOAT_VECTOR(2)
                    ) WITH (consistency_level=Strong)
                    """);
            for (String table : new String[] { "user_info", "user_order" }) {
                statement.executeUpdate("CREATE INDEX statement_v ON " + table + "(v) USING FLAT WITH (metric_type=L2)");
                statement.executeUpdate("LOAD TABLE " + table);
            }
        }
    }

    Session session(String resource) throws Exception {
        Configuration configuration = new Configuration();
        configuration.addMacro("currentTimestamp", "'2026-09-10 12:00:00'");
        configuration.loadMapper(resource);
        this.session = configuration.newSession(this.connection);
        return this.session;
    }

    @Override
    public void close() throws Exception {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                try (Statement statement = this.connection.createStatement()) {
                    statement.executeUpdate("DROP TABLE IF EXISTS user_order");
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
