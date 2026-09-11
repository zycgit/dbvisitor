/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Arrays;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterContractTest;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.Test;
import static org.junit.Assert.*;

/** SQL grants on an isolated database; not an authorization-enforcement test. */
public class MilvusPrivilegeSqlContractTest extends AdapterContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_SCOPED_PRIVILEGES)
    public void scopedGrant_shouldTargetExplicitDatabaseAndBeRevocable() throws SQLException {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String database = "dbv_priv_" + suffix;
        String role = "dbv_role_" + suffix;
        boolean databaseCreated = false;
        boolean roleCreated = false;
        boolean granted = false;
        String revoke = "REVOKE PRIVILEGE Search ON DATABASE " + database + " TABLE books FROM ROLE " + role;
        try (Connection connection = newAdapterConnection(); Statement statement = connection.createStatement()) {
            try {
                statement.executeUpdate("CREATE DATABASE " + database);
                databaseCreated = true;
                statement.executeUpdate("CREATE ROLE " + role);
                roleCreated = true;
                assertEquals(0, statement.executeUpdate("GRANT PRIVILEGE Search ON DATABASE " + database + " TABLE books TO ROLE " + role));
                granted = true;
                assertEquals("default", connection.getCatalog());
                assertEquals(0, grants(statement, role, "default").size());
                JsonArray grants = grants(statement, role, database);
                assertEquals(1, grants.size());
                JsonObject grant = grants.get(0).getAsJsonObject();
                assertEquals(database, grant.get("dbName").getAsString());
                assertEquals("books", grant.get("objectName").getAsString());
                assertEquals("Search", grant.get("privilege").getAsString());
                assertEquals(0, statement.executeUpdate(revoke));
                granted = false;
                assertEquals(0, grants(statement, role, database).size());
            } finally {
                // Clean only resources created by this invocation, including on assertion failure.
                try {
                    if (granted) {
                        statement.executeUpdate(revoke);
                    }
                } finally {
                    try {
                        if (roleCreated) {
                            statement.executeUpdate("DROP ROLE " + role);
                        }
                    } finally {
                        if (databaseCreated) {
                            statement.executeUpdate("DROP DATABASE " + database);
                        }
                    }
                }
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_PRIVILEGE_GROUPS)
    public void privilegeGroup_shouldPreserveMembershipAcrossSqlLifecycle() throws SQLException {
        String group = "dbv_group_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        boolean created = false;
        try (Connection connection = newAdapterConnection(); Statement statement = connection.createStatement()) {
            try {
                assertEquals(0, statement.executeUpdate("CREATE PRIVILEGE GROUP " + group));
                created = true;
                assertEquals(Collections.emptySet(), privileges(statement, group));
                assertEquals(0, statement.executeUpdate("ALTER PRIVILEGE GROUP " + group + " ADD (Search, Query)"));
                assertEquals(new HashSet<>(Arrays.asList("Search", "Query")), privileges(statement, group));
                assertEquals(0, statement.executeUpdate("ALTER PRIVILEGE GROUP " + group + " DROP (Query)"));
                assertEquals(Collections.singleton("Search"), privileges(statement, group));
                assertEquals(0, statement.executeUpdate("DROP PRIVILEGE GROUP " + group));
                created = false;
                assertNull(privileges(statement, group));
            } finally {
                if (created) {
                    statement.executeUpdate("DROP PRIVILEGE GROUP " + group);
                }
            }
        }
    }

    private Set<String> privileges(Statement statement, String group) throws SQLException {
        try (ResultSet result = statement.executeQuery("SHOW PRIVILEGE GROUPS")) {
            while (result.next()) {
                if (group.equals(result.getString("PRIVILEGE_GROUP"))) {
                    Set<String> privileges = new HashSet<>();
                    JsonParser.parseString(result.getString("PRIVILEGES")).getAsJsonArray()
                            .forEach(value -> privileges.add(value.getAsString()));
                    return privileges;
                }
            }
            return null;
        }
    }

    private JsonArray grants(Statement statement, String role, String database) throws SQLException {
        try (ResultSet result = statement.executeQuery("SHOW ROLE " + role + " ON DATABASE " + database)) {
            assertTrue(result.next());
            assertEquals(role, result.getString("ROLE"));
            JsonArray grants = JsonParser.parseString(result.getString("GRANTS")).getAsJsonArray();
            assertFalse(result.next());
            return grants;
        }
    }
}
