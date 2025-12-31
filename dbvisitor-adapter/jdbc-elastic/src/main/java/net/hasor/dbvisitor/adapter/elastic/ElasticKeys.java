package net.hasor.dbvisitor.adapter.elastic;

import net.hasor.dbvisitor.driver.JdbcDriver;

public class ElasticKeys {
    public static final String ADAPTER_NAME        = JdbcDriver.P_ADAPTER_NAME;
    public static final String ADAPTER_NAME_VALUE  = "elastic";
    public static final String START_URL           = JdbcDriver.START_URL + ADAPTER_NAME_VALUE + ":";
    public static final String DEFAULT_CLIENT_NAME = "Elastic-JDBC-Client";

    // for call
    public static final String CUSTOM_ELASTIC = "customElastic";

    // for client
    public static final String SERVER    = JdbcDriver.P_SERVER;
    public static final String TIME_ZONE = JdbcDriver.P_TIME_ZONE;

    // auth
    public static final String USERNAME = JdbcDriver.P_USER;
    public static final String PASSWORD = JdbcDriver.P_PASSWORD;

    // options
    public static final String CLIENT_NAME  = "clientName";
    public static final String CONN_TIMEOUT = "connectTimeout"; // milliseconds
    public static final String SO_TIMEOUT   = "socketTimeout"; // milliseconds

    // pre-read options
    public static final String PREREAD_ENABLED       = "preRead";
    public static final String PREREAD_THRESHOLD     = "preReadThreshold"; // MB
    public static final String PREREAD_MAX_FILE_SIZE = "preReadMaxFileSize"; // MB
    public static final String PREREAD_CACHE_DIR     = "preReadCacheDir";

    // index refresh
    public static final String INDEX_REFRESH = "indexRefresh";
}
