/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.feature.function;

import java.sql.SQLException;
import java.util.Map;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import net.hasor.dbvisitor.test.contract.feature.function.FunctionContractTest;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.test.realdb.redis.api.jdbc.RedisJdbcFixture;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.number.IntegerTypeHandler;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertArrayEquals;

/** Lua functions execute on Redis; the adapter preserves their native reply in VALUE. */
public class RedisFunctionContractTest extends FunctionContractTest {
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
    protected String multiplyNamedQuerySql() {
        return "EVAL 'return tonumber(ARGV[1]) * tonumber(ARGV[2])' 0 :x :y";
    }

    @Override
    @Test
    @Capability(CapabilityId.FUNCTION_QUERY_SCALAR)
    public void queryFunction_shouldReturnScalarValue() throws SQLException {
        assertEquals(Integer.valueOf(15), jdbcTemplate.queryForObject(addNumbersQuerySql(), new Object[] { 10, 5 }, Integer.class));
    }

    @Override
    @Test
    @Capability(CapabilityId.FUNCTION_QUERY_NAMED_PARAMETER)
    public void queryFunction_shouldBindNamedParameters() throws SQLException {
        assertEquals(Integer.valueOf(56), jdbcTemplate.queryForObject(multiplyNamedQuerySql(), Map.of("x", 7, "y", 8), Integer.class));
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
    @Test
    @Capability(CapabilityId.FUNCTION_QUERY_PARAMETER_VARIANTS)
    public void queryFunction_shouldSupportSingleParameterSqlArgAndStringTransform() throws SQLException {
        String name = this.jdbcTemplate.queryForObject(
                "EVAL 'return redis.call(\"HGET\", KEYS[1], ARGV[1])' 1 '" + this.fixture.key("users") + "' ?", 918101, String.class);
        Integer sum = this.jdbcTemplate.queryForObject(addNumbersQuerySql(),
                new SqlArg[] { SqlArg.valueOf(12, new IntegerTypeHandler()), SqlArg.valueOf(3, new IntegerTypeHandler()) }, Integer.class);
        String transformed = this.jdbcTemplate.queryForString(
                "EVAL 'return string.upper(ARGV[1]) .. ARGV[2]' 0 ? ?", new Object[] { "world", "!" });
        assertEquals("FuncAlice", name);
        assertEquals(Integer.valueOf(15), sum);
        assertEquals("WORLD!", transformed);
    }

    @Override
    @Test
    @Capability(CapabilityId.FUNCTION_QUERY_OUT_RECORD)
    public void queryFunction_shouldReturnOutParameterRecord() {
        Assume.assumeTrue("Redis EVAL has no SQL OUT-parameter record; the adapter preserves the native reply in one VALUE cell", false);
    }

    @Override
    @Test
    @Capability(CapabilityId.FUNCTION_QUERY_TABLE_RESULT)
    public void queryFunction_shouldReturnTableRows() {
        Assume.assumeTrue("The current adapter keeps Lua arrays in one VALUE cell instead of expanding them into result rows", false);
    }

    @Override
    @Test
    @Capability(CapabilityId.FUNCTION_QUERY_NULL_FALLBACK)
    public void queryFunction_shouldHandleMissingLookupFallback() {
        Assume.assumeTrue("This contract requires named OUT-record columns; EVAL returns a native VALUE cell", false);
    }

    @Override
    @Test
    @Capability(CapabilityId.FUNCTION_CALL_CALLBACK)
    public void callFunction_shouldSupportCallableStatementCallbackAccess() {
        Assume.assumeTrue("The Redis JDBC adapter does not implement CallableStatement", false);
    }
}
