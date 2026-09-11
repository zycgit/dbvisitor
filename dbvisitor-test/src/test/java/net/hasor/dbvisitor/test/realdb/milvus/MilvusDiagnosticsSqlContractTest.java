/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import com.google.gson.JsonParser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusDiagnosticsSqlContractTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_SERVER_DIAGNOSTICS)
    public void serverSnapshotsShouldBeAccessibleAsSeparateJdbcResults() throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            assertTrue(statement.execute("SHOW VERSION; SHOW HEALTH"));
            try (ResultSet rows = statement.getResultSet()) {
                assertTrue(rows.next());
                assertEquals(5, rows.getMetaData().getColumnCount());
                assertFalse(rows.getString("VERSION").isEmpty());
                assertEquals(Types.VARCHAR, rows.getMetaData().getColumnType(1));
                // Older servers may leave build details empty; do not manufacture values.
                rows.getString("BUILD_TIME");
                rows.getString("GIT_COMMIT");
                rows.getString("GO_VERSION");
                rows.getString("DEPLOY_MODE");
                assertFalse(rows.next());
            }
            assertTrue(statement.getMoreResults());
            try (ResultSet rows = statement.getResultSet()) {
                assertTrue(rows.next());
                assertTrue(rows.getString("REASONS"), rows.getBoolean("IS_HEALTHY"));
                assertFalse(rows.wasNull());
                assertEquals(Types.BOOLEAN, rows.getMetaData().getColumnType(1));
                assertTrue(JsonParser.parseString(rows.getString("REASONS")).isJsonArray());
                assertTrue(JsonParser.parseString(rows.getString("QUOTA_STATES")).isJsonArray());
                assertFalse(rows.next());
            }
            assertFalse(statement.getMoreResults());
            assertEquals(-1, statement.getUpdateCount());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_SEGMENT_DIAGNOSTICS)
    public void segmentSnapshotsShouldDescribeOnlyTheExplicitlyCreatedCollection() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, v FLOAT_VECTOR(2)");
        try (Statement statement = this.connection.createStatement()) {
            try (ResultSet rows = statement.executeQuery("SHOW PERSISTENT SEGMENTS FROM " + this.collection)) {
                assertEquals(9, rows.getMetaData().getColumnCount());
                assertFalse(rows.next());
            }
            this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " (id,v) VALUES (1,[1,0]),(2,[0,1])");
            // Material setup flushes explicitly; SHOW itself never changes collection state.
            statement.executeUpdate("FLUSH " + this.collection);
            createIndex("v", "FLAT", "L2");
            loadCollection();
            for (String kind : new String[] { "PERSISTENT", "QUERY" }) {
                int segments = 0;
                try (ResultSet rows = statement.executeQuery("SHOW " + kind + " SEGMENTS FROM TABLE " + this.collection)) {
                    assertEquals(kind.equals("PERSISTENT") ? 9 : 12, rows.getMetaData().getColumnCount());
                    assertEquals(Types.BIGINT, rows.getMetaData().getColumnType(1));
                    while (rows.next()) {
                        segments++;
                        assertTrue(rows.getLong("SEGMENT_ID") > 0);
                        assertTrue(rows.getLong("COLLECTION_ID") > 0);
                        assertTrue(rows.getLong("PARTITION_ID") > 0);
                        assertTrue(rows.getLong("NUM_ROWS") >= 2);
                        assertNotNull(rows.getString("STATE"));
                        if (kind.equals("QUERY")) {
                            assertTrue(JsonParser.parseString(rows.getString("NODE_IDS")).isJsonArray());
                        }
                    }
                }
                assertTrue(kind, segments > 0);
            }
            assertEquals(2, countRows(""));
            assertThrows(SQLException.class, () -> statement.executeQuery("SHOW PERSISTENT SEGMENTS FROM " + this.collection + "_missing"));
        }
    }
}
