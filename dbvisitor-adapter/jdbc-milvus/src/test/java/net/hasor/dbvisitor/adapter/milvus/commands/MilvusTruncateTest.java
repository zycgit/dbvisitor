/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.AlterCollectionPropertiesReq;
import io.milvus.v2.service.collection.request.TruncateCollectionReq;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusTruncateTest {
    private final List<String> methods  = new ArrayList<>();
    private final List<Object> requests = new ArrayList<>();
    private       Connection   connection;
    private       boolean      failTruncate;

    @Before
    public void connect() throws SQLException {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if (method.getName().equals("getServerVersion")) {
                return "v2.6.2";
            }
            this.methods.add(method.getName());
            if (args.length > 0) {
                this.requests.add(args[0]);
            }
            if (this.failTruncate && method.getName().equals("truncateCollection")) {
                throw new IllegalStateException("native truncate unavailable");
            }
            return null;
        });
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        properties.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        this.connection = new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530/db1", properties);
    }

    @After
    public void close() throws SQLException {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } finally {
            MilvusCommandInterceptor.resetInterceptor();
        }
    }

    @Test
    public void truncateUsesOneNativeRequestWithoutSelectingOrRecreatingRows() throws SQLException {
        try (Statement statement = this.connection.createStatement()) {
            statement.setFetchSize(1);
            statement.setMaxRows(1);
            assertEquals(0, statement.executeUpdate("TRUNCATE TABLE books"));
            assertNull(statement.getResultSet());
            assertEquals(0, statement.getUpdateCount());
            assertFalse(statement.getMoreResults());
            assertEquals(-1, statement.getUpdateCount());
        }
        TruncateCollectionReq request = (TruncateCollectionReq) this.requests.get(0);
        assertEquals("db1", request.getDatabaseName());
        assertEquals("books", request.getCollectionName());
        assertEquals(Collections.singletonList("truncateCollection"), this.methods);
    }

    @Test
    public void explicitDatabasePreservesCatalogAndFollowingParameterIndexes() throws SQLException {
        String payload = "value'); TRUNCATE TABLE other; --";
        try (PreparedStatement statement = this.connection.prepareStatement("""
                /*+ trace=? */ TRUNCATE TABLE truncate IN DATABASE other_db;
                ALTER TABLE truncate SET PROPERTIES (description=?)
                """)) {
            statement.setString(1, "ignored hint value");
            statement.setString(2, payload);
            assertFalse(statement.execute());
            assertEquals(0, statement.getUpdateCount());
            assertFalse(statement.getMoreResults());
            assertEquals(0, statement.getUpdateCount());
            assertFalse(statement.getMoreResults());
            assertEquals(-1, statement.getUpdateCount());
        }
        TruncateCollectionReq request = (TruncateCollectionReq) this.requests.get(0);
        assertEquals("other_db", request.getDatabaseName());
        assertEquals("truncate", request.getCollectionName());
        AlterCollectionPropertiesReq next = (AlterCollectionPropertiesReq) this.requests.get(1);
        assertEquals("db1", next.getDatabaseName());
        assertEquals("truncate", next.getCollectionName());
        assertEquals(Collections.singletonMap("description", payload), next.getProperties());
        assertEquals("db1", this.connection.getCatalog());
        assertEquals(Arrays.asList("truncateCollection", "alterCollectionProperties"), this.methods);
    }

    @Test
    public void malformedTargetsAndPartialDeletionClausesFailBeforeSdk() throws SQLException {
        String[] invalid = { "TRUNCATE TABLE ?", "TRUNCATE TABLE books IN DATABASE ?", "TRUNCATE TABLE ``", "TRUNCATE TABLE books IN DATABASE ``", "TRUNCATE TABLE *", "TRUNCATE TABLE books IN DATABASE *", "TRUNCATE TABLE books WHERE id=1", "TRUNCATE TABLE books LIMIT 1", "TRUNCATE TABLE books,other", "TRUNCATE TABLE books WITH (limit=1)" };
        for (String sql : invalid) {
            assertThrows(sql, SQLException.class, () -> {
                try (Statement statement = this.connection.createStatement()) {
                    statement.execute(sql);
                }
            });
        }
        assertTrue(this.methods.isEmpty());
    }

    @Test
    public void nativeFailureStopsFollowingSqlWithoutFallbackAndStatementCanBeReused() throws SQLException {
        this.failTruncate = true;
        try (Statement statement = this.connection.createStatement()) {
            SQLException failure = assertThrows(SQLException.class, () -> statement.execute("TRUNCATE TABLE books; TRUNCATE TABLE other"));
            assertTrue(failure.getMessage().contains("native truncate unavailable"));
            assertEquals(Collections.singletonList("truncateCollection"), this.methods);
            this.failTruncate = false;
            assertEquals(0, statement.executeUpdate("TRUNCATE TABLE recovered"));
        }
        assertEquals(Arrays.asList("truncateCollection", "truncateCollection"), this.methods);
        assertEquals("recovered", ((TruncateCollectionReq) this.requests.get(1)).getCollectionName());
    }
}
