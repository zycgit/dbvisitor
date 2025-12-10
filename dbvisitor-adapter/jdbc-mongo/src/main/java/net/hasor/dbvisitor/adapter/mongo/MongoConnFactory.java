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
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

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

    private static MongoCredential passerMongoConfig(String defaultDB, Map<String, String> caseProps) throws SQLException {
        String username;
        if (StringUtils.isNotBlank(caseProps.get(MongoKeys.USERNAME))) {
            username = caseProps.get(MongoKeys.USERNAME);
        } else if (StringUtils.isNotBlank(caseProps.get("username"))) {
            username = caseProps.get("username");
        } else {
            username = null;
        }
        String password = StringUtils.trimToEmpty(caseProps.get(MongoKeys.PASSWORD));
        String mechanism = StringUtils.trimToEmpty(caseProps.get(MongoKeys.MECHANISM)).toUpperCase();
        switch (mechanism) {
            case "PLAIN":
                return MongoCredential.createPlainCredential(username, defaultDB, password.toCharArray());
            case "SCRAM-SHA-1":
                return MongoCredential.createScramSha1Credential(username, defaultDB, password.toCharArray());
            case "SCRAM-SHA-256":
                return MongoCredential.createScramSha256Credential(username, defaultDB, password.toCharArray());
            case "GSSAPI":
                return MongoCredential.createGSSAPICredential(username);
            case "X-509":
                if (StringUtils.isNotBlank(username)) {
                    return MongoCredential.createMongoX509Credential(username);
                } else {
                    return MongoCredential.createMongoX509Credential();
                }
            case "":
                return MongoCredential.createCredential(username, defaultDB, password.toCharArray());
            default:
                throw new SQLException("unsupported authentication mechanism:" + mechanism);
        }
    }

    private static MongoClientSettings passerMongoSettings(String defaultDB, Map<String, String> caseProps, List<ServerAddress> clusterHosts) throws SQLException {
        MongoClientSettings.Builder builder = MongoClientSettings.builder();

        // hosts
        builder.applyToClusterSettings(b -> b.hosts(clusterHosts)).build();

        // credential
        builder.credential(passerMongoConfig(defaultDB, caseProps));

        // codec registry
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        builder.codecRegistry(codecRegistry);

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
        return new String[] { MongoKeys.SERVER, MongoKeys.ADAPTER_NAME, MongoKeys.TIME_ZONE, MongoKeys.CONN_TIMEOUT, MongoKeys.SO_TIMEOUT, MongoKeys.USERNAME, MongoKeys.PASSWORD, MongoKeys.CLIENT_NAME };
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

        String host = StringUtils.trimToEmpty(caseProps.get(MongoKeys.SERVER));
        if (host.contains("/")) {
            host = host.substring(0, host.indexOf("/"));
        }

        String customMongo = caseProps.get(MongoKeys.CUSTOM_MONGO);
        String defaultDB = extractPathFromJdbcUrl(jdbcUrl, host);
        if (StringUtils.isBlank(defaultDB)) {
            defaultDB = caseProps.get("database");
        }
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

            MongoClientSettings settings = passerMongoSettings(defaultDB, caseProps, clusterHosts);
            mongoObject = MongoClients.create(settings);
        } else {
            ServerAddress hostAndPort = passerIpPort(host, 27017);
            MongoClientSettings settings = passerMongoSettings(defaultDB, caseProps, Collections.singletonList(hostAndPort));
            mongoObject = MongoClients.create(settings);
        }

        MongoCmd cmd = new MongoCmd(mongoObject, defaultDB);
        MongoConn conn = new MongoConn(owner, cmd, jdbcUrl, caseProps);
        conn.initConnection();
        return conn;
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
                throw new SQLException("jdbcUrl is not a valid mongo url.");
            } else {
                return jdbcUrl.substring(1);
            }
        } else {
            throw new SQLException("jdbcUrl is not a valid mongo url.");
        }
    }
}
