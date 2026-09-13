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
import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationArrayFieldMappingCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Shared specialJavaType contracts backed by native JSON and ARRAY fields. */
public class MilvusAnnotationArrayFieldMappingTest extends AnnotationArrayFieldMappingCase {
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
                    CREATE TABLE test_special_types (
                        id INT64 PRIMARY KEY,
                        json_map JSON NULL, json_list JSON NULL, json_set JSON NULL,
                        int_array ARRAY<INT32>(100) NULL,
                        vector_text VARCHAR(100) DEFAULT 'fixture' WITH (enable_analyzer=true),
                        v SPARSE_FLOAT_VECTOR, FUNCTION fixture_vector USING BM25 (vector_text) INTO (v)
                    ) WITH (consistency_level=Strong)
                    """);
            statement.executeUpdate("CREATE INDEX special_v ON test_special_types(v) USING SPARSE_INVERTED_INDEX WITH (metric_type=BM25)");
            statement.executeUpdate("LOAD TABLE test_special_types");
        }
    }

    @After
    public void cleanupFixture() throws SQLException {
        try {
            if (this.connection != null) {
                try (Statement statement = this.connection.createStatement()) {
                    statement.executeUpdate("DROP TABLE IF EXISTS test_special_types");
                }
            }
        } finally {
            this.database.close();
        }
    }
}
