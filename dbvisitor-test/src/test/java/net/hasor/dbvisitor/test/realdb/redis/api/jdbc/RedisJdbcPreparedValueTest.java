/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import net.hasor.dbvisitor.test.contract.feature.parameter.JdbcPreparedValueCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;

public class RedisJdbcPreparedValueTest extends JdbcPreparedValueCase {
    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    protected void createFixture() {
        // The first HSET creates the private hash.
    }

    @Override
    protected String[] seedNames() {
        return new String[] { super.seedNames()[0], "ordinary", "", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth" };
    }

    @Override
    protected String insertSql() {
        return "HSET '" + this.table + "' ? ?";
    }

    @Override
    protected void bindSeed(PreparedStatement statement, long id, String name) throws SQLException {
        statement.setString(1, name);
        statement.setLong(2, id);
    }

    @Override
    protected String selectByNameSql() {
        return "HGET '" + this.table + "' ?";
    }

    @Override
    protected String idColumn() {
        return "VALUE";
    }

    @Override
    protected String nameColumn() {
        return "VALUE";
    }

    @Override
    protected String expectedName(long id, String name) {
        return Long.toString(id);
    }

    @Override
    protected boolean missingNameReturnsNullRow() {
        return true;
    }

    @Override
    protected Set<Long> expectedIds() {
        return LongStream.rangeClosed(1, 10).boxed().collect(Collectors.toSet());
    }

    @Override
    protected String selectAllSql() {
        return "HVALS '" + this.table + "'";
    }

    @Override
    protected String updateSql() {
        return "EVAL \"local value=redis.call('HGET',KEYS[1],ARGV[2]); if not value then return 0 end; redis.call('HDEL',KEYS[1],ARGV[2]); redis.call('HSET',KEYS[1],ARGV[1],value); return 1\" 1 '" + this.table + "' ? ?";
    }

    @Override
    protected int executeBoundUpdate(PreparedStatement statement) throws SQLException {
        if (!statement.execute()) {
            return statement.getUpdateCount();
        }
        try (ResultSet result = statement.getResultSet()) {
            if (!result.next()) {
                throw new SQLException("Expected a Redis mutation result.");
            }
            int count = result.getInt("VALUE");
            if (result.wasNull() || result.next()) {
                throw new SQLException("Expected one non-null Redis mutation count.");
            }
            return count;
        }
    }

    @Override
    protected String deleteSql() {
        return "HDEL '" + this.table + "' ?";
    }

    @Override
    protected String dropSql() {
        return "DEL '" + this.table + "'";
    }
}
