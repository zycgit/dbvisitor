/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.jdbc;

import java.sql.SQLException;
import java.util.List;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcBeanQueryContractTest;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import static org.junit.Assert.*;

public class RedisJdbcBeanQueryContractTest extends JdbcBeanQueryContractTest {

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
    @Capability(CapabilityId.JDBC_QUERY_BEAN)
    public void jdbcQueryForList_shouldReturnBeans() throws SQLException {
        fixture.seedScores();
        List<RedisJdbcFixture.ScoredMember> rows = jdbcTemplate.queryForList("ZRANGE ? 0 2 WITHSCORES",
            new Object[] { fixture.key("scores") }, RedisJdbcFixture.ScoredMember.class);
        assertEquals(3, rows.size());
        for (int i = 0; i < rows.size(); i++) {
            assertEquals("member-" + (i + 1), rows.get(i).getElement());
            assertEquals(Double.valueOf(21 + i), rows.get(i).getScore());
        }
    }
}
