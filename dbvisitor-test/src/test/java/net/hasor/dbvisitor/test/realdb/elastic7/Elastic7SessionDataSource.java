/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.util.Properties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;

/** A JDBC pool using an Elasticsearch request for its connection health check. */
public final class Elastic7SessionDataSource {
    private Elastic7SessionDataSource() {
    }

    public static HikariDataSource open(String environment) {
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
        config.setConnectionTestQuery("GET /");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        return new HikariDataSource(config);
    }
}
