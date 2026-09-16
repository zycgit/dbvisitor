/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic.commands;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TimeZone;
import net.hasor.dbvisitor.adapter.elastic.ElasticUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class ElasticTemporalParameterCommandTest extends AbstractElasticCommandTest {
    @Test
    public void javaTimeParametersUseSamePayloadAsJdbcTemporalSetters() throws Exception {
        assertTemporalPayloads(false);
    }

    @Test
    public void explicitlyTypedJavaTimeParametersUseSamePayloadAsJdbcTemporalSetters() throws Exception {
        assertTemporalPayloads(true);
    }

    private void assertTemporalPayloads(boolean explicitTypes) throws Exception {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+08:00"));
            assertTemporalPayloadsInCurrentTimeZone(explicitTypes);
        } finally {
            TimeZone.setDefault(original);
        }
    }

    private void assertTemporalPayloadsInCurrentTimeZone(boolean explicitTypes) throws Exception {
        LocalDate date = LocalDate.of(2024, 3, 15);
        LocalTime time = LocalTime.of(14, 30, 45);
        LocalDateTime dateTime = LocalDateTime.of(date, time).withNano(123_000_000);
        Instant instant = Instant.parse("2024-03-15T14:30:45.123Z");
        String command = "POST /books/_doc {\"date\":?,\"time\":?,\"timestamp\":?,\"instant\":?,\"missing\":?,\"text\":?}";
        respondWith("{\"_id\":\"native-time\",\"result\":\"created\"}");
        respondWith("{\"_id\":\"jdbc-time\",\"result\":\"created\"}");
        try (Connection connection = elasticConnection(); PreparedStatement statement = connection.prepareStatement(command)) {
            if (explicitTypes) {
                statement.setObject(1, date, Types.DATE);
                statement.setObject(2, time, Types.TIME);
                statement.setObject(3, dateTime, Types.TIMESTAMP);
                statement.setObject(4, instant, Types.TIMESTAMP);
            } else {
                statement.setObject(1, date);
                statement.setObject(2, time);
                statement.setObject(3, dateTime);
                statement.setObject(4, instant);
            }
            statement.setNull(5, Types.TIMESTAMP);
            statement.setString(6, "2024-03-15T14:30:45.123");
            assertEquals(1, statement.executeUpdate());

            statement.clearParameters();
            statement.setDate(1, Date.valueOf(date));
            statement.setTime(2, Time.valueOf(time));
            statement.setTimestamp(3, Timestamp.valueOf(dateTime));
            statement.setTimestamp(4, Timestamp.from(instant));
            statement.setNull(5, Types.TIMESTAMP);
            statement.setString(6, "2024-03-15T14:30:45.123");
            assertEquals(1, statement.executeUpdate());
        }
        assertEquals(2, requests.size());
        assertEquals(requestBody(1), requestBody(0));
        assertEquals(Timestamp.valueOf(dateTime).getTime(), requestBody(0).path("timestamp").longValue());
        assertEquals(instant.toEpochMilli(), requestBody(0).path("instant").longValue());
        assertEquals("2024-03-15T14:30:45.123", requestBody(0).path("text").textValue());
        assertTrue(requestBody(0).path("missing").isNull());
    }

    @Test
    public void temporalEndpointParameterUsesSameValueAsJdbcTimestamp() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123_000_000);
        respondWith("{\"_id\":\"native-time\",\"result\":\"created\"}");
        respondWith("{\"_id\":\"jdbc-time\",\"result\":\"created\"}");
        try (Connection connection = elasticConnection(); PreparedStatement statement = connection.prepareStatement("POST /books/_doc/{?} {\"id\":1}")) {
            statement.setObject(1, dateTime);
            assertEquals(1, statement.executeUpdate());
            statement.setTimestamp(1, Timestamp.valueOf(dateTime));
            assertEquals(1, statement.executeUpdate());
        }
        assertEquals(2, requests.size());
        assertEquals(requests.get(1).getEndpoint(), requests.get(0).getEndpoint());
        assertEquals(requestBody(1), requestBody(0));
    }

    @Test
    public void nonJavaTimeParametersRemainUnchanged() {
        Object[] values = { "2024-03-15", Date.valueOf("2024-03-15"), Time.valueOf("14:30:45"), Timestamp.valueOf("2024-03-15 14:30:45.123"), new float[] { 1.0f, 2.0f } };
        for (Object value : values) {
            assertSame(value, ElasticUtils.normalizeParameter(value));
        }
        assertNull(ElasticUtils.normalizeParameter(null));
    }
}
