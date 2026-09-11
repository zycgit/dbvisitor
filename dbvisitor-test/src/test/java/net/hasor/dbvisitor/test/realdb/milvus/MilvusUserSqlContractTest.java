/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import org.junit.Test;
import static org.junit.Assert.*;

/** Native user management SQL; this does not test login enforcement on an auth-disabled server. */
public class MilvusUserSqlContractTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_PRINCIPAL_CREATE_OPTIONS)
    public void creationOptionsShouldKeepBaselineDescriptionBoundaryAndExistingPrincipals() throws SQLException {
        String prefix = this.collection.substring(0, 28);
        String user = prefix + "_u";
        String role = prefix + "_r";
        String description = "描述 '\" ; DROP ROLE admin; --";
        boolean userCreated = false;
        boolean roleCreated = false;
        try (PreparedStatement createUser = this.connection.prepareStatement("CREATE USER IF NOT EXISTS " + user + " PASSWORD ? WITH (description=?)");
                PreparedStatement createRole = this.connection.prepareStatement("CREATE ROLE IF NOT EXISTS " + role + " WITH (description=?)");
                Statement statement = this.connection.createStatement()) {
            createUser.setString(1, "Dbv_initial_123!");
            createUser.setString(2, description);
            assertEquals(0, createUser.executeUpdate());
            userCreated = true;
            createRole.setString(1, description);
            assertEquals(0, createRole.executeUpdate());
            roleCreated = true;
            try (ResultSet result = statement.executeQuery("SHOW USER " + user)) {
                assertTrue(result.next());
                assertEquals(user, result.getString("USER"));
                // The native SDK also returns an empty description on the 2.6.2 baseline.
                assertEquals("", result.getString("DESCRIPTION"));
                assertFalse(result.next());
            }
            try (ResultSet result = statement.executeQuery("SHOW ROLE " + role)) {
                assertTrue(result.next());
                assertEquals(role, result.getString("ROLE"));
                assertEquals("", result.getString("DESCRIPTION"));
                assertFalse(result.next());
            }
            createUser.setString(1, "Dbv_unused_456!");
            createUser.setString(2, "replacement must not be applied");
            createRole.setString(1, "replacement must not be applied");
            assertEquals(0, createUser.executeUpdate());
            assertEquals(0, createRole.executeUpdate());
            try (PreparedStatement update = this.connection.prepareStatement("ALTER USER " + user + " PASSWORD ? REPLACE ?")) {
                update.setString(1, "Dbv_updated_789!");
                update.setString(2, "Dbv_initial_123!");
                assertEquals(0, update.executeUpdate());
            }
        } finally {
            try {
                if (userCreated) {
                    this.jdbcTemplate.execute("DROP USER " + user);
                }
            } finally {
                if (roleCreated) {
                    this.jdbcTemplate.execute("DROP ROLE " + role);
                }
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_ROLE_FORCE_DROP)
    public void forceDropShouldRemoveGrantsAndMembershipWithoutDeletingUser() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, v FLOAT_VECTOR(2)");
        String prefix = this.collection.substring(0, 28);
        String role = prefix + "_r";
        String user = prefix + "_u";
        boolean roleCreated = false;
        boolean userCreated = false;
        boolean granted = false;
        try (Statement statement = this.connection.createStatement()) {
            statement.executeUpdate("CREATE ROLE " + role);
            roleCreated = true;
            statement.executeUpdate("CREATE USER " + user + " PASSWORD 'Dbv_initial_123!'");
            userCreated = true;
            statement.executeUpdate("GRANT Search ON Collection " + this.collection + " TO ROLE " + role);
            granted = true;
            statement.executeUpdate("GRANT ROLE " + role + " TO " + user);
            try (PreparedStatement drop = this.connection.prepareStatement("DROP ROLE " + role + " WITH (force_drop=?)")) {
                drop.setBoolean(1, false);
                assertThrows(SQLException.class, drop::executeUpdate);
                drop.setBoolean(1, true);
                assertEquals(0, drop.executeUpdate());
                roleCreated = false;
            }
            try (ResultSet result = statement.executeQuery("SHOW ROLES")) {
                while (result.next()) {
                    assertNotEquals(role, result.getString("ROLE"));
                }
            }
            try (ResultSet result = statement.executeQuery("SHOW USER " + user)) {
                assertTrue(result.next());
                assertEquals(user, result.getString("USER"));
                JsonArray roles = JsonParser.parseString(result.getString("ROLES")).getAsJsonArray();
                assertFalse(roles.contains(new com.google.gson.JsonPrimitive(role)));
                assertFalse(result.next());
            }
            assertEquals(0, statement.executeUpdate("DROP ROLE IF EXISTS " + role + " WITH (force_drop=true)"));
        } finally {
            try {
                if (userCreated) {
                    this.jdbcTemplate.execute("DROP USER " + user);
                }
            } finally {
                if (roleCreated) {
                    if (granted) {
                        this.jdbcTemplate.execute("REVOKE Search ON Collection " + this.collection + " FROM ROLE " + role);
                    }
                    this.jdbcTemplate.execute("DROP ROLE " + role);
                }
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_USER_MANAGEMENT)
    public void userLifecycle_shouldDescribeRolesAndAcceptBoundPasswordChanges() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, v FLOAT_VECTOR(2)");
        // Milvus 2.6.2 usernames have a smaller length limit than collection names.
        String principalPrefix = this.collection.substring(0, 28);
        String user = principalPrefix + "_u";
        String role = principalPrefix + "_r";
        boolean userCreated = false;
        boolean roleCreated = false;
        boolean privilegeGranted = false;
        try {
            try (PreparedStatement create = this.connection.prepareStatement("CREATE USER " + user + " PASSWORD ?")) {
                create.setString(1, "Dbv_initial_123!");
                assertEquals(0, create.executeUpdate());
                userCreated = true;
            }
            try (Statement statement = this.connection.createStatement()) {
                assertEquals(0, statement.executeUpdate("CREATE ROLE " + role));
                roleCreated = true;
                try (ResultSet result = statement.executeQuery("SHOW ROLE " + role)) {
                    assertTrue(result.next());
                    assertEquals(role, result.getString("ROLE"));
                    assertEquals("[]", result.getString("GRANTS"));
                    assertFalse(result.next());
                }
                // Exercise the extra-properties connection path as well as the ordinary fixture connection.
                try (Connection grantConnection = OneApiDataSourceManager.getConnection("milvus", new Properties());
                        Statement grant = grantConnection.createStatement()) {
                    assertEquals(0, grant.executeUpdate("GRANT Search ON Collection " + this.collection + " TO ROLE " + role));
                    privilegeGranted = true;
                }
                try (ResultSet result = statement.executeQuery("SHOW ROLE " + role)) {
                    assertTrue(result.next());
                    JsonArray grants = JsonParser.parseString(result.getString("GRANTS")).getAsJsonArray();
                    assertEquals(1, grants.size());
                    assertEquals(this.collection, grants.get(0).getAsJsonObject().get("objectName").getAsString());
                    assertEquals("Search", grants.get(0).getAsJsonObject().get("privilege").getAsString());
                    assertEquals("default", grants.get(0).getAsJsonObject().get("dbName").getAsString());
                    assertFalse(result.next());
                }
                try (ResultSet result = statement.executeQuery("SHOW GRANTS FOR ROLE " + role)) {
                    assertTrue(result.next());
                    assertEquals(role, result.getString("ROLE"));
                    assertEquals(this.collection, result.getString("OBJECT_NAME"));
                    assertFalse(result.next());
                }
                assertEquals(0, statement.executeUpdate("REVOKE Search ON Collection " + this.collection + " FROM ROLE " + role));
                privilegeGranted = false;
                assertEquals(0, statement.executeUpdate("GRANT ROLE " + role + " TO " + user));
                try (ResultSet result = statement.executeQuery("SHOW USER " + user)) {
                    assertTrue(result.next());
                    assertEquals(user, result.getString("USER"));
                    JsonArray roles = JsonParser.parseString(result.getString("ROLES")).getAsJsonArray();
                    assertTrue(roles.contains(new com.google.gson.JsonPrimitive(role)));
                    assertFalse(result.next());
                }
            }
            try (PreparedStatement update = this.connection.prepareStatement("ALTER USER " + user + " PASSWORD ? REPLACE ?")) {
                update.setString(1, "Dbv_updated_456!");
                update.setString(2, "Dbv_initial_123!");
                assertEquals(0, update.executeUpdate());
                // Reuse the statement with the new credential as the old password.
                update.setString(1, "Dbv_initial_123!");
                update.setString(2, "Dbv_updated_456!");
                assertEquals(0, update.executeUpdate());
            }
            try (Statement statement = this.connection.createStatement()) {
                assertEquals(0, statement.executeUpdate("REVOKE ROLE " + role + " FROM " + user));
                try (ResultSet result = statement.executeQuery("SHOW USER " + user)) {
                    assertTrue(result.next());
                    JsonArray roles = JsonParser.parseString(result.getString("ROLES")).getAsJsonArray();
                    assertFalse(roles.contains(new com.google.gson.JsonPrimitive(role)));
                    assertFalse(result.next());
                }
            }
        } finally {
            try {
                if (userCreated) {
                    this.jdbcTemplate.execute("DROP USER " + user);
                }
            } finally {
                if (roleCreated) {
                    if (privilegeGranted) {
                        this.jdbcTemplate.execute("REVOKE Search ON Collection " + this.collection + " FROM ROLE " + role);
                    }
                    this.jdbcTemplate.execute("DROP ROLE " + role);
                }
            }
        }
    }
}
