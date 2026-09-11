/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands;

import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import io.milvus.grpc.CollectionSchema;
import io.milvus.grpc.DataType;
import io.milvus.grpc.DescribeCollectionResponse;
import io.milvus.grpc.FieldSchema;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.client.RetryConfig;
import io.milvus.v2.service.collection.request.DescribeCollectionReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.QueryIteratorReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import net.hasor.dbvisitor.adapter.milvus.*;
import net.hasor.dbvisitor.driver.JdbcDriver;

public class MilvusV2ConnectionTest {
    private HttpServer             server;
    private ExecutorService        executor;
    private final List<String>     paths          = new CopyOnWriteArrayList<>();
    private final List<JsonObject> bodies         = new CopyOnWriteArrayList<>();
    private final List<String>     authorizations = new CopyOnWriteArrayList<>();
    private volatile String        state          = "Completed";
    private volatile int           apiCode;
    private volatile int           httpStatus     = 200;
    private volatile boolean       delay;
    private CountDownLatch         entered;

    public static class ClientFactory implements CustomMilvus {
        static MilvusClientV2      client;
        static final AtomicInteger calls = new AtomicInteger();

        @Override
        public MilvusClientV2 createMilvusClient(String jdbcUrl, Map<String, String> props) {
            calls.incrementAndGet();
            return client;
        }
    }

    @Before
    public void setup() throws Exception {
        ClientFactory.calls.set(0);
        ClientFactory.client = Mockito.mock(MilvusClientV2.class);
        Mockito.when(ClientFactory.client.getServerVersion()).thenReturn("v2.6.2");
        entered = new CountDownLatch(1);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
        server.createContext("/v2/vectordb/jobs/import/", exchange -> {
            paths.add(exchange.getRequestURI().getPath());
            bodies.add(JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject());
            authorizations.add(String.valueOf(exchange.getRequestHeaders().getFirst("Authorization")));
            entered.countDown();
            if (delay) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            JsonObject data = new JsonObject();
            if (exchange.getRequestURI().getPath().endsWith("/create")) {
                data.addProperty(MilvusCommandKeys.REST_JOB_ID, "92233720368547758070");
            } else {
                data.addProperty(MilvusCommandKeys.REST_STATE, state);
                data.addProperty(MilvusCommandKeys.REST_REASON, "invalid file");
                data.addProperty(MilvusCommandKeys.REST_PROGRESS, 35);
                data.addProperty(MilvusCommandKeys.REST_TOTAL_ROWS, 10000);
                data.addProperty(MilvusCommandKeys.REST_IMPORTED_ROWS, 3500);
                if (exchange.getRequestURI().getPath().endsWith("/list")) {
                    data.addProperty(MilvusCommandKeys.REST_JOB_ID, "92233720368547758070");
                    JsonArray records = new JsonArray();
                    records.add(data.deepCopy());
                    data = new JsonObject();
                    data.add(MilvusCommandKeys.REST_RECORDS, records);
                }
            }
            JsonObject body = new JsonObject();
            body.addProperty(MilvusCommandKeys.REST_CODE, apiCode);
            body.addProperty(MilvusCommandKeys.REST_MESSAGE, "import error");
            body.add(MilvusCommandKeys.REST_DATA, data);
            byte[] response = body.toString().getBytes(StandardCharsets.UTF_8);
            try {
                exchange.sendResponseHeaders(httpStatus, response.length);
                exchange.getResponseBody().write(response);
            } finally {
                exchange.close();
            }
        });
        server.start();
    }

    @After
    public void cleanup() {
        server.stop(0);
        executor.shutdownNow();
        MilvusCommandInterceptor.resetInterceptor();
    }

