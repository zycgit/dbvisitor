/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterContractTest;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.Test;

import static org.junit.Assert.*;

public class MilvusDatabaseSqlContractTest extends AdapterContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_DATABASE_PROPERTIES)
    public void databaseProperties_shouldBeCreatedChangedObservedAndRemovedUsingSql() throws SQLException {
        String database = "dbv_properties_" + UUID.randomUUID().toString().replace("-", "");
        String key = "database.max.collections";
        try (Connection connection = newAdapterConnection(); Statement statement = connection.createStatement()) {
            assertNotNull(properties(statement, "default"));
            try {
                try (PreparedStatement create = connection.prepareStatement("CREATE DATABASE " + database + " WITH ('" + key + "'=?)")) {
                    create.setInt(1, 10);
                    assertEquals(0, create.executeUpdate());
                }
                assertEquals("10", properties(statement, database).get(key).getAsString());
                statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + database + " WITH ('" + key + "'=30)");
                assertEquals("10", properties(statement, database).get(key).getAsString());
                try (PreparedStatement alter = connection.prepareStatement("ALTER DATABASE " + database + " SET PROPERTIES ('" + key + "'=?)")) {
                    alter.setInt(1, 20);
                    assertEquals(0, alter.executeUpdate());
                }
                assertEquals("20", properties(statement, database).get(key).getAsString());
                statement.executeUpdate("ALTER DATABASE " + database + " DROP PROPERTIES ('" + key + "')");
                assertFalse(properties(statement, database).has(key));
            } finally {
                statement.executeUpdate("DROP DATABASE IF EXISTS " + database);
            }
            assertThrows(SQLException.class, () -> statement.executeQuery("SHOW DATABASE " + database));
        }
    }

    private JsonObject properties(Statement statement, String database) throws SQLException {
        try (ResultSet result = statement.executeQuery("SHOW DATABASE " + database)) {
            assertTrue(result.next());
            assertEquals(database, result.getString("DATABASE"));
            JsonObject properties = JsonParser.parseString(result.getString("PROPERTIES")).getAsJsonObject();
            assertFalse(result.next());
            return properties;
        }
    }
}
