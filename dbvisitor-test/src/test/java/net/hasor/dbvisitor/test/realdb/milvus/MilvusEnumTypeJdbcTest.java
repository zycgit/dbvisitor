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
import net.hasor.dbvisitor.test.contract.feature.type.EnumTypeJdbcCase;
import net.hasor.dbvisitor.test.contract.material.model.types.StatusEnum;
import net.hasor.dbvisitor.test.contract.material.model.types.StatusEnumOfCode;
import net.hasor.dbvisitor.test.contract.material.model.types.StatusEnumOfValue;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/** Keeps the shared enum SQL and assertions; native BM25 supplies the required vector field. */
public class MilvusEnumTypeJdbcTest extends EnumTypeJdbcCase {
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
                    CREATE TABLE enum_types_explicit_test (
                        id INT64 PRIMARY KEY, status_string VARCHAR(100) NULL,
                        status_ordinal INT32 NULL, status_code INT32 NULL,
                        vector_text VARCHAR(100) DEFAULT 'fixture' WITH (enable_analyzer=true),
                        v SPARSE_FLOAT_VECTOR, FUNCTION fixture_vector USING BM25 (vector_text) INTO (v)
                    ) WITH (consistency_level=Strong)
                    """);
            statement.executeUpdate("CREATE INDEX enum_v ON enum_types_explicit_test(v) USING SPARSE_INVERTED_INDEX WITH (metric_type=BM25)");
            statement.executeUpdate("LOAD TABLE enum_types_explicit_test");
        }
    }

    @After
    public void cleanupFixture() throws SQLException {
        try {
            if (this.connection != null) {
                try (Statement statement = this.connection.createStatement()) {
                    statement.executeUpdate("DROP TABLE IF EXISTS enum_types_explicit_test");
                }
            }
        } finally {
            this.database.close();
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_ENUM_BINDING)
    public void enumObjectsShouldBindAsNativeScalarValuesForWritesAndFilters() throws SQLException {
        String insert = "INSERT INTO enum_types_explicit_test (id,status_string,status_code) VALUES (?,?,?)";
        assertEquals(1, this.jdbcTemplate.executeUpdate(insert, new Object[] { 1, StatusEnum.ACTIVE, StatusEnumOfValue.ACTIVE }));
        assertEquals(1, this.jdbcTemplate.executeUpdate(insert, new Object[] { 2, StatusEnumOfCode.INACTIVE, StatusEnumOfValue.INACTIVE }));

        String select = "SELECT id FROM enum_types_explicit_test WHERE status_string=? AND status_code=?";
        assertEquals(Long.valueOf(1), this.jdbcTemplate.queryForLong(select, new Object[] { StatusEnum.ACTIVE, StatusEnumOfValue.ACTIVE }));
        assertEquals(Long.valueOf(2), this.jdbcTemplate.queryForLong(select, new Object[] { StatusEnumOfCode.INACTIVE, StatusEnumOfValue.INACTIVE }));
        assertEquals("ACTIVE", this.jdbcTemplate.queryForString("SELECT status_string FROM enum_types_explicit_test WHERE id=1"));
        assertEquals("inactive", this.jdbcTemplate.queryForString("SELECT status_string FROM enum_types_explicit_test WHERE id=2"));
        assertEquals(Integer.valueOf(0), this.jdbcTemplate.queryForInt("SELECT status_code FROM enum_types_explicit_test WHERE id=2"));

        assertEquals(1, this.jdbcTemplate.executeUpdate("UPDATE enum_types_explicit_test SET status_string=?,status_code=? WHERE id=?",
                new Object[] { StatusEnumOfCode.DELETED, StatusEnumOfValue.DELETED, 2 }));
        assertEquals(StatusEnumOfCode.DELETED, this.jdbcTemplate.queryForObject(
                "SELECT status_string FROM enum_types_explicit_test WHERE id=2", StatusEnumOfCode.class));
        assertEquals(StatusEnumOfValue.DELETED, this.jdbcTemplate.queryForObject(
                "SELECT status_code FROM enum_types_explicit_test WHERE id=2", StatusEnumOfValue.class));
    }
}
