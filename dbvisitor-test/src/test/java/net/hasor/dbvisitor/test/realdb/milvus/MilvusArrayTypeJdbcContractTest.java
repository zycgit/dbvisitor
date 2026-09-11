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
import net.hasor.dbvisitor.test.contract.feature.type.ArrayTypeJdbcContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Native scalar arrays; shared SQL, type handlers and assertions remain unchanged. */
public class MilvusArrayTypeJdbcContractTest extends ArrayTypeJdbcContractTest {
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
        createArrayCollection("array_types_test", """
                int_array ARRAY<INT32>(100) NULL,
                float_array ARRAY<FLOAT>(100) NULL,
                string_array ARRAY<VARCHAR(100)>(100) NULL
                """);
        createArrayCollection("array_types_annotation_test", """
                array_no_annotation ARRAY<INT32>(100) NULL,
                array_jdbc_type ARRAY<INT32>(100) NULL,
                array_type_handler ARRAY<INT32>(100) NULL,
                array_number_special ARRAY<INT32>(100) NULL,
                array_full_annotated ARRAY<INT32>(100) NULL
                """);
    }

    private void createArrayCollection(String table, String fields) throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE " + table + " (id INT64 PRIMARY KEY," + fields + """
                    ,vector_text VARCHAR(100) DEFAULT 'fixture' WITH (enable_analyzer=true),
                    v SPARSE_FLOAT_VECTOR, FUNCTION fixture_vector USING BM25 (vector_text) INTO (v)
                    ) WITH (consistency_level=Strong)
                    """);
            statement.executeUpdate("CREATE INDEX array_v ON " + table + "(v) USING SPARSE_INVERTED_INDEX WITH (metric_type=BM25)");
            statement.executeUpdate("LOAD TABLE " + table);
        }
    }

    @After
    public void cleanupFixture() throws SQLException {
        try {
            if (this.connection != null) {
                try (Statement statement = this.connection.createStatement()) {
                    statement.executeUpdate("DROP TABLE IF EXISTS array_types_test");
                    statement.executeUpdate("DROP TABLE IF EXISTS array_types_annotation_test");
                }
            }
        } finally {
            this.database.close();
        }
    }
}
