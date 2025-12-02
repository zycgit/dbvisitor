package net.hasor.dbvisitor.adapter.mongo;
import java.lang.reflect.InvocationHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.driver.AdapterFactory;
import net.hasor.dbvisitor.driver.AdapterTypeSupport;
import net.hasor.dbvisitor.driver.TypeSupport;

public class MongoConnFactory implements AdapterFactory {
    private static ServerAddress passerIpPort(String host, int defaultPort) {
        String[] ipPort = host.split(":");
        if (ipPort.length == 1) {
            return new ServerAddress(ipPort[0], defaultPort);
        } else if (ipPort.length == 2) {
            return new ServerAddress(ipPort[0], Integer.parseInt(ipPort[1]));
        } else {
            throw new IllegalArgumentException("unsupported host format:" + host);
        }
    }

    private MongoCredential passerMongoConfig(Map<String, String> caseProps) throws SQLException {
        String username = StringUtils.trimToEmpty(caseProps.get(MongoKeys.USERNAME));
        String password = StringUtils.trimToEmpty(caseProps.get(MongoKeys.PASSWORD));
        String database = StringUtils.trimToEmpty(caseProps.get(MongoKeys.DATABASE));
        String mechanism = StringUtils.trimToEmpty(caseProps.get(MongoKeys.MECHANISM)).toUpperCase();
        switch (mechanism) {
            case "PLAIN":
                return MongoCredential.createPlainCredential(username, database, password.toCharArray());
            case "SCRAM-SHA-1":
                return MongoCredential.createScramSha1Credential(username, database, password.toCharArray());
            case "SCRAM-SHA-256":
                return MongoCredential.createScramSha256Credential(username, database, password.toCharArray());
            case "MONGODB-CR":
                return MongoCredential.createMongoCRCredential(username, database, password.toCharArray());
            case "GSSAPI":
                return MongoCredential.createGSSAPICredential(username);
            case "X-509":
                if (StringUtils.isNotBlank(username)) {
                    return MongoCredential.createMongoX509Credential(username);
                } else {
                    return MongoCredential.createMongoX509Credential();
                }
            case "":
                return MongoCredential.createCredential(username, database, password.toCharArray());
            default:
                throw new SQLException("unsupported authentication mechanism:" + mechanism);
        }
    }

    private MongoClientOptions passerMongoOption(Map<String, String> caseProps) {
        String connTimeout = StringUtils.trimToEmpty(caseProps.get(MongoKeys.CONN_TIMEOUT));
        String soTimeout = StringUtils.trimToEmpty(caseProps.get(MongoKeys.SO_TIMEOUT));
        String soKeepAlive = StringUtils.trimToEmpty(caseProps.get(MongoKeys.SO_KEEP_ALIVE));
        String serverSelectionTimeout = StringUtils.trimToEmpty(caseProps.get(MongoKeys.SERVER_SELECTION_TIMEOUT));
        String maxWaitTime = StringUtils.trimToEmpty(caseProps.get(MongoKeys.MAX_WAIT_TIME));
        String maxConnectionIdleTime = StringUtils.trimToEmpty(caseProps.get(MongoKeys.MAX_CONNECTION_IDLE_TIME));
        String maxConnectionLifeTime = StringUtils.trimToEmpty(caseProps.get(MongoKeys.MAX_CONNECTION_LIFE_TIME));
        String minConnectionsPerHost = StringUtils.trimToEmpty(caseProps.get(MongoKeys.MIN_CONNECTIONS_PER_HOST));
        String maxConnectionsPerHost = StringUtils.trimToEmpty(caseProps.get(MongoKeys.MAX_CONNECTIONS_PER_HOST));
        String retryWrites = StringUtils.trimToEmpty(caseProps.get(MongoKeys.RETRY_WRITES));
        String retryReads = StringUtils.trimToEmpty(caseProps.get(MongoKeys.RETRY_READS));
        String threadsAllowedToBlock = StringUtils.trimToEmpty(caseProps.get(MongoKeys.THREADS_ALLOWED_BLOCK));
        String clientName = StringUtils.trimToEmpty(caseProps.get(MongoKeys.CLIENT_NAME));
        String clientDescription = StringUtils.trimToEmpty(caseProps.get(MongoKeys.CLIENT_DESCRIPTION));

        MongoClientOptions.Builder builder = MongoClientOptions.builder();

        if (StringUtils.isNotBlank(connTimeout)) {
            builder.connectTimeout(Integer.parseInt(connTimeout));
        }
        if (StringUtils.isNotBlank(soTimeout)) {
            builder.socketTimeout(Integer.parseInt(soTimeout));
        }
        if (StringUtils.isNotBlank(soKeepAlive)) {
            builder.socketKeepAlive(Boolean.parseBoolean(soKeepAlive));
        }
        if (StringUtils.isNotBlank(serverSelectionTimeout)) {
            builder.serverSelectionTimeout(Integer.parseInt(serverSelectionTimeout));
        }
        if (StringUtils.isNotBlank(maxWaitTime)) {
            builder.maxWaitTime(Integer.parseInt(maxWaitTime));
        }
        if (StringUtils.isNotBlank(maxConnectionIdleTime)) {
            builder.maxConnectionIdleTime(Integer.parseInt(maxConnectionIdleTime));
        }
        if (StringUtils.isNotBlank(maxConnectionLifeTime)) {
            builder.maxConnectionLifeTime(Integer.parseInt(maxConnectionLifeTime));
        }
        if (StringUtils.isNotBlank(minConnectionsPerHost)) {
            builder.minConnectionsPerHost(Integer.parseInt(minConnectionsPerHost));
        }
        if (StringUtils.isNotBlank(maxConnectionsPerHost)) {
            builder.connectionsPerHost(Integer.parseInt(maxConnectionsPerHost));
        }
        if (StringUtils.isNotBlank(retryWrites)) {
            builder.retryWrites(Boolean.parseBoolean(retryWrites));
        }
        if (StringUtils.isNotBlank(retryReads)) {
            builder.retryReads(Boolean.parseBoolean(retryReads));
        }
        if (StringUtils.isNotBlank(threadsAllowedToBlock)) {
            builder.threadsAllowedToBlockForConnectionMultiplier(Integer.parseInt(threadsAllowedToBlock));
        }

        builder.applicationName(StringUtils.isNotBlank(clientName) ? clientName : MongoKeys.DEFAULT_CLIENT_NAME);
        if (StringUtils.isNotBlank(clientDescription)) {
            builder.description(clientDescription);
        }
        return builder.build();

    }

