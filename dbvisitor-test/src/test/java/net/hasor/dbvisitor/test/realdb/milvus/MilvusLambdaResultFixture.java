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
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

/** Shared result assertions use native filtering and distance order over deterministic fixture rows. */
final class MilvusLambdaResultFixture implements AutoCloseable {
    private final MilvusDatabaseFixture database = new MilvusDatabaseFixture();
    private Connection connection;

    Connection open() throws SQLException {
        this.connection = this.database.open();
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE user_info (
                        id INT64 PRIMARY KEY, name VARCHAR(128) NULL, age INT32 NULL,
                        email VARCHAR(128) NULL, create_time VARCHAR(128) NULL, v FLOAT_VECTOR(2)
                    ) WITH (consistency_level=Strong)
                    """);
            statement.executeUpdate("CREATE INDEX result_v ON user_info(v) USING FLAT WITH (metric_type=L2)");
            statement.executeUpdate("LOAD TABLE user_info");
        }
        return this.connection;
    }

    void insert(int id, String name, Integer age, String email) throws SQLException {
        new JdbcTemplate(this.connection).executeUpdate(
                "INSERT INTO user_info (id, name, age, email, create_time, v) VALUES (?, ?, ?, ?, ?, ?)",
                new Object[] { id, name, age, email, new Date(), new float[] { id, 0 } });
    }

    EntityQuery<? extends UserInfo> queryRows(LambdaTemplate lambda, String prefix) throws SQLException {
        // Prefixes are fixed case labels, not user values or parameter-binding test inputs.
        if (!prefix.matches("[A-Za-z]+")) {
            throw new IllegalArgumentException("Expected an alphabetic fixture prefix");
        }
        return lambda.query(ResultUser.class).apply("name LIKE '" + prefix + "%'");
    }

    @SuppressWarnings("unchecked")
    EntityQuery<? extends UserInfo> orderRows(EntityQuery<? extends UserInfo> query) {
        // Every ordered query comes from queryRows and maps ResultUser. Fixture IDs and ages
        // increase together, so native distance ordering produces the asserted row sequence.
        EntityQuery<ResultUser> rows = (EntityQuery<ResultUser>) query;
        return rows.orderByL2(ResultUser::getV, new float[] { 0, 0 });
    }

    @Override
    public void close() throws SQLException {
        try {
            if (this.connection != null) {
                try (Statement statement = this.connection.createStatement()) {
                    statement.executeUpdate("DROP TABLE IF EXISTS user_info");
                }
            }
        } finally {
            this.database.close();
        }
    }

    @Table("user_info")
    public static class ResultUser extends UserInfo {
        private List<Float> v;

        public List<Float> getV() {
            return this.v;
        }

        public void setV(List<Float> v) {
            this.v = v;
        }
    }
}
