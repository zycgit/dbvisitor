/*
 * Copyright 2015-2022 the original author or authors.
 * Licensed under the Apache License, Version 2.0.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Connections {
    private Connections() {
    }

    public static Connection milvus() throws SQLException {
        Properties props = new Properties();
        props.setProperty("consistencyLevel", "Strong");
        props.setProperty("connectTimeout", "10000");
        props.setProperty("rpcDeadline", "10000");
        copyEnvironment(props, "token", "MILVUS_TOKEN");
        copyEnvironment(props, "secure", "MILVUS_SECURE");
        return DriverManager.getConnection(System.getenv().getOrDefault("MILVUS_JDBC_URL", "jdbc:dbvisitor:milvus://127.0.0.1:19530/default"), props);
    }

    public static Connection redis() throws SQLException {
        Properties props = new Properties();
        copyEnvironment(props, "password", "REDIS_PASSWORD");
        return DriverManager.getConnection(System.getenv().getOrDefault("REDIS_JDBC_URL", "jdbc:dbvisitor:jedis://127.0.0.1:6379?database=0"), props);
    }

    private static void copyEnvironment(Properties props, String key, String variable) {
        String value = System.getenv(variable);
        if (value != null && !value.isBlank()) {
            props.setProperty(key, value);
        }
    }
}
