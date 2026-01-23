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
package net.hasor.dbvisitor.adapter.elastic;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.io.IOUtils;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.driver.AdapterConnection;
import net.hasor.dbvisitor.driver.AdapterFactory;
import net.hasor.dbvisitor.driver.AdapterTypeSupport;
import net.hasor.dbvisitor.driver.TypeSupport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

public class ElasticConnFactory implements AdapterFactory {
    private static HttpHost passerIpPort(String host, int defaultPort) {
        String[] ipPort = host.split(":");
        if (ipPort.length == 1) {
            return new HttpHost(ipPort[0], defaultPort);
        } else if (ipPort.length == 2) {
            return new HttpHost(ipPort[0], Integer.parseInt(ipPort[1]));
        } else {
            throw new IllegalArgumentException("unsupported host format:" + host);
        }
    }

    private static CredentialsProvider passerCredentials(Map<String, String> caseProps) {
        String username;
        if (StringUtils.isNotBlank(caseProps.get(ElasticKeys.USERNAME))) {
            username = caseProps.get(ElasticKeys.USERNAME);
        } else if (StringUtils.isNotBlank(caseProps.get("username"))) {
            username = caseProps.get("username");
        } else {
            username = null;
        }
        String password = StringUtils.trimToEmpty(caseProps.get(ElasticKeys.PASSWORD));

        if (StringUtils.isNotBlank(username)) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            return credentialsProvider;
        }
        return null;
    }

    private static RestClient passerRestClient(Map<String, String> caseProps, List<HttpHost> clusterHosts) {
        RestClientBuilder builder = RestClient.builder(clusterHosts.toArray(new HttpHost[0]));

        // credential
        CredentialsProvider credentialsProvider = passerCredentials(caseProps);
        if (credentialsProvider != null) {
            builder.setHttpClientConfigCallback(cb -> cb.setDefaultCredentialsProvider(credentialsProvider));
        }

        // socket settings
        builder.setRequestConfigCallback(cb -> {
            String connTimeout = StringUtils.trimToEmpty(caseProps.get(ElasticKeys.CONN_TIMEOUT));
            String soTimeout = StringUtils.trimToEmpty(caseProps.get(ElasticKeys.SO_TIMEOUT));

            if (StringUtils.isNotBlank(connTimeout)) {
                cb.setConnectTimeout(Integer.parseInt(connTimeout));
            }
            if (StringUtils.isNotBlank(soTimeout)) {
                cb.setSocketTimeout(Integer.parseInt(soTimeout));
            }
            return cb;
        });

        return builder.build();
    }

    @Override
    public String getAdapterName() {
        return ElasticKeys.ADAPTER_NAME_VALUE;
    }

    @Override
    public String[] getPropertyNames() {
        return new String[] { ElasticKeys.ADAPTER_NAME, ElasticKeys.CUSTOM_ELASTIC, ElasticKeys.SERVER, ElasticKeys.TIME_ZONE,//
                ElasticKeys.USERNAME, ElasticKeys.PASSWORD, ElasticKeys.CLIENT_NAME, ElasticKeys.CONN_TIMEOUT, ElasticKeys.SO_TIMEOUT,//
                ElasticKeys.PREREAD_ENABLED, ElasticKeys.PREREAD_THRESHOLD, ElasticKeys.PREREAD_MAX_FILE_SIZE, ElasticKeys.PREREAD_CACHE_DIR,//
                ElasticKeys.INDEX_REFRESH };
    }

    @Override
    public TypeSupport createTypeSupport(Properties properties) {
        return new AdapterTypeSupport(properties);
    }

    @Override
    public AdapterConnection createConnection(Connection owner, String jdbcUrl, Properties properties) throws SQLException {
        if (!StringUtils.startsWithIgnoreCase(jdbcUrl, ElasticKeys.START_URL)) {
            throw new SQLException("jdbcUrl is not a valid elastic url.");
        }

        Map<String, String> caseProps = new LinkedCaseInsensitiveMap<>();
        if (properties != null) {
            properties.forEach((k, v) -> caseProps.put(k.toString(), v.toString()));
        }

        String host = StringUtils.trimToEmpty(caseProps.get(ElasticKeys.SERVER));
        if (host.contains("/")) {
            host = host.substring(0, host.indexOf("/"));
        }
        String customElastic = caseProps.get(ElasticKeys.CUSTOM_ELASTIC);

        RestClient elasticClient = null;
        try {
            if (StringUtils.isNotBlank(customElastic)) {
                try {
                    Class<?> customMongoClass = ElasticConnFactory.class.getClassLoader().loadClass(customElastic);
                    CustomElastic customCmd = (CustomElastic) customMongoClass.newInstance();
                    elasticClient = customCmd.createElasticClient(jdbcUrl, caseProps);
                    if (elasticClient == null) {
                        throw new SQLException("create Elastic connection failed, custom Elastic return null.");
                    }
                } catch (Exception e) {
                    throw new SQLException(e);
                }
            } else if (host.contains(";")) {
                List<HttpHost> clusterHosts = new ArrayList<>();
                for (String h : StringUtils.split(host, ';')) {
                    clusterHosts.add(passerIpPort(h, 9200));
                }

                elasticClient = passerRestClient(caseProps, clusterHosts);
            } else {
                HttpHost hostAndPort = passerIpPort(host, 9200);
                elasticClient = passerRestClient(caseProps, Collections.singletonList(hostAndPort));
            }

            ElasticCmd cmd = new ElasticCmd(elasticClient);
            ElasticConn conn = new ElasticConn(owner, cmd, jdbcUrl, caseProps);
            conn.initConnection();
            return conn;
        } catch (Exception e) {
            IOUtils.closeQuietly(elasticClient);
            throw e;
        }
    }
}
