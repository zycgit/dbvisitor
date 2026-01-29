/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.adapter.milvus;
import java.lang.reflect.InvocationHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.MultiConnectParam;
import io.milvus.param.ServerAddress;
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.driver.*;

public class MilvusConnFactory implements AdapterFactory {
    private static ServerAddress passerIpPort(String hostStr, int defaultPort, int defaultHealthPort) {
        String[] ipPort = hostStr.split(":");
        String host = null;
        int port = defaultPort;
        int health = defaultHealthPort;

        if (ipPort.length == 1) {
            host = ipPort[0];
        } else if (ipPort.length == 2) {
            host = ipPort[0];
            port = Integer.parseInt(ipPort[1]);
        } else if (ipPort.length == 3) {
            host = ipPort[0];
            port = Integer.parseInt(ipPort[1]);
            health = Integer.parseInt(ipPort[2]);
        } else {
            throw new IllegalArgumentException("unsupported host format:" + host);
        }

        return ServerAddress.newBuilder().withHost(host).withPort(port).withHealthPort(health).build();
    }

    private static ConnectParam.Builder passerSettings(String defaultDB, Map<String, String> caseProps, List<ServerAddress> clusterHosts) {
        ConnectParam.Builder builder = MultiConnectParam.newBuilder().withHosts(clusterHosts);
        if (StringUtils.isNotBlank(defaultDB)) {
            builder.withDatabaseName(defaultDB);
        }

        long connectTimeout = ConvertUtils.toLong(caseProps.get(MilvusKeys.CONNECT_TIMEOUT), true);
        long keepAliveTime = ConvertUtils.toLong(caseProps.get(MilvusKeys.KEEP_ALIVE_TIME), true);
        long keepAliveTimeout = ConvertUtils.toLong(caseProps.get(MilvusKeys.KEEP_ALIVE_TIMEOUT), true);
        long idleTimeout = ConvertUtils.toLong(caseProps.get(MilvusKeys.IDLE_TIMEOUT), true);
        long rpcDeadline = ConvertUtils.toLong(caseProps.get(MilvusKeys.RPC_DEADLINE), true);
        String keepAliveWithoutCalls = caseProps.get(MilvusKeys.KEEP_ALIVE_WITHOUT_CALLS);

        if (connectTimeout > 0) {
            builder.withConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
        }
        if (keepAliveTime > 0) {
            builder.withKeepAliveTime(keepAliveTime, TimeUnit.MILLISECONDS);
        }
        if (keepAliveTimeout > 0) {
            builder.withKeepAliveTimeout(keepAliveTimeout, TimeUnit.MILLISECONDS);
        }
        if (idleTimeout > 0) {
            builder.withIdleTimeout(idleTimeout, TimeUnit.MILLISECONDS);
        }
        if (rpcDeadline > 0) {
            builder.withRpcDeadline(rpcDeadline, TimeUnit.MILLISECONDS);
        }
        if (StringUtils.isNotBlank(keepAliveWithoutCalls)) {
            builder.keepAliveWithoutCalls(Boolean.parseBoolean(keepAliveWithoutCalls));
        }

        configAuthorization(builder, caseProps);
        return builder;
    }

