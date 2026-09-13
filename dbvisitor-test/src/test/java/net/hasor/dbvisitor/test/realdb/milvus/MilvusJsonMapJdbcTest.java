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
import net.hasor.dbvisitor.test.contract.feature.type.JsonMapJdbcCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Shared JSON serialization contracts, retaining their VARCHAR storage semantics. */
public class MilvusJsonMapJdbcTest extends JsonMapJdbcCase {
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
                    CREATE TABLE json_types_explicit_test (
                        id INT64 PRIMARY KEY, json_varchar VARCHAR(65535) NULL,
                        json_mysql JSON NULL, nested_json JSON NULL,
                        vector_text VARCHAR(100) DEFAULT 'fixture' WITH (enable_analyzer=true),
                        v SPARSE_FLOAT_VECTOR, FUNCTION fixture_vector USING BM25 (vector_text) INTO (v)
                    ) WITH (consistency_level=Strong)
                    """);
            statement.executeUpdate("CREATE INDEX json_v ON json_types_explicit_test(v) USING SPARSE_INVERTED_INDEX WITH (metric_type=BM25)");
            statement.executeUpdate("LOAD TABLE json_types_explicit_test");
        }
    }

    @After
    public void cleanupFixture() throws SQLException {
        try {
            if (this.connection != null) {
                try (Statement statement = this.connection.createStatement()) {
                    statement.executeUpdate("DROP TABLE IF EXISTS json_types_explicit_test");
                }
            }
        } finally {
            this.database.close();
        }
    }
}
