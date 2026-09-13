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
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcRowCallbackCase;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import static org.junit.Assert.*;

public class RedisJdbcRowCallbackTest extends JdbcRowCallbackCase {

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
    @Capability(CapabilityId.JDBC_RESULT_ROW_CALLBACK)
    public void rowCallbackHandler_shouldStreamRows() throws SQLException {
        fixture.seedScores();
        List<String> members = new ArrayList<>();
        double[] sum = { 0 };
        jdbcTemplate.query("ZRANGE ? 0 4 WITHSCORES", new Object[] { fixture.key("scores") }, (RowCallbackHandler) (rs, n) -> {
            assertEquals(members.size(), n);
            members.add(rs.getString("ELEMENT"));
            sum[0] += rs.getDouble("SCORE");
        });
        assertEquals(Arrays.asList("member-1", "member-2", "member-3", "member-4", "member-5"), members);
        assertEquals(115, sum[0], 0);
    }
}
