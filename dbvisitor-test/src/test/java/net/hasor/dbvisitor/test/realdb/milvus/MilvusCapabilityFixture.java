/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

/** Owns the native collection material used by the remaining shared capability contracts. */
final class MilvusCapabilityFixture implements AutoCloseable {
    private final MilvusDatabaseFixture database = new MilvusDatabaseFixture();
    private final List<String> collections = new ArrayList<>();
    private JdbcTemplate jdbc;

    JdbcTemplate open() throws SQLException {
        if (this.jdbc == null) {
            this.jdbc = new JdbcTemplate(this.database.open());
        }
        return this.jdbc;
    }

    void userTable(String table, String primaryKey) throws SQLException {
        create(table, primaryKey + ", name VARCHAR(128) NULL, age INT32 NULL, email VARCHAR(128) NULL, create_time VARCHAR(128) NULL");
    }

    void create(String table, String fields) throws SQLException {
        this.collections.add(table);
        this.jdbc.execute(schema(table, fields));
        this.jdbc.execute("CREATE INDEX fixture_v ON " + table + "(v) USING SPARSE_INVERTED_INDEX WITH (metric_type=BM25)");
        this.jdbc.execute("LOAD TABLE " + table);
    }

    static String schema(String table, String fields) {
        return "CREATE TABLE " + table + " (" + fields + """
                , vector_text VARCHAR(128) DEFAULT 'fixture' WITH (enable_analyzer=true),
                v SPARSE_FLOAT_VECTOR, FUNCTION fixture_vector USING BM25 (vector_text) INTO (v)
                ) WITH (consistency_level=Strong)
                """;
    }

    String executableSchema(String table, String fields) {
        this.collections.add(table);
        return schema(table, fields) + "; CREATE INDEX fixture_v ON " + table
                + "(v) USING SPARSE_INVERTED_INDEX WITH (metric_type=BM25); LOAD TABLE " + table;
    }

    void seedUsers(int baseId, int count, String prefix) throws SQLException {
        for (int i = 1; i <= count; i++) {
            this.jdbc.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)",
                    new Object[] { baseId + i, prefix + i, 20 + i, "fixture" + i + "@test.com", new Date() });
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            if (this.jdbc != null) {
                for (String table : this.collections) {
                    this.jdbc.execute("DROP TABLE IF EXISTS " + table);
                }
            }
        } finally {
            this.database.close();
        }
    }
}
