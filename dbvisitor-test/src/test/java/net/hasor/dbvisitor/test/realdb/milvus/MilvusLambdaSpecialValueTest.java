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
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaSpecialValueCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Keep the shared contract's VARCHAR(100) length constraint, without changing its assertions. */
public class MilvusLambdaSpecialValueTest extends LambdaSpecialValueCase {
    private final MilvusDatabaseFixture database = new MilvusDatabaseFixture();
    private Connection connection;

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.connection = this.database.open();
        this.jdbcTemplate = new JdbcTemplate(this.connection);
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE user_info (
                        id INT64 PRIMARY KEY, name VARCHAR(100) NULL, age INT32 NULL,
                        email VARCHAR(128) NULL, create_time VARCHAR(128) NULL,
                        vector_text VARCHAR(128) DEFAULT 'fixture' WITH (enable_analyzer=true),
                        v SPARSE_FLOAT_VECTOR, FUNCTION fixture_vector USING BM25 (vector_text) INTO (v)
                    ) WITH (consistency_level=Strong)
                    """);
            statement.executeUpdate("CREATE INDEX special_value_v ON user_info(v) USING SPARSE_INVERTED_INDEX WITH (metric_type=BM25)");
            statement.executeUpdate("LOAD TABLE user_info");
        }
    }

    @After
    public void cleanupFixture() throws SQLException {
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
}