    private Connection connect(Properties extra) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.CUSTOM_MILVUS, ClientFactory.class.getName());
        properties.putAll(extra);
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://127.0.0.1:" + server.getAddress().getPort() + "/db1", properties);
    }

    @Test
    public void oneFactoryOneClientAndJdbcLifecycle() throws Exception {
        assertEquals(1, CustomMilvus.class.getDeclaredMethods().length);
        try (Connection connection = connect(new Properties())) {
            assertSame(ClientFactory.client, connection.unwrap(MilvusClientV2.class));
            assertEquals(1, ClientFactory.calls.get());
            assertEquals("db1", connection.getCatalog());
            assertEquals(2, connection.getMetaData().getDatabaseMajorVersion());
            assertEquals(6, connection.getMetaData().getDatabaseMinorVersion());
            connection.setCatalog("db1");
            assertEquals("db1", connection.getSchema());
            try {
                connection.setCatalog("db2");
                fail("Catalog switching remains unsupported");
            } catch (UnsupportedOperationException expected) {
                assertEquals("db1", connection.getCatalog());
            }
        }
        Mockito.verify(ClientFactory.client).close();
    }

    @Test
    public void failedInitializationClosesTheOnlyClient() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.MAX_RETRY, "-1");
        try {
            connect(properties);
            fail("Expected invalid property");
        } catch (SQLException expected) {
            assertTrue(expected.getMessage().contains(MilvusKeys.MAX_RETRY));
        }
        assertEquals(1, ClientFactory.calls.get());
        Mockito.verify(ClientFactory.client).close();
    }

    @Test
    public void customClientAndInterceptorAreSharedByInsertUpdateAndDelete() throws Exception {
        CollectionSchema schema = CollectionSchema.newBuilder().setName("t").addFields(FieldSchema.newBuilder().setName("id").setDataType(DataType.Int64).setIsPrimaryKey(true).setAutoID(true)).addFields(FieldSchema.newBuilder().setName("val").setDataType(DataType.Int32)).build();
        DescribeCollectionResponse description = DescribeCollectionResponse.newBuilder().setSchema(schema).build();
        Mockito.when(ClientFactory.client.describeCollection(Mockito.any(DescribeCollectionReq.class))).thenReturn((DescribeCollectionResp) MilvusTestResponses.v2Response("describeCollection", description));
        QueryIterator iterator = Mockito.mock(QueryIterator.class);
        QueryResultsWrapper.RowRecord row = new QueryResultsWrapper.RowRecord();
        row.put("id", 42L);
        Mockito.when(iterator.next()).thenReturn(Collections.singletonList(row), Collections.emptyList());
        Mockito.when(ClientFactory.client.queryIterator(Mockito.any(QueryIteratorReq.class))).thenReturn(iterator);
        Mockito.when(ClientFactory.client.insert(Mockito.any(InsertReq.class))).thenReturn(InsertResp.builder().InsertCnt(1).build());
        Mockito.when(ClientFactory.client.upsert(Mockito.any(UpsertReq.class))).thenReturn(UpsertResp.builder().upsertCnt(1).build());
        Mockito.when(ClientFactory.client.delete(Mockito.any(DeleteReq.class))).thenReturn(DeleteResp.builder().deleteCnt(1).build());

        List<String> calls = new CopyOnWriteArrayList<>();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (target, method, args) -> {
            assertSame(ClientFactory.client, target);
            calls.add(method.getName());
            return method.invoke(target, args);
        });
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        try (Connection connection = connect(properties); Statement statement = connection.createStatement()) {
            statement.setFetchSize(1);
            assertEquals(1, statement.executeUpdate("INSERT INTO t (val) VALUES (7)"));
            assertEquals(1, statement.executeUpdate("UPDATE t SET val = 8 WHERE id = 42 LIMIT 1"));
            assertEquals(1, statement.executeUpdate("DELETE FROM t WHERE id = 42"));
        }
        assertEquals(1, ClientFactory.calls.get());
        assertTrue(calls.containsAll(Arrays.asList("getServerVersion", "insert", "queryIterator", "upsert", "delete")));
        ArgumentCaptor<UpsertReq> update = ArgumentCaptor.forClass(UpsertReq.class);
        Mockito.verify(ClientFactory.client).upsert(update.capture());
        assertTrue(update.getValue().isPartialUpdate());
        assertEquals("db1", update.getValue().getDatabaseName());
        assertEquals(1, update.getValue().getData().size());
        JsonObject entity = update.getValue().getData().get(0);
        assertEquals(2, entity.size());
        assertEquals(42L, entity.get("id").getAsLong());
        assertEquals(8, entity.get("val").getAsInt());
        Mockito.verify(iterator).close();
        Mockito.verify(ClientFactory.client).close();
    }

    @Test
    public void closingConnectionClosesSdkOnceAndPreventsFurtherCalls() throws Exception {
        Connection connection = connect(new Properties());
        MilvusCmd command = connection.unwrap(MilvusCmd.class);
        assertSame(command.importClient(), command.importClient());
        connection.close();
        connection.close();
        command.close();
        assertTrue(connection.isClosed());
        try {
            command.getServerVersion();
            fail("Expected closed SDK access to fail");
        } catch (SQLException expected) {
            assertTrue(expected.getMessage().contains("closed"));
        }
        try {
            command.importClient();
            fail("Expected closed Import access to fail");
        } catch (SQLException expected) {
            assertTrue(expected.getMessage().contains("closed"));
        }
        Mockito.verify(ClientFactory.client).getServerVersion();
        Mockito.verify(ClientFactory.client).close();
    }

    @Test
    public void failedSdkSetupClosesClientAndPreservesCleanupFailure() throws Exception {
        RuntimeException setupFailure = new IllegalStateException("retry configuration failed");
        RuntimeException closeFailure = new IllegalStateException("close failed");
        Mockito.doThrow(setupFailure).when(ClientFactory.client).retryConfig(Mockito.any(RetryConfig.class));
        Mockito.doThrow(closeFailure).when(ClientFactory.client).close();
        try {
            connect(new Properties());
            fail("Expected SDK setup failure");
        } catch (SQLException expected) {
            assertSame(setupFailure, expected.getCause());
            assertArrayEquals(new Throwable[] { closeFailure }, expected.getCause().getSuppressed());
        }
        assertEquals(1, ClientFactory.calls.get());
        Mockito.verify(ClientFactory.client).close();
    }

    @Test
    public void importUsesOfficialRestAndCurrentJdbcCatalog() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.TOKEN, "token-value");
        try (Connection connection = connect(properties); Statement statement = connection.createStatement()) {
            assertEquals(0, statement.executeUpdate("IMPORT FROM 'bucket/data.json' INTO t PARTITION p"));
        }
        assertEquals(Arrays.asList("/v2/vectordb/jobs/import/create", "/v2/vectordb/jobs/import/describe"), paths);
        assertEquals("db1", bodies.get(0).get(MilvusCommandKeys.REST_DB_NAME).getAsString());
        assertEquals("t", bodies.get(0).get(MilvusCommandKeys.REST_COLLECTION_NAME).getAsString());
        assertEquals("p", bodies.get(0).get(MilvusCommandKeys.REST_PARTITION_NAME).getAsString());
        assertEquals("[[\"bucket/data.json\"]]", bodies.get(0).get(MilvusCommandKeys.REST_FILES).toString());
        assertEquals("92233720368547758070", bodies.get(1).get(MilvusCommandKeys.REST_JOB_ID).getAsString());
        assertEquals(Arrays.asList("Bearer token-value", "Bearer token-value"), authorizations);
    }

    @Test
    public void asyncImportAndUsernamePasswordKeepExistingHints() throws Exception {
        apiCode = 200;
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.USERNAME, "root");
        properties.setProperty(MilvusKeys.PASSWORD, "secret");
        try (Connection connection = connect(properties); Statement statement = connection.createStatement()) {
            assertEquals(0, statement.executeUpdate("/*+ sync=false */ IMPORT FROM 'data.json' INTO t"));
        }
        assertEquals(1, paths.size());
        assertEquals("Bearer root:secret", authorizations.get(0));
    }

    @Test
    public void groupedImportReturnsJobIdAndSupportsJdbcJobInspection() throws Exception {
        state = "Importing";
        try (Connection conn = connect(new Properties()); PreparedStatement submit = conn.prepareStatement("/*+ sync=false */ IMPORT FROM ? INTO t WITH (timeout='2h') RETURNING JOB_ID"); PreparedStatement progress = conn.prepareStatement("SHOW PROGRESS OF IMPORT ?"); Statement stmt = conn.createStatement()) {
            submit.setObject(1, Arrays.asList(Collections.singletonList("a.parquet"), Collections.singletonList("b.parquet")));
            String job;
            try (ResultSet rs = submit.executeQuery()) {
                assertTrue(rs.next());
                job = rs.getString("JOB_ID");
                assertFalse(rs.next());
            }
            assertEquals("92233720368547758070", job);
            progress.setString(1, job);
            try (ResultSet rs = progress.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(job, rs.getString("JOB_ID"));
                assertEquals("Importing", rs.getString("STATE"));
                assertEquals(35, rs.getInt("PROGRESS"));
                assertEquals(10000, rs.getLong("TOTAL_ROWS"));
                assertEquals(3500, rs.getLong("IMPORTED_ROWS"));
                assertTrue(rs.getObject("DETAILS") instanceof JsonObject);
            }
            state = "Failed";
            try (ResultSet rs = stmt.executeQuery("SHOW IMPORTS FROM t WITH (page_size=20,current_page=2)")) {
                assertTrue(rs.next());
                assertEquals(job, rs.getString("JOB_ID"));
                assertEquals("Failed", rs.getString("STATE"));
                assertEquals("invalid file", rs.getString("REASON"));
            }
        }
        assertEquals("[[\"a.parquet\"],[\"b.parquet\"]]", bodies.get(0).get(MilvusCommandKeys.REST_FILES).toString());
        assertEquals("2h", bodies.get(0).getAsJsonObject(MilvusCommandKeys.REST_OPTIONS).get(MilvusCommandKeys.TIMEOUT).getAsString());
        assertEquals(20, bodies.get(2).get(MilvusCommandKeys.REST_PAGE_SIZE).getAsInt());
        assertEquals(2, bodies.get(2).get(MilvusCommandKeys.REST_CURRENT_PAGE).getAsInt());
        assertEquals(1, paths.stream().filter(p -> p.endsWith("/create")).count());
    }

    @Test
    public void importListValidatesSqlOptionsBeforeSendingHttp() throws Exception {
        try (Connection connection = connect(new Properties()); Statement statement = connection.createStatement()) {
            for (String options : Arrays.asList("page_size=0", "current_page=-1", "page_size=1.5", "unknown_option=1")) {
                try {
                    statement.executeQuery("SHOW IMPORTS FROM t WITH (" + options + ")");
                    fail("Expected invalid import list options: " + options);
                } catch (SQLException expected) {
                    assertNotNull(expected.getMessage());
                }
            }
        }
        assertTrue("Invalid SQL options must not reach the REST client", paths.isEmpty());
    }

    @Test
    public void importListKeepsUnspecifiedPaginationOptionsAbsent() throws Exception {
        try (Connection connection = connect(new Properties()); PreparedStatement statement = connection.prepareStatement("SHOW IMPORTS FROM t WITH (current_page=?)")) {
            statement.setLong(1, 3);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
            }
        }
        assertEquals(1, bodies.size());
        assertEquals(3, bodies.get(0).get(MilvusCommandKeys.REST_CURRENT_PAGE).getAsLong());
        assertFalse(bodies.get(0).has(MilvusCommandKeys.REST_PAGE_SIZE));
    }

    @Test
    public void invalidFileGroupsDoNotStartJobs() throws Exception {
        try (Connection conn = connect(new Properties()); PreparedStatement ps = conn.prepareStatement("IMPORT FROM ? INTO t")) {
            // @formatter:off
            for (Object value : Arrays.asList(
                Collections.emptyList(),
                Arrays.asList("a.parquet", "b.parquet"),
                Collections.singletonList(Collections.emptyList()),
                Collections.singletonList(Arrays.asList("a.npy", ""))
            )) {
                // @formatter:on
                ps.setObject(1, value);
                try {
                    ps.executeUpdate();
                    fail("Invalid import input");
                } catch (SQLException expected) {
                    assertNotNull(expected.getMessage());
                }
            }
        }
        assertTrue(paths.isEmpty());
    }

    @Test
    public void importWaitFailureIncludesRecoverableJobAndProgress() throws Exception {
        state = "Failed";
        try (Connection conn = connect(new Properties()); Statement stmt = conn.createStatement()) {
            try {
                stmt.executeUpdate("IMPORT FROM [['a.npy','v.npy']] INTO t");
                fail("Expected failed job");
            } catch (SQLException e) {
                assertTrue(e.getMessage(), e.getMessage().contains("jobId=92233720368547758070"));
                assertTrue(e.getMessage(), e.getMessage().contains("importedRows=3500"));
                assertTrue(e.getMessage(), e.getMessage().contains("invalid file"));
                assertTrue(e.getMessage(), e.getMessage().contains("not cancelled or resubmitted"));
            }
        }
        assertEquals(1, paths.stream().filter(p -> p.endsWith("/create")).count());
    }

    @Test
    public void importReportsApiHttpAndJobFailures() throws Exception {
        for (String failure : Arrays.asList("api", "http", "job")) {
            apiCode = "api".equals(failure) ? 1100 : 0;
            httpStatus = "http".equals(failure) ? 503 : 200;
            state = "Failed";
            try (Connection connection = connect(new Properties()); Statement statement = connection.createStatement()) {
                statement.executeUpdate("IMPORT FROM 'data.json' INTO t");
                fail("Expected " + failure + " failure");
            } catch (SQLException expected) {
                assertTrue(expected.getMessage(), expected.getMessage().contains("failed"));
            }
        }
    }

    @Test
    public void importPollingHasBoundedWait() throws Exception {
        state = "Importing";
        try (Connection connection = connect(new Properties()); Statement statement = connection.createStatement()) {
            statement.executeUpdate("/*+ timeout=200 */ IMPORT FROM 'data.json' INTO t");
            fail("Expected timeout");
        } catch (SQLException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("Timeout"));
        }
    }

    @Test
    public void cancelInterruptsInFlightImport() throws Exception {
        delay = true;
        try (Connection connection = connect(new Properties()); Statement statement = connection.createStatement()) {
            java.util.concurrent.Future<SQLException> execution = executor.submit(() -> {
                try {
                    statement.executeUpdate("IMPORT FROM 'data.json' INTO t");
                    return null;
                } catch (SQLException e) {
                    return e;
                }
            });
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            statement.cancel();
            SQLException failure = execution.get(2, TimeUnit.SECONDS);
            assertNotNull(failure);
        }
    }
}
