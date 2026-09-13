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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Assume;

import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;

/** Stores each logical column in a private Redis hash, keyed by the test row ID. */
final class RedisTypeCommandFixture implements AutoCloseable {
    private final String prefix = "nxn:redis:type-case:" + UUID.randomUUID() + ":";
    private final Set<String> keys = new LinkedHashSet<>();
    private Connection connection;
    private JdbcTemplate template;

    JdbcTemplate open() throws SQLException {
        Assume.assumeTrue("Redis type contract", "redis".equals(OneApiDataSourceManager.getDbDialect()));
        if (this.connection == null) {
            this.connection = OneApiDataSourceManager.getConnection("redis");
            this.template = new JdbcTemplate(this.connection);
        }
        return this.template;
    }

    String insertCommand(String table, String columns, String... parameters) {
        String[] fields = columns.split(",");
        if (fields.length != parameters.length || !"id".equals(fields[0].trim())) {
            throw new IllegalArgumentException("Expected an ID followed by matching column parameters.");
        }
        StringBuilder script = new StringBuilder();
        for (int i = 1; i < fields.length; i++) {
            script.append("redis.call(\"HSET\",\"").append(key(table, fields[i].trim()))
                    .append("\",ARGV[1],ARGV[").append(i + 1).append("]); ");
        }
        script.append("return 1");
        return "EVAL '" + script + "' 0 " + String.join(" ", parameters);
    }

    String selectCommand(String table, String column) {
        if (column.contains(",")) {
            throw new IllegalArgumentException("Redis scalar type reads require one column.");
        }
        return "EVAL 'return redis.call(\"HGET\",\"" + key(table, column.trim()) + "\",ARGV[1])' 0 ?";
    }

    private String key(String table, String column) {
        String key = this.prefix + table + ":" + column;
        this.keys.add(key);
        return key;
    }

    @Override
    public void close() throws SQLException {
        try {
            if (this.template != null) {
                for (String key : this.keys) {
                    this.template.executeUpdate("DEL ?", key);
                }
            }
        } finally {
            if (this.connection != null) {
                this.connection.close();
                this.connection = null;
                this.template = null;
            }
        }
    }
}