    private static void configAuthorization(ConnectParam.Builder builder, Map<String, String> caseProps) {
        String token = StringUtils.trimToEmpty(caseProps.get(MilvusKeys.TOKEN));
        String username = StringUtils.trimToEmpty(caseProps.get(MilvusKeys.USERNAME));
        String password = StringUtils.trimToEmpty(caseProps.get(MilvusKeys.PASSWORD));

        if (StringUtils.isNotBlank(token)) {
            builder.withToken(token);
        } else if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            builder.withAuthorization(username, password);
        }
    }

    @Override
    public String getAdapterName() {
        return MilvusKeys.ADAPTER_NAME_VALUE;
    }

    @Override
    public String[] getPropertyNames() {
        return new String[] { MilvusKeys.ADAPTER_NAME, MilvusKeys.INTERCEPTOR, MilvusKeys.CUSTOM_MILVUS, MilvusKeys.SERVER,//
                MilvusKeys.TIME_ZONE, MilvusKeys.DATABASE, MilvusKeys.TOKEN, MilvusKeys.USERNAME, MilvusKeys.PASSWORD,     //
                MilvusKeys.TIMEOUT, MilvusKeys.CONNECT_TIMEOUT, MilvusKeys.KEEP_ALIVE_TIME, MilvusKeys.KEEP_ALIVE_TIMEOUT, //
                MilvusKeys.KEEP_ALIVE_WITHOUT_CALLS, MilvusKeys.IDLE_TIMEOUT, MilvusKeys.RPC_DEADLINE };
    }

    @Override
    public TypeSupport createTypeSupport(Properties properties) {
        return new AdapterTypeSupport(properties);
    }

    @Override
    public AdapterConnection createConnection(Connection owner, String jdbcUrl, Properties properties) throws SQLException {
        if (!StringUtils.startsWithIgnoreCase(jdbcUrl, MilvusKeys.START_URL)) {
            throw new SQLException("jdbcUrl is not a valid milvus url.");
        }

        Map<String, String> caseProps = new LinkedCaseInsensitiveMap<>();
        if (properties != null) {
            properties.forEach((k, v) -> caseProps.put(k.toString(), v.toString()));
        }

        String host = StringUtils.trimToEmpty(caseProps.get(MilvusKeys.SERVER));
        if (host.contains("/")) {
            host = host.substring(0, host.indexOf("/"));
        }
        String customMilvus = caseProps.get(MilvusKeys.CUSTOM_MILVUS);
        String defaultDB = extractPathFromJdbcUrl(jdbcUrl, host);
        if (StringUtils.isBlank(defaultDB)) {
            defaultDB = caseProps.get(MilvusKeys.DATABASE);
        }

        MilvusClient milvusClient = null;
        try {
            if (StringUtils.isNotBlank(customMilvus)) {
                try {
                    Class<?> customMongoClass = MilvusConnFactory.class.getClassLoader().loadClass(customMilvus);
                    CustomMilvus customCmd = (CustomMilvus) customMongoClass.newInstance();
                    milvusClient = customCmd.createMilvusClient(jdbcUrl, caseProps);
                    if (milvusClient == null) {
                        throw new SQLException("create Milvus connection failed, custom Milvus return null.");
                    }
                } catch (Exception e) {
                    throw new SQLException(e);
                }
            } else if (host.contains(";")) {
                List<ServerAddress> clusterHosts = new ArrayList<>();
                for (String h : StringUtils.split(host, ';')) {
                    clusterHosts.add(passerIpPort(h, 19530, 9091));
                }

                ConnectParam.Builder builder = passerSettings(defaultDB, caseProps, clusterHosts);
                milvusClient = new MilvusServiceClient(builder.build());
            } else {
                ServerAddress hostAndPort = passerIpPort(host, 19530, 9091);

                ConnectParam.Builder builder = passerSettings(defaultDB, caseProps, Collections.singletonList(hostAndPort));
                milvusClient = new MilvusServiceClient(builder.build());
            }

            InvocationHandler handler = this.createInvocation(caseProps);
            MilvusCmd milvusCmd = new MilvusCmd(milvusClient, defaultDB, handler);
            MilvusConn conn = new MilvusConn(owner, milvusCmd, jdbcUrl, caseProps);
            conn.initConnection();
            return conn;
        } catch (Exception e) {
            try {
                if (milvusClient != null) {
                    milvusClient.close();
                }
            } catch (Exception ioe) {
                // ignore
            }
            throw e;
        }
    }

    private InvocationHandler createInvocation(Map<String, String> props) throws SQLException {
        if (props.containsKey(MilvusKeys.INTERCEPTOR)) {
            try {
                String interceptorClass = props.get(MilvusKeys.INTERCEPTOR);
                Class<?> interceptor = ClassUtils.getClass(MilvusConnFactory.class.getClassLoader(), interceptorClass);
                return (InvocationHandler) interceptor.newInstance();
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
