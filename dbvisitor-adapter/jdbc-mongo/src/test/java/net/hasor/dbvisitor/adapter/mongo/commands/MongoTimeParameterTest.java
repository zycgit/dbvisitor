/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.mongo.commands;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MongoTimeParameterTest extends AbstractJdbcTest {
    private MongoCollection<Document> collection;

    @Before
    public void install() {
        this.collection = PowerMockito.mock(MongoCollection.class);
        MongoCommandInterceptor.resetInterceptor();
        MongoCommandInterceptor.addInterceptor(MongoDatabase.class, (proxy, method, args) -> {
            if ("runCommand".equals(method.getName())) {
                return new Document("version", "6.0.0");
            }
            if ("getCollection".equals(method.getName())) {
                assertEquals("books", args[0]);
                return this.collection;
            }
            return null;
        });
    }

    @After
    public void cleanup() {
        MongoCommandInterceptor.resetInterceptor();
    }

    @Test
    public void inferredJavaTimeParametersUseJdbcValuesInNonUtcTimeZone() throws Exception {
        TimeZone previous = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
            assertTemporalBindings(0);
        } finally {
            TimeZone.setDefault(previous);
        }
    }

    @Test
    public void integerTypedJavaTimeParametersPreserveNullAndLegacyValues() throws Exception {
        assertTemporalBindings(1);
    }

    @Test
    public void jdbcTypedJavaTimeParametersPreserveNullAndLegacyValues() throws Exception {
        assertTemporalBindings(2);
    }

    private void assertTemporalBindings(int binding) throws Exception {
        List<Document> filters = new ArrayList<>();
        when(this.collection.countDocuments(any(Bson.class))).thenAnswer(invocation -> {
            filters.add(invocation.getArgument(0));
            return 1L;
        });
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123_000_000);
        LocalDate date = LocalDate.of(2024, 3, 15);
        LocalTime time = LocalTime.of(14, 30, 45);
        Instant instant = Instant.parse("2024-03-15T14:30:45.123Z");
        Timestamp legacy = Timestamp.valueOf(dateTime.plusDays(1));
        Object[] values = { dateTime, date, time, instant, null, legacy, "unchanged" };
        JDBCType[] types = { JDBCType.TIMESTAMP, JDBCType.DATE, JDBCType.TIME, JDBCType.TIMESTAMP, JDBCType.TIMESTAMP, JDBCType.TIMESTAMP, JDBCType.VARCHAR };
        try (Connection connection = redisConnection("testdb"); PreparedStatement statement = connection.prepareStatement("db.books.count({date_time: ?, date: ?, time: ?, instant: ?, missing: ?, legacy: ?, name: ?})")) {
            for (int index = 0; index < values.length; index++) {
                if (binding == 0) {
                    statement.setObject(index + 1, values[index]);
                } else if (binding == 1) {
                    statement.setObject(index + 1, values[index], types[index].getVendorTypeNumber());
                } else {
                    statement.setObject(index + 1, values[index], types[index]);
                }
            }
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals(1L, result.getLong("COUNT"));
                assertFalse(result.next());
            }
        }

        assertEquals(1, filters.size());
        Document filter = filters.get(0);
        assertEquals(7, filter.size());
        assertEquals(Timestamp.class, filter.get("date_time").getClass());
        assertEquals(Timestamp.valueOf(dateTime), filter.get("date_time"));
        assertEquals(java.sql.Date.class, filter.get("date").getClass());
        assertEquals(java.sql.Date.valueOf(date), filter.get("date"));
        assertEquals(Time.class, filter.get("time").getClass());
        assertEquals(Time.valueOf(time), filter.get("time"));
        assertEquals(Timestamp.from(instant), filter.get("instant"));
        assertTrue(filter.containsKey("missing"));
        assertNull(filter.get("missing"));
        assertSame(legacy, filter.get("legacy"));
        assertEquals("unchanged", filter.getString("name"));

        BsonDocument encoded = filter.toBsonDocument(Document.class, MongoClientSettings.getDefaultCodecRegistry());
        assertEquals(Timestamp.valueOf(dateTime).getTime(), encoded.getDateTime("date_time").getValue());
        assertEquals(java.sql.Date.valueOf(date).getTime(), encoded.getDateTime("date").getValue());
        assertEquals(Time.valueOf(time).getTime(), encoded.getDateTime("time").getValue());
        assertEquals(instant.toEpochMilli(), encoded.getDateTime("instant").getValue());
        assertTrue(encoded.isNull("missing"));
        assertEquals(legacy.getTime(), encoded.getDateTime("legacy").getValue());
    }

    @Test
    public void insertAndUpdateNormalizeIndependentJavaTimeParameters() throws Exception {
        when(this.collection.insertOne(any(Document.class), any(InsertOneOptions.class))).thenReturn(InsertOneResult.acknowledged(new BsonInt32(1)));
        when(this.collection.updateOne(any(Bson.class), any(Bson.class), any(UpdateOptions.class))).thenReturn(UpdateResult.acknowledged(1, 1L, null));
        LocalDateTime created = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123_000_000);
        LocalDateTime updated = created.plusHours(1);
        try (Connection connection = redisConnection("testdb"); PreparedStatement statement = connection.prepareStatement("db.books.insertOne({created: ?}); db.books.updateOne({created: ?}, {$set: {created: ?}})")) {
            statement.setObject(1, created);
            statement.setObject(2, created, Types.TIMESTAMP);
            statement.setObject(3, updated, JDBCType.TIMESTAMP);
            assertFalse(statement.execute());
            assertEquals(1, statement.getUpdateCount());
            assertFalse(statement.getMoreResults());
            assertEquals(1, statement.getUpdateCount());
            assertFalse(statement.getMoreResults());
            assertEquals(-1, statement.getUpdateCount());
        }

        ArgumentCaptor<Document> inserted = ArgumentCaptor.forClass(Document.class);
        verify(this.collection).insertOne(inserted.capture(), any(InsertOneOptions.class));
        assertEquals(new Document("created", Timestamp.valueOf(created)), inserted.getValue());
        verify(this.collection).updateOne(eq(new Document("created", Timestamp.valueOf(created))), eq(new Document("$set", new Document("created", Timestamp.valueOf(updated)))), any(UpdateOptions.class));
    }
}
