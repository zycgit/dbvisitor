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
import java.util.LinkedHashMap;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcRowMapperCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisJdbcRowMapperTest extends JdbcRowMapperCase {
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
    protected void seedUsers() throws SQLException {
        this.fixture.seedScores();
    }

    @Override
    protected Class<?> resultBeanType() {
        return RedisJdbcFixture.ScoredMember.class;
    }

    @After
    public void closeRedisFixture() throws SQLException {
        this.fixture.close();
    }

    @Override
    protected void insertUser(int id, String name, int age, String email) throws SQLException {
        this.fixture.seedScores();
    }

    @Override
    protected String selectSql(String columns, String predicate, boolean ordered) {
        return "ZRANGE ? ? ?" + ("name".equals(columns) ? "" : " WITHSCORES");
    }

    @Override
    protected Object[] selectArguments(String columns, String predicate, Object... values) {
        if (predicate.contains("BETWEEN")) {
            return new Object[] { this.fixture.key("scores"), 0, ((Number) values[1]).intValue() - baseId() - 1 };
        }
        return new Object[] { this.fixture.key("scores"), 0, 0 };
    }

    @Override
    protected String customNameColumn() {
        return "ELEMENT";
    }

    @Override
    protected String customNumberColumn() {
        return "SCORE";
    }

    @Override
    protected List<String> expectedCustomRows() {
        return List.of("MEMBER-1:21", "MEMBER-2:22", "MEMBER-3:23");
    }

    @Override
    protected Map<String, Object> expectedColumnMap() {
        return Map.of("ELEMENT", "member-1", "SCORE", 21.0);
    }

    @Override
    protected List<String> expectedScalarNames() {
        return List.of("member-1", "member-2");
    }

    @Override
    protected Map<String, Object> expectedBean() {
        return Map.of("element", "member-1", "score", 21.0);
    }

    @Override
    protected Class<?> ignoredBeanType() {
        return RedisJdbcFixture.IgnoredScore.class;
    }

    @Override
    protected Map<String, Object> expectedIgnoredBean(int id) {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("element", "member-1");
        expected.put("score", null);
        return expected;
    }
}
