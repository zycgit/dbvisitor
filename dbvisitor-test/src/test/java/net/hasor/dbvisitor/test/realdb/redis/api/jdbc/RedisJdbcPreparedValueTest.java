/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.jdbc;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Assume;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcPreparedValueCase;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import static org.junit.Assert.*;

public class RedisJdbcPreparedValueTest extends JdbcPreparedValueCase {
    private final RedisJdbcFixture fixture = new RedisJdbcFixture();
    private static final String HOSTILE = "引号 \"; DEL other-key; -- \\ 换行\n下一行";

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    protected void createFixture() {}

    @Override
    protected String insertSql() { return "HSET ? ? ?"; }

    @Override
    @Before
    public void prepareValues() throws SQLException {
        this.jdbcTemplate = fixture.open();
        this.connection = fixture.connection();
        jdbcTemplate.executeUpdate("HSET ? ? ? ? ?", new Object[] { fixture.key("hash"), HOSTILE, "hostile", "ordinary", "ordinary-value" });
    }

    @Override
    @After
    public void cleanupValues() throws SQLException {
        fixture.close();
    }

    private void assertValue(PreparedStatement statement, String expected) throws SQLException {
        try (ResultSet rs = statement.executeQuery()) {
            assertTrue(rs.next()); assertEquals(expected, rs.getString(1)); assertFalse(rs.next());
        }
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_BOUND_STRING_LITERAL)
    public void setString_shouldTreatExpressionCharactersAsData() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("HGET ? ?")) {
            ps.setString(1, fixture.key("hash")); ps.setString(2, HOSTILE); assertValue(ps, "hostile");
        }
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_BOUND_OBJECT_LITERAL)
    public void setObject_shouldTreatExpressionCharactersAsData() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("HGET ? ?")) {
            ps.setObject(1, fixture.key("hash")); ps.setObject(2, HOSTILE); assertValue(ps, "hostile");
        }
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_BOUND_TYPED_LITERAL)
    public void setObjectWithJdbcType_shouldTreatExpressionCharactersAsData() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("HGET ? ?")) {
            ps.setObject(1, fixture.key("hash"), Types.VARCHAR); ps.setObject(2, HOSTILE, Types.VARCHAR); assertValue(ps, "hostile");
        }
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_BOUND_REUSE)
    public void reusedStatement_shouldReplaceValuesWithoutLeakingOldFilter() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("HGET ? ?")) {
            ps.setString(1, fixture.key("hash")); ps.setString(2, HOSTILE); assertValue(ps, "hostile");
            ps.setString(2, "ordinary"); assertValue(ps, "ordinary-value");
            ps.clearParameters(); ps.setString(1, fixture.key("hash")); ps.setString(2, "absent");
            try (ResultSet rs = ps.executeQuery()) { assertTrue(rs.next()); assertNull(rs.getString(1)); assertTrue(rs.wasNull()); }
        }
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_BOUND_NULL_EMPTY)
    public void nullAndEmptyString_shouldRemainDistinct() {
        Assume.assumeTrue("Redis string/hash values cannot store SQL NULL; absent keys are not bound NULL values", false);
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_BOUND_MUTATION_LITERAL)
    public void updateAndDelete_shouldNotBroadenBoundFilter() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("HSET ? ? ?")) {
            ps.setString(1, fixture.key("hash")); ps.setString(2, HOSTILE); ps.setObject(3, "changed", Types.VARCHAR); ps.executeUpdate();
        }
        assertEquals("changed", jdbcTemplate.queryForString("HGET ? ?", new Object[] { fixture.key("hash"), HOSTILE }));
        try (PreparedStatement ps = connection.prepareStatement("HDEL ? ?")) {
            ps.setString(1, fixture.key("hash")); ps.setObject(2, HOSTILE, Types.VARCHAR); ps.executeUpdate();
        }
        assertEquals(Long.valueOf(1), jdbcTemplate.queryForLong("HLEN ?", new Object[] { fixture.key("hash") }));
        assertEquals("ordinary-value", jdbcTemplate.queryForString("HGET ? ordinary", new Object[] { fixture.key("hash") }));
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_RESULT_FETCH_SIZE_LIMIT)
    public void fetchSize_shouldNotLimitTotalRowsAndMaxRowsShould() throws SQLException {
        fixture.seedScores();
        try (PreparedStatement ps = connection.prepareStatement("ZRANGE ? 0 -1")) {
            ps.setString(1, fixture.key("scores")); ps.setFetchSize(2);
            int count = 0;
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) { count++; } }
            assertEquals(10, count);
            ps.setMaxRows(3); count = 0;
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) { count++; } }
            assertEquals(3, count);
        }
    }
}
