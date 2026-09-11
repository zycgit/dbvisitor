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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.UUID;
import net.hasor.dbvisitor.driver.JdbcDriver;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;

/** Owns one isolated database on the configured endpoint; callers own their collection schemas. */
final class MilvusDatabaseFixture implements AutoCloseable {
    private final String database = "dbv_contract_" + UUID.randomUUID().toString().replace("-", "");
    private Connection admin;
    private Connection connection;
    private boolean databaseCreated;

    Connection open() throws SQLException {
        if (this.connection != null) {
            return this.connection;
        }
        String env = MilvusProfile.INSTANCE.env();
        OneApiDataSourceManager.assumeCurrentDataSource(env);
        this.admin = OneApiDataSourceManager.getConnection(env);
        try (Statement statement = this.admin.createStatement()) {
            statement.executeUpdate("CREATE DATABASE " + this.database);
            this.databaseCreated = true;
        }
        this.connection = connectToDatabase(env);
        return this.connection;
    }

    private Connection connectToDatabase(String env) throws SQLException {
        Properties fixture = OneApiDataSourceManager.loadAdapterProperties(env);
        URI endpoint = URI.create(fixture.getProperty("jdbc.url").substring("jdbc:dbvisitor:".length()));
        String query = endpoint.getRawQuery() == null ? "" : "?" + endpoint.getRawQuery();
        String url = "jdbc:dbvisitor:" + endpoint.getScheme() + "://" + endpoint.getRawAuthority() + "/" + this.database + query;
        Properties properties = new Properties();
        if (fixture.getProperty("jdbc.username") != null) {
            properties.setProperty(JdbcDriver.P_USER, fixture.getProperty("jdbc.username"));
        }
        if (fixture.getProperty("jdbc.password") != null) {
            properties.setProperty(JdbcDriver.P_PASSWORD, fixture.getProperty("jdbc.password"));
        }
        for (String key : fixture.stringPropertyNames()) {
            if (key.startsWith("conn.")) {
                properties.setProperty(key.substring(5), fixture.getProperty(key));
            }
        }
        return DriverManager.getConnection(url, properties);
    }

    @Override
    public void close() throws SQLException {
        try {
            if (this.connection != null) {
                this.connection.close();
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
