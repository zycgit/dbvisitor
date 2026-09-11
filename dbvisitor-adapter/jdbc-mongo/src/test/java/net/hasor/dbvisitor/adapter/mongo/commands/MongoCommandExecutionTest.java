/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.mongo.commands;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MongoCommandExecutionTest extends AbstractJdbcTest {
    private MongoCollection<Document> collection;

    @Before
    public void install() {
        collection = PowerMockito.mock(MongoCollection.class);
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("runCommand".equals(method.getName())) {
                return new Document("version", "6.0.0");
            }
            if ("getCollection".equals(method.getName())) {
                assertEquals("books", args[0]);
                return collection;
            }
            return null;
        });
    }

    @After
    public void cleanup() {
        MongoCommandInterceptor.resetInterceptor();
    }

    @Test
    public void stringBindingsStayBsonValuesRatherThanOperators() throws Exception {
        List<Document> filters = new ArrayList<>();
        when(collection.countDocuments(any(Bson.class))).thenAnswer(invocation -> {
            filters.add(invocation.getArgument(0));
            return 1L;
        });
        String value = "'}, $where: 'return true', x: '\\\n中文";
        try (Connection connection = redisConnection("testdb"); PreparedStatement statement = connection.prepareStatement("db.books.count({name:?,active:?,age:?})")) {
            for (int binding = 0; binding < 3; binding++) {
                if (binding == 0) {
                    statement.setString(1, value);
                } else if (binding == 1) {
                    statement.setObject(1, value);
                } else {
                    statement.setObject(1, value, Types.VARCHAR);
                }
                statement.setBoolean(2, true);
                statement.setInt(3, 12);
                try (ResultSet result = statement.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals(1L, result.getLong("COUNT"));
                    assertFalse(result.next());
                }
            }
        }
        assertEquals(3, filters.size());
        for (Document filter : filters) {
            assertEquals(value, filter.getString("name"));
            assertEquals(Boolean.TRUE, filter.getBoolean("active"));
            assertEquals(Integer.valueOf(12), filter.getInteger("age"));
            assertEquals(3, filter.size());
            assertFalse(filter.containsKey("$where"));
        }
    }

    @Test
    public void updateAndDeleteBindIndependentParametersAndUseSdkCounts() throws Exception {
        when(collection.updateOne(any(Bson.class), any(Bson.class), any(UpdateOptions.class))).thenReturn(UpdateResult.acknowledged(9, 2L, null));
        when(collection.deleteMany(any(Bson.class), any(DeleteOptions.class))).thenReturn(DeleteResult.acknowledged(3));
        try (Connection connection = redisConnection("testdb"); PreparedStatement statement = connection.prepareStatement("db.books.updateOne({name:?},{$set:{active:?}},{upsert:true}); db.books.deleteMany({age:{$lt:?}})")) {
            statement.setString(1, "Java");
            statement.setBoolean(2, true);
            statement.setInt(3, 18);
            assertFalse(statement.execute());
            assertEquals(2, statement.getUpdateCount());
            assertFalse(statement.getMoreResults());
            assertEquals(3, statement.getUpdateCount());
            assertFalse(statement.getMoreResults());
            assertEquals(-1, statement.getUpdateCount());
        }
        ArgumentCaptor<Bson> filter = ArgumentCaptor.forClass(Bson.class);
        ArgumentCaptor<Bson> update = ArgumentCaptor.forClass(Bson.class);
        ArgumentCaptor<UpdateOptions> options = ArgumentCaptor.forClass(UpdateOptions.class);
        verify(collection).updateOne(filter.capture(), update.capture(), options.capture());
        assertEquals(new Document("name", "Java"), filter.getValue());
        assertEquals(new Document("$set", new Document("active", true)), update.getValue());
        assertTrue(options.getValue().isUpsert());
        verify(collection).deleteMany(eq(new Document("age", new Document("$lt", 18))), any(DeleteOptions.class));
    }

    @Test
    public void findPassesProjectionSortAndPaginationAndHonorsJdbcMaxRows() throws Exception {
        FindIterable<Document> iterable = PowerMockito.mock(FindIterable.class);
        MongoCursor<Document> cursor = PowerMockito.mock(MongoCursor.class);
        Iterator<Document> rows = Arrays.asList(new Document("_id", "1").append("name", "Java"), new Document("_id", "2").append("name", "SQL")).iterator();
        when(collection.find(any(Bson.class))).thenReturn(iterable);
        when(iterable.iterator()).thenReturn(cursor);
        when(cursor.hasNext()).thenAnswer(invocation -> rows.hasNext());
        when(cursor.next()).thenAnswer(invocation -> rows.next());
        try (Connection connection = redisConnection("testdb"); PreparedStatement statement = connection.prepareStatement("db.books.find({active:?},{name:1}).sort({name:1}).skip(?).limit(?)")) {
            statement.setBoolean(1, true);
            statement.setInt(2, 4);
            statement.setInt(3, 2);
            statement.setMaxRows(1);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals("Java", Document.parse(result.getString("_JSON")).getString("name"));
                assertFalse(result.next());
            }
        }
        verify(collection).find(eq(new Document("active", true)));
        verify(iterable).projection(eq(new Document("name", 1L)));
        verify(iterable).sort(eq(new Document("name", 1L)));
        verify(iterable).skip(4);
        verify(iterable).limit(2);
        verify(cursor, times(1)).next();
    }

    @Test
    public void missingBindingFailsBeforeCollectionAccess() throws Exception {
        try (Connection connection = redisConnection("testdb"); PreparedStatement statement = connection.prepareStatement("db.books.count({name:?})")) {
            SQLException error = assertThrows(SQLException.class, statement::executeQuery);
            assertTrue(error.getMessage().contains("param size not match"));
        }
        verifyZeroInteractions(collection);
    }

    @Test
    public void sdkFailureStopsFollowingCommandsAndStatementRemainsReusable() throws Exception {
        MongoException failure = new MongoException(123, "simulated server failure");
        when(collection.countDocuments(any(Bson.class))).thenThrow(failure).thenReturn(5L);
        try (Connection connection = redisConnection("testdb"); Statement statement = connection.createStatement()) {
            SQLException error = assertThrows(SQLException.class, () -> statement.execute("db.books.count({}); db.books.deleteMany({})"));
            assertTrue(error.getMessage().contains("simulated server failure"));
            verify(collection, never()).deleteMany(any(Bson.class), any(DeleteOptions.class));
            try (ResultSet result = statement.executeQuery("db.books.count({})")) {
                assertTrue(result.next());
                assertEquals(5L, result.getLong("COUNT"));
            }
        }
        verify(collection, times(2)).countDocuments(any(Bson.class));
    }
}
