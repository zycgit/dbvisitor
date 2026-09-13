/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.*;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterCase;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.Test;
import static org.junit.Assert.*;

/** SQL flush scope and completion; no transaction, recovery, or all-databases performance claim. */
public class MilvusFlushSqlTest extends AdapterCase {
    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_FLUSH_COLLECTIONS)
    public void collectionListShouldFlushInExplicitDatabaseAndPreserveConnectionCatalog() throws SQLException {
        try (MilvusUserInfoFixture fixture = new MilvusUserInfoFixture(); Connection caller = newAdapterConnection()) {
            Connection data = fixture.open();
            String database = data.getCatalog();
            String originalCatalog = caller.getCatalog();
            try (Statement statement = data.createStatement()) {
                try {
                    statement.executeUpdate("CREATE TABLE flush_extra (id INT64 PRIMARY KEY, v FLOAT_VECTOR(2))");
                    statement.executeUpdate("INSERT INTO user_info(id, name) VALUES (1, 'flush user')");
                    statement.executeUpdate("INSERT INTO flush_extra(id, v) VALUES (2, [1, 0])");
                    try (PreparedStatement flush = caller.prepareStatement("FLUSH user_info, flush_extra IN DATABASE " + database + " WITH (wait_flushed_timeout_ms=?)")) {
                        flush.setLong(1, 60000);
                        assertEquals(0, flush.executeUpdate());
                        assertNull(flush.getResultSet());
                    }
                    assertEquals(originalCatalog, caller.getCatalog());
                    for (String collection : new String[] { "user_info", "flush_extra" }) {
                        try (ResultSet rows = statement.executeQuery("SHOW STATS FROM " + collection)) {
                            assertTrue(rows.next());
                            assertEquals(1, rows.getLong("NUM_ENTITIES"));
                            assertFalse(rows.next());
                        }
                    }
                } finally {
                    statement.executeUpdate("DROP TABLE IF EXISTS flush_extra");
                }
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_FLUSH_ALL)
    public void flushAllShouldReturnCompletedTimestampForExplicitAndCurrentDatabase() throws SQLException {
        try (MilvusUserInfoFixture first = new MilvusUserInfoFixture(); MilvusUserInfoFixture second = new MilvusUserInfoFixture()) {
            Connection firstData = first.open();
            Connection caller = second.open();
            String firstDatabase = firstData.getCatalog();
            String callerDatabase = caller.getCatalog();
            try (Statement seed = firstData.createStatement(); Statement statement = caller.createStatement()) {
                seed.executeUpdate("INSERT INTO user_info(id, name) VALUES (1, 'first flush')");
                statement.executeUpdate("INSERT INTO user_info(id, name) VALUES (2, 'second flush')");
                for (String scope : new String[] { " IN DATABASE " + firstDatabase, "" }) {
                    long timestamp;
                    try (PreparedStatement flush = caller.prepareStatement("FLUSH ALL TABLES" + scope + " WITH (wait_flushed_timeout_ms=?)")) {
                        flush.setInt(1, 60000);
                        assertTrue(flush.execute());
                        assertEquals(-1, flush.getUpdateCount());
                        try (ResultSet rows = flush.getResultSet()) {
                            assertEquals(1, rows.getMetaData().getColumnCount());
                            assertEquals(Types.BIGINT, rows.getMetaData().getColumnType(1));
                            assertTrue(rows.next());
                            timestamp = rows.getLong("FLUSH_ALL_TS");
                            assertFalse(rows.wasNull());
                            assertTrue(timestamp > 0);
                            assertFalse(rows.next());
                        }
                        assertFalse(flush.getMoreResults());
                    }
                    try (PreparedStatement state = caller.prepareStatement("SHOW FLUSH ALL ?" + scope)) {
                        state.setLong(1, timestamp);
                        try (ResultSet rows = state.executeQuery()) {
                            assertTrue(rows.next());
                            assertEquals(timestamp, rows.getLong("FLUSH_ALL_TS"));
                            assertTrue(rows.getBoolean("FLUSHED"));
                            assertFalse(rows.wasNull());
                            assertFalse(rows.next());
                        }
                    }
                    assertEquals(callerDatabase, caller.getCatalog());
                }
                try (ResultSet rows = statement.executeQuery("SHOW STATS FROM user_info")) {
                    assertTrue(rows.next());
                    assertEquals(1, rows.getLong("NUM_ENTITIES"));
                }
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_FLUSH_ALL_STATE)
    public void stateShouldPreserveNativeIncompleteResultAndRejectMissingDatabase() throws SQLException {
        try (MilvusUserInfoFixture fixture = new MilvusUserInfoFixture()) {
            Connection connection = fixture.open();
            String database = connection.getCatalog();
            try (Statement seed = connection.createStatement()) {
                seed.executeUpdate("INSERT INTO user_info(id, name) VALUES(1, 'state query')");
            }
            // A future token cannot be completed by the existing collection's current checkpoints.
            for (String scope : new String[] { "", " IN DATABASE " + database }) {
                try (PreparedStatement statement = connection.prepareStatement("SHOW FLUSH ALL ?" + scope)) {
                    statement.setLong(1, Long.MAX_VALUE);
                    try (ResultSet rows = statement.executeQuery()) {
                        assertEquals(2, rows.getMetaData().getColumnCount());
                        assertEquals(Types.BIGINT, rows.getMetaData().getColumnType(1));
                        assertEquals(Types.BOOLEAN, rows.getMetaData().getColumnType(2));
                        assertTrue(rows.next());
                        assertEquals(Long.MAX_VALUE, rows.getLong("FLUSH_ALL_TS"));
                        assertFalse(rows.getBoolean("FLUSHED"));
                        assertFalse(rows.wasNull());
                        assertFalse(rows.next());
                    }
                }
            }
            try (Statement statement = connection.createStatement()) {
                SQLException error = assertThrows(SQLException.class,
                        () -> statement.executeQuery("SHOW FLUSH ALL 0 IN DATABASE " + database + "_missing"));
                assertNotNull(error.getMessage());
                try (ResultSet rows = statement.executeQuery("SHOW STATS FROM user_info")) {
                    assertTrue(rows.next());
                }
            }
        }
    }
}
