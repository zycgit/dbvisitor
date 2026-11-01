/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Elastic6GenericTest {
    private static final String ES_URL = "jdbc:dbvisitor:elastic://127.0.0.1:2920?indexRefresh=true";

    @BeforeEach
    public void before() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /test_generic");
            } catch (Exception e) {
                Elastic6Cleanup.requireMissingIndex(e);
            }
        }
    }

    @AfterEach
    public void after() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            try {
                s.execute("DELETE /test_generic");
            } catch (Exception e) {
                Elastic6Cleanup.requireMissingIndex(e);
            }
        }
    }

    @Test
    public void testGenericGet() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            boolean result = s.execute("GET /");
            Assertions.assertTrue(result);
            try (ResultSet rs = s.getResultSet()) {
                Assertions.assertTrue(rs.next());
                Assertions.assertFalse(rs.getString("cluster_name").isEmpty());
                JsonNode version = new ObjectMapper().readTree(rs.getString("version"));
                Assertions.assertTrue(version.path("number").asText().startsWith("6."));
                Assertions.assertFalse(rs.next());
            }
        }
    }

    @Test
    public void testGenericPost() throws Exception {
        try (Connection c = DriverManager.getConnection(ES_URL); Statement s = c.createStatement()) {
            // Create index/doc using GENERIC
            s.execute("POST /test_generic/_doc/1 { \"name\": \"generic_test\" }");

            // Verify with standard GET
            try (ResultSet rs = s.executeQuery("GET /test_generic/_doc/1")) {
                Assertions.assertTrue(rs.next());
                String source = rs.getString("_source");
                Assertions.assertTrue(source.contains("generic_test"));
            }
        }
    }

    public static void main(String[] args) {
        net.hasor.dbvisitor.test.realdb.RealDbTestRunner.run(Elastic6GenericTest.class);
    }
}
