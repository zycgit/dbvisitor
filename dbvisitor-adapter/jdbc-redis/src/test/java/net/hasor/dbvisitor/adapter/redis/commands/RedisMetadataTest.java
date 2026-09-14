/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.redis.commands;

import java.sql.*;
import net.hasor.dbvisitor.adapter.redis.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.redis.JedisConn;
import net.hasor.dbvisitor.adapter.redis.RedisCommandInterceptor;
import net.hasor.dbvisitor.driver.MetadataSupport;
import org.junit.After;
import org.junit.Test;
import redis.clients.jedis.commands.DatabaseCommands;
import redis.clients.jedis.exceptions.JedisDataException;
import static org.junit.Assert.*;

public class RedisMetadataTest extends AbstractJdbcTest {
    @Test
    public void metadataProviderBelongsToConnection() throws Exception {
        RedisCommandInterceptor.resetInterceptor();
        try (Connection first = redisConnection(); Connection second = redisConnection()) {
            JedisConn adapter = first.unwrap(JedisConn.class);
            assertTrue(first.isWrapperFor(MetadataSupport.class));
            assertSame(adapter, first.unwrap(MetadataSupport.class));
            assertNotSame(adapter, second.unwrap(MetadataSupport.class));
        }
    }

    @After
    public void resetInterceptor() {
        RedisCommandInterceptor.resetInterceptor();
    }

    @Test
    public void selectedDatabaseIsCatalogButKeysAreNotTables() throws Exception {
        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(DatabaseCommands.class, createInvocationHandler("select", (method, args) -> "OK"));
        try (Connection connection = redisConnection()) {
            assertCatalog(connection, "0");
            connection.setCatalog("3");
            assertCatalog(connection, "3");
            connection.setSchema("4");
            assertCatalog(connection, "4");
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("SELECT 5");
            }
            assertCatalog(connection, "5");
            DatabaseMetaData metadata = connection.getMetaData();
            try (ResultSet tables = metadata.getTables(null, null, "%", null); ResultSet columns = metadata.getColumns(null, null, "%", "%"); ResultSet types = metadata.getTableTypes(); ResultSet schemas = metadata.getSchemas()) {
                assertEquals(10, tables.getMetaData().getColumnCount());
                assertEquals(24, columns.getMetaData().getColumnCount());
                assertEquals(Types.INTEGER, columns.getMetaData().getColumnType(5));
                assertFalse(tables.next());
                assertFalse(columns.next());
                assertFalse(types.next());
                assertFalse(schemas.next());
            }
            connection.close();
            assertThrows(SQLException.class, metadata::getCatalogs);
            assertThrows(SQLException.class, metadata::getTableTypes);
        }
    }

    @Test
    public void failedSelectionDoesNotChangeCatalog() throws Exception {
        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(DatabaseCommands.class, createInvocationHandler("select", (method, args) -> "ERR"));
        try (Connection connection = redisConnection()) {
            assertThrows(SQLException.class, () -> connection.setCatalog("3"));
            assertEquals("0", connection.getCatalog());
            assertCatalog(connection, "0");
        }
    }

    @Test
    public void selectionExceptionPreservesCatalog() throws Exception {
        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(DatabaseCommands.class, createInvocationHandler("select", (method, args) -> {
            throw new JedisDataException("ERR DB index is out of range");
        }));
        try (Connection connection = redisConnection()) {
            assertThrows(JedisDataException.class, () -> connection.setCatalog("99"));
            assertCatalog(connection, "0");
        }
    }

    private void assertCatalog(Connection connection, String expected) throws SQLException {
        try (ResultSet catalogs = connection.getMetaData().getCatalogs()) {
            assertEquals(1, catalogs.getMetaData().getColumnCount());
            assertEquals("TABLE_CAT", catalogs.getMetaData().getColumnLabel(1));
            assertTrue(catalogs.next());
            assertEquals(expected, catalogs.getString("TABLE_CAT"));
            assertFalse(catalogs.next());
        }
    }
}
