/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.unit.nxn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import javax.sql.DataSource;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.test.contract.material.handler.ResultHandlerProbe;
import org.junit.Test;
import static org.junit.Assert.*;

public class ResultHandlerProbeTest {
    @Test
    public void successfulCloseMustNotDependOnDriverIsClosedReporting() throws SQLException {
        try (DriverFixture fixture = new DriverFixture(null)) {
            JdbcTemplate jdbc = new JdbcTemplate(ResultHandlerProbe.observe(fixture.dataSource()));
            Integer result = ResultHandlerProbe.verify(1, () -> jdbc.query("SELECT CAST(NULL AS VARCHAR) AS missing, 7 AS present", rs -> {
                ResultHandlerProbe.record(rs);
                assertSame(fixture.statement, rs.getStatement());
                assertSame(fixture.resultSet, rs.unwrap(ResultSet.class));
                assertEquals(2, rs.getMetaData().getColumnCount());
                assertEquals("PRESENT", rs.getMetaData().getColumnLabel(2));
                assertTrue(rs.next());
                assertNull(rs.getString("missing"));
                assertTrue(rs.wasNull());
                int value = rs.getInt("present");
                assertFalse(rs.wasNull());
                assertFalse(rs.next());
                return value;
            }));
            assertEquals(Integer.valueOf(7), result);
            assertEquals(1, fixture.closeCalls);
            assertFalse("The simulated driver still reports open after close", fixture.driverResult.isClosed());
            assertTrue(fixture.resultSet.isClosed());
            assertTrue(fixture.statement.isClosed());
        }
    }

    @Test
    public void handlerFailureMustRetainItsCauseAndCloseBothResources() throws SQLException {
        try (DriverFixture fixture = new DriverFixture(null)) {
            JdbcTemplate jdbc = new JdbcTemplate(ResultHandlerProbe.observe(fixture.connection));
            ResultHandlerProbe.verifyFailure(() -> jdbc.query("SELECT 1", (ResultSetExtractor<Void>) rs -> {
                ResultHandlerProbe.record(rs);
                fail("The probe must throw the handler's original SQLException");
                return null;
            }));
            assertEquals(1, fixture.closeCalls);
            assertTrue(fixture.statement.isClosed());
        }
    }

    @Test
    public void statementCleanupMustNotHideMissingResultSetClose() throws SQLException {
        try (DriverFixture fixture = new DriverFixture(null)) {
            Connection observed = ResultHandlerProbe.observe(fixture.connection);
            AssertionError failure = assertThrows(AssertionError.class, () -> ResultHandlerProbe.verify(1, () -> {
                try (Statement statement = observed.createStatement()) {
                    ResultSet rs = statement.executeQuery("SELECT 1");
                    assertTrue(rs.next());
                    ResultHandlerProbe.record(rs);
                    // Deliberately omit rs.close(): statement cleanup alone must not satisfy the probe.
                    return null;
                }
            }));
            assertEquals("dbVisitor must successfully close the handler ResultSet", failure.getMessage());
            assertEquals(0, fixture.closeCalls);
            assertTrue(fixture.statement.isClosed());
            assertTrue(fixture.resultSet.isClosed());
        }
    }

    @Test
    public void throwingCloseMustNotCountAsSuccessfulCleanup() throws SQLException {
        SQLException expected = new SQLException("driver close failed", "HY000");
        try (DriverFixture fixture = new DriverFixture(expected)) {
            Connection observed = ResultHandlerProbe.observe(fixture.connection);
            AssertionError failure = assertThrows(AssertionError.class, () -> ResultHandlerProbe.verify(1, () -> {
                try (Statement statement = observed.createStatement()) {
                    ResultSet rs = statement.executeQuery("SELECT 1");
                    assertTrue(rs.next());
                    ResultHandlerProbe.record(rs);
                    assertSame("Reflection must not wrap the driver's SQLException", expected, assertThrows(SQLException.class, rs::close));
                    return null;
                }
            }));
            assertEquals("dbVisitor must successfully close the handler ResultSet", failure.getMessage());
            assertEquals(1, fixture.closeCalls);
            assertTrue(fixture.statement.isClosed());
        }
    }

    @Test
    public void inactiveObservationMustLeaveReturnedResourcesUnchanged() throws SQLException {
        try (DriverFixture fixture = new DriverFixture(null)) {
            DataSource observed = ResultHandlerProbe.observe(fixture.dataSource());
            assertSame(fixture.connection, observed.getConnection());
            Connection connection = ResultHandlerProbe.observe(fixture.connection);
            try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery("SELECT 1")) {
                assertSame(fixture.driverStatement, statement);
                assertSame(fixture.driverResult, rs);
            }
        }
    }

    /** H2 does the real JDBC work; only the driver's close reporting/failure is controlled. */
    private static final class DriverFixture implements AutoCloseable {
        private final Connection delegate = DriverManager.getConnection("jdbc:h2:mem:result_handler_probe");
        private final Connection connection;
        private       Statement  statement;
        private       Statement  driverStatement;
        private       ResultSet  resultSet;
        private       ResultSet  driverResult;
        private       int        closeCalls;

        private DriverFixture(SQLException closeFailure) throws SQLException {
            this.connection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] { Connection.class }, (proxy, method, args) -> {
                Object result = invoke(this.delegate, method, args);
                if (!(result instanceof Statement)) {
                    return result;
                }
                this.statement = (Statement) result;
                Class<?> statementType = result instanceof PreparedStatement ? PreparedStatement.class : Statement.class;
                this.driverStatement = (Statement) Proxy.newProxyInstance(Statement.class.getClassLoader(), new Class<?>[] { statementType }, (statementProxy, statementMethod, statementArgs) -> {
                    Object statementResult = invoke(this.statement, statementMethod, statementArgs);
                    if (!(statementResult instanceof ResultSet)) {
                        return statementResult;
                    }
                    this.resultSet = (ResultSet) statementResult;
                    this.driverResult = (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(), new Class<?>[] { ResultSet.class }, (resultProxy, resultMethod, resultArgs) -> {
                        if ("isClosed".equals(resultMethod.getName())) {
                            return false;
                        }
                        if ("close".equals(resultMethod.getName())) {
                            this.closeCalls++;
                            if (closeFailure != null) {
                                throw closeFailure;
                            }
                        }
                        return invoke(this.resultSet, resultMethod, resultArgs);
                    });
                    return this.driverResult;
                });
                return this.driverStatement;
            });
        }

        private DataSource dataSource() {
            return (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(), new Class<?>[] { DataSource.class }, (proxy, method, args) -> {
                if ("getConnection".equals(method.getName())) {
                    return this.connection;
                }
                if (method.getDeclaringClass() == Object.class) {
                    return invoke(this, method, args);
                }
                throw new UnsupportedOperationException(method.getName());
            });
        }

        @Override
        public void close() throws SQLException {
            this.delegate.close();
        }
    }

    private static Object invoke(Object target, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
