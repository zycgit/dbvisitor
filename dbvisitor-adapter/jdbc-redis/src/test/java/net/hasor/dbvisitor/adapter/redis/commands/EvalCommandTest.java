/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.redis.commands;

import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.After;
import org.junit.Test;
import net.hasor.dbvisitor.adapter.redis.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.redis.RedisCommandInterceptor;
import redis.clients.jedis.commands.ScriptingKeyCommands;
import redis.clients.jedis.commands.ScriptingKeyBinaryCommands;
import static org.junit.Assert.*;

public class EvalCommandTest extends AbstractJdbcTest {
    @After
    public void reset() {
        RedisCommandInterceptor.resetInterceptor();
    }

    @Test
    public void scriptAndValuesRemainSeparate() throws Exception {
        RedisCommandInterceptor.addInterceptor(ScriptingKeyCommands.class, createInvocationHandler("eval", (name, args) -> {
            assertEquals("return ARGV[1]", args[0]);
            assertEquals(1, args[1]);
            assertArrayEquals(new String[] { "key", "'\nreturn 999" }, (String[]) args[2]);
            return 15L;
        }));
        try (Connection c = redisConnection(); PreparedStatement ps = c.prepareStatement("EVAL ? ? ? ?")) {
            ps.setString(1, "return ARGV[1]");
            ps.setInt(2, 1);
            ps.setString(3, "key");
            ps.setString(4, "'\nreturn 999");
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(15L, rs.getLong("VALUE"));
                assertEquals(15L, rs.getObject(1));
                assertFalse(rs.next());
            }
        }
    }

    @Test
    public void nativeNestedReplyIsNotFlattened() throws Exception {
        Object reply = Arrays.asList(1L, Arrays.asList("text", 2L));
        RedisCommandInterceptor.addInterceptor(ScriptingKeyCommands.class, createInvocationHandler("eval", (name, args) -> reply));
        try (Connection c = redisConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("EVAL 'return {1}' 0")) {
            assertTrue(rs.next());
            assertEquals(reply, rs.getObject("VALUE"));
            assertFalse(rs.next());
        }
    }

    @Test
    public void binaryArgumentUsesNativeBinaryScriptApi() throws Exception {
        byte[] bytes = new byte[] { 0, (byte) 255, (byte) 128 };
        RedisCommandInterceptor.addInterceptor(ScriptingKeyBinaryCommands.class, createInvocationHandler("eval", (name, args) -> {
            assertArrayEquals("return ARGV[1]".getBytes(StandardCharsets.UTF_8), (byte[]) args[0]);
            assertEquals(0, args[1]);
            assertArrayEquals(bytes, ((byte[][]) args[2])[0]);
            return bytes;
        }));
        try (Connection c = redisConnection(); PreparedStatement ps = c.prepareStatement("EVAL 'return ARGV[1]' 0 ?")) {
            ps.setBytes(1, bytes);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertArrayEquals(bytes, rs.getBytes(1));
            }
        }
    }

    @Test
    public void invalidKeyCountIsRejectedBeforeCallingServer() throws Exception {
        try (Connection c = redisConnection(); Statement s = c.createStatement()) {
            assertThrows(SQLException.class, () -> s.executeQuery("EVAL 'return 1' -1"));
            assertThrows(SQLException.class, () -> s.executeQuery("EVAL 'return 1' 2 onlykey"));
            assertThrows(SQLException.class, () -> s.executeQuery("EVAL 'return 1' 4294967296"));
        }
    }
}
