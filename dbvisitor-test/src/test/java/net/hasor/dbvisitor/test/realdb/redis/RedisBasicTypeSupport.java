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
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertEquals;

/** Native single-value reads cover the same values as the common numeric/boolean/character cases. */
public abstract class RedisBasicTypeSupport extends AdapterCase {
    private final String prefix = "nxn:basic:" + UUID.randomUUID() + ":";
    private final List<String> keys = new ArrayList<>();
    private Connection connection;

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Before
    public void openFixture() throws SQLException {
        this.connection = newAdapterConnection();
        this.jdbcTemplate = new JdbcTemplate(this.connection);
    }

    protected <T> T roundTrip(String label, Object value, Class<T> type) throws SQLException {
        String key = this.prefix + label;
        this.keys.add(key);
        assertEquals(1, this.jdbcTemplate.executeUpdate("SET ? ?", new Object[] { key, value }));
        return this.jdbcTemplate.queryForObject("GET ?", new Object[] { key }, type);
    }

    @After
    public void closeFixture() throws SQLException {
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
