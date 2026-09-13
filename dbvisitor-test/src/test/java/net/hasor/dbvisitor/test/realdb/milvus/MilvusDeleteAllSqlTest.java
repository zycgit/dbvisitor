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
import java.util.Arrays;
import java.util.List;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/** Exercises DELETE without WHERE through JDBC, against a collection owned by each test. */
public class MilvusDeleteAllSqlTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_DELETE_ALL_LONG)
    public void deleteAllShouldIncludeNegativeZeroAndPositivePrimaryKeys() throws Exception {
        createFixture("INT64", false);
        insertKeys(Arrays.asList(Long.MIN_VALUE, -1L, 0L, Long.MAX_VALUE), "");
        assertEquals(4, countRows(""));
        assertEquals(4, this.jdbcTemplate.executeUpdate("DELETE FROM " + this.collection));
        assertEquals(0, countRows(""));
        assertEquals(0, this.jdbcTemplate.executeUpdate("DELETE FROM " + this.collection));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_DELETE_ALL_STRING)
    public void deleteAllShouldIncludeEmptyQuotedAndUnicodeStringKeys() throws Exception {
        createFixture("VARCHAR(128)", false);
        insertKeys(Arrays.asList("", "key\"with'quotes", "中文主键"), "");
        assertEquals(3, countRows(""));
        assertEquals(3, this.jdbcTemplate.executeUpdate("DELETE FROM " + this.collection));
        assertEquals(0, countRows(""));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_DELETE_ALL_LIMIT)
    public void deleteAllShouldRespectParameterizedLimitAndFetchSize() throws Exception {
        createFixture("INT64", false);
        insertKeys(Arrays.asList(-1L, 0L, 1L), "");
        try (PreparedStatement statement = this.connection.prepareStatement("DELETE FROM " + this.collection + " LIMIT ?")) {
            statement.setFetchSize(1);
            statement.setInt(1, 2);
            assertEquals(2, statement.executeUpdate());
            assertEquals(1, countRows(""));
            statement.setInt(1, 5);
            assertEquals(1, statement.executeUpdate());
            assertEquals(0, countRows(""));
            assertEquals(0, statement.executeUpdate());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_DELETE_ALL_PARTITION)
    public void deleteAllShouldKeepUnselectedPartitionsIntact() throws Exception {
        createFixture("VARCHAR(128)", true);
        insertKeys(Arrays.asList("selected-a", "selected-b"), " PARTITION selected_partition");
        insertKeys(List.of("untouched"), " PARTITION other_partition");
        assertEquals(3, countRows(""));
        assertEquals(1, this.jdbcTemplate.executeUpdate("DELETE FROM " + this.collection + " PARTITION selected_partition LIMIT 1"));
        assertEquals(2, countRows(""));
        assertEquals(1, this.jdbcTemplate.executeUpdate("DELETE FROM " + this.collection + " PARTITION selected_partition"));
        assertEquals(1, countRows(""));
        assertEquals(1, countRows(" WHERE record_key = 'untouched'"));
        assertEquals(1, this.jdbcTemplate.executeUpdate("DELETE FROM " + this.collection));
        assertEquals(0, countRows(""));
    }

    private void createFixture(String keyType, boolean partitions) throws SQLException {
        createCollection("record_key " + keyType + " PRIMARY KEY, note VARCHAR(32) NULL, v FLOAT_VECTOR(2)");
        if (partitions) {
            this.jdbcTemplate.execute("CREATE PARTITION selected_partition ON " + this.collection);
            this.jdbcTemplate.execute("CREATE PARTITION other_partition ON " + this.collection);
        }
        createIndex("v", "FLAT", "L2");
        loadCollection();
    }

    private void insertKeys(List<?> keys, String partition) throws SQLException {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO " + this.collection + partition + " (record_key, note, v) VALUES (?, ?, ?)")) {
            for (Object key : keys) {
                statement.setObject(1, key);
                statement.setString(2, null);
                statement.setObject(3, new float[] { 1, 0 });
                assertEquals(1, statement.executeUpdate());
            }
        }
    }
}
