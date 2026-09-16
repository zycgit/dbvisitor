/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.mongo;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.driver.AdapterType;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MongoUtilsTest {
    @Test
    public void normalizeJavaTimeParameters() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123_456_789);
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();
        Instant instant = Instant.parse("2024-03-15T14:30:45.123456789Z");

        assertEquals(Timestamp.valueOf(dateTime), MongoUtils.normalizeParameter(dateTime));
        assertEquals(java.sql.Date.valueOf(date), MongoUtils.normalizeParameter(date));
        assertEquals(Time.valueOf(time), MongoUtils.normalizeParameter(time));
        assertEquals(Timestamp.from(instant), MongoUtils.normalizeParameter(instant));
    }

    @Test
    public void preserveOtherParameters() {
        Timestamp timestamp = Timestamp.valueOf("2024-03-15 14:30:45.123456789");
        Date date = new Date(1_710_513_045_123L);
        Document document = new Document("name", "unchanged");
        byte[] bytes = { 1, 2, 3 };

        assertNull(MongoUtils.normalizeParameter(null));
        assertSame(timestamp, MongoUtils.normalizeParameter(timestamp));
        assertSame(date, MongoUtils.normalizeParameter(date));
        assertSame(document, MongoUtils.normalizeParameter(document));
        assertSame(bytes, MongoUtils.normalizeParameter(bytes));
        assertSame("unchanged", MongoUtils.normalizeParameter("unchanged"));
    }

    @Test
    public void parseSizeUnits() {
        assertEquals(512L, MongoUtils.parseSize("512B", -1));
        assertEquals(2 * 1024L, MongoUtils.parseSize(" 2 kb ", -1));
        assertEquals(3 * 1024 * 1024L, MongoUtils.parseSize("3MB", -1));
        assertEquals(4 * 1024 * 1024 * 1024L, MongoUtils.parseSize("4GB", -1));
        assertEquals(5 * 1024 * 1024L, MongoUtils.parseSize("5", -1));
        assertEquals(0L, MongoUtils.parseSize("0B", -1));
    }

    @Test
    public void invalidSizeUsesConfiguredDefault() {
        assertEquals(123L, MongoUtils.parseSize(null, 123));
        assertEquals(123L, MongoUtils.parseSize("  ", 123));
        assertEquals(123L, MongoUtils.parseSize("invalid", 123));
        assertEquals(123L, MongoUtils.parseSize("1.5MB", 123));
        assertEquals(123L, MongoUtils.parseSize("9223372036854775808B", 123));
    }

    @Test
    public void convertBsonResultValues() {
        BigDecimal decimal = new BigDecimal("123.45");
        byte[] bytes = { 1, 2, 3 };
        Date date = new Date(1_710_513_045_123L);

        assertEquals(decimal, MongoUtils.jdbcValue(new Decimal128(decimal)));
        assertArrayEquals(bytes, (byte[]) MongoUtils.jdbcValue(new Binary(bytes)));
        assertEquals(new Timestamp(date.getTime()), MongoUtils.jdbcValue(date));
    }

    @Test
    public void preserveOtherResultValues() {
        ObjectId id = new ObjectId("65f45c8d57c6bd34d5b989a1");
        Document document = new Document("name", "unchanged");
        List<Integer> list = List.of(1, 2);
        BigDecimal decimal = new BigDecimal("123.45");

        assertNull(MongoUtils.jdbcValue(null));
        assertSame(id, MongoUtils.jdbcValue(id));
        assertSame(document, MongoUtils.jdbcValue(document));
        assertSame(list, MongoUtils.jdbcValue(list));
        assertSame(decimal, MongoUtils.jdbcValue(decimal));
    }

    @Test
    public void inferJdbcResultTypes() {
        assertEquals(AdapterType.Null, MongoUtils.jdbcType(null));
        assertEquals(AdapterType.Int, MongoUtils.jdbcType(1));
        assertEquals(AdapterType.Long, MongoUtils.jdbcType(1L));
        assertEquals(AdapterType.Double, MongoUtils.jdbcType(1.5D));
        assertEquals(AdapterType.Double, MongoUtils.jdbcType(1.5F));
        assertEquals(AdapterType.BigDecimal, MongoUtils.jdbcType(BigDecimal.ONE));
        assertEquals(AdapterType.BigDecimal, MongoUtils.jdbcType(new Decimal128(BigDecimal.ONE)));
        assertEquals(AdapterType.Boolean, MongoUtils.jdbcType(true));
        assertEquals(AdapterType.SqlTimestamp, MongoUtils.jdbcType(new Date(0)));
        assertEquals(AdapterType.Bytes, MongoUtils.jdbcType(new byte[] { 1, 2 }));
        assertEquals(AdapterType.Bytes, MongoUtils.jdbcType(new Binary(new byte[] { 1, 2 })));
        assertEquals(AdapterType.Array, MongoUtils.jdbcType(List.of(1, 2)));
        assertEquals(AdapterType.String, MongoUtils.jdbcType("value"));
        assertEquals(AdapterType.String, MongoUtils.jdbcType(new ObjectId("65f45c8d57c6bd34d5b989a1")));
        assertEquals(AdapterType.Unknown, MongoUtils.jdbcType(new Document()));
    }
}
