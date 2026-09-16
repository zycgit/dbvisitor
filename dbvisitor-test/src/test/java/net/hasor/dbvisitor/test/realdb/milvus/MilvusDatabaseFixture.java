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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import net.hasor.dbvisitor.driver.JdbcDriver;
import net.hasor.dbvisitor.test.contract.material.handler.ResultHandlerProbe;
import net.hasor.dbvisitor.test.nxn.config.NxnDatabaseNames;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.MilvusProfile;

/** Owns one isolated database on the configured endpoint; callers own their collection schemas. */
final class MilvusDatabaseFixture implements AutoCloseable {
    private String database;
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
        try {
            this.database = NxnDatabaseNames.create(this.admin, env);
            this.databaseCreated = true;
            this.connection = connectToDatabase(env);
        } catch (SQLException failure) {
            try {
                close();
            } catch (SQLException cleanup) {
                failure.addSuppressed(cleanup);
            }
            throw failure;
        }
        return this.connection;
    }

    Connection newConnection() throws SQLException {
        if (!this.databaseCreated) {
            throw new SQLException("The fixture database has not been created.");
        }
        return connectToDatabase(MilvusProfile.INSTANCE.env());
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
        return ResultHandlerProbe.observe(DriverManager.getConnection(url, properties));
    }

    @Override
    public void close() throws SQLException {
        SQLException failure = null;
        try {
            if (this.databaseCreated) {
                // Milvus cannot drop a non-empty database. Clean only the database we created,
                // including collections left behind when setup or a test failed.
                if (this.connection == null || this.connection.isClosed()) {
                    this.connection = connectToDatabase(MilvusProfile.INSTANCE.env());
                }
                try (Statement statement = this.connection.createStatement()) {
                    List<String> tables = new ArrayList<>();
                    try (var rows = statement.executeQuery("SHOW TABLES")) {
                        while (rows.next()) {
                            tables.add(rows.getString("TABLE"));
                        }
                    }
                    for (String table : tables) {
                        if (!table.matches("[a-zA-Z_][a-zA-Z0-9_.-]*")) {
                            throw new SQLException("Cannot safely address fixture collection: " + table);
                        }
                        statement.executeUpdate("DROP TABLE IF EXISTS " + table);
                    }
                }
                try (Statement statement = this.admin.createStatement()) {
                    statement.executeUpdate("DROP DATABASE " + this.database);
                    this.databaseCreated = false;
                    System.out.println("NxN fixture dropped database " + this.database);
                }
            }
        } catch (SQLException cleanup) {
            failure = new SQLException("Cannot clean owned Milvus database " + this.database, cleanup);
        } finally {
            for (Connection closing : new Connection[] { this.connection, this.admin }) {
                if (closing != null) {
                    try {
                        closing.close();
                    } catch (SQLException closeFailure) {
                        if (failure == null) {
                            failure = closeFailure;
                        } else {
                            failure.addSuppressed(closeFailure);
                        }
                    }
                }
            }
            this.connection = null;
            this.admin = null;
            this.databaseCreated = false;
        }
        if (failure != null) {
            throw failure;
        }
    }
}
