/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusTemporalSqlContractTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_TEMPORAL_VARCHAR)
    public void jdbcTemporalBindingsShouldPreserveTextPrecisionAndMatchBoundFilters() throws SQLException {
        createCollection("id INT64 PRIMARY KEY,d VARCHAR(128),tm VARCHAR(128),ts VARCHAR(128) NULL,u VARCHAR(128),v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        loadCollection();
        java.sql.Date date = java.sql.Date.valueOf("2026-09-10");
        Time time = Time.valueOf("08:09:10");
        Timestamp timestamp = Timestamp.valueOf("2026-09-10 08:09:10.123456789");
        java.util.Date utilDate = new java.util.Date(timestamp.getTime());
        for (String command : new String[] { "INSERT", "UPSERT" }) {
            try (PreparedStatement insert = this.connection.prepareStatement(command + " INTO " + this.collection + " (id,d,tm,ts,u,v) VALUES (1,?,?,?,?,[1,0])")) {
                insert.setDate(1, date);
                insert.setTime(2, time);
                insert.setTimestamp(3, timestamp);
                insert.setObject(4, utilDate);
                assertEquals(1, insert.executeUpdate());
            }
            try (PreparedStatement query = this.connection.prepareStatement("SELECT d,tm,ts,u FROM " + this.collection + " WHERE d=? AND tm=? AND ts=? AND u=?")) {
                query.setDate(1, date);
                query.setTime(2, time);
                query.setTimestamp(3, timestamp);
                query.setObject(4, utilDate);
                try (ResultSet rows = query.executeQuery()) {
                    assertTrue(rows.next());
                    assertEquals(date, rows.getDate("d"));
                    assertEquals(time, rows.getTime("tm"));
                    assertEquals(timestamp, rows.getTimestamp("ts"));
                    assertEquals(timestamp.toString(), rows.getString("ts"));
                    assertEquals(utilDate.getTime(), rows.getTimestamp("u").getTime());
                    assertEquals(Types.VARCHAR, rows.getMetaData().getColumnType(3));
                    assertFalse(rows.next());
                }
            }
        }
        Timestamp updated = Timestamp.valueOf("2026-10-11 12:13:14.987654321");
        try (PreparedStatement update = this.connection.prepareStatement("UPDATE " + this.collection + " SET ts=? WHERE id=1")) {
            update.setTimestamp(1, updated);
            assertEquals(1, update.executeUpdate());
            try (Statement statement = this.connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT ts FROM " + this.collection + " WHERE id=1")) {
                assertTrue(rows.next());
                assertEquals(updated, rows.getTimestamp(1));
            }
            update.setNull(1, Types.TIMESTAMP);
            assertEquals(1, update.executeUpdate());
            try (Statement statement = this.connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT ts FROM " + this.collection + " WHERE id=1")) {
                assertTrue(rows.next());
                assertNull(rows.getTimestamp(1));
                assertTrue(rows.wasNull());
            }
        }
    }
}
