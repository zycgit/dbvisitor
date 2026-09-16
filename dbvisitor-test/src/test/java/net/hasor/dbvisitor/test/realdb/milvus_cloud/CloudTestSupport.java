/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus_cloud;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;

/** Optional Cloud checks; no NxN profile, database creation or capability export. */
public abstract class CloudTestSupport {
    public static final String ENDPOINT = "MILVUS_CLOUD_ENDPOINT";
    public static final String DATABASE = "MILVUS_CLOUD_DATABASE";
    public static final String TOKEN = "MILVUS_CLOUD_TOKEN";
    public static final String USER = "MILVUS_CLOUD_USER";
    public static final String PASSWORD = "MILVUS_CLOUD_PASSWORD";

    protected Connection connection;
    protected JdbcTemplate jdbc;
    private final List<String> collections = new ArrayList<>();

    @BeforeClass
    public static void requireExplicitOptIn() {
        Assume.assumeTrue("Cloud tests require -Dmilvus.cloud=true (Gradle: -Pmilvus.cloud=true)", Boolean.getBoolean("milvus.cloud"));
    }

    @Before
    public void openConnection() throws SQLException {
        Map<String, String> env = System.getenv();
        this.connection = DriverManager.getConnection(jdbcUrl(env), connectionProperties(env));
        this.jdbc = new JdbcTemplate(this.connection);
    }

    public static String jdbcUrl(Map<String, String> env) {
        String address = required(env, ENDPOINT);
        URI endpoint;
        try {
            endpoint = URI.create(address);
        } catch (IllegalArgumentException e) {
            // Do not include a possibly credential-bearing input or cause in diagnostics.
            throw new IllegalArgumentException(ENDPOINT + " must be an HTTPS endpoint without credentials or a database path.");
        }
        String path = endpoint.getRawPath();
        if (!"https".equalsIgnoreCase(endpoint.getScheme()) || endpoint.getHost() == null || endpoint.getUserInfo() != null
                || endpoint.getRawQuery() != null || endpoint.getRawFragment() != null
                || (path != null && !path.isEmpty() && !"/".equals(path)) || endpoint.getPort() == 0) {
            throw new IllegalArgumentException(ENDPOINT + " must be an HTTPS endpoint without credentials or a database path.");
        }
        String database = required(env, DATABASE);
        if (!database.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException(DATABASE + " must be a database name, not a URL or path.");
        }
        int port = endpoint.getPort() < 0 ? 443 : endpoint.getPort();
        if (port > 65535) {
            throw new IllegalArgumentException(ENDPOINT + " contains an invalid port.");
        }
        return "jdbc:dbvisitor:milvus://" + endpoint.getHost() + ":" + port + "/" + database;
    }

    public static Properties connectionProperties(Map<String, String> env) {
        String token = env.get(TOKEN);
        String user = env.get(USER);
        String password = env.get(PASSWORD);
        boolean hasToken = token != null && !token.isBlank();
        boolean hasUser = user != null && !user.isBlank();
        boolean hasPassword = password != null && !password.isEmpty();
        if (hasUser != hasPassword || (!hasToken && !hasUser)) {
            throw new IllegalArgumentException("Set " + TOKEN + " or both " + USER + " and " + PASSWORD + ".");
        }
        Properties props = new Properties();
        props.setProperty(MilvusKeys.SECURE, "true");
        props.setProperty(MilvusKeys.CONNECT_TIMEOUT, "20000");
        props.setProperty(MilvusKeys.RPC_DEADLINE, "120000");
        props.setProperty(MilvusKeys.MAX_RETRY, "0");
        props.setProperty(MilvusKeys.CONSISTENCY_LEVEL, "Strong");
        if (hasToken) {
            props.setProperty(MilvusKeys.TOKEN, token);
        }
        if (hasUser) {
            props.setProperty(MilvusKeys.USERNAME, user);
            props.setProperty(MilvusKeys.PASSWORD, password);
        }
        return props;
    }

    private static String required(Map<String, String> env, String key) {
        String value = env.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing environment variable: " + key);
        }
        return value.trim();
    }

    protected String createCollection(String fields, String metric) throws SQLException {
        String table = "dbv_cloud_" + UUID.randomUUID().toString().replace("-", "");
        // Register before CREATE: a timed-out response does not prove the operation failed.
        this.collections.add(table);
        this.jdbc.execute("CREATE TABLE " + table + " (" + fields + ") WITH (consistency_level='Strong')");
        this.jdbc.execute("CREATE INDEX idx_v ON " + table + "(v) USING AUTOINDEX WITH (metric_type=" + metric + ")");
        this.jdbc.execute("LOAD TABLE " + table);
        return table;
    }

    protected static List<Long> readIds(ResultSet result) throws SQLException {
        List<Long> ids = new ArrayList<>();
        while (result.next()) {
            ids.add(result.getLong("id"));
        }
        return ids;
    }

    @After
    public void closeConnection() throws SQLException {
        if (this.connection == null) {
            return;
        }
        SQLException failure = null;
        for (String table : this.collections) {
            try {
                this.jdbc.execute("DROP TABLE IF EXISTS " + table);
            } catch (Exception e) {
                SQLException cleanup = new SQLException("Cloud cleanup failed for test collection " + table, e);
                if (failure == null) {
                    failure = cleanup;
                } else {
                    failure.addSuppressed(cleanup);
                }
            }
        }
        try {
            this.connection.close();
        } catch (SQLException e) {
            if (failure == null) {
                failure = e;
            } else {
                failure.addSuppressed(e);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }
}
