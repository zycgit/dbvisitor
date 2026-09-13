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
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcResultExtractorCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public class RedisJdbcResultExtractorTest extends JdbcResultExtractorCase {
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
    protected String selectSql(String columns, String predicate, boolean ordered) {
        return "ZRANGE ? ? ? WITHSCORES";
    }

    @Override
    protected Object[] selectArguments(String columns, String predicate, Object... values) {
        if (predicate.contains("BETWEEN")) {
            int count = ((Number) values[1]).intValue() - ((Number) values[0]).intValue() + 1;
            return new Object[] { this.fixture.key("scores"), 0, count - 1 };
        }
        return new Object[] { this.fixture.key("scores"), 0, predicate.equals("age > ?") ? -1 : 0 };
    }

    @Override
    protected String customKeyColumn() {
        return "ELEMENT";
    }
    @Override
    protected String customValueColumn() {
        return "SCORE";
    }
    @Override
    protected boolean customIntegerKey() {
        return false;
    }
    @Override
    protected boolean customIntegerValue() {
        return true;
    }
    @Override
    protected Class<?> pairKeyType() {
        return String.class;
    }
    @Override
    protected Class<?> pairValueType() {
        return Double.class;
    }
    @Override
    protected String filterNumberProperty() {
        return "score";
    }
    @Override
    protected int expectedColumnMapCount() {
        return 10;
    }

    @Override
    protected Map<Object, Object> expectedCustomMap() {
        return Map.of("member-1", 21, "member-2", 22, "member-3", 23);
    }

    @Override
    protected Map<Object, Object> expectedPairs() {
        return Map.of("member-1", 21.0, "member-2", 22.0, "member-3", 23.0);
    }

    @Override
    protected Map<String, Object> expectedMappedRow() {
        return Map.of("element", "member-1", "score", 21.0);
    }

    @Override
    protected Map<String, Object> expectedMappingBean() {
        return Map.of("element", "member-1", "score", 21.0);
    }

    @Override
    protected Map<String, Object> expectedMappingMap() {
        return Map.of("element", "member-1", "score", 21.0);
    }
}
