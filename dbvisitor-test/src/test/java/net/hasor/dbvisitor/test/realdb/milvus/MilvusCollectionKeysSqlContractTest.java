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

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusCollectionKeysSqlContractTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_PARTITION_KEY_VARCHAR)
    public void varcharPartitionKeyAndSeparateClusteringKeyShouldSupportFilteredWritesAndSearch() throws SQLException {
        try (PreparedStatement create = this.connection.prepareStatement("CREATE TABLE " + this.collection + """
                 (id INT64 PRIMARY KEY, tenant VARCHAR(64) PARTITION KEY, age INT32 CLUSTERING KEY, v FLOAT_VECTOR(2))
                 WITH (num_partitions=?, num_shards=?, description=?, consistency_level=Strong)
                """)) {
            create.setInt(1, 4);
            create.setInt(2, 1);
            create.setString(3, "tenant's collection");
            assertEquals(0, create.executeUpdate());
        }
        assertSchemaKeys("tenant", "age");
        assertPartitionsAndCreateScript(4);
        createIndex("v", "FLAT", "L2");
        loadCollection();
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " (id,tenant,age,v) VALUES (1,'a',20,[1,0]),(2,'b',30,[0,1]),(3,'a',40,[0.9,0.1])");
        try (PreparedStatement search = this.connection.prepareStatement("SELECT id FROM " + this.collection + " WHERE tenant=? AND age>=? ORDER BY v <-> ? LIMIT 3")) {
            search.setString(1, "a");
            search.setInt(2, 20);
            search.setObject(3, new float[] { 1, 0 });
            try (ResultSet result = search.executeQuery()) {
                assertEquals(Arrays.asList(1L, 3L), readIds(result));
            }
        }
        assertEquals(2, this.jdbcTemplate.executeUpdate("UPDATE " + this.collection + " SET age=50 WHERE tenant=?", "a"));
        assertEquals(2, countRows(" WHERE tenant='a' AND age=50"));
        assertEquals(1, countRows(" WHERE tenant='b' AND age=30"));
        assertEquals(2, this.jdbcTemplate.executeUpdate("DELETE FROM " + this.collection + " WHERE tenant=?", "a"));
        assertEquals(1, countRows(""));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_PARTITION_KEY_INT64)
    public void sameInt64FieldCanBePartitionAndClusteringKeyWithoutClientRouting() throws SQLException {
        this.jdbcTemplate.execute("CREATE TABLE " + this.collection + """
                 (id INT64 PRIMARY KEY, tenant INT64 PARTITION KEY CLUSTERING KEY, v FLOAT_VECTOR(2))
                 WITH (num_partitions=4, consistency_level=Strong)
                """);
        assertSchemaKeys("tenant", "tenant");
        assertPartitionsAndCreateScript(4);
        createIndex("v", "FLAT", "L2");
        loadCollection();
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " (id,tenant,v) VALUES (1,10,[1,0]),(2,20,[0,1]),(3,10,[0.9,0.1])");
        assertEquals(2, this.jdbcTemplate.executeUpdate("UPSERT INTO " + this.collection + " (id,tenant,v) VALUES (1,10,[0.8,0.2]),(2,20,[0.2,0.8])"));
        try (PreparedStatement query = this.connection.prepareStatement("SELECT id FROM " + this.collection + " WHERE tenant IN ? ORDER BY v <-> [1,0] LIMIT 3")) {
            query.setObject(1, Arrays.asList(10L));
            try (ResultSet result = query.executeQuery()) {
                assertEquals(Arrays.asList(3L, 1L), readIds(result));
            }
        }
        assertEquals(1, this.jdbcTemplate.executeUpdate("DELETE FROM " + this.collection + " WHERE tenant=10 LIMIT 1"));
        assertEquals(1, countRows(" WHERE tenant=10"));
        assertEquals(1, countRows(" WHERE tenant=20"));
    }

    private void assertSchemaKeys(String partitionKey, String clusteringKey) throws SQLException {
        int fields = 0;
        try (Statement statement = this.connection.createStatement(); ResultSet rows = statement.executeQuery("SHOW TABLE " + this.collection)) {
            while (rows.next()) {
                fields++;
                assertEquals(rows.getString("FIELD").equals(partitionKey), rows.getBoolean("PARTITION_KEY"));
                assertEquals(rows.getString("FIELD").equals(clusteringKey), rows.getBoolean("CLUSTERING_KEY"));
            }
        }
        assertTrue(fields >= 3);
    }

    private void assertPartitionsAndCreateScript(int expected) throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            int partitions = 0;
            try (ResultSet rows = statement.executeQuery("SHOW PARTITIONS FROM " + this.collection)) {
                while (rows.next()) {
                    partitions++;
                }
            }
            assertEquals(expected, partitions);
            try (ResultSet rows = statement.executeQuery("SHOW CREATE TABLE " + this.collection)) {
                assertTrue(rows.next());
                String sql = rows.getString("CREATE SCRIPT");
                assertTrue(sql, sql.contains("PARTITION KEY"));
                assertTrue(sql, sql.contains("CLUSTERING KEY"));
                assertTrue(sql, sql.contains("num_partitions=" + expected));
                assertTrue(sql, sql.contains("consistency_level='STRONG'"));
            }
        }
    }
}
