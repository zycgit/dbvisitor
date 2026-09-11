/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.adapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;

/** JSON text stored through SET/GET; Redis has no stored SQL NULL value. */
public final class RedisJsonTypeFixture implements AutoCloseable {
    private final String prefix = "nxn_json_" + UUID.randomUUID() + "_";
    private final Set<String> keys = new LinkedHashSet<>();
    private Connection connection;
    private JdbcTemplate jdbc;

    public JdbcTemplate open() throws SQLException {
        this.connection = OneApiDataSourceManager.getConnection("redis");
        this.jdbc = new JdbcTemplate(this.connection);
        return this.jdbc;
    }

    public String key(int id) {
        String key = this.prefix + id;
        this.keys.add(key);
        return key;
    }

    public String insertCommand(String columns, String... parameters) {
        if (!"id, json_varchar".equals(columns) || parameters.length != 2) {
            throw new IllegalArgumentException("Redis JSON fixture stores one JSON text per key");
        }
        return "SET " + String.join(" ", parameters);
    }

    public String selectCommand(String columns) {
        if (!"json_varchar".equals(columns)) {
            throw new IllegalArgumentException("Redis JSON fixture exposes one stored value");
        }
        return "GET ?";
    }

    public String storedJson(Object id) throws SQLException {
        return this.jdbc.queryForString("GET ?", new Object[] { id });
    }

    @Override
    public void close() throws SQLException {
        try {
            for (String key : this.keys) {
                this.jdbc.executeUpdate("DEL ?", new Object[] { key });
            }
        } finally {
            if (this.connection != null) {
                this.connection.close();
            }
        }
    }
}
