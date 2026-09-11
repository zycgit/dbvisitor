package net.hasor.dbvisitor.adapter.milvus.commands;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.utility.request.FlushAllReq;
import io.milvus.v2.service.utility.request.FlushReq;
import io.milvus.v2.service.utility.request.GetFlushAllStateReq;
import io.milvus.v2.service.utility.response.FlushAllResp;
import io.milvus.v2.service.utility.response.GetFlushAllStateResp;
import net.hasor.dbvisitor.adapter.milvus.MilvusCommandInterceptor;
import net.hasor.dbvisitor.adapter.milvus.MilvusCustomClient;
import net.hasor.dbvisitor.adapter.milvus.MilvusKeys;
import net.hasor.dbvisitor.driver.JdbcDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusFlushTest {
    private final List<Object> requests  = new ArrayList<>();
    private final List<String> methods   = new ArrayList<>();
    private final long         timestamp = 9007199254740993L;
    private       Boolean      flushed   = true;
    private       String       failedMethod;

    @Before
    public void install() {
        MilvusCommandInterceptor.resetInterceptor();
        MilvusCommandInterceptor.addInterceptor(MilvusClientV2.class, (proxy, method, args) -> {
            if (method.getName().equals("getServerVersion")) {
                return "v2.6.2";
            }
            methods.add(method.getName());
            if (args.length > 0) {
                requests.add(args[0]);
            }
            if (method.getName().equals(failedMethod)) {
                throw new IllegalStateException("flush request failed");
            }
            if (method.getName().equals("flushAll")) {
                return FlushAllResp.builder().flushAllTs(timestamp).build();
            }
            if (method.getName().equals("getFlushAllState")) {
                return GetFlushAllStateResp.builder().flushed(flushed).build();
            }
            return null;
        });
    }

    @After
    public void cleanup() {
        MilvusCommandInterceptor.resetInterceptor();
    }

    private Connection connect() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty(MilvusKeys.CUSTOM_MILVUS, MilvusCustomClient.class.getName());
        properties.setProperty(MilvusKeys.INTERCEPTOR, MilvusCommandInterceptor.class.getName());
        return new JdbcDriver().connect("jdbc:dbvisitor:milvus://test:19530/flush_db", properties);
    }

    @Test
    public void collectionFlushKeepsLegacyAllIdentifierAndPassesListAndTimeout() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertEquals(0, statement.executeUpdate("FLUSH all"));
            assertEquals(0, statement.executeUpdate("FLUSH first, second IN DATABASE other_db WITH (wait_flushed_timeout_ms=0)"));
            assertEquals("flush_db", connection.getCatalog());
        }
        FlushReq legacy = (FlushReq) requests.get(0);
        assertEquals(Collections.singletonList("all"), legacy.getCollectionNames());
        assertEquals("flush_db", legacy.getDatabaseName());
        assertEquals(Long.valueOf(60000), legacy.getWaitFlushedTimeoutMs());
        FlushReq multiple = (FlushReq) requests.get(1);
        assertEquals(Arrays.asList("first", "second"), multiple.getCollectionNames());
        assertEquals("other_db", multiple.getDatabaseName());
        assertEquals(Long.valueOf(0), multiple.getWaitFlushedTimeoutMs());
        assertEquals(Arrays.asList("flush", "flush"), methods);
    }

    @Test
    public void flushAllReturnsNativeLongTimestampWithoutDriverPollingOrEnumeration() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            assertTrue(statement.execute("FLUSH ALL TABLES"));
            assertEquals(-1, statement.getUpdateCount());
            try (ResultSet rows = statement.getResultSet()) {
                assertEquals(1, rows.getMetaData().getColumnCount());
                assertEquals(Types.BIGINT, rows.getMetaData().getColumnType(1));
                assertTrue(rows.next());
                assertEquals(timestamp, rows.getLong("FLUSH_ALL_TS"));
                assertFalse(rows.next());
            }
            assertFalse(statement.getMoreResults());
        }
        FlushAllReq request = (FlushAllReq) requests.get(0);
        assertEquals("flush_db", request.getDatabaseName());
        assertEquals(Long.valueOf(60000), request.getWaitFlushedTimeoutMs());
        assertEquals(Collections.singletonList("flushAll"), methods);
    }

    @Test
    public void scopesAndParametersRemainIndependentAcrossMultipleStatements() throws SQLException {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("""
                FLUSH ALL TABLES IN DATABASE other_db WITH (wait_flushed_timeout_ms=?);
                SHOW FLUSH ALL ? IN DATABASE other_db;
                FLUSH ALL TABLES IN DATABASE * WITH (wait_flushed_timeout_ms=?);
                SHOW FLUSH ALL ? IN DATABASE *
                """)) {
            statement.setObject(1, BigInteger.valueOf(1234));
            statement.setLong(2, timestamp);
            statement.setObject(3, BigDecimal.ZERO);
            statement.setObject(4, BigInteger.valueOf(Long.MAX_VALUE));
            assertTrue(statement.execute());
            for (int i = 0; i < 4; i++) {
                try (ResultSet rows = statement.getResultSet()) {
                    assertTrue(rows.next());
                    assertEquals(i == 3 ? Long.MAX_VALUE : timestamp, rows.getLong("FLUSH_ALL_TS"));
                    assertFalse(rows.next());
                }
                assertEquals(i < 3, statement.getMoreResults());
            }
            assertEquals("flush_db", connection.getCatalog());
        }
        assertEquals("other_db", ((FlushAllReq) requests.get(0)).getDatabaseName());
        assertEquals(Long.valueOf(1234), ((FlushAllReq) requests.get(0)).getWaitFlushedTimeoutMs());
        assertEquals("other_db", ((GetFlushAllStateReq) requests.get(1)).getDatabaseName());
        assertEquals("", ((FlushAllReq) requests.get(2)).getDatabaseName());
        assertEquals("", ((GetFlushAllStateReq) requests.get(3)).getDatabaseName());
        assertEquals(Arrays.asList("flushAll", "getFlushAllState", "flushAll", "getFlushAllState"), methods);
    }

    @Test
    public void statePreservesFalseAndNullWithoutWaitingOrInventingCompletion() throws SQLException {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement("SHOW FLUSH ALL ?")) {
            statement.setLong(1, 0);
            for (Boolean state : Arrays.asList(true, false, null)) {
                flushed = state;
                try (ResultSet rows = statement.executeQuery()) {
                    assertTrue(rows.next());
                    assertEquals(0, rows.getLong("FLUSH_ALL_TS"));
                    assertEquals(Boolean.TRUE.equals(state), rows.getBoolean("FLUSHED"));
                    assertEquals(state == null, rows.wasNull());
                    assertFalse(rows.next());
                }
            }
        }
        assertEquals(Arrays.asList("getFlushAllState", "getFlushAllState", "getFlushAllState"), methods);
    }

    @Test
    public void invalidBoundsAndOptionsFailBeforeNativeCallsAndPermitReuse() throws SQLException {
        Object[] invalid = { null, -1L, new BigDecimal("1.5"), new BigInteger("9223372036854775808"), "1000", true };
        try (Connection connection = connect(); Statement plain = connection.createStatement()) {
            for (String sql : Arrays.asList("FLUSH first WITH (wait_flushed_timeout_ms=?)", "FLUSH ALL TABLES WITH (wait_flushed_timeout_ms=?)", "SHOW FLUSH ALL ?")) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    for (Object value : invalid) {
                        statement.setObject(1, value);
                        assertThrows(SQLException.class, statement::execute);
                    }
                }
            }
            assertThrows(SQLException.class, () -> plain.execute("FLUSH ALL TABLES WITH (sync=false)"));
            assertThrows(SQLException.class, () -> plain.execute("FLUSH first WITH (wait_flushed_timeout_ms=1, timeout=2)"));
            assertTrue(methods.isEmpty());
            assertEquals(0, plain.executeUpdate("FLUSH first WITH (wait_flushed_timeout_ms=1)"));
        }
    }

    @Test
    public void valueParametersCannotBecomeNamesOrAccidentallySelectAllDatabases() throws SQLException {
        try (Connection connection = connect()) {
            for (String sql : Arrays.asList("FLUSH ?", "FLUSH first IN DATABASE ?", "FLUSH ALL TABLES IN DATABASE ?", "SHOW FLUSH ALL 1 IN DATABASE ?")) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, "other_db");
                    assertThrows(SQLException.class, statement::execute);
                }
            }
            try (Statement statement = connection.createStatement()) {
                assertThrows(SQLException.class, () -> statement.execute("FLUSH ALL TABLES IN DATABASE ``"));
                assertThrows(SQLException.class, () -> statement.execute("SHOW FLUSH ALL 1 IN DATABASE ``"));
            }
        }
        assertTrue(methods.isEmpty());
    }

    @Test
    public void failuresDoNotRunLaterCommandsOrRetryThroughCollectionEnumeration() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            failedMethod = "flushAll";
            assertThrows(SQLException.class, () -> statement.execute("FLUSH ALL TABLES; FLUSH first"));
            assertEquals(Collections.singletonList("flushAll"), methods);
            failedMethod = "getFlushAllState";
            assertThrows(SQLException.class, () -> statement.executeQuery("SHOW FLUSH ALL 1"));
            failedMethod = null;
            assertEquals(0, statement.executeUpdate("FLUSH first"));
        }
        assertEquals(Arrays.asList("flushAll", "getFlushAllState", "flush"), methods);
    }
}
