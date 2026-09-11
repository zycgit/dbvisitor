/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.*;
import java.util.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exercise JDBC entry points with intercepted adapter requests/responses. */
public class JdbcBoundaryRegressionTest {
    private JdbcConnection connection;
    private AdapterRequest lastRequest;
    private ResponseScript script;

    private interface ResponseScript {
        void respond(AdapterRequest request, AdapterReceive receive) throws SQLException;
    }

    @Before
    public void open() throws Exception {
        Class.forName("net.hasor.dbvisitor.driver.JdbcDriver");
        script = (request, receive) -> receive.responseUpdateCount(request, 1);
        AdapterManager.register("boundary-regression", new MockAdapterFactory() {
            @Override
            public AdapterConnection createConnection(Connection owner, String url, Properties properties) {
                return new MockAdapterConnection(url, "test") {
                    @Override
                    public void doRequest(AdapterRequest request, AdapterReceive receive) throws SQLException {
                        lastRequest = request;
                        script.respond(request, receive);
                        receive.responseFinish(request);
                    }
                };
            }
        });
        Properties properties = new Properties();
        properties.setProperty(JdbcDriver.P_ADAPTER_NAME, "boundary-regression");
        connection = new JdbcConnection("jdbc:dbvisitor:boundary-regression://localhost", properties);
    }

    @After
    public void close() {
        connection.close();
        AdapterManager.register("boundary-regression", new MockAdapterFactory());
    }

    private AdapterMemoryCursor cursor(Object value, String type) {
        return new AdapterMemoryCursor(Collections.singletonList(new JdbcColumn("v", type, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array)), new Object[][] { { value } });
    }

    private void assertBound(String name, String type, Object value) {
        JdbcArg arg = lastRequest.getArgMap().get(name);
        assertNotNull(arg);
        assertEquals(type, arg.getType());
        assertEquals(value, arg.getValue());
    }

    @Test
    public void outLongPreservesAll64BitsAndNull() throws Exception {
        try (CallableStatement statement = connection.prepareCall("CALL wide(?)")) {
            statement.registerOutParameter(1, Types.BIGINT);
            for (Long value : new Long[] { Long.MIN_VALUE, -4294967296L, 4294967296L, Long.MAX_VALUE, null }) {
                script = (request, receive) -> {
                    receive.responseParameter(request, "arg1", AdapterType.Long, value);
                    receive.responseUpdateCount(request, 0);
                };
                statement.execute();
                assertEquals(value == null ? 0L : value.longValue(), statement.getLong(1));
                assertEquals(value == null, statement.wasNull());
                assertEquals(value == null ? 0L : value.longValue(), statement.getLong("arg1"));
                assertEquals(value == null, statement.wasNull());
            }
        }
    }

