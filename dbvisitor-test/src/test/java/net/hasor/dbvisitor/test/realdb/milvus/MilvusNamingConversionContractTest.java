/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.feature.naming.NamingConversionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Only schema fixtures change; shared mappings, SQL and assertions remain intact. */
public class MilvusNamingConversionContractTest extends NamingConversionContractTest {
    private final MilvusUserInfoFixture fixture = new MilvusUserInfoFixture();

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(this.fixture.open());
        this.lambdaTemplate = new LambdaTemplate(this.jdbcTemplate);
    }

    @Override
    protected void ensureCaseSensitivityTables() throws SQLException {
        createCollection("case_test_lower", "id INT64 PRIMARY KEY, name VARCHAR(100), age INT32, memo VARCHAR(200)");
        createCollection("Case_Test_Upper", "Id INT64 PRIMARY KEY, Name VARCHAR(100), Age INT32, Memo VARCHAR(200)");
    }

    @Override
    protected void ensurePlainUserTable() throws SQLException {
        createCollection("plain_user", "id INT64 PRIMARY KEY, name VARCHAR(100), age INT32, email VARCHAR(100), create_time VARCHAR(64)");
    }

    private void createCollection(String name, String fields) throws SQLException {
        this.jdbcTemplate.executeUpdate("CREATE TABLE " + name + " (" + fields + ", "
                + "vector_text VARCHAR(128) DEFAULT 'fixture' WITH (enable_analyzer=true), "
                + "v SPARSE_FLOAT_VECTOR, FUNCTION fixture_vector USING BM25 (vector_text) INTO (v)) "
                + "WITH (consistency_level=Strong)");
        this.jdbcTemplate.executeUpdate("CREATE INDEX fixture_v ON " + name
                + "(v) USING SPARSE_INVERTED_INDEX WITH (metric_type=BM25)");
        this.jdbcTemplate.executeUpdate("LOAD TABLE " + name);
    }

    @After
    public void cleanupFixture() throws SQLException {
        try {
            this.jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS plain_user");
            this.jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS case_test_lower");
            this.jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS Case_Test_Upper");
        } finally {
            this.fixture.close();
        }
    }
}
