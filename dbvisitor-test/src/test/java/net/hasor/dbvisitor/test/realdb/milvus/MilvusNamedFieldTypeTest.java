/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;

import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.feature.type.NativeNamedFieldTypeCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertEquals;

public class MilvusNamedFieldTypeTest extends NativeNamedFieldTypeCase {
    private final MilvusDatabaseFixture database = new MilvusDatabaseFixture();
    private       Connection            connection;

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
                    CREATE TABLE named_types (
                        id INT64 PRIMARY KEY, date_value VARCHAR(128) NULL,
                        state_value VARCHAR(32) NULL, boolean_value BOOL NULL,
                        vector_text VARCHAR(100) DEFAULT 'fixture' WITH (enable_analyzer=true),
                        v SPARSE_FLOAT_VECTOR, FUNCTION fixture_vector USING BM25 (vector_text) INTO (v)
                    ) WITH (consistency_level=Strong)
                    """);
            statement.executeUpdate("CREATE INDEX named_v ON named_types(v) USING SPARSE_INVERTED_INDEX WITH (metric_type=BM25)");
            statement.executeUpdate("LOAD TABLE named_types");
        }
    }

    @Override
    protected <T> void assertRoundTrip(T expected, Class<T> type) throws SQLException {
        String column;
        if (type == Date.class) {
            column = "date_value";
        } else if (type == Boolean.class) {
            column = "boolean_value";
        } else {
            column = "state_value";
        }
        int id = Boolean.FALSE.equals(expected) ? 2 : 1;
        Object storedValue = expected instanceof State ? ((State) expected).name() : expected;
        this.jdbcTemplate.executeUpdate("INSERT INTO named_types (id, " + column + ") VALUES (?, ?)", new Object[] { id, storedValue });
        Object actual = this.jdbcTemplate.queryForObject("SELECT " + column + " FROM named_types WHERE id = ?", new Object[] { id }, (rs, row) -> TypeHandlerRegistry.DEFAULT.getTypeHandler(type).getResult(rs, column));
        assertEquals(expected, actual);
    }

    @Override
    @After
    public void closeFixture() throws SQLException {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                try (Statement statement = this.connection.createStatement()) {
                    statement.executeUpdate("DROP TABLE IF EXISTS named_types");
                }
            }
        } finally {
            this.database.close();
        }
    }
}
