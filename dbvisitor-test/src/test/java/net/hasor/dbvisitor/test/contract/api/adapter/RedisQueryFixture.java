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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCrudCommand;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;

/** Private hashes supply native scalar, list, count and two-column Pairs result shapes. */
public final class RedisQueryFixture implements AutoCloseable {
    private final String prefix = "nxn_query_" + UUID.randomUUID() + "_";
    private final Set<String> keys = new LinkedHashSet<>();
    private Connection connection;
    private JdbcTemplate jdbc;
    private int baseId;

    public JdbcTemplate open(int baseId) throws SQLException {
        OneApiDataSourceManager.assumeCurrentDataSource("redis");
        this.baseId = baseId;
        this.connection = OneApiDataSourceManager.getConnection("redis");
        this.jdbc = new JdbcTemplate(this.connection);
        return this.jdbc;
    }

    public void insert(int id, String name, int age, Date createdAt) throws SQLException {
        put(this.prefix + "ages", id, age);
        put(rangeKey("names", 3), id, name);
        put(rangeKey("name_age", 3), name, age);
        // A separate two-entry hash is the temporal Pairs fixture; queries do no client filtering.
        if (id <= this.baseId + 2) {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT).format(createdAt);
            put(rangeKey("dates", 2), id, date);
        }
    }

    private void put(String key, Object field, Object value) throws SQLException {
        this.keys.add(key);
        this.jdbc.executeUpdate("HSET ? ? ?", new Object[] { key, field, value });
    }

    public String selectById(String column, String parameter) {
        String key;
        if ("age".equals(column)) {
            key = this.prefix + "ages";
        } else if ("name".equals(column)) {
            key = rangeKey("names", 3);
        } else {
            throw new IllegalArgumentException("Unsupported scalar fixture column: " + column);
        }
        return "HGET " + key + " " + parameter;
    }

    public String selectRange(String columns, String lower, String upper) {
        if ("name".equals(columns)) {
            // HMGET preserves the requested field order and exposes a single VALUE column.
            return "HMGET " + rangeKey("names", 3) + " #{" + argument(lower, 0) + "} #{" + argument(lower, 0) + " + 1} #{" + argument(upper, 1) + "}";
        }
        String kind;
        if ("id, name".equals(columns)) {
            kind = "names";
        } else if ("name, age".equals(columns)) {
            kind = "name_age";
        } else if ("id, create_time".equals(columns)) {
            kind = "dates";
        } else {
            throw new IllegalArgumentException("Unsupported Pairs fixture columns: " + columns);
        }
        return "HGETALL " + boundRangeKey(kind, lower, upper);
    }

    public String countRange(String lower, String upper) {
        return "HLEN " + boundRangeKey("names", lower, upper);
    }

    public String crudCommand(JdbcCrudCommand command) {
        switch (command) {
            case SELECT_NAME:
                return selectById("name", "?");
            case SELECT_AGE:
                return selectById("age", "?");
            case UPDATE_AGE:
                return "HSET " + this.prefix + "ages #{arg1} #{arg0}";
            case DELETE:
                return "HDEL " + rangeKey("names", 3) + " ?";
            case COUNT_BY_ID:
                return "HEXISTS " + rangeKey("names", 3) + " ?";
            default:
                throw new IllegalArgumentException("Unsupported query fixture command: " + command);
        }
    }

    private String rangeKey(String kind, int lastRow) {
        return this.prefix + kind + "_" + (this.baseId + 1) + "_" + (this.baseId + lastRow);
    }

    private String boundRangeKey(String kind, String lower, String upper) {
        return "#{'" + this.prefix + kind + "_' + " + argument(lower, 0) + " + '_' + " + argument(upper, 1) + "}";
    }

    private String argument(String marker, int position) {
        if ("?".equals(marker)) {
            return "arg" + position;
        }
        if (marker.startsWith(":")) {
            return marker.substring(1);
        }
        throw new IllegalArgumentException("Unsupported fixture parameter marker: " + marker);
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
