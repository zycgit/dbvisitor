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
import net.hasor.dbvisitor.test.contract.feature.type.BasicDecimalTypeJdbcCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Native scalar storage; DOUBLE is not an arbitrary-precision DECIMAL type. */
public class MilvusBasicDecimalTypeJdbcTest extends BasicDecimalTypeJdbcCase {
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
        createScalarCollection("basic_types_test", """
                byte_value INT8 NULL, short_value INT16 NULL, int_value INT32 NULL,
                long_value INT64 NULL, float_value FLOAT NULL, double_value DOUBLE NULL,
                decimal_value DOUBLE NULL, big_int_value INT64 NULL, bool_value BOOL NULL
                """);
        createScalarCollection("basic_types_explicit_test", """
                char_value VARCHAR(4) NULL, varchar_value VARCHAR(100) NULL,
                nvarchar_value VARCHAR(100) NULL
                """);
    }

    private void createScalarCollection(String table, String fields) throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE " + table + " (id INT64 PRIMARY KEY," + fields + """
                    ,vector_text VARCHAR(100) DEFAULT 'fixture' WITH (enable_analyzer=true),
                    v SPARSE_FLOAT_VECTOR, FUNCTION fixture_vector USING BM25 (vector_text) INTO (v)
                    ) WITH (consistency_level=Strong)
                    """);
            statement.executeUpdate("CREATE INDEX scalar_v ON " + table + "(v) USING SPARSE_INVERTED_INDEX WITH (metric_type=BM25)");
            statement.executeUpdate("LOAD TABLE " + table);
        }
    }

    @After
    public void cleanupFixture() throws SQLException {
        try {
            if (this.connection != null) {
                try (Statement statement = this.connection.createStatement()) {
                    statement.executeUpdate("DROP TABLE IF EXISTS basic_types_test");
                    statement.executeUpdate("DROP TABLE IF EXISTS basic_types_explicit_test");
                }
            }
        } finally {
            this.database.close();
        }
    }
}
