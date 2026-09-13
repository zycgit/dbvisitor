/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.redis.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.dbvisitor.adapter.redis.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.redis.RedisCommandInterceptor;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import redis.clients.jedis.commands.StringCommands;
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.params.SetParams;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class LongExpirationCommandTest extends AbstractJdbcTest {
    private static final long EXPIRATION = 4_294_967_296L;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> expirationOptions() {
        return Arrays.asList(new Object[][] {
                { "EX", new SetParams().ex(EXPIRATION), new GetExParams().ex(EXPIRATION) },
                { "PX", new SetParams().px(EXPIRATION), new GetExParams().px(EXPIRATION) },
                { "EXAT", new SetParams().exAt(EXPIRATION), new GetExParams().exAt(EXPIRATION) },
                { "PXAT", new SetParams().pxAt(EXPIRATION), new GetExParams().pxAt(EXPIRATION) }
        });
    }

    private final String option;
    private final SetParams expectedSet;
    private final GetExParams expectedGetEx;

    public LongExpirationCommandTest(String option, SetParams expectedSet, GetExParams expectedGetEx) {
        this.option = option;
        this.expectedSet = expectedSet;
        this.expectedGetEx = expectedGetEx;
    }

    @After
    public void clearInterceptors() {
        RedisCommandInterceptor.resetInterceptor();
    }

    private AtomicInteger interceptSet() {
        AtomicInteger calls = new AtomicInteger();
        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("set", (name, args) -> {
            assertEquals("expiry-key", args[0]);
            assertEquals("value", args[1]);
            assertEquals(this.expectedSet, args[2]);
            calls.incrementAndGet();
            return "OK";
        }));
        return calls;
    }

    private AtomicInteger interceptGetEx() {
        AtomicInteger calls = new AtomicInteger();
        RedisCommandInterceptor.resetInterceptor();
        RedisCommandInterceptor.addInterceptor(StringCommands.class, createInvocationHandler("getEx", (name, args) -> {
            assertEquals("expiry-key", args[0]);
            assertEquals(this.expectedGetEx, args[1]);
            calls.incrementAndGet();
            return "value";
        }));
        return calls;
    }

    @Test
    public void setLiteralPreservesLongExpiration() throws Exception {
        AtomicInteger calls = interceptSet();
        try (Connection connection = redisConnection(); Statement statement = connection.createStatement()) {
            assertEquals(1, statement.executeUpdate("SET expiry-key value " + this.option + " " + EXPIRATION));
        }
        assertEquals(1, calls.get());
    }

    @Test
    public void setBoundValuePreservesLongExpiration() throws Exception {
        AtomicInteger calls = interceptSet();
        try (Connection connection = redisConnection();
             PreparedStatement statement = connection.prepareStatement("SET ? ? " + this.option + " ?")) {
            statement.setString(1, "expiry-key");
            statement.setString(2, "value");
            statement.setLong(3, EXPIRATION);
            assertEquals(1, statement.executeUpdate());
        }
        assertEquals(1, calls.get());
    }

    @Test
    public void getExLiteralPreservesLongExpiration() throws Exception {
        AtomicInteger calls = interceptGetEx();
        try (Connection connection = redisConnection(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("GETEX expiry-key " + this.option + " " + EXPIRATION)) {
            assertTrue(result.next());
            assertEquals("value", result.getString(1));
            assertFalse(result.next());
        }
        assertEquals(1, calls.get());
    }

    @Test
    public void getExBoundValuePreservesLongExpiration() throws Exception {
        AtomicInteger calls = interceptGetEx();
        try (Connection connection = redisConnection();
             PreparedStatement statement = connection.prepareStatement("GETEX ? " + this.option + " ?")) {
            statement.setString(1, "expiry-key");
            statement.setLong(2, EXPIRATION);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals("value", result.getString(1));
                assertFalse(result.next());
            }
        }
        assertEquals(1, calls.get());
    }
}

