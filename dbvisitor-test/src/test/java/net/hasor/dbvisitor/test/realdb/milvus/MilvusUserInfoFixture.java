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

/** Isolated native schema for shared UserInfo contracts; SQL and entity mappings remain unchanged. */
final class MilvusUserInfoFixture implements AutoCloseable {
    private final MilvusDatabaseFixture database = new MilvusDatabaseFixture();
    private final boolean autoId;
    private Connection connection;

    MilvusUserInfoFixture() {
        this(false);
    }

    MilvusUserInfoFixture(boolean autoId) {
        this.autoId = autoId;
    }

    Connection open() throws SQLException {
        if (this.connection != null) {
            return this.connection;
        }
        this.connection = this.database.open();
        try (Statement statement = this.connection.createStatement()) {
            // Keep the mapped fields nullable; a native default supplies BM25's required text input.
            statement.executeUpdate("""
                    CREATE TABLE user_info (
                        id INT64 PRIMARY KEY %s, name VARCHAR(128) NULL,
                        age INT32 NULL, email VARCHAR(128) NULL, create_time VARCHAR(128) NULL,
                        vector_text VARCHAR(128) DEFAULT 'fixture' WITH (enable_analyzer=true),
                        v SPARSE_FLOAT_VECTOR, FUNCTION fixture_vector USING BM25 (vector_text) INTO (v)
                    ) WITH (consistency_level=Strong)
                    """.formatted(this.autoId ? "AUTO_ID" : ""));
            statement.executeUpdate("CREATE INDEX user_v ON user_info(v) USING SPARSE_INVERTED_INDEX WITH (metric_type=BM25)");
            statement.executeUpdate("LOAD TABLE user_info");
        }
        return this.connection;
    }

    @Override
    public void close() throws SQLException {
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
