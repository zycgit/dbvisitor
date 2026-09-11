/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.mongo;
import net.hasor.dbvisitor.driver.JdbcDriver;

public interface MongoKeys {
    String ADAPTER_NAME        = JdbcDriver.P_ADAPTER_NAME;
    String ADAPTER_NAME_VALUE  = "mongo";
    String START_URL           = JdbcDriver.START_URL + ADAPTER_NAME_VALUE + ":";
    String DEFAULT_CLIENT_NAME = "Mongo-JDBC-Client";

    // for call
    String CUSTOM_MONGO          = "customMongo";
    // for client
    String SERVER                = JdbcDriver.P_SERVER;
    String TIME_ZONE             = JdbcDriver.P_TIME_ZONE;
    String DATABASE              = "database";
    // auth
    String USERNAME              = JdbcDriver.P_USER;
    String PASSWORD              = JdbcDriver.P_PASSWORD;
    String MECHANISM             = "mechanism";
    // options
    String CLIENT_NAME           = "clientName";
    String CONN_TIMEOUT          = "connectTimeout";         // milliseconds
    String SO_TIMEOUT            = "socketTimeout";          // milliseconds
    String SO_SND_BUFF           = "socketSndBuffer";
    String SO_RCV_BUFF           = "socketRcvBuffer";
    String RETRY_WRITES          = "retryWrites";
    String RETRY_READS           = "retryReads";
    // pre-read options
    String PREREAD_ENABLED       = "preRead";
    String PREREAD_THRESHOLD     = "preReadThreshold"; // MB
    String PREREAD_MAX_FILE_SIZE = "preReadMaxFileSize"; // MB
    String PREREAD_CACHE_DIR     = "preReadCacheDir";
}
