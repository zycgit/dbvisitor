package net.hasor.dbvisitor.adapter.milvus.commands;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import io.milvus.grpc.*;
import io.milvus.shaded.io.grpc.*;
import io.milvus.shaded.io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.milvus.shaded.io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.milvus.shaded.io.grpc.netty.shaded.io.netty.handler.ssl.*;
import io.milvus.shaded.io.grpc.stub.StreamObserver;
import io.milvus.v2.client.MilvusClientV2;
import net.hasor.dbvisitor.adapter.milvus.CustomMilvus;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.*;

/** Exercises each TLS protocol independently; realdb tests cover both through a single ingress. */
public class MilvusTlsTest {
    private final List<Node> nodes = new ArrayList<>();

    public static class MockSdk implements CustomMilvus {
        @Override
        public MilvusClientV2 createMilvusClient(String url, Map<String, String> properties) {
            MilvusClientV2 client = Mockito.mock(MilvusClientV2.class);
            Mockito.when(client.getServerVersion()).thenReturn("v2.6.2");
            return client;
        }
    }

    private static File certificate(String name) {
        Path directory = Paths.get("../../dbvisitor-test/docker/certs");
        if (!Files.isDirectory(directory))
            directory = Paths.get("dbvisitor-test/docker/certs");
        return directory.resolve(name).toAbsolutePath().normalize().toFile();
    }

    private static final class Node extends MilvusServiceGrpc.MilvusServiceImplBase implements AutoCloseable {
        final String          host;
        final Server          grpc;
        final HttpsServer     rest;
        final ExecutorService executor         = Executors.newCachedThreadPool();
        final List<String>    grpcTokens       = new CopyOnWriteArrayList<>();
        final List<String>    httpTokens       = new CopyOnWriteArrayList<>();
        final List<String>    clientIdentities = new CopyOnWriteArrayList<>();
        final List<String>    bodies           = new CopyOnWriteArrayList<>();

