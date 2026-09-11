/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusFunctionSqlContractTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_FUNCTION_DESCRIPTION)
    public void functionDescriptionShouldRoundTripWithNativeBm25WritesAndSearch() throws SQLException {
        try (PreparedStatement statement = this.connection.prepareStatement("CREATE TABLE " + this.collection + """
                 (id INT64 PRIMARY KEY,body VARCHAR(100) WITH (enable_analyzer=true),
                 sparse SPARSE_FLOAT_VECTOR,FUNCTION bm25_fn USING BM25 (body) INTO (sparse) DESCRIPTION ?)
                 WITH (consistency_level=Strong)
                """)) {
            statement.setString(1, "author's 中文说明");
            assertEquals(0, statement.executeUpdate());
        }
        assertTrue(createScript().contains("DESCRIPTION 'author''s 中文说明'"));
        createIndex("sparse", "SPARSE_INVERTED_INDEX", "BM25");
        loadCollection();
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " (id,body) VALUES (1,'native function search')");
        assertEquals(Long.valueOf(1), this.jdbcTemplate.queryForLong("SELECT id FROM " + this.collection
                + " ORDER BY sparse <?> 'native' LIMIT 1"));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_FUNCTION_UNAVAILABLE)
    public void missingOnlineApisShouldPreserveSchemaAndStopFollowingDrop() throws SQLException {
        createCollection("id INT64 PRIMARY KEY,body VARCHAR(100) WITH (enable_analyzer=true),sparse SPARSE_FLOAT_VECTOR,"
                + "FUNCTION bm25_fn USING BM25 (body) INTO (sparse)");
        String original = createScript();
        String[] changes = {
                "ADD FUNCTION other USING BM25 (body) INTO (sparse)",
                "ALTER FUNCTION bm25_fn USING BM25 (body) INTO (sparse) DESCRIPTION 'replacement'",
                "DROP FUNCTION bm25_fn"
        };
        try (Statement statement = this.connection.createStatement()) {
            for (String change : changes) {
                SQLException failure = assertThrows(SQLException.class, () -> statement.execute(
                        "ALTER TABLE " + this.collection + " " + change + "; DROP TABLE " + this.collection));
                assertTrue(failure.getMessage(), failure.getMessage().contains("UNIMPLEMENTED"));
                assertEquals(original, createScript());
            }
        }
        createIndex("sparse", "SPARSE_INVERTED_INDEX", "BM25");
        loadCollection();
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " (id,body) VALUES (1,'retained function')");
        assertEquals(1, countRows(""));
    }

    private String createScript() throws SQLException {
        return this.jdbcTemplate.queryForObject("SHOW CREATE TABLE " + this.collection,
                (rows, rowNum) -> rows.getString("CREATE SCRIPT"));
    }
}
