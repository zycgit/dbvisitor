/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.clickhouse;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.material.handler.ResultHandlerProbe;
import net.hasor.dbvisitor.test.nxn.config.NxnDatabaseNames;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import org.junit.rules.ExternalResource;

/** One numbered database per class; only UserInfo scenarios may use this fixture. */
public final class ClickHouseUserInfoFixture extends ExternalResource {
    private String database;
    private Connection admin;
    private HikariDataSource pool;
    private DataSource observed;

    @Override
    protected void before() throws Throwable {
        OneApiDataSourceManager.assumeCurrentDataSource("clickhouse");
        try {
            admin = OneApiDataSourceManager.getConnection("clickhouse");
            database = NxnDatabaseNames.create(admin, "clickhouse");
            Properties properties = OneApiDataSourceManager.loadAdapterProperties("clickhouse");
            URI endpoint = URI.create(properties.getProperty("jdbc.url").substring("jdbc:".length()));
            String query = endpoint.getRawQuery() == null ? "" : "?" + endpoint.getRawQuery();
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:" + endpoint.getScheme() + "://" + endpoint.getRawAuthority() + "/" + database + query);
            config.setUsername(properties.getProperty("jdbc.username"));
            config.setPassword(properties.getProperty("jdbc.password"));
            config.setMaximumPoolSize(2);
            config.setMinimumIdle(0);
            pool = new HikariDataSource(config);
            observed = ResultHandlerProbe.observe(pool);
            new JdbcTemplate(observed).executeUpdate("""
                    CREATE TABLE user_info (
                        id Int32 DEFAULT cityHash64(generateUUIDv4()),
                        name Nullable(String), age Nullable(Int32), email Nullable(String),
                        create_time Nullable(DateTime64(3)) DEFAULT now64(3)
                    ) ENGINE = MergeTree ORDER BY id
                    """);
        } catch (Throwable failure) {
            try {
                after();
            } catch (Throwable cleanup) {
                failure.addSuppressed(cleanup);
            }
            throw failure;
        }
    }

    public DataSource dataSource() {
        return observed;
    }

    public void clearRows() {
        try {
            new JdbcTemplate(observed).executeUpdate("TRUNCATE TABLE user_info");
        } catch (SQLException failure) {
            throw new IllegalStateException("Cannot reset owned ClickHouse database " + database, failure);
        }
    }

    @Override
    protected void after() {
        if (pool != null) {
            pool.close();
            pool = null;
        }
        if (admin != null) {
            try (Connection closing = admin; var statement = closing.createStatement()) {
                if (database != null) {
                    statement.executeUpdate("DROP DATABASE " + database + " SYNC");
                    System.out.println("NxN fixture dropped database " + database);
                }
            } catch (SQLException failure) {
                throw new IllegalStateException("Cannot clean owned ClickHouse database " + database, failure);
            } finally {
                admin = null;
                database = null;
                observed = null;
            }
        }
    }
}
