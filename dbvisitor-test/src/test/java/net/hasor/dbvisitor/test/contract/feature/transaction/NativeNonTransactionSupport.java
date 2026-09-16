/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.transaction;

import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import org.junit.After;
import org.junit.Before;

/** Native commands run through the same pool as their propagation scope. */
public abstract class NativeNonTransactionSupport extends NonTransactionPropagationCase {
    private final String           name = "nxn_no_tx_" + UUID.randomUUID().toString().replace("-", "");
    private       HikariDataSource pool;
    private       boolean          created;

    @Override
    @Before
    public void setup() throws SQLException {
        String environment = profile().env();
        OneApiDataSourceManager.assumeCurrentDataSource(environment);
        Properties properties = OneApiDataSourceManager.loadAdapterProperties(environment);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("jdbc.url"));
        config.setDriverClassName(properties.getProperty("jdbc.driver"));
        config.setUsername(properties.getProperty("jdbc.username"));
        config.setPassword(properties.getProperty("jdbc.password"));
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("conn.")) {
                config.addDataSourceProperty(key.substring(5), properties.getProperty(key));
            }
        }
        switch (environment) {
            case "redis":
                config.setConnectionTestQuery("PING");
                break;
            case "mongo":
                config.setConnectionTestQuery("test.runCommand({ping: 1})");
                break;
            case "milvus":
                config.setConnectionTestQuery("SHOW DATABASES");
                break;
            default:
                config.setConnectionTestQuery("GET /");
                break;
        }
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(1);
        this.pool = new HikariDataSource(config);
        this.jdbcTemplate = new JdbcTemplate(this.pool);
        switch (environment) {
            case "redis":
                this.created = true;
                break;
            case "mongo":
                jdbcTemplate.execute("test.createCollection('" + this.name + "')");
                this.created = true;
                break;
            case "milvus":
                jdbcTemplate.executeUpdate("CREATE TABLE " + this.name + " (id INT64 PRIMARY KEY, name VARCHAR(128), v FLOAT_VECTOR(2)) WITH (consistency_level=Strong)");
                this.created = true;
                jdbcTemplate.executeUpdate("CREATE INDEX no_tx_v ON " + this.name + "(v) USING FLAT WITH (metric_type=L2)");
                jdbcTemplate.executeUpdate("LOAD TABLE " + this.name);
                break;
            default:
                jdbcTemplate.execute("PUT /" + this.name);
                this.created = true;
                break;
        }
    }

    @Override
    protected void insertUser(int id, String value) throws SQLException {
        switch (profile().env()) {
            case "redis":
                jdbcTemplate.executeUpdate("HSET " + this.name + " ? ?", new Object[] { id, value });
                break;
            case "mongo":
                jdbcTemplate.executeUpdate("test." + this.name + ".insert({id: ?, name: ?})", new Object[] { id, value });
                break;
            case "milvus":
                jdbcTemplate.executeUpdate("INSERT INTO " + this.name + " (id, name, v) VALUES (?, ?, ?)", new Object[] { id, value, new float[] { 1F, 0F } });
                break;
            default:
                jdbcTemplate.executeUpdate("POST /" + this.name + "/_doc {\"id\": ?, \"name\": ?}", new Object[] { id, value });
                break;
        }
    }

    @Override
    protected long countById(int id) throws SQLException {
        switch (profile().env()) {
            case "redis":
                return jdbcTemplate.queryForLong("HEXISTS " + this.name + " ?", new Object[] { id });
            case "mongo":
                return jdbcTemplate.queryForLong("test." + this.name + ".count({id: ?})", new Object[] { id });
            case "milvus":
                return jdbcTemplate.queryForLong("SELECT COUNT(*) FROM " + this.name + " WHERE id = ?", new Object[] { id });
            default:
                jdbcTemplate.execute("POST /" + this.name + "/_refresh");
                return jdbcTemplate.queryForLong("POST /" + this.name + "/_count {\"query\": {\"term\": {\"id\": ?}}}", new Object[] { id });
        }
    }

    @After
    public void cleanupNativeData() throws SQLException {
        if (this.pool == null) {
            return;
        }
        try {
            if (this.created) {
                switch (profile().env()) {
                    case "redis":
                        jdbcTemplate.executeUpdate("DEL " + this.name);
                        break;
                    case "mongo":
                        jdbcTemplate.execute("test." + this.name + ".drop()");
                        break;
                    case "milvus":
                        jdbcTemplate.executeUpdate("DROP TABLE " + this.name);
                        break;
                    default:
                        jdbcTemplate.execute("DELETE /" + this.name);
                        break;
                }
            }
        } finally {
            this.pool.close();
        }
    }
}
