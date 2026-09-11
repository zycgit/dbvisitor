/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic;
import net.hasor.dbvisitor.driver.JdbcDriver;

public interface ElasticKeys {
    String ADAPTER_NAME        = JdbcDriver.P_ADAPTER_NAME;
    String ADAPTER_NAME_VALUE  = "elastic";
    String START_URL           = JdbcDriver.START_URL + ADAPTER_NAME_VALUE + ":";
    String DEFAULT_CLIENT_NAME = "Elastic-JDBC-Client";

    // for call
    String CUSTOM_ELASTIC        = "customElastic";
    // for client
    String SERVER                = JdbcDriver.P_SERVER;
    String TIME_ZONE             = JdbcDriver.P_TIME_ZONE;
    // auth
    String USERNAME              = JdbcDriver.P_USER;
    String PASSWORD              = JdbcDriver.P_PASSWORD;
    // options
    String CLIENT_NAME           = "clientName";
    String CONN_TIMEOUT          = "connectTimeout"; // milliseconds
    String SO_TIMEOUT            = "socketTimeout"; // milliseconds
    // pre-read options
    String PREREAD_ENABLED       = "preRead";
    String PREREAD_THRESHOLD     = "preReadThreshold"; // MB
    String PREREAD_MAX_FILE_SIZE = "preReadMaxFileSize"; // MB
    String PREREAD_CACHE_DIR     = "preReadCacheDir";
    // index refresh
    String INDEX_REFRESH         = "indexRefresh";
}
