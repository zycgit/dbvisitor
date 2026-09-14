/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.mongo.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import net.hasor.dbvisitor.adapter.mongo.MongoConn;
import net.hasor.dbvisitor.driver.MetadataSupport;
import org.bson.Document;
import org.junit.Test;
import static org.junit.Assert.*;

public class CollectionMetadataTest extends AbstractJdbcTest {
    @Test
    public void metadataProviderBelongsToConnection() throws Exception {
        installCollections();
        try (Connection first = redisConnection("test"); Connection second = redisConnection("test")) {
            MongoConn adapter = first.unwrap(MongoConn.class);
            assertTrue(first.isWrapperFor(MetadataSupport.class));
            assertSame(adapter, first.unwrap(MetadataSupport.class));
            assertNotSame(adapter, second.unwrap(MetadataSupport.class));
        }
    }

    @Test
    public void catalogsAndTableTypesUseJdbcLayoutAndOrder() throws Exception {
        installCollections();
        MongoCommandInterceptor.addInterceptor(MongoClient.class, (client, method, args) -> {
            if ("listDatabaseNames".equals(method.getName())) {
                return mockMongoIterable(Arrays.asList("z_db", "a_db"));
            }
            return method.invoke(client, args);
        });
        try (Connection connection = redisConnection("test"); ResultSet catalogs = connection.getMetaData().getCatalogs(); ResultSet types = connection.getMetaData().getTableTypes()) {
            assertTrue(catalogs.next());
            assertEquals("a_db", catalogs.getString("TABLE_CAT"));
            assertTrue(catalogs.next());
            assertEquals("z_db", catalogs.getString("TABLE_CAT"));
            assertFalse(catalogs.next());
            assertTrue(types.next());
            assertEquals("TABLE", types.getString("TABLE_TYPE"));
            assertTrue(types.next());
            assertEquals("VIEW", types.getString("TABLE_TYPE"));
            assertFalse(types.next());
        }
    }

    @Test
    public void tablesPreserveCatalogAndApplyEscapedNamePattern() throws Exception {
        installCollections();
        try (Connection connection = redisConnection("test"); ResultSet tables = connection.getMetaData().getTables("test", null, "user\\_%", new String[] { "TABLE" })) {
            assertEquals(10, tables.getMetaData().getColumnCount());
            assertTrue(tables.next());
            assertEquals("test", tables.getString("TABLE_CAT"));
            assertNull(tables.getString("TABLE_SCHEM"));
            assertEquals("user_info", tables.getString("TABLE_NAME"));
            assertEquals("TABLE", tables.getString("TABLE_TYPE"));
            assertFalse(tables.next());
        }
    }

    @Test
    public void nullCatalogUsesDatabaseListingAndViewsRemainViews() throws Exception {
        installCollections();
        MongoCommandInterceptor.addInterceptor(MongoClient.class, (client, method, args) -> {
            if ("listDatabaseNames".equals(method.getName())) {
                return mockMongoIterable(Collections.singletonList("test"));
            }
            return method.invoke(client, args);
        });
        try (Connection connection = redisConnection("test"); ResultSet tables = connection.getMetaData().getTables(null, "", "%", new String[] { "VIEW" })) {
            assertTrue(tables.next());
            assertEquals("user_view", tables.getString("TABLE_NAME"));
            assertEquals("VIEW", tables.getString("TABLE_TYPE"));
            assertFalse(tables.next());
        }
    }

    @Test
    public void nonexistentSchemaAndEmptyTypesReturnNoTables() throws Exception {
        installCollections();
        try (Connection connection = redisConnection("test")) {
            try (ResultSet tables = connection.getMetaData().getTables("test", "not_a_schema", "%", null)) {
                assertFalse(tables.next());
            }
            try (ResultSet tables = connection.getMetaData().getTables("test", null, "%", new String[0])) {
                assertFalse(tables.next());
            }
        }
    }

    private void installCollections() {
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("listCollections", (method, args) -> mockListCollectionsIterable(Arrays.asList(new Document("name", "user_info").append("type", "collection"), new Document("name", "userXinfo").append("type", "collection"), new Document("name", "user_view").append("type", "view")))));
    }
}
