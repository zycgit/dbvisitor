/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.feature.function;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import net.hasor.dbvisitor.test.contract.feature.function.FunctionCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.test.realdb.redis.api.jdbc.RedisJdbcFixture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertArrayEquals;

/** Lua functions execute on Redis; the adapter preserves their native reply in VALUE. */
public class RedisFunctionTest extends FunctionCase {
    private final RedisJdbcFixture fixture = new RedisJdbcFixture();

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @Override
    @Before
    public void createFunctionFixtures() throws SQLException {
        this.jdbcTemplate.executeUpdate("HSET ? ? ?", new Object[] { this.fixture.key("users"), "918101", "FuncAlice" });
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }

    @Override
    protected String addNumbersQuerySql() {
        return "EVAL 'return tonumber(ARGV[1]) + tonumber(ARGV[2])' 0 ? ?";
    }

    @Override
    protected String addNumbersCallableSql() {
        return addNumbersQuerySql();
    }

    @Override
    protected String multiplyNamedQuerySql() {
        return "EVAL 'return tonumber(ARGV[1]) * tonumber(ARGV[2])' 0 :x :y";
    }

    @Test
    public void binaryArgumentRoundTrip() throws SQLException {
        byte[] value = new byte[] { 0, (byte) 255, (byte) 128 };
        assertArrayEquals(value, jdbcTemplate.queryForObject("EVAL 'return ARGV[1]' 0 ?", new Object[] { value }, byte[].class));
    }

    @Test
    public void nullReplyAndScriptErrors() throws Exception {
        assertNull(jdbcTemplate.queryForObject("EVAL 'return false' 0", Object.class));
        assertThrows(SQLException.class, () -> jdbcTemplate.queryForObject("EVAL 'error(123)' 0", Object.class));
        String text = "quote'\nreturn 999 中文";
        assertEquals(text, jdbcTemplate.queryForObject("EVAL 'return ARGV[1]' 0 ?", text, String.class));
    }

    @Override
    protected String getUsernameQuerySql() {
        return "EVAL 'return redis.call(\"HGET\", KEYS[1], ARGV[1])' 1 '" + fixture.key("users") + "' ?";
    }

    @Override
    protected String transformStringQuerySql() {
        return "EVAL 'return string.upper(ARGV[1]) .. ARGV[2]' 0 ? ?";
    }

    @Override
    protected String transformedValueColumn() {
        return "VALUE";
    }
}
