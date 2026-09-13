/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.*;
import java.util.*;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.ListCollectionsReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.collection.response.ListCollectionsResp;
import net.hasor.dbvisitor.adapter.milvus.*;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusMetadataTest {
    private CreateCollectionReq collection;

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    @Test
    public void shouldExposeActualSchemaAndRespectFilters() throws SQLException {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            switch (method.getName()) {
                case "getServerVersion":
                    return "2.6.2";
                case "createCollection":
                    collection = (CreateCollectionReq) args[0];
                    return null;
                case "listCollectionsV2":
                    assertEquals("db1", ((ListCollectionsReq) args[0]).getDatabaseName());
                    return ListCollectionsResp.builder().collectionNames(List.of("sample_table")).build();
                case "describeCollection":
                    return DescribeCollectionResp.builder().collectionName("sample_table")
                            .collectionSchema(collection.getCollectionSchema()).description("Stored collection").build();
                default:
                    return null;
            }
        });
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        properties.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        try (Connection connection = new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530/db1", properties);
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE sample_table (id INT64 PRIMARY KEY AUTO_ID, name VARCHAR(128) NULL, v FLOAT_VECTOR(2))");
            DatabaseMetaData metadata = connection.getMetaData();
            try (ResultSet rows = metadata.getTables("db1", null, "sample\\_table", new String[] { "TABLE" })) {
                assertTrue(rows.next());
                assertEquals("sample_table", rows.getString("TABLE_NAME"));
                assertEquals("db1", rows.getString("TABLE_CAT"));
                assertNull(rows.getString("TABLE_SCHEM"));
                assertEquals("Stored collection", rows.getString("REMARKS"));
                assertFalse(rows.next());
            }
            try (ResultSet rows = metadata.getColumns(null, "", "sample%", "%")) {
                assertTrue(rows.next());
                assertEquals("id", rows.getString("COLUMN_NAME"));
                assertEquals(Types.BIGINT, rows.getInt("DATA_TYPE"));
                assertEquals("YES", rows.getString("IS_AUTOINCREMENT"));
                assertEquals(1, rows.getInt("ORDINAL_POSITION"));
                assertTrue(rows.next());
                assertEquals("name", rows.getString("COLUMN_NAME"));
                assertEquals(Types.VARCHAR, rows.getInt("DATA_TYPE"));
                assertEquals("YES", rows.getString("IS_NULLABLE"));
                assertEquals(128, rows.getInt("CHAR_OCTET_LENGTH"));
                assertTrue(rows.next());
                assertEquals(Types.ARRAY, rows.getInt("DATA_TYPE"));
                assertFalse(rows.next());
            }
            try (ResultSet rows = metadata.getTables("other", null, "%", null)) {
                assertFalse(rows.next());
            }
            try (ResultSet rows = metadata.getTables(null, "other", "%", null)) {
                assertFalse(rows.next());
            }
            try (ResultSet rows = metadata.getTables(null, null, "%", new String[] { "VIEW" })) {
                assertFalse(rows.next());
            }
            try (ResultSet rows = metadata.getColumns(null, null, "sample%", "missing")) {
                assertFalse(rows.next());
            }
        }
    }
}