    @Override
    public String getAdapterName() {
        return MongoKeys.ADAPTER_NAME_VALUE;
    }

    @Override
    public String[] getPropertyNames() {
        return new String[] { MongoKeys.SERVER, MongoKeys.ADAPTER_NAME, MongoKeys.INTERCEPTOR, MongoKeys.TIME_ZONE, MongoKeys.CONN_TIMEOUT, MongoKeys.SO_TIMEOUT, MongoKeys.USERNAME, MongoKeys.PASSWORD, MongoKeys.DATABASE, MongoKeys.CLIENT_NAME };
    }

    @Override
    public TypeSupport createTypeSupport(Properties properties) {
        return new AdapterTypeSupport(properties);
    }

    @Override
    public MongoConn createConnection(Connection owner, String jdbcUrl, Properties props) throws SQLException {
        if (!StringUtils.startsWithIgnoreCase(jdbcUrl, MongoKeys.START_URL)) {
            throw new SQLException("jdbcUrl is not a valid mongo url.");
        }

        Map<String, String> caseProps = new LinkedCaseInsensitiveMap<>();
        props.forEach((k, v) -> caseProps.put((String) k, (String) v));

        String host = caseProps.get(MongoKeys.SERVER);
        String customMongo = caseProps.get(MongoKeys.CUSTOM_MONGO);
        String defaultDataBase = caseProps.get(MongoKeys.DATABASE);
        String defaultCollection = caseProps.get(MongoKeys.COLLECTION);
        MongoClient mongoObject;

        if (StringUtils.isNotBlank(customMongo)) {
            try {
                Class<?> customMongoClass = MongoConnFactory.class.getClassLoader().loadClass(customMongo);
                CustomMongo customMongoCmd = (CustomMongo) customMongoClass.newInstance();
                mongoObject = customMongoCmd.createMongoClient(jdbcUrl, caseProps);
                if (mongoObject == null) {
                    throw new SQLException("create Mongo connection failed, custom Mongo return null.");
                }
            } catch (Exception e) {
                throw new SQLException(e);
            }
        } else if (host.contains(";")) {
            List<ServerAddress> clusterHosts = new ArrayList<>();
            for (String h : StringUtils.split(host, ';')) {
                clusterHosts.add(passerIpPort(h, 27017));
            }

            MongoCredential credentialConfig = passerMongoConfig(caseProps);
            MongoClientOptions optionConfig = passerMongoOption(caseProps);
            mongoObject = new MongoClient(clusterHosts, credentialConfig, optionConfig);
        } else {
            ServerAddress hostAndPort = new ServerAddress(host, 27017);
            MongoCredential credentialConfig = passerMongoConfig(caseProps);
            MongoClientOptions optionConfig = passerMongoOption(caseProps);
            mongoObject = new MongoClient(hostAndPort, credentialConfig, optionConfig);
        }

        MongoCmd cmd = new MongoCmd(mongoObject, this.createInvocation(caseProps), defaultDataBase, defaultCollection);
        MongoConn conn = new MongoConn(owner, cmd, jdbcUrl, caseProps);
        conn.initConnection();
        return conn;
    }

    private InvocationHandler createInvocation(Map<String, String> props) throws SQLException {
        if (props.containsKey(MongoKeys.INTERCEPTOR)) {
            try {
                String interceptorClass = props.get(MongoKeys.INTERCEPTOR);
                Class<?> interceptor = ClassUtils.getClass(MongoConnFactory.class.getClassLoader(), interceptorClass);
                return (InvocationHandler) interceptor.newInstance();
            } catch (Exception e) {
                throw new SQLException("create interceptor failed, " + e.getMessage(), e);
            }
        } else {
            return null;
        }
    }
}
