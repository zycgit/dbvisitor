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
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCrudCommand;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;

/** Hash fields store native fixture data; no relational row-count semantics are inferred. */
public final class RedisJdbcCrudFixture implements AutoCloseable {
    private final String prefix = "nxn:crud:" + UUID.randomUUID() + ":";
    private final Set<String> keys = new LinkedHashSet<>();
    private Connection connection;
    private JdbcTemplate jdbc;

    public JdbcTemplate open() throws SQLException {
        OneApiDataSourceManager.assumeCurrentDataSource("redis");
        this.connection = OneApiDataSourceManager.getConnection("redis");
        this.jdbc = new JdbcTemplate(this.connection);
        return this.jdbc;
    }

    public int insert(int id, String name, int age, String email) throws SQLException {
        this.keys.add(this.prefix + id);
        return this.jdbc.executeUpdate(command(JdbcCrudCommand.INSERT), new Object[] { id, name, age, email, new Date() });
    }

    public String command(JdbcCrudCommand command) {
        String key = "#{'" + this.prefix + "' + arg0}";
        switch (command) {
            case INSERT:
                return "HSET " + key + " name #{arg1} age #{arg2} email #{arg3} create_time #{arg4}";
            case UPDATE_AGE:
                return "HSET #{'" + this.prefix + "' + arg1} age #{arg0}";
            case DELETE:
                return "DEL " + key;
            case SELECT_NAME:
                return "HGET " + key + " name";
            case SELECT_AGE:
                return "HGET " + key + " age";
            case COUNT_BY_ID:
                return "EXISTS " + key;
            default:
                throw new IllegalArgumentException("Unknown CRUD fixture command: " + command);
        }
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
