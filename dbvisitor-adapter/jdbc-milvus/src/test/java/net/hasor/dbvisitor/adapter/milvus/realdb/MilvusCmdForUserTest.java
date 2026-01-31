package net.hasor.dbvisitor.adapter.milvus.realdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MilvusCmdForUserTest extends AbstractMilvusCmdForTest {
    private static final String TEST_USER = "test_dbv_user";
    private static final String TEST_ROLE = "test_dbv_role";

    @Before
    public void setUp() {
        if (!milvusReady) {
            return;
        }
        // Ensure clean state
        cleanUp();
    }

    @After
    public void tearDown() {
        if (!milvusReady) {
            return;
        }
        cleanUp();
    }

    private void cleanUp() {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            try {
                stmt.executeUpdate("DROP USER IF EXISTS " + TEST_USER);
            } catch (Exception e) {
            }
            try {
                System.out.println("Trying to show grants for " + TEST_ROLE);
                java.sql.ResultSet rs = stmt.executeQuery("SHOW GRANTS FOR ROLE " + TEST_ROLE);
                int count = 0;
                while (rs.next()) {
                    count++;
                    String privilege = rs.getString("PRIVILEGE");
                    String objectType = rs.getString("OBJECT");
                    String objectName = rs.getString("OBJECT_NAME");
                    String sql = "REVOKE " + privilege + " ON " + objectType + " " + objectName + " FROM ROLE " + TEST_ROLE;
                    System.out.println("Revoking: " + sql);
                    try (Statement revokeStmt = conn.createStatement()) {
                        revokeStmt.executeUpdate(sql);
                    } catch (Exception e) {
                        System.err.println("Failed to revoke: " + sql);
                        e.printStackTrace();
                    }
                }
                System.out.println("Found " + count + " grants.");
            } catch (Exception e) {
                System.err.println("SHOW GRANTS failed");
                e.printStackTrace();
            }
            try {
                stmt.executeUpdate("DROP ROLE IF EXISTS " + TEST_ROLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateUser() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE USER " + TEST_USER + " PASSWORD \"123456\"");

            // Verify
            assertTrue("User should exist (SDK)", hasUserSdk(TEST_USER));
        }
    }

    @Test
    public void testCreateUserIfNotExists() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE USER " + TEST_USER + " PASSWORD \"123456\"");
            assertTrue(hasUserSdk(TEST_USER));

            // Should not fail
            stmt.executeUpdate("CREATE USER IF NOT EXISTS " + TEST_USER + " PASSWORD \"123456\"");
            assertTrue(hasUserSdk(TEST_USER));
        }
    }

    @Test
    public void testDropUser() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE USER " + TEST_USER + " PASSWORD \"123456\"");
            assertTrue(hasUserSdk(TEST_USER));

            stmt.executeUpdate("DROP USER " + TEST_USER);

            // Verify
            assertFalse("User should not exist (SDK)", hasUserSdk(TEST_USER));
        }
    }

    @Test
    public void testCreateRole() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE ROLE " + TEST_ROLE);

            // Verify
            assertTrue("Role should exist (SDK)", hasRoleSdk(TEST_ROLE));
        }
    }

    @Test
    public void testDropRole() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE ROLE " + TEST_ROLE);
            assertTrue(hasRoleSdk(TEST_ROLE));

            stmt.executeUpdate("DROP ROLE " + TEST_ROLE);

            // Verify
            assertFalse("Role should not exist (SDK)", hasRoleSdk(TEST_ROLE));
        }
    }

    @Test
    public void testGrantRole() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE USER " + TEST_USER + " PASSWORD \"123456\"");
            stmt.executeUpdate("CREATE ROLE " + TEST_ROLE);

            assertTrue("Pre-condition: User exists", hasUserSdk(TEST_USER));
            assertTrue("Pre-condition: Role exists", hasRoleSdk(TEST_ROLE));
            assertFalse("Pre-condition: User has no role", userHasRoleSdk(TEST_USER, TEST_ROLE));

            // Execute Grant
            stmt.executeUpdate("GRANT ROLE " + TEST_ROLE + " TO " + TEST_USER);

            // Verify
            assertTrue("User should have role (SDK)", userHasRoleSdk(TEST_USER, TEST_ROLE));
        }
    }

    @Test
    public void testGrantPrivilege() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE ROLE " + TEST_ROLE);

            // create collection for test
            if (!hasCollection(TEST_COLLECTION)) {
                createCollection(TEST_COLLECTION);
            }

            assertTrue("Pre-condition: Role exists", hasRoleSdk(TEST_ROLE));
            assertFalse("Pre-condition: Role has no privilege", roleHasPrivilegeSdk(TEST_ROLE, "Collection", TEST_COLLECTION, "Query"));

            // Execute Grant Privilege
            // GRANT Query ON Collection dbv_table_col TO ROLE test_dbv_role
            stmt.executeUpdate("GRANT Query ON Collection " + TEST_COLLECTION + " TO ROLE " + TEST_ROLE);

            // Verify
            assertTrue("Role should have privilege (SDK)", roleHasPrivilegeSdk(TEST_ROLE, "Collection", TEST_COLLECTION, "Query"));
        } finally {
            try {
                dropCollection(TEST_COLLECTION);
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void testRevokePrivilege() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE ROLE " + TEST_ROLE);

            // create collection for test
            if (!hasCollection(TEST_COLLECTION)) {
                createCollection(TEST_COLLECTION);
            }

            // Setup grant
            stmt.executeUpdate("GRANT Query ON Collection " + TEST_COLLECTION + " TO ROLE " + TEST_ROLE);
            assertTrue("Pre-condition: Role has privilege", roleHasPrivilegeSdk(TEST_ROLE, "Collection", TEST_COLLECTION, "Query"));

            // Execute Revoke
            stmt.executeUpdate("REVOKE Query ON Collection " + TEST_COLLECTION + " FROM ROLE " + TEST_ROLE);

            // Verify
            assertFalse("Role should not have privilege (SDK)", roleHasPrivilegeSdk(TEST_ROLE, "Collection", TEST_COLLECTION, "Query"));
        } finally {
            try {
                dropCollection(TEST_COLLECTION);
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void testGrantGlobalPrivilege() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE ROLE " + TEST_ROLE);

            // Grant global privilege (usually objectName='*')
            // GRANT CreateCollection ON Global * TO ROLE test_dbv_role
            stmt.executeUpdate("GRANT CreateCollection ON Global * TO ROLE " + TEST_ROLE);

            // Verify
            assertTrue("Role should have global privilege (SDK)", roleHasPrivilegeSdk(TEST_ROLE, "Global", "*", "CreateCollection"));
        }
    }
}