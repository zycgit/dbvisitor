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
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusReplicasSqlTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_REPLICAS)
    public void loadedCollectionShouldExposeNativeReplicaAndShardSnapshot() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        loadCollection();
        int count = 0;
        try (Statement statement = this.connection.createStatement();
                ResultSet rows = statement.executeQuery("SHOW REPLICAS FROM TABLE " + this.collection)) {
            assertEquals(7, rows.getMetaData().getColumnCount());
            assertEquals(Types.BIGINT, rows.getMetaData().getColumnType(1));
            while (rows.next()) {
                count++;
                assertTrue(rows.getLong("REPLICA_ID") > 0);
                assertTrue(rows.getLong("COLLECTION_ID") > 0);
                assertTrue(JsonParser.parseString(rows.getString("PARTITION_IDS")).isJsonArray());
                var nodes = JsonParser.parseString(rows.getString("NODE_IDS")).getAsJsonArray();
                assertFalse(nodes.isEmpty());
                assertEquals("__default_resource_group", rows.getString("RESOURCE_GROUP"));
                assertTrue(JsonParser.parseString(rows.getString("NUM_OUTBOUND_NODE")).isJsonObject());
                var shards = JsonParser.parseString(rows.getString("SHARD_REPLICAS")).getAsJsonArray();
                assertFalse(shards.isEmpty());
                for (JsonElement shard : shards) {
                    var object = shard.getAsJsonObject();
                    assertTrue(object.get("leaderID").getAsLong() > 0);
                    assertFalse(object.get("leaderAddress").getAsString().isEmpty());
                    assertFalse(object.get("channelName").getAsString().isEmpty());
                    assertTrue(nodes.contains(object.get("leaderID")));
                    // Native 2.6.2 returns an empty shard node list even after insertion and FLUSH.
                    // Do not invent entries from the replica's node list.
                    assertTrue(object.get("nodeIDs").isJsonArray());
                }
            }
        }
        assertEquals(1, count);
        assertEquals(0, countRows(""));
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_REPLICAS_FAILURE)
    public void missingCollectionShouldFailWithoutBreakingLaterJdbcResults() throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            assertThrows(SQLException.class, () -> statement.executeQuery("SHOW REPLICAS FROM " + this.collection));
            try (ResultSet rows = statement.executeQuery("SHOW VERSION")) {
                assertTrue(rows.next());
                assertNotNull(rows.getString("VERSION"));
                assertFalse(rows.next());
            }
            assertFalse(statement.getMoreResults());
            assertEquals(-1, statement.getUpdateCount());
        }
    }
}
