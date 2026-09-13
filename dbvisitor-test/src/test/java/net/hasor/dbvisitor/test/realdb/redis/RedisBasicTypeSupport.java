/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import static org.junit.Assert.assertEquals;

/** Native single-value reads cover the same values as the common numeric/boolean/character cases. */
public final class RedisBasicTypeSupport implements AutoCloseable {
    private final String prefix = "nxn:basic:" + UUID.randomUUID() + ":";
    private final List<String> keys = new ArrayList<>();
    private Connection connection;
    private JdbcTemplate jdbcTemplate;

    public void openFixture() throws SQLException {
        OneApiDataSourceManager.assumeCurrentDataSource("redis");
        this.connection = OneApiDataSourceManager.getConnection("redis");
        this.jdbcTemplate = new JdbcTemplate(this.connection);
    }

    public <T> T roundTrip(String label, Object value, Class<T> type) throws SQLException {
        String key = this.prefix + label;
        this.keys.add(key);
        assertEquals(1, this.jdbcTemplate.executeUpdate("SET ? ?", new Object[] { key, value }));
        return this.jdbcTemplate.queryForObject("GET ?", new Object[] { key }, type);
    }

    @Override
    public void close() throws SQLException {
        try {
            for (String key : this.keys) {
                this.jdbcTemplate.executeUpdate("DEL ?", new Object[] { key });
            }
        } finally {
            if (this.connection != null) {
                this.connection.close();
            }
        }
    }
}
