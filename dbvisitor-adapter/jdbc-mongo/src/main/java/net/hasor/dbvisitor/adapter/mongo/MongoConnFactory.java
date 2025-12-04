package net.hasor.dbvisitor.adapter.mongo;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
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

    private static MongoCredential passerMongoConfig(Map<String, String> caseProps) throws SQLException {
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

    private static MongoClientSettings passerMongoSettings(Map<String, String> caseProps, List<ServerAddress> clusterHosts) throws SQLException {
        MongoClientSettings.Builder builder = MongoClientSettings.builder();

        // hosts
        builder.applyToClusterSettings(b -> b.hosts(clusterHosts)).build();

        // credential
        builder.credential(passerMongoConfig(caseProps));

        // socket settings
        builder.applyToSocketSettings(b -> {
            String soTimeout = StringUtils.trimToEmpty(caseProps.get(MongoKeys.SO_TIMEOUT));
            String soSndBuff = StringUtils.trimToEmpty(caseProps.get(MongoKeys.SO_SND_BUFF));
            String soRcvBuff = StringUtils.trimToEmpty(caseProps.get(MongoKeys.SO_RCV_BUFF));

            if (StringUtils.isNotBlank(soTimeout)) {
                b.readTimeout(Integer.parseInt(soTimeout), TimeUnit.MILLISECONDS);
            }
            if (StringUtils.isNotBlank(soSndBuff)) {
                b.sendBufferSize(Integer.parseInt(soSndBuff));
            }
            if (StringUtils.isNotBlank(soRcvBuff)) {
                b.receiveBufferSize(Integer.parseInt(soRcvBuff));
            }
        });

        //
        String retryWrites = StringUtils.trimToEmpty(caseProps.get(MongoKeys.RETRY_WRITES));
        String retryReads = StringUtils.trimToEmpty(caseProps.get(MongoKeys.RETRY_READS));
        String clientName = StringUtils.trimToEmpty(caseProps.get(MongoKeys.CLIENT_NAME));
        if (StringUtils.isNotBlank(retryWrites)) {
            builder.retryWrites(Boolean.parseBoolean(retryWrites));
        }
        if (StringUtils.isNotBlank(retryReads)) {
            builder.retryReads(Boolean.parseBoolean(retryReads));
        }
        if (StringUtils.isNotBlank(clientName)) {
            builder.applicationName(clientName);
        } else {
            builder.applicationName(MongoKeys.DEFAULT_CLIENT_NAME);
        }
        return builder.build();
    }

    @Override
    public String getAdapterName() {
        return MongoKeys.ADAPTER_NAME_VALUE;
    }

    @Override
    public String[] getPropertyNames() {
        return new String[] { MongoKeys.SERVER, MongoKeys.ADAPTER_NAME, MongoKeys.TIME_ZONE, MongoKeys.CONN_TIMEOUT, MongoKeys.SO_TIMEOUT, MongoKeys.USERNAME, MongoKeys.PASSWORD, MongoKeys.DATABASE, MongoKeys.CLIENT_NAME };
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
        MongoClient mongoObject;

        if (StringUtils.isNotBlank(customMongo)) {
            try {
                Class<?> customMongoClass = MongoConnFactory.class.getClassLoader().loadClass(customMongo);
                CustomMongo customCmd = (CustomMongo) customMongoClass.newInstance();
                mongoObject = customCmd.createMongoClient(jdbcUrl, caseProps);
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

            MongoClientSettings settings = passerMongoSettings(caseProps, clusterHosts);
            mongoObject = MongoClients.create(settings);
        } else {
            ServerAddress hostAndPort = passerIpPort(host, 27017);
            MongoClientSettings settings = passerMongoSettings(caseProps, Collections.singletonList(hostAndPort));
            mongoObject = MongoClients.create(settings);
        }

        MongoCmd cmd = new MongoCmd(mongoObject, defaultDataBase);
        MongoConn conn = new MongoConn(owner, cmd, jdbcUrl, caseProps);
        conn.initConnection();
        return conn;
    }
}
