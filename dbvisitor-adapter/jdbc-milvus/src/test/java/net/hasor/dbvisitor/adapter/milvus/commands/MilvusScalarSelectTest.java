/*
 * Copyright 2015-2022 the original author or authors.
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 */
package net.hasor.dbvisitor.adapter.milvus.commands;
import java.sql.*;
import java.util.Properties;
import io.milvus.v2.client.MilvusClientV2;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusScalarSelectTest {
    private int     probes;
    private boolean unavailable;

    @Before
    public void install() {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if ("getServerVersion".equals(method.getName())) {
                probes++;
                if (unavailable) {
                    throw new SQLException("server unavailable");
                }
                return "2.6.2";
            }
            if (!"close".equals(method.getName())) {
                fail("Constant SELECT must not access a collection: " + method.getName());
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
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530", properties);
    }

    @Test
    public void dataGripKeepAliveReturnsOneRowWithoutServerCalls() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            int initial = probes;
            for (int i = 0; i < 2; i++) {
                try (ResultSet result = statement.executeQuery("SELECT 'keep alive'")) {
                    assertEquals(1, result.getMetaData().getColumnCount());
                    assertEquals(Types.VARCHAR, result.getMetaData().getColumnType(1));
                    assertTrue(result.next());
                    assertEquals("keep alive", result.getString(1));
                    assertFalse(result.next());
                }
            }
            assertEquals(initial, probes);
        }
    }

    @Test
    public void scalarTypesAndEscaping() throws Exception {
        Object[][] cases = { { "1", 1L, Types.BIGINT }, { "-42", -42L, Types.BIGINT }, { "+1.25", 1.25D, Types.DOUBLE }, { "1e2", 100D, Types.DOUBLE }, { "TRUE", true, Types.BOOLEAN }, { "false", false, Types.BOOLEAN }, { "NULL", null, Types.NULL }, { "'it''s alive'", "it's alive", Types.VARCHAR } };
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            int initial = probes;
            for (Object[] entry : cases) {
                try (ResultSet result = statement.executeQuery("  select " + entry[0] + ";")) {
                    assertEquals(entry[2], result.getMetaData().getColumnType(1));
                    assertTrue(result.next());
                    assertEquals(entry[1], result.getObject(1));
                    assertEquals(entry[1] == null, result.wasNull());
                    assertFalse(result.next());
                }
            }
            assertEquals(initial, probes);
        }
    }

    @Test
    public void boundNullAndUnsupportedValue() throws Exception {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("SELECT ?")) {
            int initial = probes;
            statement.setNull(1, Types.VARCHAR);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                assertNull(result.getObject(1));
                assertTrue(result.wasNull());
                assertFalse(result.next());
            }
            statement.setBytes(1, new byte[] { 1, 2 });
            assertThrows(SQLException.class, statement::executeQuery);
            assertEquals(initial, probes);
        }
    }

    @Test
    public void preparedAndMultipleResultsPreserveBindingOrder() throws Exception {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("/*+ probe=? */ SELECT ?; SELECT ?")) {
            for (int i = 0; i < 2; i++) {
                statement.setString(1, "ignored hint");
                statement.setString(2, "alive ' " + i);
                statement.setInt(3, i);
                assertTrue(statement.execute());
                try (ResultSet result = statement.getResultSet()) {
                    assertTrue(result.next());
                    assertEquals("alive ' " + i, result.getString(1));
                    assertFalse(result.next());
                }
                assertTrue(statement.getMoreResults());
                try (ResultSet result = statement.getResultSet()) {
                    assertTrue(result.next());
                    assertEquals(i, result.getInt(1));
                    assertFalse(result.next());
                }
                assertFalse(statement.getMoreResults());
                assertEquals(-1, statement.getUpdateCount());
            }
        }
    }

    @Test
    public void pingProbesServerAndPropagatesFailureWhileConstantsRemainLocal() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            int initial = probes;
            for (int i = 0; i < 2; i++) {
                try (ResultSet result = statement.executeQuery("PING")) {
                    assertTrue(result.next());
                    assertEquals("PONG", result.getString("PING"));
                    assertFalse(result.next());
                }
            }
            assertEquals(initial + 2, probes);
            unavailable = true;
            SQLException error = assertThrows(SQLException.class, () -> statement.executeQuery("PING"));
            assertTrue(error.getMessage(), error.getMessage().contains("server unavailable"));
            int afterFailure = probes;
            try (ResultSet result = statement.executeQuery("SELECT 'keep alive'")) {
                assertTrue(result.next());
                assertEquals("keep alive", result.getString(1));
            }
            assertEquals(afterFailure, probes);
        }
    }

    @Test
    public void rejectUnsupportedExpressionsAndMissingCollection() throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            for (String sql : new String[] { "SELECT missing_field", "SELECT 1 + 2", "SELECT 1, 2", "SELECT *", "SELECT [1,2]" }) {
                assertThrows(sql, SQLException.class, () -> statement.executeQuery(sql));
            }
        }
    }
}
