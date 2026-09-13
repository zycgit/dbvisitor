/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.feature.write;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.realdb.redis.api.jdbc.RedisJdbcFixture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RedisLongExpirationTest {
    private final RedisJdbcFixture fixture = new RedisJdbcFixture();
    private JdbcTemplate jdbc;

    @Before
    public void openFixture() throws SQLException {
        this.jdbc = this.fixture.open();
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }

    private long futureExpiration() {
        long expiration = System.currentTimeMillis() + 300_000L;
        assertTrue(expiration > Integer.MAX_VALUE);
        return expiration;
    }

    private void assertExpiration(String key, long expiration) throws SQLException {
        assertEquals("value", this.jdbc.queryForString("GET ?", new Object[] { key }));
        assertEquals(Long.valueOf(expiration), this.jdbc.queryForLong("PEXPIRETIME ?", new Object[] { key }));
    }

    @Test
    public void setPxAtAcceptsLiteralUnixMilliseconds() throws SQLException {
        String key = this.fixture.key("set-literal");
        long expiration = futureExpiration();
        assertEquals(1, this.jdbc.executeUpdate("SET '" + key + "' value PXAT " + expiration));
        assertExpiration(key, expiration);
    }

    @Test
    public void setPxAtAcceptsBoundUnixMilliseconds() throws SQLException {
        String key = this.fixture.key("set-bound");
        long expiration = futureExpiration();
        assertEquals(1, this.jdbc.executeUpdate("SET ? ? PXAT ?", new Object[] { key, "value", expiration }));
        assertExpiration(key, expiration);
    }

    @Test
    public void getExPxAtAcceptsLiteralUnixMilliseconds() throws SQLException {
        String key = this.fixture.key("getex-literal");
        long expiration = futureExpiration();
        this.jdbc.executeUpdate("SET ? ?", new Object[] { key, "value" });
        assertEquals("value", this.jdbc.queryForString("GETEX '" + key + "' PXAT " + expiration));
        assertExpiration(key, expiration);
    }

    @Test
    public void getExPxAtAcceptsBoundUnixMilliseconds() throws SQLException {
        String key = this.fixture.key("getex-bound");
        long expiration = futureExpiration();
        this.jdbc.executeUpdate("SET ? ?", new Object[] { key, "value" });
        assertEquals("value", this.jdbc.queryForString("GETEX ? PXAT ?", new Object[] { key, expiration }));
        assertExpiration(key, expiration);
    }
}

