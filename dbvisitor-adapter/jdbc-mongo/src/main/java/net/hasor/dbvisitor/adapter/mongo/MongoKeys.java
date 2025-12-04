package net.hasor.dbvisitor.adapter.mongo;
import net.hasor.dbvisitor.driver.JdbcDriver;

public class MongoKeys {
    public static final String ADAPTER_NAME        = JdbcDriver.P_ADAPTER_NAME;
    public static final String ADAPTER_NAME_VALUE  = "mongo";
    public static final String START_URL           = JdbcDriver.START_URL + ADAPTER_NAME_VALUE + ":";
    public static final String DEFAULT_CLIENT_NAME = "Mongo-JDBC-Client";

    // for call
    public static final String INTERCEPTOR  = "interceptor";
    public static final String CUSTOM_MONGO = "customMongo";

    // for client
    public static final String SERVER                   = JdbcDriver.P_SERVER;
    public static final String TIME_ZONE                = JdbcDriver.P_TIME_ZONE;
    // auth
    public static final String USERNAME                 = "username";
    public static final String PASSWORD                 = "password";
    public static final String MECHANISM                = "mechanism";
    public static final String DATABASE                 = "database";
    // options
    public static final String CLIENT_NAME              = "clientName";
    public static final String CLIENT_DESCRIPTION       = "clientDescription";
    public static final String CONN_TIMEOUT             = "connectTimeout";         // milliseconds
    public static final String SO_TIMEOUT               = "socketTimeout";          // milliseconds
    public static final String SO_KEEP_ALIVE            = "socketKeepAlive";
    public static final String SERVER_SELECTION_TIMEOUT = "serverSelectionTimeout";
    public static final String MAX_WAIT_TIME            = "maxWaitTime";
    public static final String MAX_CONNECTION_IDLE_TIME = "maxConnectionIdleTime";
    public static final String MAX_CONNECTION_LIFE_TIME = "maxConnectionLifeTime";
    public static final String MIN_CONNECTIONS_PER_HOST = "minConnectionsPerHost";
    public static final String MAX_CONNECTIONS_PER_HOST = "maxConnectionsPerHost";
    public static final String RETRY_WRITES             = "retryWrites";
    public static final String RETRY_READS              = "retryReads";
    public static final String THREADS_ALLOWED_BLOCK    = "threadsAllowedToBlockForConnectionMultiplier";
}
