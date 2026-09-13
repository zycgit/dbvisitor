/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.jdbc;

import java.sql.SQLException;
import java.util.Map;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcBatchMutationCase;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import static org.junit.Assert.*;

public class RedisJdbcBatchMutationTest extends JdbcBatchMutationCase {

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

    @After
    public void closeRedisFixture() throws SQLException {
        this.fixture.close();
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_BATCH_POSITIONAL_INSERT)
    public void jdbcBatchPositionalInsert_shouldInsertRows() throws SQLException {
        Object[][] args = new Object[3][];
        for (int i = 0; i < args.length; i++) { args[i] = new Object[] { fixture.key("p" + i), "value-" + i }; }
        int[] counts = jdbcTemplate.executeBatch("SET ? ?", args);
        assertEquals(3, counts.length);
        for (int i = 0; i < args.length; i++) { assertEquals("value-" + i, jdbcTemplate.queryForString("GET ?", new Object[] { args[i][0] })); }
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_BATCH_NAMED_INSERT)
    public void jdbcBatchNamedInsert_shouldInsertRows() throws SQLException {
        Map[] args = new Map[3];
        for (int i = 0; i < args.length; i++) { args[i] = Map.of("key", fixture.key("n" + i), "value", "named-" + i); }
        assertEquals(3, jdbcTemplate.executeBatch("SET :key :value", args).length);
        for (int i = 0; i < args.length; i++) { assertEquals("named-" + i, jdbcTemplate.queryForString("GET :key", args[i])); }
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_BATCH_UPDATE)
    public void jdbcBatchUpdate_shouldChangeRows() throws SQLException {
        String key = fixture.key("update");
        jdbcTemplate.executeUpdate("SET ? ?", new Object[] { key, "old" });
        assertEquals(1, jdbcTemplate.executeBatch("SET ? ?", new Object[][] { { key, "new" } }).length);
        assertEquals("new", jdbcTemplate.queryForString("GET ?", new Object[] { key }));
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_BATCH_DELETE)
    public void jdbcBatchDelete_shouldRemoveRows() throws SQLException {
        String a = fixture.key("a"), b = fixture.key("b");
        jdbcTemplate.executeBatch("SET ? ?", new Object[][] { { a, "a" }, { b, "b" } });
        assertEquals(2, jdbcTemplate.executeBatch("DEL ?", new Object[][] { { a }, { b } }).length);
        assertEquals(Long.valueOf(0), jdbcTemplate.queryForLong("EXISTS ? ?", new Object[] { a, b }));
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_BATCH_STATEMENTS)
    public void jdbcBatchStatements_shouldReturnCountsForEveryStatement() throws SQLException {
        String a = fixture.key("a"), b = fixture.key("b");
        int[] counts = jdbcTemplate.executeBatch(new String[] { "SET " + a + " first", "SET " + b + " second", "SET " + a + " updated" });
        assertEquals(3, counts.length);
        assertEquals("updated", jdbcTemplate.queryForString("GET ?", new Object[] { a }));
        assertEquals("second", jdbcTemplate.queryForString("GET ?", new Object[] { b }));
    }
}
