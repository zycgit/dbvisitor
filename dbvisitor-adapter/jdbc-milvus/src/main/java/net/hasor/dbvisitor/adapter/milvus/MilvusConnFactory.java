/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus;
import java.lang.reflect.InvocationHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import javax.net.ssl.SSLContext;
import io.milvus.param.ServerAddress;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.client.RetryConfig;
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.adapter.milvus.transport.MilvusTls;
import net.hasor.dbvisitor.driver.*;

public class MilvusConnFactory implements AdapterFactory {
    private static final String ADAPTER_NAME_VALUE = "milvus";
    private static final String START_URL          = JdbcDriver.START_URL + ADAPTER_NAME_VALUE + ":";

    @Override
    public String getAdapterName() {
        return ADAPTER_NAME_VALUE;
    }

    @Override
    public String[] getPropertyNames() {
        // @formatter:off
        return new String[] {
            MilvusKeys.ADAPTER_NAME, MilvusKeys.INTERCEPTOR, MilvusKeys.CUSTOM_MILVUS, MilvusKeys.SERVER,
            MilvusKeys.TIME_ZONE, MilvusKeys.DATABASE, MilvusKeys.TOKEN, MilvusKeys.USERNAME, MilvusKeys.PASSWORD,
            MilvusKeys.CONNECT_TIMEOUT, MilvusKeys.KEEP_ALIVE_TIME, MilvusKeys.KEEP_ALIVE_TIMEOUT,
            MilvusKeys.KEEP_ALIVE_WITHOUT_CALLS, MilvusKeys.IDLE_TIMEOUT, MilvusKeys.RPC_DEADLINE,
            MilvusKeys.MAX_RETRY, MilvusKeys.CONSISTENCY_LEVEL,
            MilvusKeys.SECURE, MilvusKeys.CA_PEM_PATH,
            MilvusKeys.SERVER_PEM_PATH, MilvusKeys.CLIENT_PEM_PATH, MilvusKeys.CLIENT_KEY_PATH, MilvusKeys.SERVER_NAME
        };
        // @formatter:on
    }

    @Override
    public TypeSupport createTypeSupport(Properties properties) {
        AdapterTypeSupport types = new AdapterTypeSupport(properties);
        types.addTypeMappingTo(AdapterType.Array, java.sql.Types.ARRAY, java.util.List.class);
        types.addTypeMappingTo("JSON", java.sql.Types.OTHER, com.google.gson.JsonElement.class);
        types.addTypeMappingTo("SPARSE_FLOAT_VECTOR", java.sql.Types.OTHER, java.util.SortedMap.class);
        return types;
    }

