/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.redis.connect;
import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.dbvisitor.adapter.redis.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.redis.RedisCommandInterceptor;
import org.junit.Test;
import redis.clients.jedis.commands.StringCommands;
import static org.junit.Assert.*;
public class RedisLeadingWhitespaceTest extends AbstractJdbcTest {
    @Test
    public void leadingEmptyLinesMustNotHideCommandBoundaries() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("set", (name, args) -> {
            calls.incrementAndGet();
            return "OK";
        }));
        try (Connection connection = redisConnection(); Statement statement = connection.createStatement()) {
            assertFalse(statement.execute("\n\n SET a b\n\n SET c d\n"));
            assertEquals(1, statement.getUpdateCount());
            assertFalse(statement.getMoreResults());
            assertEquals(1, statement.getUpdateCount());
            assertFalse(statement.getMoreResults());
            assertEquals(-1, statement.getUpdateCount());
            assertEquals(2, calls.get());
        } finally {
            RedisCommandInterceptor.resetInterceptor();
        }
    }
    @Test
    public void newlineInsideCommandMustNotBecomeSpace() throws Exception {
        try (Connection connection = redisConnection(); Statement statement = connection.createStatement()) {
            assertThrows(java.sql.SQLException.class, () -> statement.execute("SET a\nb"));
        }
    }
}

