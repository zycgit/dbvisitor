/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonParser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

/** Native resource-group inventory and isolated zero-node configuration lifecycle. */
public class MilvusResourceGroupSqlTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_RESOURCE_GROUP_CONFIG)
    public void emptyResourceGroups_shouldRoundTripConfigWithoutMovingExistingNodes() throws SQLException {
        String first = this.collection.substring(0, 28) + "_r1";
        String second = this.collection.substring(0, 28) + "_r2";
        Map<String, Object> config = Map.of("requests", Map.of("nodeNum", 0), "limits", Map.of("nodeNum", 0));
        List<String> created = new ArrayList<>();
        try {
            for (String group : List.of(first, second)) {
                try (PreparedStatement statement = this.connection.prepareStatement("CREATE RESOURCE GROUP " + group + " CONFIG ?")) {
                    statement.setObject(1, config);
                    assertEquals(0, statement.executeUpdate());
                    created.add(group);
                }
            }
            String snapshot;
            try (Statement statement = this.connection.createStatement(); ResultSet result = statement.executeQuery("SHOW RESOURCE GROUP " + first)) {
                assertTrue(result.next());
                snapshot = result.getString("CONFIG");
                assertNotNull(snapshot);
                assertEquals(0, JsonParser.parseString(snapshot).getAsJsonObject().getAsJsonObject("requests").get("nodeNum").getAsInt());
                assertEquals(0, result.getInt("AVAILABLE_NODES"));
            }
            try (PreparedStatement statement = this.connection.prepareStatement("ALTER RESOURCE GROUP " + first + " CONFIG ?")) {
                statement.setString(1, snapshot);
                assertEquals(0, statement.executeUpdate());
            }
            try (PreparedStatement statement = this.connection.prepareStatement("ALTER RESOURCE GROUPS CONFIG ?")) {
                statement.setObject(1, Map.of(first, config, second, config));
                assertEquals(0, statement.executeUpdate());
            }
            for (String group : created) {
                try (Statement statement = this.connection.createStatement(); ResultSet result = statement.executeQuery("SHOW RESOURCE GROUP " + group)) {
                    assertTrue(result.next());
                    assertEquals(0, result.getInt("AVAILABLE_NODES"));
                    assertEquals(0, JsonParser.parseString(result.getString("CONFIG")).getAsJsonObject().getAsJsonObject("limits").get("nodeNum").getAsInt());
                }
            }
        } finally {
            SQLException failure = null;
            for (String group : created) {
                try {
                    assertEquals(0, this.jdbcTemplate.executeUpdate("DROP RESOURCE GROUP " + group));
                } catch (SQLException e) {
                    if (failure == null) {
                        failure = e;
                    } else {
                        failure.addSuppressed(e);
                    }
                }
            }
            if (failure != null) {
                throw failure;
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_RESOURCE_GROUPS)
    public void resourceGroups_shouldListAndDescribeNativeClusterState() throws SQLException {
        String group;
        try (Statement statement = this.connection.createStatement(); ResultSet result = statement.executeQuery("SHOW RESOURCE GROUPS")) {
            assertTrue(result.next());
            group = result.getString("RESOURCE_GROUP");
            assertNotNull(group);
            assertTrue(group.matches("[a-zA-Z_][a-zA-Z0-9_.-]*"));
        }
        try (Statement statement = this.connection.createStatement();
                ResultSet result = statement.executeQuery("SHOW RESOURCE GROUP " + group)) {
            assertTrue(result.next());
            assertEquals(group, result.getString("RESOURCE_GROUP"));
            assertEquals(Types.INTEGER, result.getMetaData().getColumnType(2));
            assertTrue(result.getInt("CAPACITY") >= 0);
            assertTrue(result.getInt("AVAILABLE_NODES") >= 0);
            for (String column : new String[] { "LOADED_REPLICAS", "INCOMING_NODES", "OUTGOING_NODES" }) {
                assertTrue(JsonParser.parseString(result.getString(column)).isJsonObject());
            }
            assertTrue(JsonParser.parseString(result.getString("NODES")).isJsonArray());
            String config = result.getString("CONFIG");
            if (config != null) {
                assertTrue(JsonParser.parseString(config).isJsonObject());
            }
            assertFalse(result.next());
        }
    }
}
