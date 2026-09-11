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
import net.hasor.dbvisitor.test.contract.feature.type.TimeClockJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Verifies the shared Java temporal conversions using native text and integer storage. */
public class MilvusTimeClockJdbcContractTest extends TimeClockJdbcContractTest {
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
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE time_types_explicit_test (
                        id INT64 PRIMARY KEY, date_value VARCHAR(128) NULL,
                        time_value VARCHAR(128) NULL, timestamp_value VARCHAR(128) NULL,
                        local_date_ts VARCHAR(128) NULL, local_time_ts VARCHAR(128) NULL,
                        local_datetime_ts VARCHAR(128) NULL, julian_day INT64 NULL,
                        vector_text VARCHAR(100) DEFAULT 'fixture' WITH (enable_analyzer=true),
                        v SPARSE_FLOAT_VECTOR, FUNCTION fixture_vector USING BM25 (vector_text) INTO (v)
                    ) WITH (consistency_level=Strong)
                    """);
            statement.executeUpdate("CREATE INDEX time_v ON time_types_explicit_test(v) USING SPARSE_INVERTED_INDEX WITH (metric_type=BM25)");
            statement.executeUpdate("LOAD TABLE time_types_explicit_test");
        }
    }

    @After
    public void cleanupFixture() throws SQLException {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                try (Statement statement = this.connection.createStatement()) {
                    statement.executeUpdate("DROP TABLE IF EXISTS time_types_explicit_test");
                }
            }
        } finally {
            this.database.close();
        }
    }
}