    @Test
    public void timeCalendarBindsBothIndexedAndNamedParameters() throws Exception {
        try (CallableStatement statement = connection.prepareCall("CALL times(?, ?)")) {
            for (String zone : new String[] { "UTC", "Asia/Shanghai", "America/New_York" }) {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(zone));
                long calendarMillis = calendar.getTimeInMillis();
                for (String instant : new String[] { "2026-01-15T12:30:00Z", "2026-07-15T12:30:00Z" }) {
                    Time value = new Time(Instant.parse(instant).toEpochMilli());
                    statement.setTime(1, value, calendar);
                    statement.setTime("named", value, calendar);
                    statement.execute();
                    OffsetTime expected = OffsetTime.ofInstant(Instant.parse(instant), ZoneId.of(zone));
                    assertBound("arg1", AdapterType.OffsetTime, expected);
                    assertBound("named", AdapterType.OffsetTime, expected);
                    assertEquals(calendarMillis, calendar.getTimeInMillis());
                    assertEquals(Instant.parse(instant).toEpochMilli(), value.getTime());
                }
                statement.setTime(1, null, calendar);
                statement.setTime("named", null, calendar);
                statement.execute();
                assertBound("arg1", AdapterType.SqlTime, null);
                assertBound("named", AdapterType.SqlTime, null);
            }
            Time value = Time.valueOf("12:30:00");
            statement.setTime(1, value, null);
            statement.setTime("named", value, null);
            statement.execute();
            assertBound("arg1", AdapterType.SqlTime, value);
            assertBound("named", AdapterType.SqlTime, value);
        }
    }

    private SQLType namedType(String name) {
        return new SQLType() {
            public String getName() {
                return name;
            }

            public String getVendor() {
                return "test";
            }

            public Integer getVendorTypeNumber() {
                return null;
            }
        };
    }

    @Test
    public void namedSqlTypeBindsAndInvalidTypeDoesNotChangePreviousValue() throws Exception {
        try (CallableStatement statement = connection.prepareCall("CALL typed(?)")) {
            statement.setObject("value", 12, JDBCType.INTEGER);
            statement.execute();
            assertBound("value", AdapterType.Int, 12);
            assertThrows(SQLException.class, () -> statement.setObject("value", 99, null));
            assertThrows(SQLException.class, () -> statement.setObject("value", 99, namedType("")));
            statement.execute();
            assertBound("value", AdapterType.Int, 12);
            statement.setObject("value", "text", namedType("vendor-text"));
            statement.execute();
            assertBound("value", "vendor-text", "text");
            statement.setObject("value", null, JDBCType.INTEGER);
            statement.execute();
            assertBound("value", AdapterType.Int, null);
        }
    }

    @Test
    public void decimalScaleAppliedAcrossAllSetObjectOverloads() throws Exception {
        try (CallableStatement statement = connection.prepareCall("CALL decimals(?, ?)")) {
            for (String input : new String[] { "1.234", "1.235", "-1.235", "1" }) {
                BigDecimal value = new BigDecimal(input);
                BigDecimal expected = value.setScale(2, java.math.RoundingMode.HALF_UP);
                statement.setObject(1, value, Types.DECIMAL, 2);
                statement.setObject(2, value, JDBCType.NUMERIC, 2);
                statement.setObject("namedInt", value, Types.NUMERIC, 2);
                statement.setObject("namedType", value, JDBCType.DECIMAL, 2);
                statement.execute();
                for (String name : new String[] { "arg1", "arg2", "namedInt", "namedType" }) {
                    assertBound(name, AdapterType.BigDecimal, expected);
                }
            }
            statement.setObject(1, null, Types.DECIMAL, 2);
            statement.setObject("namedType", null, JDBCType.DECIMAL, 2);
            statement.setObject(2, "1.236", Types.DECIMAL, 2);
            statement.setObject("integer", 17, JDBCType.INTEGER, 2);
            statement.execute();
            assertBound("arg1", AdapterType.BigDecimal, null);
            assertBound("namedType", AdapterType.BigDecimal, null);
            assertBound("arg2", AdapterType.BigDecimal, new BigDecimal("1.24"));
            assertBound("integer", AdapterType.Int, 17);
            assertThrows(SQLException.class, () -> statement.setObject(2, "bad", Types.DECIMAL, 2));
            statement.execute();
            assertBound("arg2", AdapterType.BigDecimal, new BigDecimal("1.24"));
        }
    }

    private ByteArrayInputStream ascii() {
        return new ByteArrayInputStream("abcdef".getBytes(StandardCharsets.US_ASCII));
    }

    @Test
    public void asciiOverloadsBindCharacterDataAndRespectLength() throws Exception {
        try (CallableStatement statement = connection.prepareCall("CALL ascii(?, ?, ?)")) {
            statement.setAsciiStream(1, ascii());
            statement.setAsciiStream(2, ascii(), 3);
            statement.setAsciiStream(3, ascii(), 3L);
            statement.setAsciiStream("all", ascii());
            statement.setAsciiStream("intLength", ascii(), 3);
            statement.setAsciiStream("longLength", ascii(), 3L);
            statement.execute();
            assertBound("arg1", AdapterType.String, "abcdef");
            assertBound("all", AdapterType.String, "abcdef");
            for (String name : new String[] { "arg2", "arg3", "intLength", "longLength" }) {
                assertBound(name, AdapterType.String, "abc");
            }
            statement.setAsciiStream(1, null);
            statement.setAsciiStream(2, null, 0);
            statement.setAsciiStream(3, null, 0L);
            statement.setAsciiStream("all", null);
            statement.setAsciiStream("intLength", null, 0);
            statement.setAsciiStream("longLength", null, 0L);
            statement.execute();
            for (String name : lastRequest.getArgMap().keySet()) {
                assertBound(name, AdapterType.String, null);
            }
            statement.setAsciiStream(1, ascii(), 0);
            statement.setAsciiStream("all", ascii(), 0L);
            statement.execute();
            assertBound("arg1", AdapterType.String, "");
            assertBound("all", AdapterType.String, "");
            assertThrows(SQLException.class, () -> statement.setAsciiStream(1, ascii(), -1));
            assertThrows(SQLException.class, () -> statement.setAsciiStream("all", ascii(), -1L));
        }
    }

    @Test
    public void calendarBasedYearMonthConversionPreservesYearAndMonth() throws Exception {
        for (int month : new int[] { Calendar.JANUARY, Calendar.SEPTEMBER, Calendar.DECEMBER }) {
            Calendar calendar = new GregorianCalendar(2026, month, 15, 12, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long millis = calendar.getTimeInMillis();
            Object[] values = { calendar, calendar.getTime(), new java.sql.Date(millis), new Timestamp(millis), Instant.ofEpochMilli(millis), Long.toString(millis) };
            for (Object value : values) {
                script = (request, receive) -> receive.responseResult(request, cursor(value, AdapterType.SqlTimestamp));
                try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SELECT date")) {
                    assertTrue(result.next());
                    assertEquals(Year.of(2026), result.getObject(1, Year.class));
                    assertEquals(YearMonth.of(2026, month + 1), result.getObject(1, YearMonth.class));
                    assertEquals(Month.of(month + 1), result.getObject(1, Month.class));
                }
            }
        }
    }

    @Test
    public void closeOnCompletionKeepsFollowingUpdateCountsAccessible() throws Exception {
        script = (request, receive) -> {
            receive.responseResult(request, cursor(1, AdapterType.Int));
            receive.responseUpdateCount(request, 7);
            receive.responseUpdateCount(request, 8);
        };
        try (Statement statement = connection.createStatement()) {
            statement.closeOnCompletion();
            assertTrue(statement.execute("SELECT; UPDATE; UPDATE"));
            statement.getResultSet().close();
            assertFalse(statement.isClosed());
            assertFalse(statement.getMoreResults());
            assertFalse(statement.isClosed());
            assertEquals(7, statement.getUpdateCount());
            assertFalse(statement.getMoreResults());
            assertFalse(statement.isClosed());
            assertEquals(8, statement.getUpdateCount());
            assertFalse(statement.getMoreResults());
            assertTrue(statement.isClosed());
        }
    }

    @Test
    public void closeOnCompletionDoesNotHideLaterError() throws Exception {
        script = (request, receive) -> {
            receive.responseResult(request, cursor(1, AdapterType.Int));
            receive.responseFailed(request, new SQLException("later failure"));
        };
        try (Statement statement = connection.createStatement()) {
            statement.closeOnCompletion();
            statement.execute("SELECT; FAIL");
            statement.getResultSet().close();
            assertFalse(statement.isClosed());
            assertEquals("later failure", assertThrows(SQLException.class, statement::getMoreResults).getMessage());
            assertTrue(statement.isClosed());
        }
    }

    @Test
    public void implicitCloseOnGetMoreResultsKeepsUpdateCountAccessible() throws Exception {
        script = (request, receive) -> {
            receive.responseResult(request, cursor(1, AdapterType.Int));
            receive.responseUpdateCount(request, 7);
        };
        try (Statement statement = connection.createStatement()) {
            statement.closeOnCompletion();
            statement.execute("SELECT; UPDATE");
            ResultSet first = statement.getResultSet();
            assertFalse(statement.getMoreResults());
            assertTrue(first.isClosed());
            assertFalse(statement.isClosed());
            assertEquals(7, statement.getUpdateCount());
            assertFalse(statement.getMoreResults());
            assertTrue(statement.isClosed());
        }
    }

    @Test
    public void delayedFinishDoesNotCloseStatementWithUnreadUpdateCount() throws Exception {
        try (JdbcStatement statement = (JdbcStatement) connection.createStatement()) {
            statement.closeOnCompletion();
            AdapterRequest request = new MockAdapterRequest("async results");
            statement.container.prepareReceive(request);
            statement.container.responseResult(request, cursor(1, AdapterType.Int));
            statement.getResultSet().close();
            assertFalse(statement.isClosed());
            statement.container.responseUpdateCount(request, 7);
            statement.container.responseFinish(request);
            assertFalse(statement.isClosed());
            assertFalse(statement.getMoreResults());
            assertFalse(statement.isClosed());
            assertEquals(7, statement.getUpdateCount());
            statement.getMoreResults();
            assertTrue(statement.isClosed());
        }
    }

    @Test
    public void generatedKeysCanStillTriggerCloseOnCompletion() throws Exception {
        script = (request, receive) -> receive.responseUpdateCount(request, 1, cursor(99L, AdapterType.Long));
        Statement statement = connection.createStatement();
        statement.closeOnCompletion();
        assertEquals(1, statement.executeUpdate("INSERT", Statement.RETURN_GENERATED_KEYS));
        try (ResultSet keys = statement.getGeneratedKeys()) {
            assertTrue(keys.next());
            assertEquals(99L, keys.getLong(1));
            assertFalse(statement.isClosed());
        }
        assertTrue(statement.isClosed());
    }

    @Test
    public void wrapperChecksActualRuntimeTypeForStatementsAndMetadata() throws Exception {
        script = (request, receive) -> receive.responseResult(request, cursor(1, AdapterType.Int));
        try (Statement statement = connection.createStatement(); PreparedStatement prepared = connection.prepareStatement("SELECT ?"); CallableStatement callable = connection.prepareCall("CALL p"); ResultSet result = statement.executeQuery("SELECT 1")) {
            assertFalse(statement.isWrapperFor(PreparedStatement.class));
            assertThrows(SQLException.class, () -> statement.unwrap(PreparedStatement.class));
            assertTrue(prepared.isWrapperFor(PreparedStatement.class));
            assertSame(prepared, prepared.unwrap(PreparedStatement.class));
            assertFalse(prepared.isWrapperFor(CallableStatement.class));
            assertThrows(SQLException.class, () -> prepared.unwrap(CallableStatement.class));
            assertSame(callable, callable.unwrap(PreparedStatement.class));
            Wrapper[] wrappers = { connection, statement, prepared, callable, result, connection.getMetaData(), result.getMetaData(), prepared.getParameterMetaData() };
            for (Wrapper wrapper : wrappers) {
                assertTrue(wrapper.isWrapperFor(Wrapper.class));
                assertSame(wrapper, wrapper.unwrap(Wrapper.class));
                assertFalse(wrapper.isWrapperFor(Runnable.class));
                assertThrows(SQLException.class, () -> wrapper.unwrap(Runnable.class));
            }
            assertFalse(result.getMetaData().isWrapperFor(PreparedStatement.class));
            assertSame(connection, result.unwrap(Connection.class));
            assertSame(connection.typeSupport(), result.unwrap(TypeSupport.class));
        }
    }
}
