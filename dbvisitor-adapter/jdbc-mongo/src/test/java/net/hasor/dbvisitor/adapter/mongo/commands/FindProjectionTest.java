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
import java.sql.Statement;
import java.sql.Types;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Arrays;
import java.util.Iterator;
import com.mongodb.client.FindIterable;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.Binary;
import org.bson.conversions.Bson;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;

public class FindProjectionTest extends AbstractJdbcTest {
    @Test
    public void maxRowsClosesTheServerCursor() throws Exception {
        MongoCursor<Document> cursor = mockDocuments(new Document("name", "Alice"), new Document("name", "Bob"));
        try (Connection connection = redisConnection("test"); Statement statement = connection.createStatement()) {
            statement.setMaxRows(1);
            try (ResultSet result = statement.executeQuery("db.people.find({}, {_id: 0, name: 1})")) {
                assertTrue(result.next());
                assertEquals("Alice", result.getString(1));
                assertFalse(result.next());
            }
        }
        verify(cursor).close();
    }

    @Test
    public void aggregateProjectionExposesAliasesAfterSort() throws Exception {
        mockDocuments(new Document("age", 18).append("user_name", "Alice"));
        try (Connection connection = redisConnection("test"); Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("db.people.aggregate([{$project: {_id: 0, user_name: '$name', age: 1}}, {$sort: {age: 1}}])")) {
            assertEquals(2, result.getMetaData().getColumnCount());
            assertEquals("user_name", result.getMetaData().getColumnLabel(1));
            assertTrue(result.next());
            assertEquals("Alice", result.getString(1));
            assertEquals(18, result.getInt(2));
        }
    }

    @Test
    public void aggregateWithoutProjectionRetainsRawDocument() throws Exception {
        mockDocuments(new Document("age", 18));
        try (Connection connection = redisConnection("test"); Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("db.people.aggregate([{$match: {age: 18}}])")) {
            assertEquals(2, result.getMetaData().getColumnCount());
            assertTrue(result.next());
            assertEquals(18, Document.parse(result.getString("_JSON")).getInteger("age").intValue());
        }
    }

    @Test
    public void bsonValuesRetainJdbcTypes() throws Exception {
        mockDocuments(new Document("age", 18).append("amount", new Decimal128(new BigDecimal("123.45")))
                .append("created", new Date(1700000000000L)).append("data", new Binary(new byte[] { 1, 2, 3 })));
        try (Connection connection = redisConnection("test"); Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("db.people.find({}, {_id: 0, age: 1, amount: 1, created: 1, data: 1})")) {
            assertEquals(Types.INTEGER, result.getMetaData().getColumnType(1));
            assertEquals(Types.DECIMAL, result.getMetaData().getColumnType(2));
            assertEquals(Types.TIMESTAMP, result.getMetaData().getColumnType(3));
            assertTrue(result.next());
            assertEquals(18, result.getObject(1));
            assertEquals(new BigDecimal("123.45"), result.getBigDecimal(2));
            assertEquals(1700000000000L, result.getTimestamp(3).getTime());
            assertTrue(result.getObject(3) instanceof Timestamp);
            assertArrayEquals(new byte[] { 1, 2, 3 }, result.getBytes(4));
        }
    }

    @Test
    public void includedFieldsPreserveProjectionOrder() throws Exception {
        mockDocuments(new Document("age", 18).append("name", "Alice"));
        try (Connection connection = redisConnection("test"); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("db.people.find({}, {_id: 0, name: 1, age: 1})")) {
            assertEquals(2, result.getMetaData().getColumnCount());
            assertEquals("name", result.getMetaData().getColumnLabel(1));
            assertEquals("age", result.getMetaData().getColumnLabel(2));
            assertTrue(result.next());
            assertEquals("Alice", result.getString(1));
            assertEquals(18, result.getInt(2));
        }
    }

    @Test
    public void emptyProjectionResultRetainsDeclaredColumns() throws Exception {
        mockDocuments();
        try (Connection connection = redisConnection("test"); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("db.people.findOne({}, {_id: 0, name: 1})")) {
            assertEquals(1, result.getMetaData().getColumnCount());
            assertEquals("name", result.getMetaData().getColumnLabel(1));
            assertFalse(result.next());
        }
    }

    @Test
    public void defaultMongoIdIsNotDiscarded() throws Exception {
        mockDocuments(new Document("_id", "key").append("name", "Alice"));
        try (Connection connection = redisConnection("test"); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("db.people.find({}, {name: 1})")) {
            assertEquals(2, result.getMetaData().getColumnCount());
            assertTrue(result.next());
            assertEquals("key", result.getString("_id"));
            assertEquals("Alice", result.getString("name"));
        }
    }

    @Test
    public void exclusionProjectionDiscoversRemainingFields() throws Exception {
        mockDocuments(new Document("_id", "key").append("age", 18));
        try (Connection connection = redisConnection("test"); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("db.people.find({}, {name: 0})")) {
            assertEquals(2, result.getMetaData().getColumnCount());
            assertTrue(result.next());
            assertEquals(18, result.getInt("age"));
        }
    }

    @Test
    public void unprojectedReadRetainsDocumentColumns() throws Exception {
        mockDocuments(new Document("name", "Alice"));
        try (Connection connection = redisConnection("test"); Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("db.people.find({})")) {
            assertEquals(3, result.getMetaData().getColumnCount());
            assertTrue(result.next());
            assertEquals("Alice", result.getString("name"));
        }
    }

    @SuppressWarnings("unchecked")
    private MongoCursor<Document> mockDocuments(Document... documents) {
        MongoCommandInterceptor.resetInterceptor();
        MongoCursor<Document> cursor = PowerMockito.mock(MongoCursor.class);
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, createInvocationHandler("getCollection", (name, arguments) -> {
            MongoCollection<Document> collection = PowerMockito.mock(MongoCollection.class);
            FindIterable<Document> iterable = PowerMockito.mock(FindIterable.class);
            AggregateIterable<Document> aggregate = PowerMockito.mock(AggregateIterable.class);
            Iterator<Document> values = Arrays.asList(documents).iterator();
            PowerMockito.when(collection.find(any(Bson.class))).thenReturn(iterable);
            PowerMockito.when(collection.aggregate(anyList())).thenReturn(aggregate);
            PowerMockito.when(aggregate.iterator()).thenReturn(cursor);
            PowerMockito.when(iterable.projection(any(Bson.class))).thenReturn(iterable);
            PowerMockito.when(iterable.limit(anyInt())).thenReturn(iterable);
            PowerMockito.when(iterable.iterator()).thenReturn(cursor);
            PowerMockito.when(cursor.hasNext()).thenAnswer(invocation -> values.hasNext());
            PowerMockito.when(cursor.next()).thenAnswer(invocation -> values.next());
            return collection;
        }));
        return cursor;
    }
}
