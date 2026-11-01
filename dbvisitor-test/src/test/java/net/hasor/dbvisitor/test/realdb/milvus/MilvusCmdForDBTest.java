/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MilvusCmdForDBTest extends AbstractMilvusCmdForTest {
    @BeforeEach
    public void setUp() {
        if (hasDatabase(TEST_DATABASE)) {
            dropDatabase(TEST_DATABASE);
        }
    }

    @AfterEach
    public void tearDown() {
        if (!milvusSelected) {
            return;
        }
        if (hasDatabase(TEST_DATABASE)) {
            dropDatabase(TEST_DATABASE);
        }
    }

    @Test
    public void testCreateDatabase() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE " + TEST_DATABASE);
            assertTrue(hasDatabase(TEST_DATABASE), "Database should exist after CREATE");
        }
    }

    @Test
    public void testAlterDatabase() throws Exception {
        createDatabase(TEST_DATABASE);
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER DATABASE " + TEST_DATABASE + " SET PROPERTIES (\"replica.number\" = 1)");
            assertTrue(hasDatabase(TEST_DATABASE), "Database should still exist after ALTER");
        }
    }

    @Test
    public void testDropDatabase() throws Exception {
        createDatabase(TEST_DATABASE);
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP DATABASE " + TEST_DATABASE);
            assertFalse(hasDatabase(TEST_DATABASE), "Database should not exist after DROP");
        }
    }

    @Test
    public void testCreateDatabaseIfNotExists() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + TEST_DATABASE);
            assertTrue(hasDatabase(TEST_DATABASE));

            // Should not fail if exists
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + TEST_DATABASE);
            assertTrue(hasDatabase(TEST_DATABASE));
        }
    }

    @Test
    public void testDropDatabaseIfExists() throws Exception {
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            // Should not fail if not exists
            stmt.executeUpdate("DROP DATABASE IF EXISTS " + TEST_DATABASE);

            createDatabase(TEST_DATABASE);
            stmt.executeUpdate("DROP DATABASE IF EXISTS " + TEST_DATABASE);
            assertFalse(hasDatabase(TEST_DATABASE));
        }
    }

    @Test
    public void testShowDatabases() throws Exception {
        createDatabase(TEST_DATABASE);
        try (Connection conn = DriverManager.getConnection(MILVUS_URL); Statement stmt = conn.createStatement()) {
            boolean found = false;
            try (ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {
                ResultSetMetaData metaData = rs.getMetaData();
                Set<String> columns = new HashSet<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    columns.add(metaData.getColumnLabel(i));
                }
                assertTrue(columns.contains("DATABASE"));

                while (rs.next()) {
                    String dbName = rs.getString("DATABASE");
                    if (TEST_DATABASE.equals(dbName)) {
                        found = true;
                        break;
                    }
                }
            }

            assertTrue(found, "Should find the test database in SHOW DATABASES");
        }
    }

    public static void main(String[] args) {
        net.hasor.dbvisitor.test.realdb.RealDbTestRunner.run(MilvusCmdForDBTest.class);
    }
}
