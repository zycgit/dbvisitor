/*
 * Copyright 2015-2022 the original author or authors.
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 */
package net.hasor.dbvisitor.test.realdb.milvus;
import java.sql.*;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import org.junit.Test;
import static org.junit.Assert.*;

/** SQL-client validation must work without creating or loading a collection. */
public class MilvusKeepAliveTest {
    @Test
    public void dataGripAndPoolValidationQueries() throws Exception {
        OneApiDataSourceManager.assumeCurrentDataSource("milvus");
        try (Connection connection = OneApiDataSourceManager.getConnection("milvus"); Statement statement = connection.createStatement()) {
            for (int i = 0; i < 2; i++) {
                try (ResultSet result = statement.executeQuery("SELECT 'keep alive'")) {
                    assertEquals(1, result.getMetaData().getColumnCount());
                    assertEquals(Types.VARCHAR, result.getMetaData().getColumnType(1));
                    assertTrue(result.next());
                    assertEquals("keep alive", result.getString(1));
                    assertFalse(result.next());
                }
                try (ResultSet result = statement.executeQuery("SELECT 1")) {
                    assertTrue(result.next());
                    assertEquals(1L, result.getLong(1));
                    assertFalse(result.next());
                }
                try (ResultSet result = statement.executeQuery("PING")) {
                    assertTrue(result.next());
                    assertEquals("PONG", result.getString("PING"));
                    assertFalse(result.next());
                }
            }
            try (PreparedStatement prepared = connection.prepareStatement("SELECT ?")) {
                prepared.setString(1, "alive");
                try (ResultSet result = prepared.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals("alive", result.getString(1));
                    assertFalse(result.next());
                }
            }
        }
    }
}
