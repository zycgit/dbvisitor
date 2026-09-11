/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.UUID;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.hasor.dbvisitor.test.contract.api.session.SessionCoreContractTest;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;
import org.junit.After;
import org.junit.Before;

/** Reuses Session assertions unchanged; native BM25 supplies the required vector field. */
public class MilvusSessionCoreTest extends SessionCoreContractTest {
    private final String database = "dbv_session_" + UUID.randomUUID().toString().replace("-", "");
    private HikariDataSource sessionSource;
    private Connection admin;
    private boolean databaseCreated;

    @Override
    protected DataSourceProfile profile() {
        return MilvusProfile.INSTANCE;
    }

    @Override
    protected DataSource sessionDataSource() {
        return this.sessionSource;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        OneApiDataSourceManager.assumeCurrentDataSource(profile().env());
        this.admin = OneApiDataSourceManager.getConnection(profile().env());
        try (Statement statement = this.admin.createStatement()) {
            statement.executeUpdate("CREATE DATABASE " + this.database);
            this.databaseCreated = true;
        }

        this.sessionSource = new HikariDataSource(connectionConfig());
        try (Connection connection = this.sessionSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE user_info (
                        id INT64 PRIMARY KEY, name VARCHAR(128) WITH (enable_analyzer=true),
                        age INT32 NULL, email VARCHAR(128) NULL, create_time VARCHAR(128) NULL,
                        v SPARSE_FLOAT_VECTOR, FUNCTION name_vector USING BM25 (name) INTO (v)
                    ) WITH (consistency_level='Strong')
                    """);
            statement.executeUpdate("""
                    CREATE TABLE user_order (
                        id INT64 PRIMARY KEY, user_id INT64, order_no VARCHAR(128) WITH (enable_analyzer=true),
                        amount DOUBLE, create_time VARCHAR(128) NULL,
                        v SPARSE_FLOAT_VECTOR, FUNCTION order_vector USING BM25 (order_no) INTO (v)
                    ) WITH (consistency_level='Strong')
                    """);
            for (String table : new String[] { "user_info", "user_order" }) {
                statement.executeUpdate("CREATE INDEX session_v ON " + table + "(v) USING SPARSE_INVERTED_INDEX WITH (metric_type=BM25)");
                statement.executeUpdate("LOAD TABLE " + table);
            }
        }
    }

    private HikariConfig connectionConfig() {
        Properties properties = OneApiDataSourceManager.loadAdapterProperties(profile().env());
        URI endpoint = URI.create(properties.getProperty("jdbc.url").substring("jdbc:dbvisitor:".length()));
        String query = endpoint.getRawQuery() == null ? "" : "?" + endpoint.getRawQuery();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:dbvisitor:" + endpoint.getScheme() + "://" + endpoint.getRawAuthority() + "/" + this.database + query);
        config.setDriverClassName(properties.getProperty("jdbc.driver"));
        config.setUsername(properties.getProperty("jdbc.username"));
        config.setPassword(properties.getProperty("jdbc.password"));
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("conn.")) {
                config.addDataSourceProperty(key.substring(5), properties.getProperty(key));
            }
        }
        config.setMaximumPoolSize(3);
        config.setMinimumIdle(0);
        // The adapter does not implement Connection.isValid; use its read-only native SQL.
        config.setConnectionTestQuery("SHOW TABLES");
        return config;
    }

    @After
    public void cleanupDatabase() throws SQLException {
        try {
            if (this.sessionSource != null) {
                try (Connection connection = this.sessionSource.getConnection(); Statement statement = connection.createStatement()) {
                    statement.executeUpdate("DROP TABLE IF EXISTS user_order");
                    statement.executeUpdate("DROP TABLE IF EXISTS user_info");
                } finally {
                    this.sessionSource.close();
                }
            }
        } finally {
            if (this.admin != null) {
                try (Connection closing = this.admin; Statement statement = closing.createStatement()) {
                    if (this.databaseCreated) {
                        statement.executeUpdate("DROP DATABASE " + this.database);
                    }
                }
            }
        }
    }
}
