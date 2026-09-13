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
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.dbvisitor.adapter.redis.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.redis.RedisCommandInterceptor;
import org.junit.After;
import org.junit.Test;
import redis.clients.jedis.commands.StringBinaryCommands;
import static org.junit.Assert.*;

public class BinaryStringCommandTest extends AbstractJdbcTest {
    @After
    public void clearInterceptors() {
        RedisCommandInterceptor.resetInterceptor();
    }

    @Test
    public void setBytesUsesNativeBinaryValueAndUtf8TextKey() throws Exception {
        byte[] value = new byte[] { 0, (byte) 255, (byte) 128, 13, 10 };
        AtomicInteger calls = new AtomicInteger();
        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringBinaryCommands.class, createInvocationHandler("set", (name, args) -> {
            calls.incrementAndGet();
            assertArrayEquals("中文-key".getBytes(StandardCharsets.UTF_8), (byte[]) args[0]);
            assertArrayEquals(value, (byte[]) args[1]);
            return "OK";
        }));
        try (Connection connection = redisConnection(); PreparedStatement ps = connection.prepareStatement("SET ? ?")) {
            ps.setString(1, "中文-key");
            ps.setBytes(2, value);
            assertEquals(1, ps.executeUpdate());
        }
        assertEquals(1, calls.get());
    }

    @Test
    public void binaryKeyReadsBytesWithoutDecoding() throws Exception {
        byte[] key = new byte[] { 0, (byte) 255, 42 };
        byte[] value = new byte[] { (byte) 255, 0, (byte) 128 };
        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringBinaryCommands.class, createInvocationHandler("get", (name, args) -> {
            assertArrayEquals(key, (byte[]) args[0]);
            return value;
        }));
        try (Connection connection = redisConnection(); PreparedStatement ps = connection.prepareStatement("GET ?")) {
            ps.setBytes(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertArrayEquals(value, rs.getBytes("VALUE"));
                assertArrayEquals(value, (byte[]) rs.getObject(1));
                assertFalse(rs.next());
            }
        }
    }

    @Test
    public void binarySetGetReturnsPreviousBytes() throws Exception {
        byte[] previous = new byte[] { 0, (byte) 255 };
        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringBinaryCommands.class, createInvocationHandler("setGet", (name, args) -> previous));
        try (Connection connection = redisConnection(); PreparedStatement ps = connection.prepareStatement("SET ? ? GET")) {
            ps.setBytes(1, new byte[] { 42 });
            ps.setBytes(2, new byte[] { 43 });
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertArrayEquals(previous, rs.getBytes(1));
            }
        }
    }
}

