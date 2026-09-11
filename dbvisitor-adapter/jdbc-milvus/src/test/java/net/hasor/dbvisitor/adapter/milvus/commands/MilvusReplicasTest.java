/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.*;
import java.util.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.ReplicaInfo;
import io.milvus.v2.service.collection.ShardReplica;
import io.milvus.v2.service.collection.request.DescribeReplicasReq;
import io.milvus.v2.service.collection.response.DescribeReplicasResp;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusReplicasTest {
    private final List<String>              methods  = new ArrayList<>();
    private final List<DescribeReplicasReq> requests = new ArrayList<>();
    private       DescribeReplicasResp      response = DescribeReplicasResp.builder().build();
    private       boolean                   fail;

    @Before
    public void install() {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if (method.getName().equals("getServerVersion")) {
                return "v2.6.2";
            }
            methods.add(method.getName());
            if (method.getName().equals("describeReplicas")) {
                requests.add((DescribeReplicasReq) args[0]);
                if (fail) {
                    throw new IllegalStateException("replica snapshot unavailable");
                }
                return response;
            }
            return null;
        });
    }

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    private Connection connect() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        properties.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530/replica_db", properties);
    }

    @Test
    public void snapshotShouldPreserveEverySdkFieldWithoutExpandingShardRows() throws SQLException {
        long largeId = 9007199254740993L;
        ShardReplica shard = ShardReplica.builder().leaderID(largeId).leaderAddress("node:21123").channelName("channel\"\nname").nodeIDs(Arrays.asList(largeId, 7L)).build();
        ReplicaInfo replica = ReplicaInfo.builder().replicaID(largeId).collectionID(20L).partitionIDs(Arrays.asList(30L, 31L)).shardReplicas(Arrays.asList(shard, shard)).nodeIDs(Arrays.asList(largeId, 7L)).resourceGroupName("query_group").numOutboundNode(Collections.singletonMap("other_group", 2)).build();
        response = DescribeReplicasResp.builder().replicas(Collections.singletonList(replica)).build();
        try (Connection connection = connect(); Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SHOW REPLICAS FROM TABLE replicas")) {
            assertEquals(7, rows.getMetaData().getColumnCount());
            assertEquals(Types.BIGINT, rows.getMetaData().getColumnType(1));
            assertEquals(Types.BIGINT, rows.getMetaData().getColumnType(2));
            for (int column = 3; column <= 7; column++) {
                assertEquals(Types.VARCHAR, rows.getMetaData().getColumnType(column));
            }
            assertTrue(rows.next());
            assertEquals(largeId, rows.getLong("REPLICA_ID"));
            assertEquals(20L, rows.getLong("COLLECTION_ID"));
            assertEquals("[30,31]", rows.getString("PARTITION_IDS"));
            assertEquals("[9007199254740993,7]", rows.getString("NODE_IDS"));
            assertEquals("query_group", rows.getString("RESOURCE_GROUP"));
            assertEquals(2, JsonParser.parseString(rows.getString("NUM_OUTBOUND_NODE")).getAsJsonObject().get("other_group").getAsInt());
            var shards = JsonParser.parseString(rows.getString("SHARD_REPLICAS")).getAsJsonArray();
            assertEquals(2, shards.size());
            JsonObject first = shards.get(0).getAsJsonObject();
            assertEquals(largeId, first.get("leaderID").getAsLong());
            assertEquals("node:21123", first.get("leaderAddress").getAsString());
            assertEquals("channel\"\nname", first.get("channelName").getAsString());
            assertEquals(largeId, first.getAsJsonArray("nodeIDs").get(0).getAsLong());
            assertFalse(rows.next());
        }
        assertEquals("replica_db", requests.get(0).getDatabaseName());
        assertEquals("replicas", requests.get(0).getCollectionName());
        assertEquals(Collections.singletonList("describeReplicas"), methods);
    }

    @Test
    public void emptyAndNullableSnapshotsShouldRetainJdbcMetadataAndNulls() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            try (ResultSet rows = statement.executeQuery("SHOW REPLICAS FROM books")) {
                assertEquals(7, rows.getMetaData().getColumnCount());
                assertFalse(rows.next());
            }
            ReplicaInfo replica = ReplicaInfo.builder().build();
            // The SDK builder defaults containers to empty; setters can still expose NULL.
            replica.setPartitionIDs(null);
            replica.setShardReplicas(null);
            replica.setNodeIDs(null);
            replica.setResourceGroupName(null);
            replica.setNumOutboundNode(null);
            response = DescribeReplicasResp.builder().replicas(Collections.singletonList(replica)).build();
            try (ResultSet rows = statement.executeQuery("SHOW REPLICAS FROM books")) {
                assertTrue(rows.next());
                assertEquals(0L, rows.getLong("REPLICA_ID"));
                assertTrue(rows.wasNull());
                for (int column = 2; column <= 7; column++) {
                    assertNull(rows.getObject(column));
                    assertTrue(rows.wasNull());
                }
                assertFalse(rows.next());
            }
        }
    }

    @Test
    public void maxRowsAndMultipleStatementsShouldKeepOneResultPerCommand() throws SQLException {
        response = DescribeReplicasResp.builder().replicas(Arrays.asList(ReplicaInfo.builder().replicaID(1L).build(), ReplicaInfo.builder().replicaID(2L).build())).build();
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.setMaxRows(1);
            statement.setFetchSize(1);
            assertTrue(statement.execute("SHOW REPLICAS FROM a; SHOW REPLICAS FROM TABLE b"));
            for (int index = 0; index < 2; index++) {
                try (ResultSet rows = statement.getResultSet()) {
                    assertTrue(rows.next());
                    assertEquals(1L, rows.getLong("REPLICA_ID"));
                    assertEquals("[]", rows.getString("SHARD_REPLICAS"));
                    assertEquals("{}", rows.getString("NUM_OUTBOUND_NODE"));
                    assertFalse(rows.next());
                }
                assertEquals(index == 0, statement.getMoreResults());
            }
            assertEquals(-1, statement.getUpdateCount());
        }
        assertEquals(2, requests.size());
        assertEquals("a", requests.get(0).getCollectionName());
        assertEquals("b", requests.get(1).getCollectionName());
    }

    @Test
    public void malformedCommandsShouldFailBeforeSdkExecution() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String sql : Arrays.asList("SHOW REPLICAS", "SHOW REPLICAS FROM ?", "SHOW REPLICAS FROM books LIMIT 1", "SHOW REPLICAS FROM books PARTITION p", "SHOW REPLICAS FROM books WITH (num_replicas=2)")) {
                assertThrows(sql, SQLException.class, () -> statement.execute(sql));
            }
        }
        assertTrue(methods.isEmpty());
    }

    @Test
    public void sdkFailureShouldNotBecomeEmptyRowsOrTriggerLoad() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            fail = true;
            SQLException error = assertThrows(SQLException.class, () -> statement.executeQuery("SHOW REPLICAS FROM books"));
            assertTrue(error.getMessage().contains("replica snapshot unavailable"));
        }
        assertEquals(Collections.singletonList("describeReplicas"), methods);
    }
}
