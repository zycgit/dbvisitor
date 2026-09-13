/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;

/** Owns only private keys; Redis commands remain native throughout the tests. */
public final class RedisJdbcFixture implements AutoCloseable {
    private final String prefix = "nxn:jdbc:" + UUID.randomUUID() + ":";
    private final Set<String> keys = new LinkedHashSet<>();
    private Connection connection;
    private JdbcTemplate jdbc;

    public JdbcTemplate open() throws SQLException {
        OneApiDataSourceManager.assumeCurrentDataSource("redis");
        this.connection = OneApiDataSourceManager.getConnection("redis");
        this.jdbc = new JdbcTemplate(this.connection);
        return this.jdbc;
    }

    public Connection connection() {
        return this.connection;
    }

    public String key(String suffix) {
        String key = this.prefix + suffix;
        this.keys.add(key);
        return key;
    }

    public void seedScores() throws SQLException {
        for (int i = 1; i <= 10; i++) {
            this.jdbc.queryForLong("ZADD ? ? ?", new Object[] { key("scores"), 20 + i, "member-" + i });
        }
    }

    @Override
    public void close() throws SQLException {
        if (this.connection != null) {
            try {
                for (String key : this.keys) {
                    this.jdbc.executeUpdate("DEL ?", new Object[] { key });
                }
            } finally {
                this.connection.close();
            }
        }
    }

    public static class ScoredMember {
        private String element;
        private Double score;
        public String getElement() {
            return this.element;
        }
        public void setElement(String element) {
            this.element = element;
        }
        public Double getScore() {
            return this.score;
        }
        public void setScore(Double score) {
            this.score = score;
        }
    }

    public static class IgnoredScore {
        private String element;
        @net.hasor.dbvisitor.mapping.Ignore
        private Double score;
        public String getElement() {
            return this.element;
        }
        public void setElement(String element) {
            this.element = element;
        }
        public Double getScore() {
            return this.score;
        }
        public void setScore(Double score) {
            this.score = score;
        }
    }
}
