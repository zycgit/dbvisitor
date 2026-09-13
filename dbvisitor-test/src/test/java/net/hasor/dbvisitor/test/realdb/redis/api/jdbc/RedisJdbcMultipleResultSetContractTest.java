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
import java.util.List;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcMultipleResultSetContractTest;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import static org.junit.Assert.*;

public class RedisJdbcMultipleResultSetContractTest extends JdbcMultipleResultSetContractTest {

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
    @Capability(CapabilityId.JDBC_MULTIPLE_RESULT_SETS)
    public void jdbcMultipleExecute_shouldReturnMultipleResultSets() throws SQLException {
        fixture.seedScores();
        Map<String, Object> result = jdbcTemplate.multipleExecute("ZRANGE '" + fixture.key("scores") + "' 0 0\nZRANGE '" + fixture.key("scores") + "' 1 2");
        List<Object> sets = new ArrayList<>(result.values());
        assertEquals(2, sets.size());
        assertEquals(1, ((List<?>) sets.get(0)).size());
        assertEquals(2, ((List<?>) sets.get(1)).size());
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_MULTIPLE_POSITIONAL)
    public void jdbcMultipleExecute_shouldBindPositionalParametersAcrossStatements() throws SQLException {
        fixture.seedScores();
        Map<String, Object> result = jdbcTemplate.multipleExecute("ZRANGE ? ? ?\nZRANGE ? ? ?",
            new Object[] { fixture.key("scores"), 0, 1, fixture.key("scores"), 2, 2 });
        List<Object> sets = new ArrayList<>(result.values());
        assertEquals(2, sets.size());
        assertEquals(2, ((List<?>) sets.get(0)).size());
        assertEquals("member-3", ((Map<?, ?>) ((List<?>) sets.get(1)).get(0)).get("ELEMENT"));
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_MULTIPLE_NAMED)
    public void jdbcMultipleExecute_shouldBindNamedParametersAcrossStatements() throws SQLException {
        fixture.seedScores();
        Map<String, Object> result = jdbcTemplate.multipleExecute("ZRANGE :key 0 :end\nZRANGE :key :start 2",
            Map.of("key", fixture.key("scores"), "end", 0, "start", 1));
        List<Object> sets = new ArrayList<>(result.values());
        assertEquals(2, sets.size());
        assertEquals(1, ((List<?>) sets.get(0)).size());
        assertEquals(2, ((List<?>) sets.get(1)).size());
    }

    @Override
    @Test
    @Capability(CapabilityId.JDBC_MULTIPLE_RESULTSET_RULE)
    public void jdbcMultipleExecute_shouldNameAndMapResultSetsWithRule() throws SQLException {
        fixture.seedScores();
        String type = RedisJdbcFixture.ScoredMember.class.getName();
        Map<String, Object> result = jdbcTemplate.multipleExecute("ZRANGE '" + fixture.key("scores") + "' 0 0 WITHSCORES @{resultSet,name=first,javaType=" + type + "}\n"
            + "ZRANGE '" + fixture.key("scores") + "' 2 2 WITHSCORES @{resultSet,name=third,javaType=" + type + "}");
        assertEquals(2, result.size());
        List<?> first = (List<?>) result.get("first");
        List<?> third = (List<?>) result.get("third");
        assertEquals(1, first.size()); assertEquals(1, third.size());
        assertEquals("member-1", ((RedisJdbcFixture.ScoredMember) first.get(0)).getElement());
        assertEquals("member-3", ((RedisJdbcFixture.ScoredMember) third.get(0)).getElement());
    }
}
