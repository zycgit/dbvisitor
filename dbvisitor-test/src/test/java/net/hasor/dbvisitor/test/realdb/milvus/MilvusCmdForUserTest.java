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
import java.sql.Statement;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MilvusCmdForUserTest extends AbstractMilvusCmdForTest {
    private static final String TEST_USER = "test_dbv_user";
    private static final String TEST_ROLE = "test_dbv_role";

    @BeforeEach
    public void setUp() throws Exception {
        // Ensure clean state
        cleanUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (!milvusSelected) {
            return;
        }
        cleanUp();
    }

    private void cleanUp() throws Exception {
        try (Connection conn = OneApiDataSourceManager.getConnection(MilvusProfile.INSTANCE.env()); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP USER IF EXISTS " + TEST_USER);
            if (hasRoleSdk(TEST_ROLE)) {
                try (ResultSet rows = stmt.executeQuery("SHOW GRANTS FOR ROLE " + TEST_ROLE)) {
                    while (rows.next()) {
                        String privilege = rows.getString("PRIVILEGE");
                        String objectType = rows.getString("OBJECT");
                        String objectName = rows.getString("OBJECT_NAME");
                        String sql = "REVOKE " + privilege + " ON " + objectType + " " + objectName + " FROM ROLE " + TEST_ROLE;
                        try (Statement revoke = conn.createStatement()) {
                            revoke.executeUpdate(sql);
                        }
                    }
                }
                stmt.executeUpdate("DROP ROLE " + TEST_ROLE);
            }
            dropCollection(TEST_COLLECTION);
        }
    }

    @Test
    public void testCreateUser() throws Exception {
        try (Connection conn = OneApiDataSourceManager.getConnection(MilvusProfile.INSTANCE.env()); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE USER " + TEST_USER + " PASSWORD \"123456\"");

            // Verify
            assertTrue(hasUserSdk(TEST_USER), "User should exist (SDK)");
        }
    }

    @Test
    public void testCreateUserIfNotExists() throws Exception {
        try (Connection conn = OneApiDataSourceManager.getConnection(MilvusProfile.INSTANCE.env()); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE USER " + TEST_USER + " PASSWORD \"123456\"");
            assertTrue(hasUserSdk(TEST_USER));

            // Should not fail
            stmt.executeUpdate("CREATE USER IF NOT EXISTS " + TEST_USER + " PASSWORD \"123456\"");
            assertTrue(hasUserSdk(TEST_USER));
        }
    }

    @Test
    public void testDropUser() throws Exception {
        try (Connection conn = OneApiDataSourceManager.getConnection(MilvusProfile.INSTANCE.env()); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE USER " + TEST_USER + " PASSWORD \"123456\"");
            assertTrue(hasUserSdk(TEST_USER));

            stmt.executeUpdate("DROP USER " + TEST_USER);

            // Verify
            assertFalse(hasUserSdk(TEST_USER), "User should not exist (SDK)");
        }
    }

    @Test
    public void testCreateRole() throws Exception {
        try (Connection conn = OneApiDataSourceManager.getConnection(MilvusProfile.INSTANCE.env()); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE ROLE " + TEST_ROLE);

            // Verify
            assertTrue(hasRoleSdk(TEST_ROLE), "Role should exist (SDK)");
        }
    }

    @Test
    public void testDropRole() throws Exception {
        try (Connection conn = OneApiDataSourceManager.getConnection(MilvusProfile.INSTANCE.env()); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE ROLE " + TEST_ROLE);
            assertTrue(hasRoleSdk(TEST_ROLE));

            stmt.executeUpdate("DROP ROLE " + TEST_ROLE);

            // Verify
            assertFalse(hasRoleSdk(TEST_ROLE), "Role should not exist (SDK)");
        }
    }

    @Test
    public void testGrantRole() throws Exception {
        try (Connection conn = OneApiDataSourceManager.getConnection(MilvusProfile.INSTANCE.env()); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE USER " + TEST_USER + " PASSWORD \"123456\"");
            stmt.executeUpdate("CREATE ROLE " + TEST_ROLE);

            assertTrue(hasUserSdk(TEST_USER), "Pre-condition: User exists");
            assertTrue(hasRoleSdk(TEST_ROLE), "Pre-condition: Role exists");
            assertFalse(userHasRoleSdk(TEST_USER, TEST_ROLE), "Pre-condition: User has no role");

            // Execute Grant
            stmt.executeUpdate("GRANT ROLE " + TEST_ROLE + " TO " + TEST_USER);

            // Verify
            assertTrue(userHasRoleSdk(TEST_USER, TEST_ROLE), "User should have role (SDK)");
        }
    }

    @Test
    public void testGrantPrivilege() throws Exception {
        try (Connection conn = OneApiDataSourceManager.getConnection(MilvusProfile.INSTANCE.env()); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE ROLE " + TEST_ROLE);

            // create collection for test
            if (!hasCollection(TEST_COLLECTION)) {
                createCollection(TEST_COLLECTION);
            }

            assertTrue(hasRoleSdk(TEST_ROLE), "Pre-condition: Role exists");
            assertFalse(roleHasPrivilegeSdk(TEST_ROLE, "Collection", TEST_COLLECTION, "Query"), "Pre-condition: Role has no privilege");

            // Execute Grant Privilege
            // GRANT Query ON Collection dbv_table_col TO ROLE test_dbv_role
            stmt.executeUpdate("GRANT Query ON Collection " + TEST_COLLECTION + " TO ROLE " + TEST_ROLE);

            // Verify
            assertTrue(roleHasPrivilegeSdk(TEST_ROLE, "Collection", TEST_COLLECTION, "Query"), "Role should have privilege (SDK)");
        }
    }

    @Test
    public void testRevokePrivilege() throws Exception {
        try (Connection conn = OneApiDataSourceManager.getConnection(MilvusProfile.INSTANCE.env()); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE ROLE " + TEST_ROLE);

            // create collection for test
            if (!hasCollection(TEST_COLLECTION)) {
                createCollection(TEST_COLLECTION);
            }

            // Setup grant
            stmt.executeUpdate("GRANT Query ON Collection " + TEST_COLLECTION + " TO ROLE " + TEST_ROLE);
            assertTrue(roleHasPrivilegeSdk(TEST_ROLE, "Collection", TEST_COLLECTION, "Query"), "Pre-condition: Role has privilege");

            // Execute Revoke
            stmt.executeUpdate("REVOKE Query ON Collection " + TEST_COLLECTION + " FROM ROLE " + TEST_ROLE);

            // Verify
            assertFalse(roleHasPrivilegeSdk(TEST_ROLE, "Collection", TEST_COLLECTION, "Query"), "Role should not have privilege (SDK)");
        }
    }

    @Test
    public void testGrantGlobalPrivilege() throws Exception {
        try (Connection conn = OneApiDataSourceManager.getConnection(MilvusProfile.INSTANCE.env()); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE ROLE " + TEST_ROLE);

            // Grant global privilege (usually objectName='*')
            // GRANT CreateCollection ON Global * TO ROLE test_dbv_role
            stmt.executeUpdate("GRANT CreateCollection ON Global * TO ROLE " + TEST_ROLE);

            // Verify
            assertTrue(roleHasPrivilegeSdk(TEST_ROLE, "Global", "*", "CreateCollection"), "Role should have global privilege (SDK)");
        }
    }

    public static void main(String[] args) {
        net.hasor.dbvisitor.test.realdb.RealDbTestRunner.run(MilvusCmdForUserTest.class);
    }
}
