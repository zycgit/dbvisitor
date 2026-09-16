/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus_cloud;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import org.junit.Test;
import static org.junit.Assert.*;

public class CloudConnectionTest extends CloudTestSupport {
    @Test
    public void configuredAuthenticationShouldConnectToTheExplicitDatabase() throws Exception {
        Map<String, String> env = System.getenv();
        assertEquals(env.get(DATABASE).trim(), this.connection.getCatalog());
        try (Statement statement = this.connection.createStatement(); ResultSet result = statement.executeQuery("SHOW TABLES")) {
            assertNotNull(result.getMetaData());
        }
        Properties properties = connectionProperties(env);
        // When both credentials are provided, verify password authentication independently of token precedence.
        if (properties.containsKey(MilvusKeys.TOKEN) && properties.containsKey(MilvusKeys.USERNAME)) {
            properties.remove(MilvusKeys.TOKEN);
            try (Connection passwordConnection = DriverManager.getConnection(jdbcUrl(env), properties);
                    Statement statement = passwordConnection.createStatement(); ResultSet result = statement.executeQuery("SHOW TABLES")) {
                assertEquals(this.connection.getCatalog(), passwordConnection.getCatalog());
                assertNotNull(result.getMetaData());
            }
        }
    }

    @Test
    public void jdbcVersionShouldPreserveTheServerDescription() throws Exception {
        try (Statement statement = this.connection.createStatement(); ResultSet result = statement.executeQuery("SHOW VERSION")) {
            assertTrue(result.next());
            String version = result.getString("VERSION");
            assertNotNull(version);
            assertFalse(version.isBlank());
            assertEquals(version, this.connection.getMetaData().getDatabaseProductVersion());
        }
    }

    @Test
    public void catalogsAndPoolValidationShouldUseTheConfiguredDatabase() throws Exception {
        boolean found = false;
        try (ResultSet catalogs = this.connection.getMetaData().getCatalogs()) {
            while (catalogs.next()) {
                found |= this.connection.getCatalog().equals(catalogs.getString("TABLE_CAT"));
            }
        }
        assertTrue(found);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl(System.getenv()));
        config.setDataSourceProperties(connectionProperties(System.getenv()));
        config.setConnectionTestQuery("SHOW TABLES");
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(0);
        try (HikariDataSource pool = new HikariDataSource(config); Connection pooled = pool.getConnection();
                Statement statement = pooled.createStatement(); ResultSet result = statement.executeQuery("SHOW TABLES")) {
            assertEquals(this.connection.getCatalog(), pooled.getCatalog());
            assertNotNull(result.getMetaData());
        }
    }
}
