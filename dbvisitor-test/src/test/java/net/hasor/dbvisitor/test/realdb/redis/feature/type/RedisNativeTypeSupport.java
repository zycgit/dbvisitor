/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.feature.type;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import org.junit.After;
import org.junit.Before;

public abstract class RedisNativeTypeSupport extends AdapterContractTest {
    private final String prefix = "nxn:redis:types:" + UUID.randomUUID() + ":";
    private final List<String> keys = new ArrayList<>();
    protected Connection connection;

    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Before
    public void openTypes() throws SQLException {
        this.connection = newAdapterConnection();
        this.jdbcTemplate = new JdbcTemplate(this.connection);
    }

    protected String key(String label) {
        String key = this.prefix + label;
        this.keys.add(key);
        return key;
    }

    protected <T> T roundTrip(Object value, Class<T> type) throws SQLException {
        String key = key("value" + this.keys.size());
        this.jdbcTemplate.executeUpdate("SET ? ?", new Object[] { key, value });
        return this.jdbcTemplate.queryForObject("GET ?", new Object[] { key }, type);
    }

    @After
    public void closeTypes() throws SQLException {
        try {
            if (this.jdbcTemplate != null) {
                for (String key : this.keys) {
                    this.jdbcTemplate.executeUpdate("DEL ?", key);
                }
            }
        } finally {
            if (this.connection != null) {
                this.connection.close();
            }
        }
    }
}

