/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.redis.commands;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.dbvisitor.adapter.redis.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.redis.RedisCommandInterceptor;
import org.junit.After;
import org.junit.Test;
import redis.clients.jedis.commands.StringCommands;
import redis.clients.jedis.commands.StringBinaryCommands;
import static org.junit.Assert.*;

public class QuotedIdentifierCommandTest extends AbstractJdbcTest {
    @After
    public void clearInterceptors() {
        RedisCommandInterceptor.resetInterceptor();
    }

    @Test
    public void quotedSetAndGetUseTheSameKeyAsBoundParameters() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("set", (name, args) -> {
            assertEquals("group:key with spaces", args[0]);
            assertEquals("value with spaces", args[1]);
            calls.incrementAndGet();
            return "OK";
        }));
        try (Connection connection = redisConnection(); Statement statement = connection.createStatement()) {
            assertEquals(1, statement.executeUpdate("SET 'group:key with spaces' \"value with spaces\""));
        }
        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("get", (name, args) -> {
            assertEquals("group:key with spaces", args[0]);
            calls.incrementAndGet();
            return "value with spaces";
        }));
        try (Connection connection = redisConnection(); Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery("GET \"group:key with spaces\"")) {
                assertTrue(result.next());
                assertEquals("value with spaces", result.getString(1));
            }
            try (PreparedStatement query = connection.prepareStatement("GET ?")) {
                query.setString(1, "group:key with spaces");
                try (ResultSet result = query.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals("value with spaces", result.getString(1));
                }
            }
        }
        assertEquals(3, calls.get());
    }

    @Test
    public void stringArgumentsRemoveSingleAndDoubleQuotes() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("incr", (name, args) -> {
            assertEquals("counter:key with spaces", args[0]);
            calls.incrementAndGet();
            return 1L;
        }));
        try (Connection connection = redisConnection(); Statement statement = connection.createStatement()) {
            for (String command : new String[] { "INCR 'counter:key with spaces'", "INCR \"counter:key with spaces\"" }) {
                try (ResultSet result = statement.executeQuery(command)) {
                    assertTrue(result.next());
                    assertEquals(1L, result.getLong(1));
                }
            }
        }
        assertEquals(2, calls.get());
    }

    @Test
    public void quotedKeyWithBoundBinaryValuePreservesBytes() throws Exception {
        byte[] value = { 0, (byte) 255, (byte) 128 };
        AtomicInteger calls = new AtomicInteger();
        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringBinaryCommands.class, createInvocationHandler("set", (name, args) -> {
            assertArrayEquals("binary:key".getBytes(StandardCharsets.UTF_8), (byte[]) args[0]);
            assertArrayEquals(value, (byte[]) args[1]);
            calls.incrementAndGet();
            return "OK";
        }));
        try (Connection connection = redisConnection(); PreparedStatement statement = connection.prepareStatement("SET 'binary:key' ?")) {
            statement.setBytes(1, value);
            assertEquals(1, statement.executeUpdate());
        }
        assertEquals(1, calls.get());
    }
}

