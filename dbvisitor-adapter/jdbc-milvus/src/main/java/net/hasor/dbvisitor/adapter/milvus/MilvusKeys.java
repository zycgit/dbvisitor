/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus;
import net.hasor.dbvisitor.driver.JdbcDriver;

/** JDBC connection property names only; command options belong to MilvusCommandKeys. */
public interface MilvusKeys {
    // JDBC connection: customization and address
    String ADAPTER_NAME  = JdbcDriver.P_ADAPTER_NAME;
    String INTERCEPTOR   = "interceptor";
    String CUSTOM_MILVUS = "customMilvus";
    String SERVER        = JdbcDriver.P_SERVER;
    String TIME_ZONE     = JdbcDriver.P_TIME_ZONE;
    String DATABASE      = "database";

    // JDBC connection: authentication
    String TOKEN    = "token";
    String USERNAME = JdbcDriver.P_USER;
    String PASSWORD = JdbcDriver.P_PASSWORD;

    // JDBC connection: TLS (PEM names match the official SDK)
    String SECURE          = "secure";
    String CA_PEM_PATH     = "caPemPath";
    String SERVER_PEM_PATH = "serverPemPath";
    String CLIENT_PEM_PATH = "clientPemPath";
    String CLIENT_KEY_PATH = "clientKeyPath";
    String SERVER_NAME     = "serverName";

    // JDBC connection: transport, retries and query defaults
    String CONNECT_TIMEOUT          = "connectTimeout";
    String KEEP_ALIVE_TIME          = "keepAliveTime";
    String KEEP_ALIVE_TIMEOUT       = "keepAliveTimeout";
    String KEEP_ALIVE_WITHOUT_CALLS = "keepAliveWithoutCalls";
    String IDLE_TIMEOUT             = "idleTimeout";
    String RPC_DEADLINE             = "rpcDeadline";
    String MAX_RETRY                = "maxRetry";
    String CONSISTENCY_LEVEL        = "consistencyLevel";
}
