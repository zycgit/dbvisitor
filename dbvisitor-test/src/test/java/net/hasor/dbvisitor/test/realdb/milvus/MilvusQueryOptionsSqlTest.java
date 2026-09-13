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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusQueryOptionsSqlTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_QUERY_OPTIONS)
    public void ignoreGrowingShouldApplyToBoundedPagedAndCountQueries() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, v FLOAT_VECTOR(2)");
        jdbcTemplate.executeUpdate("INSERT INTO " + collection + " (id,v) VALUES (1,[1,0]),(2,[0,1])");
        jdbcTemplate.executeUpdate("FLUSH " + collection);
        createIndex("v", "FLAT", "L2");
        loadCollection();
        // The first two rows are sealed before LOAD; the third is a fresh growing row.
        jdbcTemplate.executeUpdate("INSERT INTO " + collection + " (id,v) VALUES (3,[1,1])");
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM " + collection
                + " WHERE id > ? LIMIT ? WITH(ignore_growing=?,timezone=?)")) {
            statement.setLong(1, 0L);
            statement.setInt(2, 10);
            statement.setString(4, "UTC");
            for (int fetchSize : new int[] { 20, 1 }) {
                statement.setFetchSize(fetchSize);
                for (boolean ignoreGrowing : new boolean[] { true, false }) {
                    statement.setBoolean(3, ignoreGrowing);
                    Set<Long> ids;
                    try (ResultSet rows = statement.executeQuery()) {
                        ids = new HashSet<>(readIds(rows));
                    }
                    Set<Long> expected = new HashSet<>(Arrays.asList(1L, 2L));
                    if (!ignoreGrowing) {
                        expected.add(3L);
                    }
                    assertEquals("fetchSize=" + fetchSize + ", ignoreGrowing=" + ignoreGrowing, expected, ids);
                }
            }
        }
        for (String count : new String[] { "COUNT", "SELECT COUNT(*)" }) {
            try (PreparedStatement statement = connection.prepareStatement(count + " FROM " + collection
                    + " WHERE id > ? WITH(ignore_growing=?,timezone=?)")) {
                statement.setLong(1, 0L);
                statement.setString(3, "UTC");
                for (boolean ignoreGrowing : new boolean[] { true, false }) {
                    statement.setBoolean(2, ignoreGrowing);
                    try (ResultSet rows = statement.executeQuery()) {
                        assertTrue(rows.next());
                        assertEquals(ignoreGrowing ? 2L : 3L, rows.getLong("COUNT"));
                        assertFalse(rows.next());
                    }
                }
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_QUERY_OPTIONS_VALIDATION)
    public void unsupportedOptionsShouldFailWithoutPoisoningTheStatement() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String options : new String[] { "nprobe=8", "ignore_growing='false'", "timezone=123", "round_decimal=2" }) {
                SQLException error = assertThrows(SQLException.class, () -> statement.executeQuery(
                        "SELECT id FROM " + collection + " WITH(" + options + ")"));
                assertTrue(error.getMessage(), error.getMessage().contains("WITH"));
            }
            try (ResultSet rows = statement.executeQuery("SHOW VERSION")) {
                assertTrue(rows.next());
                assertNotNull(rows.getString("VERSION"));
            }
        }
    }
}
