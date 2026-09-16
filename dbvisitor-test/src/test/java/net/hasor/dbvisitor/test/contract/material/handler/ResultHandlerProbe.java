/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.extractor.ColumnMapResultSetExtractor;
import net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper;
import static org.junit.Assert.*;

/** Observes successful close calls while delegating all JDBC work to the resources owned by dbVisitor. */
public final class ResultHandlerProbe {
    private static final ThreadLocal<ResultHandlerProbe> ACTIVE          = new ThreadLocal<>();
    private final        SQLException                    failure;
    private              ResultSet                       resultSet;
    private              Statement                       statement;
    private              int                             invocations;
    private final        Map<ResultSet, ResultSet>       observedResults = new IdentityHashMap<>();
    private final        Map<ResultSet, Boolean>         closedResults   = new IdentityHashMap<>();

    private ResultHandlerProbe(SQLException failure) {
        this.failure = failure;
    }

    /** Fixture boundary: outside a verification the datasource returns its original connections. */
    public static DataSource observe(DataSource dataSource) {
        return (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(), new Class<?>[] { DataSource.class }, (proxy, method, args) -> {
            Object result = invoke(dataSource, method, args);
            if (ACTIVE.get() != null && result instanceof Connection && "getConnection".equals(method.getName())) {
                return observe((Connection) result);
            }
            return result;
        });
    }

    /** Native fixtures retain their connection; only statements created during verification are observed. */
    public static Connection observe(Connection connection) {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] { Connection.class }, (proxy, method, args) -> {
            Object result = invoke(connection, method, args);
            ResultHandlerProbe probe = ACTIVE.get();
            if (probe != null && result instanceof Statement) {
                return probe.observeStatement((Statement) result);
            }
            return result;
        });
    }

    private Statement observeStatement(Statement statement) {
        Class<?> statementType = statement instanceof CallableStatement ? CallableStatement.class : statement instanceof PreparedStatement ? PreparedStatement.class : Statement.class;
        return (Statement) Proxy.newProxyInstance(Statement.class.getClassLoader(), new Class<?>[] { statementType }, (proxy, method, args) -> {
            Object result = invoke(statement, method, args);
            if (result instanceof ResultSet) {
                return observeResultSet((ResultSet) result);
            }
            return result;
        });
    }

    private ResultSet observeResultSet(ResultSet resultSet) {
        ResultSet existing = this.observedResults.get(resultSet);
        if (existing != null) {
            return existing;
        }
        ResultSet observed = (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(), new Class<?>[] { ResultSet.class }, (proxy, method, args) -> {
            Object result = invoke(resultSet, method, args);
            if ("close".equals(method.getName())) {
                // Some drivers report isClosed=false after a successful close of an exhausted result.
                // A throwing close must not count as successful resource cleanup.
                this.closedResults.put((ResultSet) proxy, true);
            }
            return result;
        });
        this.observedResults.put(resultSet, observed);
        this.closedResults.put(observed, false);
        return observed;
    }

    private static Object invoke(Object target, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    public static void record(ResultSet resultSet) throws SQLException {
        ResultHandlerProbe probe = ACTIVE.get();
        if (probe == null) {
            return;
        }
        assertFalse("ResultSet must be open inside its handler", resultSet.isClosed());
        Statement statement = resultSet.getStatement();
        assertNotNull("The query must expose its owning statement", statement);
        assertFalse("Statement must be open inside its handler", statement.isClosed());
        if (probe.resultSet == null) {
            probe.resultSet = resultSet;
            probe.statement = statement;
        } else {
            assertSame("Every row belongs to the same ResultSet", probe.resultSet, resultSet);
            assertSame("Every row belongs to the same statement", probe.statement, statement);
        }
        probe.invocations++;
        if (probe.failure != null) {
            throw probe.failure;
        }
    }

    public static <T> T verify(int expectedInvocations, Query<T> query) throws SQLException {
        assertNull("Result handler probes must not overlap", ACTIVE.get());
        ResultHandlerProbe probe = new ResultHandlerProbe(null);
        ACTIVE.set(probe);
        try {
            T result = query.execute();
            probe.assertCompleted(expectedInvocations);
            return result;
        } finally {
            ACTIVE.remove();
        }
    }

    public static void verifyFailure(Query<?> query) throws SQLException {
        assertNull("Result handler probes must not overlap", ACTIVE.get());
        SQLException expected = new SQLException("NXN result handler failure", "HY000");
        ResultHandlerProbe probe = new ResultHandlerProbe(expected);
        ACTIVE.set(probe);
        try {
            Exception actual = assertThrows(Exception.class, query::execute);
            Throwable cause = actual;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            // Mapper interfaces without a checked throws clause may wrap the same SQLException.
            assertSame("The handler failure must reach the caller", expected, cause);
            probe.assertCompleted(1);
        } finally {
            ACTIVE.remove();
        }
    }

    private void assertCompleted(int expectedInvocations) throws SQLException {
        assertEquals("Handler invocation count", expectedInvocations, this.invocations);
        if (expectedInvocations > 0) {
            assertNotNull(this.resultSet);
            assertTrue("The fixture must observe the handler ResultSet", this.closedResults.containsKey(this.resultSet));
            assertTrue("dbVisitor must successfully close the handler ResultSet", this.closedResults.get(this.resultSet));
            assertTrue("dbVisitor must close the handler statement", this.statement.isClosed());
        } else {
            // An empty RowMapper/RowCallback query does not expose its ResultSet to the handler.
            assertNull(this.resultSet);
        }
    }

    @FunctionalInterface
    public interface Query<T> {
        T execute() throws SQLException;
    }

    /** Keeps the built-in mapping behavior while observing XML-configured handler resources. */
    public static class ColumnMapMapper implements RowMapper<Map<String, Object>> {
        private final ColumnMapRowMapper delegate = new ColumnMapRowMapper();

        @Override
        public Map<String, Object> mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            record(resultSet);
            return this.delegate.mapRow(resultSet, rowNum);
        }
    }

    /** Keeps the built-in extraction behavior while observing XML-configured handler resources. */
    public static class ColumnMapExtractor extends ColumnMapResultSetExtractor {
        @Override
        public List<Map<String, Object>> extractData(ResultSet resultSet) throws SQLException {
            record(resultSet);
            return super.extractData(resultSet);
        }
    }
}