    @Override
    public AdapterConnection createConnection(Connection owner, String jdbcUrl, Properties properties) throws SQLException {
        if (!StringUtils.startsWithIgnoreCase(jdbcUrl, START_URL)) {
            throw new SQLException("jdbcUrl is not a valid milvus url.");
        }

        Map<String, String> caseProps = new LinkedCaseInsensitiveMap<>();
        if (properties != null) {
            properties.forEach((k, v) -> caseProps.put(k.toString(), v.toString()));
        }
        // Preserve URL precedence after case normalization, including secure/SECURE aliases.
        JdbcDriver.parseURL(jdbcUrl, new Properties()).forEach((k, v) -> caseProps.put(k.toString(), v.toString()));

        String host = StringUtils.trimToEmpty(caseProps.get(MilvusKeys.SERVER));
        if (host.contains("/")) {
            host = host.substring(0, host.indexOf("/"));
        }
        String customMilvus = caseProps.get(MilvusKeys.CUSTOM_MILVUS);
        String defaultDB = extractPathFromJdbcUrl(jdbcUrl, host);
        if (StringUtils.isBlank(defaultDB)) {
            defaultDB = caseProps.get(MilvusKeys.DATABASE);
        }

        MilvusClientV2 client = null;
        try {
            ServerAddress server = parseServerAddress(host);
            ConnectConfig config = buildV2Config(defaultDB, caseProps, server);
            // Validate REST TLS settings before opening the SDK connection.
            SSLContext httpTls = MilvusTls.httpContext(config);
            InvocationHandler invocation = this.createInvocation(caseProps);
            if (StringUtils.isNotBlank(customMilvus)) {
                Class<?> customClass = MilvusConnFactory.class.getClassLoader().loadClass(customMilvus);
                CustomMilvus factory = ClassUtils.newInstance(customClass.asSubclass(CustomMilvus.class));
                client = factory.createMilvusClient(jdbcUrl, caseProps);
                if (client == null) {
                    throw new SQLException("create Milvus connection failed, custom Milvus return null.");
                }
            } else {
                client = new MilvusClientV2(config);
            }
            // Driver maxRetry owns write retries; avoid nested SDK retry loops.
            client.retryConfig(RetryConfig.builder().maxRetryTimes(1).build());
            MilvusCmd command = new MilvusCmd(client, defaultDB, invocation, config, httpTls);
            MilvusConn connection = new MilvusConn(owner, command, jdbcUrl, caseProps);
            connection.initConnection();
            return connection;
        } catch (Exception e) {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception closeFailure) {
                    e.addSuppressed(closeFailure);
                }
            }
            throw e instanceof SQLException ? (SQLException) e : new SQLException("create Milvus connection failed.", e);
        }
    }

    // SDK connection settings

    private static ServerAddress parseServerAddress(String address) {
        int separator = address.indexOf(':');
        String host = separator < 0 ? address : address.substring(0, separator);
        int port = separator < 0 ? 19530 : Integer.parseInt(address.substring(separator + 1));
        return ServerAddress.newBuilder().withHost(host).withPort(port).build();
    }

    private static ConnectConfig buildV2Config(String defaultDB, Map<String, String> caseProps, ServerAddress server) throws SQLException {
        ConnectConfig.ConnectConfigBuilder builder = ConnectConfig.builder();
        MilvusTls.configure(builder, caseProps, server);
        if (StringUtils.isNotBlank(defaultDB)) {
            builder.dbName(defaultDB);
        }

        long connectTimeout = ConvertUtils.toLong(caseProps.get(MilvusKeys.CONNECT_TIMEOUT), true);
        long keepAliveTime = ConvertUtils.toLong(caseProps.get(MilvusKeys.KEEP_ALIVE_TIME), true);
        long keepAliveTimeout = ConvertUtils.toLong(caseProps.get(MilvusKeys.KEEP_ALIVE_TIMEOUT), true);
        long idleTimeout = ConvertUtils.toLong(caseProps.get(MilvusKeys.IDLE_TIMEOUT), true);
        long rpcDeadline = ConvertUtils.toLong(caseProps.get(MilvusKeys.RPC_DEADLINE), true);
        String keepAliveWithoutCalls = caseProps.get(MilvusKeys.KEEP_ALIVE_WITHOUT_CALLS);
        if (connectTimeout > 0) {
            builder.connectTimeoutMs(connectTimeout);
        }
        if (keepAliveTime > 0) {
            builder.keepAliveTimeMs(keepAliveTime);
        }
        if (keepAliveTimeout > 0) {
            builder.keepAliveTimeoutMs(keepAliveTimeout);
        }
        if (idleTimeout > 0) {
            builder.idleTimeoutMs(idleTimeout);
        }
        if (rpcDeadline > 0) {
            builder.rpcDeadlineMs(rpcDeadline);
        }
        if (StringUtils.isNotBlank(keepAliveWithoutCalls)) {
            builder.keepAliveWithoutCalls(Boolean.parseBoolean(keepAliveWithoutCalls));
        }

        String token = StringUtils.trimToEmpty(caseProps.get(MilvusKeys.TOKEN));
        String username = StringUtils.trimToEmpty(caseProps.get(MilvusKeys.USERNAME));
        String password = StringUtils.trimToEmpty(caseProps.get(MilvusKeys.PASSWORD));
        if (StringUtils.isNotBlank(token)) {
            builder.token(token);
        } else if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            builder.username(username).password(password);
        }
        return builder.build();
    }

    // Interceptors and JDBC URL parsing

    private InvocationHandler createInvocation(Map<String, String> props) throws SQLException {
        if (props.containsKey(MilvusKeys.INTERCEPTOR)) {
            try {
                String interceptorClass = props.get(MilvusKeys.INTERCEPTOR);
                Class<?> interceptor = ClassUtils.getClass(MilvusConnFactory.class.getClassLoader(), interceptorClass);
                return (InvocationHandler) net.hasor.cobble.ClassUtils.newInstance(interceptor);
            } catch (Exception e) {
                throw new SQLException("create interceptor failed, " + e.getMessage(), e);
            }
        } else {
            return null;
        }
    }

    private static String extractPathFromJdbcUrl(String jdbcUrl, String host) throws SQLException {
        jdbcUrl = jdbcUrl.substring(jdbcUrl.indexOf("://") + 3 + host.length());

        int params = jdbcUrl.indexOf("?");
        if (params > -1) {
            jdbcUrl = jdbcUrl.substring(0, params);
        }

        if (jdbcUrl.isEmpty()) {
            return null;
        } else if (jdbcUrl.startsWith("/")) {
            int path = jdbcUrl.indexOf("/", 1);
            if (path > -1) {
                throw new SQLException("jdbcUrl is not a valid milvus url.");
            } else {
                return jdbcUrl.substring(1);
            }
        } else {
            throw new SQLException("jdbcUrl is not a valid milvus url.");
        }
    }
}
