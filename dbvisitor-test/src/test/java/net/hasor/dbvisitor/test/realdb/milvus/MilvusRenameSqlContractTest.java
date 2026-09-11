/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.net.URI;
import java.sql.*;
import java.util.Properties;
import java.util.UUID;
import net.hasor.dbvisitor.driver.JdbcDriver;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusRenameSqlContractTest extends MilvusSqlContractSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_RENAME)
    public void localRenameShouldPreserveDataAndIndexWithoutChangingCatalog() throws SQLException {
        String renamed = this.collection + "_renamed";
        String catalog = this.connection.getCatalog();
        prepareRow();
        try (Statement statement = this.connection.createStatement()) {
            try {
                assertEquals(0, statement.executeUpdate("ALTER TABLE " + this.collection + " RENAME TO " + renamed));
                assertEquals(catalog, this.connection.getCatalog());
                assertThrows(SQLException.class, () -> statement.executeQuery("SHOW TABLE " + this.collection));
                statement.executeUpdate("LOAD TABLE " + renamed);
                assertRow(statement, renamed, "before");
                statement.executeUpdate("RELEASE TABLE " + renamed);
                assertEquals(0, statement.executeUpdate("ALTER TABLE " + renamed + " RENAME TO " + this.collection));
                loadCollection();
                assertRow(statement, this.collection, "before");
            } finally {
                statement.executeUpdate("DROP TABLE IF EXISTS " + renamed);
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SQL_RENAME_DATABASE)
    public void crossDatabaseRenameShouldPreserveDataAndPermitMovingBack() throws SQLException {
        String database = "dbv_move_" + UUID.randomUUID().toString().replace("-", "");
        String moved = this.collection + "_moved";
        String sourceCatalog = this.connection.getCatalog();
        prepareRow();
        try (Statement source = this.connection.createStatement()) {
            source.executeUpdate("CREATE DATABASE " + database);
            try {
                try (Connection destination = connectToDatabase(database); Statement target = destination.createStatement()) {
                    try {
                        assertEquals(0, source.executeUpdate("ALTER TABLE " + this.collection + " RENAME TO " + moved + " IN DATABASE " + database));
                        assertEquals(sourceCatalog, this.connection.getCatalog());
                        assertEquals(database, destination.getCatalog());
                        assertThrows(SQLException.class, () -> source.executeQuery("SHOW TABLE " + this.collection));
                        target.executeUpdate("LOAD TABLE " + moved);
                        assertRow(target, moved, "before");
                        assertEquals(1, target.executeUpdate("UPDATE " + moved + " SET name='after' WHERE id=1"));
                        target.executeUpdate("FLUSH " + moved);
                        target.executeUpdate("RELEASE TABLE " + moved);

                        assertEquals(0, target.executeUpdate("ALTER TABLE " + moved + " RENAME TO " + this.collection + " IN DATABASE " + sourceCatalog));
                        assertEquals(database, destination.getCatalog());
                        assertEquals(sourceCatalog, this.connection.getCatalog());
                        assertThrows(SQLException.class, () -> target.executeQuery("SHOW TABLE " + moved));
                        loadCollection();
                        assertRow(source, this.collection, "after");
                    } finally {
                        target.executeUpdate("DROP TABLE IF EXISTS " + moved);
                    }
                }
            } finally {
                source.executeUpdate("DROP DATABASE " + database);
            }
        }
    }

    private void prepareRow() throws SQLException {
        createCollection("id INT64 PRIMARY KEY, name VARCHAR(128), v FLOAT_VECTOR(2)");
        createIndex("v", "FLAT", "L2");
        this.jdbcTemplate.executeUpdate("INSERT INTO " + this.collection + " (id,name,v) VALUES (1,'before',[1,0])");
        this.jdbcTemplate.executeUpdate("FLUSH " + this.collection);
    }

    private void assertRow(Statement statement, String collection, String name) throws SQLException {
        // A search after each rename also verifies that the existing vector index remains usable.
        try (ResultSet result = statement.executeQuery("SELECT id,name FROM " + collection + " ORDER BY v <-> [1,0] LIMIT 1")) {
            assertTrue(result.next());
            assertEquals(1L, result.getLong("id"));
            assertEquals(name, result.getString("name"));
            assertFalse(result.next());
        }
    }

    private Connection connectToDatabase(String database) throws SQLException {
        Properties fixture = OneApiDataSourceManager.loadAdapterProperties(profile().env());
        URI endpoint = URI.create(fixture.getProperty("jdbc.url").substring("jdbc:dbvisitor:".length()));
        String query = endpoint.getRawQuery() == null ? "" : "?" + endpoint.getRawQuery();
        String url = "jdbc:dbvisitor:" + endpoint.getScheme() + "://" + endpoint.getRawAuthority() + "/" + database + query;
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
}
