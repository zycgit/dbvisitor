/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;

/** Reads the single numeric column returned by commands such as EVAL and ZADD. */
public final class RedisIntegerResultExtractor implements ResultSetExtractor<Integer> {
    @Override
    public Integer extractData(ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) {
            throw new SQLException("Expected a Redis integer result");
        }
        if (resultSet.getMetaData().getColumnCount() != 1) {
            throw new SQLException("Expected exactly one Redis result column");
        }
        int value = resultSet.getInt(1);
        if (resultSet.wasNull() || resultSet.next()) {
            throw new SQLException("Expected exactly one non-null Redis integer result");
        }
        return value;
    }
}