        Node(String host, boolean mutual) throws Exception {
            this.host = host;
            ClientAuth auth = mutual ? ClientAuth.REQUIRE : ClientAuth.NONE;
            SslContext grpcTls = GrpcSslContexts.forServer(certificate("server.crt"), certificate("server.key")).trustManager(certificate("ca.crt")).clientAuth(auth).build();
            this.grpc = NettyServerBuilder.forAddress(new InetSocketAddress(host, 0)).sslContext(grpcTls).addService(ServerInterceptors.intercept(this, new ServerInterceptor() {
                @Override
                public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
                    grpcTokens.add(String.valueOf(headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER))));
                    return next.startCall(call, headers);
                }
            })).build().start();

            SSLContext restTls = ((JdkSslContext) SslContextBuilder.forServer(certificate("server.crt"), certificate("server.key")).sslProvider(SslProvider.JDK).trustManager(certificate("ca.crt")).clientAuth(auth).build()).context();
            this.rest = HttpsServer.create(new InetSocketAddress(host, 0), 0);
            this.rest.setHttpsConfigurator(new HttpsConfigurator(restTls) {
                @Override
                public void configure(HttpsParameters parameters) {
                    SSLParameters ssl = getSSLContext().getDefaultSSLParameters();
                    ssl.setNeedClientAuth(mutual);
                    parameters.setSSLParameters(ssl);
                }
            });
            this.rest.setExecutor(executor);
            this.rest.createContext("/v2/vectordb/jobs/import/", exchange -> {
                try {
                    httpTokens.add(String.valueOf(exchange.getRequestHeaders().getFirst("Authorization")));
                    if (mutual)
                        clientIdentities.add(((HttpsExchange) exchange).getSSLSession().getPeerPrincipal().getName());
                    bodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                    String result = exchange.getRequestURI().getPath().endsWith("create") ? "{\"code\":0,\"data\":{\"jobId\":\"tls-job\"}}" : "{\"code\":0,\"data\":{\"state\":\"Completed\",\"progress\":100}}";
                    byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, bytes.length);
                    exchange.getResponseBody().write(bytes);
                } finally {
                    exchange.close();
                }
            });
            this.rest.start();
        }

        @Override
        public void connect(ConnectRequest request, StreamObserver<ConnectResponse> response) {
            response.onNext(ConnectResponse.newBuilder().setStatus(success()).build());
            response.onCompleted();
        }

        @Override
        public void listDatabases(ListDatabasesRequest request, StreamObserver<ListDatabasesResponse> response) {
            response.onNext(ListDatabasesResponse.newBuilder().setStatus(success()).addDbNames("db1").build());
            response.onCompleted();
        }

        @Override
        public void getVersion(GetVersionRequest request, StreamObserver<GetVersionResponse> response) {
            response.onNext(GetVersionResponse.newBuilder().setStatus(success()).setVersion("v2.6.2").build());
            response.onCompleted();
        }

        @Override
        public void checkHealth(CheckHealthRequest request, StreamObserver<CheckHealthResponse> response) {
            response.onNext(CheckHealthResponse.newBuilder().setStatus(success()).setIsHealthy(true).build());
            response.onCompleted();
        }

        private static io.milvus.grpc.Status success() {
            return io.milvus.grpc.Status.newBuilder().setErrorCode(ErrorCode.Success).build();
        }

        @Override
        public void close() throws Exception {
            rest.stop(0);
            executor.shutdownNow();
            grpc.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private Node node(String host, boolean mutual) throws Exception {
        Node node = new Node(host, mutual);
        nodes.add(node);
        return node;
    }

    private Properties properties(Node node) {
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.CA_PEM_PATH, certificate("ca.crt").getPath());
        properties.setProperty(MilvusKeys.CONNECT_TIMEOUT, "1500");
        properties.setProperty(MilvusKeys.RPC_DEADLINE, "1500");
        return properties;
    }

    private Connection connect(Node node, Properties properties) throws SQLException {
        int port = properties.containsKey(MilvusKeys.CUSTOM_MILVUS) ? node.rest.getAddress().getPort() : node.grpc.getPort();
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://" + node.host + ":" + port + "/db1", properties);
    }

    private void verifyProtocols(Node node, Properties properties) throws SQLException {
        try (Connection connection = connect(node, properties)) {
            assertEquals("v2.6.2", connection.getMetaData().getDatabaseProductVersion());
        }
        Properties restProperties = new Properties();
        restProperties.putAll(properties);
        restProperties.setProperty(MilvusKeys.CUSTOM_MILVUS, MockSdk.class.getName());
        try (Connection connection = connect(node, restProperties)) {
            importJob(connection);
        }
    }

    private void importJob(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("/*+ sync=false */ IMPORT FROM 'prepared.parquet' INTO t RETURNING JOB_ID")) {
            assertTrue(result.next());
            assertEquals("tls-job", result.getString("JOB_ID"));
            assertFalse(result.next());
        }
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SHOW IMPORT 'tls-job'")) {
            assertTrue(result.next());
            assertEquals("Completed", result.getString("STATE"));
        }
    }

    @After
    public void cleanup() throws Exception {
        for (Node node : nodes)
            node.close();
    }

    @Test
    public void caTrustEnablesBothProtocolsAndPreservesTokenAndDatabase() throws Exception {
        Node node = node("127.0.0.1", false);
        Properties properties = properties(node);
        properties.setProperty(MilvusKeys.TOKEN, "api-key:test");
        properties.setProperty(MilvusKeys.USERNAME, "ignored");
        properties.setProperty(MilvusKeys.PASSWORD, "ignored");
        verifyProtocols(node, properties);
        String encoded = Base64.getEncoder().encodeToString("api-key:test".getBytes(StandardCharsets.UTF_8));
        assertFalse(node.grpcTokens.isEmpty());
        for (String token : node.grpcTokens)
            assertEquals(encoded, token);
        assertEquals(Arrays.asList("Bearer api-key:test", "Bearer api-key:test"), node.httpTokens);
        assertTrue(node.bodies.get(0), node.bodies.get(0).contains("\"dbName\":\"db1\""));
    }

    @Test
    public void serverCertificateAndUrlPropertiesWorkWithoutCustomFactory() throws Exception {
        Node node = node("127.0.0.1", false);
        Properties properties = properties(node);
        properties.remove(MilvusKeys.CA_PEM_PATH);
        properties.setProperty(MilvusKeys.SERVER_PEM_PATH, certificate("server.crt").getPath());
        properties.setProperty(MilvusKeys.SECURE, "false");
        String url = "jdbc:dbvisitor:milvus://" + node.host + ":" + node.grpc.getPort() + "/db1?SeCuRe=TrUe";
        try (Connection connection = new JdbcDriver().connect(url, properties)) {
            assertEquals("v2.6.2", connection.getMetaData().getDatabaseProductVersion());
        }
        properties.setProperty(MilvusKeys.CUSTOM_MILVUS, MockSdk.class.getName());
        String restUrl = "jdbc:dbvisitor:milvus://" + node.host + ":" + node.rest.getAddress().getPort() + "/db1?SeCuRe=TrUe";
        try (Connection connection = new JdbcDriver().connect(restUrl, properties)) {
            importJob(connection);
        }
    }

    @Test
    public void mutualTlsPresentsClientIdentityToBothProtocols() throws Exception {
        Node node = node("127.0.0.1", true);
        Properties properties = properties(node);
        properties.setProperty(MilvusKeys.CLIENT_PEM_PATH, certificate("client.crt").getPath());
        properties.setProperty(MilvusKeys.CLIENT_KEY_PATH, certificate("client.key").getPath());
        verifyProtocols(node, properties);
        assertEquals(2, node.clientIdentities.size());
        for (String name : node.clientIdentities)
            assertTrue(name, name.contains("sslclient"));
    }

    @Test
    public void serverNameOverrideKeepsTcpAddressAndChecksExpectedIdentity() throws Exception {
        // The certificate has localhost and 127.0.0.1, not 127.0.0.2.
        Node node = node("127.0.0.2", false);
        Properties properties = properties(node);
        properties.setProperty(MilvusKeys.SERVER_NAME, "localhost");
        verifyProtocols(node, properties);
        assertEquals(2, node.httpTokens.size());
    }

    @Test
    public void grpcRejectsUntrustedCertificatesAndWrongNames() throws Exception {
        Node node = node("127.0.0.1", false);
        for (String failure : Arrays.asList("system-trust", "wrong-ca", "wrong-name")) {
            Properties properties = properties(node);
            if ("system-trust".equals(failure)) {
                properties.remove(MilvusKeys.CA_PEM_PATH);
                properties.setProperty(MilvusKeys.SECURE, "true");
            } else if ("wrong-ca".equals(failure)) {
                properties.setProperty(MilvusKeys.CA_PEM_PATH, certificate("client.crt").getPath());
            } else {
                properties.setProperty(MilvusKeys.SERVER_NAME, "wrong.invalid");
            }
            try (Connection ignored = connect(node, properties)) {
                fail("Expected TLS failure: " + failure);
            } catch (SQLException expected) {
                assertNotNull(expected.getCause());
            }
        }
        assertTrue(node.grpcTokens.isEmpty());
    }

    @Test
    public void restRejectsWrongNamesWithoutFallingBackToMatchingUriHost() throws Exception {
        Node node = node("127.0.0.1", false);
        Properties properties = properties(node);
        properties.setProperty(MilvusKeys.CUSTOM_MILVUS, MockSdk.class.getName());
        properties.setProperty(MilvusKeys.SERVER_NAME, "wrong.invalid");
        try (Connection connection = connect(node, properties)) {
            try {
                importJob(connection);
                fail("Expected HTTPS identity failure");
            } catch (SQLException expected) {
                assertNotNull(expected.getCause());
            }
        }
        assertTrue(node.httpTokens.isEmpty());
    }

    @Test
    public void restRejectsUntrustedCertificateAndMissingClientIdentity() throws Exception {
        for (boolean mutual : Arrays.asList(false, true)) {
            Node node = node("127.0.0.1", mutual);
            Properties properties = properties(node);
            properties.setProperty(MilvusKeys.CUSTOM_MILVUS, MockSdk.class.getName());
            if (!mutual) {
                properties.remove(MilvusKeys.CA_PEM_PATH);
                properties.setProperty(MilvusKeys.SECURE, "true");
            }
            try (Connection connection = connect(node, properties)) {
                try {
                    importJob(connection);
                    fail("Expected HTTPS authentication failure");
                } catch (SQLException expected) {
                    assertNotNull(expected.getCause());
                }
            }
            assertTrue(node.httpTokens.isEmpty());
        }
    }

    @Test
    public void grpcRejectsMissingClientIdentity() throws Exception {
        Node node = node("127.0.0.1", true);
        try (Connection ignored = connect(node, properties(node))) {
            fail("Expected mutual TLS failure");
        } catch (SQLException expected) {
            assertNotNull(expected.getCause());
        }
        assertTrue(node.grpcTokens.isEmpty());
    }

    @Test
    public void invalidTlsConfigurationFailsBeforeNetworkAccess() throws Exception {
        Node node = node("127.0.0.1", false);
        String[][] invalid = { { MilvusKeys.SECURE, "yes" }, { MilvusKeys.SECURE, "false" }, { MilvusKeys.SECURE, "" }, { MilvusKeys.SERVER_PEM_PATH, certificate("server.crt").getPath() }, { MilvusKeys.CLIENT_PEM_PATH, certificate("client.crt").getPath() }, { MilvusKeys.CLIENT_KEY_PATH, certificate("client.key").getPath() }, { MilvusKeys.CA_PEM_PATH, certificate("does-not-exist.pem").getPath() }, { MilvusKeys.CA_PEM_PATH, certificate("client.key").getPath() }, { MilvusKeys.SERVER_NAME, "https://localhost:443" } };
        for (String[] setting : invalid) {
            Properties properties = properties(node);
            properties.setProperty(setting[0], setting[1]);
            try (Connection ignored = connect(node, properties)) {
                fail("Expected invalid " + setting[0]);
            } catch (SQLException expected) {
                assertNotNull(expected.getMessage());
            }
        }
        assertTrue(node.grpcTokens.isEmpty());
        assertTrue(node.httpTokens.isEmpty());
    }

}
