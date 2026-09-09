package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import io.milvus.grpc.*;
import io.milvus.shaded.io.grpc.Server;
import io.milvus.shaded.io.grpc.ServerBuilder;
import io.milvus.shaded.io.grpc.stub.StreamObserver;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exercises the default connection factory through the official SDK and a local gRPC service. */
public class MilvusSingleAddressTest {
    private final Service service = new Service();
    private Server server;

    private static final class Service extends MilvusServiceGrpc.MilvusServiceImplBase {
        final List<DeleteRequest> deletes = new CopyOnWriteArrayList<>();
        final List<InsertRequest> inserts = new CopyOnWriteArrayList<>();
        final AtomicInteger versions = new AtomicInteger();
        volatile boolean failDelete;

        @Override
        public void connect(ConnectRequest request, StreamObserver<ConnectResponse> observer) {
            observer.onNext(ConnectResponse.newBuilder().setStatus(success()).build());
            observer.onCompleted();
        }

        @Override
        public void listDatabases(ListDatabasesRequest request, StreamObserver<ListDatabasesResponse> observer) {
            observer.onNext(ListDatabasesResponse.newBuilder().setStatus(success()).addDbNames("db1").build());
            observer.onCompleted();
        }

        @Override
        public void getVersion(GetVersionRequest request, StreamObserver<GetVersionResponse> observer) {
            versions.incrementAndGet();
            observer.onNext(GetVersionResponse.newBuilder().setStatus(success()).setVersion("v2.6.2").build());
            observer.onCompleted();
        }

        @Override
        public void delete(DeleteRequest request, StreamObserver<MutationResult> observer) {
            deletes.add(request);
            if (failDelete) {
                observer.onError(io.milvus.shaded.io.grpc.Status.UNAVAILABLE.withDescription("service unavailable").asRuntimeException());
            } else {
                observer.onNext(MutationResult.newBuilder().setStatus(success()).setDeleteCnt(5).build());
                observer.onCompleted();
            }
        }

        @Override
        public void describeCollection(DescribeCollectionRequest request, StreamObserver<DescribeCollectionResponse> observer) {
            CollectionSchema schema = CollectionSchema.newBuilder().setName("t")
                    .addFields(FieldSchema.newBuilder().setName("id").setDataType(DataType.Int64).setIsPrimaryKey(true).setAutoID(true))
                    .addFields(FieldSchema.newBuilder().setName("val").setDataType(DataType.Int32)).build();
            observer.onNext(DescribeCollectionResponse.newBuilder().setStatus(success()).setSchema(schema).build());
            observer.onCompleted();
        }

        @Override
        public void insert(InsertRequest request, StreamObserver<MutationResult> observer) {
            inserts.add(request);
            observer.onNext(MutationResult.newBuilder().setStatus(success()).setInsertCnt(1)
                    .setIDs(IDs.newBuilder().setIntId(LongArray.newBuilder().addData(42))).build());
            observer.onCompleted();
        }

        private static Status success() {
            return Status.newBuilder().setErrorCode(ErrorCode.Success).build();
        }
    }

    @Before
    public void setup() throws Exception {
        server = ServerBuilder.forPort(0).addService(service).build().start();
    }

    @After
    public void cleanup() throws Exception {
        server.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    private Connection connect(int maxRetry) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.MAX_RETRY, Integer.toString(maxRetry));
        properties.setProperty(MilvusKeys.CONNECT_TIMEOUT, "2000");
        properties.setProperty(MilvusKeys.RPC_DEADLINE, "2000");
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://127.0.0.1:" + server.getPort() + "/db1", properties);
    }

    @Test
    public void readsAndWritesUseTheConfiguredService() throws Exception {
        try (Connection connection = connect(0); Statement statement = connection.createStatement()) {
            assertEquals("db1", connection.getCatalog());
            assertEquals("v2.6.2", connection.getMetaData().getDatabaseProductVersion());
            try (ResultSet databases = statement.executeQuery("SHOW DATABASES")) {
                assertTrue(databases.next());
                assertEquals("db1", databases.getString(1));
                assertFalse(databases.next());
            }
            assertEquals(5, statement.executeUpdate("DELETE FROM t WHERE id > 0"));
        }
        assertEquals(1, service.versions.get());
        assertEquals(1, service.deletes.size());
        assertEquals("db1", service.deletes.get(0).getDbName());
        assertEquals("id > 0", service.deletes.get(0).getExpr());
    }

    @Test
    public void generatedKeysReturnTheSdkAcknowledgement() throws Exception {
        try (Connection connection = connect(0); Statement statement = connection.createStatement()) {
            assertEquals(1, statement.executeUpdate("INSERT INTO t (val) VALUES (7)", Statement.RETURN_GENERATED_KEYS));
            try (ResultSet keys = statement.getGeneratedKeys()) {
                assertEquals("id", keys.getMetaData().getColumnName(1));
                assertTrue(keys.next());
                assertEquals(42, keys.getLong(1));
                assertFalse(keys.next());
            }
        }
        assertEquals(1, service.inserts.size());
        assertEquals("db1", service.inserts.get(0).getDbName());
    }

    @Test
    public void disabledRetriesMakeOneSdkAttempt() throws Exception {
        assertDeleteAttempts(0);
    }

    @Test
    public void driverRetryLimitIsNotMultipliedBySdkRetries() throws Exception {
        assertDeleteAttempts(2);
    }

    private void assertDeleteAttempts(int maxRetry) throws Exception {
        service.failDelete = true;
        try (Connection connection = connect(maxRetry); Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM t WHERE id > 0");
            fail("Expected delete failure");
        } catch (SQLException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains((maxRetry + 1) + " attempt"));
        }
        assertEquals(maxRetry + 1, service.deletes.size());
    }
}
