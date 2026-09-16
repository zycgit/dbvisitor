/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.transaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.transaction.support.LocalTransactionManager;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Isolation changes must follow the connection actually used by each propagation scope. */
public class TransactionIsolationLifecycleTest {
    @Test
    public void requiresNewMustLeaveOuterIsolationAndUncommittedWorkUntouched() throws Exception {
        assertSuspendingScopePreservesOuter(Propagation.REQUIRES_NEW, true);
        assertSuspendingScopePreservesOuter(Propagation.REQUIRES_NEW, false);
    }

    @Test
    public void notSupportedMustApplyIsolationOnlyToItsAutocommitConnection() throws Exception {
        assertSuspendingScopePreservesOuter(Propagation.NOT_SUPPORTED, false);
    }

    private void assertSuspendingScopePreservesOuter(Propagation propagation, boolean commitInner) throws Exception {
        try (Fixture fixture = new Fixture()) {
            TransactionStatus outer = fixture.manager.begin(Propagation.REQUIRED, Isolation.READ_COMMITTED);
            ConnectionHolder outerHolder = DataSourceUtils.getHolder(fixture.dataSource);
            ConnectionState outerConnection = fixture.connections.get(0);
            fixture.insert(1);

            TransactionStatus inner = fixture.manager.begin(propagation, Isolation.SERIALIZABLE);
            ConnectionHolder innerHolder = DataSourceUtils.getHolder(fixture.dataSource);
            assertNotSame(outerHolder, innerHolder);
            assertTrue(inner.isSuspend());
            assertEquals(Connection.TRANSACTION_READ_COMMITTED, outerHolder.getConnection().getTransactionIsolation());
            assertEquals(0, outerConnection.isolationChanges);
            assertEquals(Connection.TRANSACTION_SERIALIZABLE, innerHolder.getConnection().getTransactionIsolation());
            assertEquals(propagation == Propagation.NOT_SUPPORTED, innerHolder.getConnection().getAutoCommit());
            assertEquals(0, fixture.committedCount(1));
            fixture.insert(2);

            if (commitInner) {
                fixture.manager.commit(inner);
            } else {
                fixture.manager.rollBack(inner);
            }
            assertTrue(inner.isCompleted());
            assertSame(outerHolder, DataSourceUtils.getHolder(fixture.dataSource));
            assertEquals(1, outerHolder.getRefCount());
            assertTrue(fixture.manager.isTopTransaction(outer));
            assertEquals(Connection.TRANSACTION_READ_COMMITTED, outerHolder.getConnection().getTransactionIsolation());
            assertEquals(0, outerConnection.isolationChanges);
            assertTrue(fixture.connections.get(1).connection.isClosed());
            fixture.insert(3);
            fixture.manager.rollBack(outer);

            assertEquals(0, fixture.committedCount(1));
            assertEquals(0, fixture.committedCount(3));
            assertEquals(commitInner || propagation == Propagation.NOT_SUPPORTED ? 1 : 0, fixture.committedCount(2));
            assertFalse(fixture.manager.hasTransaction());
        }
    }

    @Test
    public void joiningPropagationMustRetainItsScopedIsolationSetting() throws Exception {
        for (Propagation propagation : new Propagation[] { Propagation.REQUIRED, Propagation.NESTED, Propagation.SUPPORTS, Propagation.MANDATORY }) {
            try (Fixture fixture = new Fixture()) {
                TransactionStatus outer = fixture.manager.begin(Propagation.REQUIRED, Isolation.READ_COMMITTED);
                ConnectionHolder holder = DataSourceUtils.getHolder(fixture.dataSource);

                // Existing joined-scope semantics intentionally set then restore an explicit level.
                // Do not write before changing isolation: JDBC leaves that behavior driver-defined.
                TransactionStatus inner = fixture.manager.begin(propagation, Isolation.SERIALIZABLE);
                assertSame(holder, DataSourceUtils.getHolder(fixture.dataSource));
                assertFalse(inner.isNewConnection());
                assertEquals(Isolation.SERIALIZABLE, inner.getIsolationLevel());
                assertEquals(Connection.TRANSACTION_SERIALIZABLE, holder.getConnection().getTransactionIsolation());
                fixture.manager.commit(inner);
                assertEquals(Connection.TRANSACTION_READ_COMMITTED, holder.getConnection().getTransactionIsolation());
                assertEquals(2, fixture.connections.get(0).isolationChanges);
                assertEquals(1, holder.getRefCount());

                fixture.insert(1);
                fixture.manager.rollBack(outer);
                assertEquals(0, fixture.committedCount(1));
            }
        }
    }

    @Test
    public void defaultAndUnchangedJoinedIsolationMustNotIssueASetter() throws Exception {
        for (Isolation isolation : new Isolation[] { null, Isolation.DEFAULT, Isolation.READ_COMMITTED }) {
            try (Fixture fixture = new Fixture()) {
                TransactionStatus outer = fixture.manager.begin(Propagation.REQUIRED, Isolation.READ_COMMITTED);
                fixture.insert(1);
                TransactionStatus inner = fixture.manager.begin(Propagation.REQUIRED, isolation);
                fixture.insert(2);
                fixture.manager.commit(inner);

                assertEquals(0, fixture.connections.get(0).isolationChanges);
                assertEquals(0, fixture.committedCount(1));
                assertEquals(0, fixture.committedCount(2));
                fixture.manager.rollBack(outer);
                assertEquals(0, fixture.committedCount(1));
                assertEquals(0, fixture.committedCount(2));
            }
        }
    }

    @Test
    public void failedSuspendedBeginMustRestoreOuterBindingAndReleaseTheFailedConnection() throws Exception {
        for (FailurePoint point : new FailurePoint[] { FailurePoint.OPEN, FailurePoint.ISOLATION, FailurePoint.BEGIN }) {
            try (Fixture fixture = new Fixture()) {
                TransactionStatus outer = fixture.manager.begin(Propagation.REQUIRED, Isolation.READ_COMMITTED);
                ConnectionHolder outerHolder = DataSourceUtils.getHolder(fixture.dataSource);
                fixture.insert(1);
                SQLException failure = fixture.failNext(point);
                assertBeginFails(fixture, Propagation.REQUIRES_NEW, Isolation.SERIALIZABLE, failure);

                assertSame(outerHolder, DataSourceUtils.getHolder(fixture.dataSource));
                assertTrue(fixture.manager.isTopTransaction(outer));
                assertEquals(1, outerHolder.getRefCount());
                assertEquals(Connection.TRANSACTION_READ_COMMITTED, outerHolder.getConnection().getTransactionIsolation());
                assertEquals(0, fixture.connections.get(0).isolationChanges);
                if (fixture.connections.size() > 1) {
                    assertTrue(fixture.connections.get(1).connection.isClosed());
                }
                assertEquals(0, fixture.committedCount(1));

                TransactionStatus retry = fixture.manager.begin(Propagation.REQUIRES_NEW, Isolation.SERIALIZABLE);
                fixture.insert(2);
                fixture.manager.commit(retry);
                fixture.insert(3);
                fixture.manager.rollBack(outer);
                assertEquals(0, fixture.committedCount(1));
                assertEquals(1, fixture.committedCount(2));
                assertEquals(0, fixture.committedCount(3));
                assertFalse(fixture.manager.hasTransaction());
            }
        }
    }

    @Test
    public void failedFirstBeginMustReleaseItsHolderAndLeaveNoTransactionOnTheStack() throws Exception {
        for (FailurePoint point : new FailurePoint[] { FailurePoint.OPEN, FailurePoint.ISOLATION, FailurePoint.BEGIN }) {
            try (Fixture fixture = new Fixture()) {
                ConnectionHolder holder = DataSourceUtils.getHolder(fixture.dataSource);
                SQLException failure = fixture.failNext(point);
                assertBeginFails(fixture, Propagation.REQUIRED, Isolation.SERIALIZABLE, failure);

                assertEquals(0, holder.getRefCount());
                assertFalse(fixture.manager.hasTransaction());
                if (!fixture.connections.isEmpty()) {
                    assertTrue(fixture.connections.get(0).connection.isClosed());
                }

                TransactionStatus retry = fixture.manager.begin(Propagation.REQUIRED, Isolation.READ_COMMITTED);
                fixture.insert(1);
                fixture.manager.commit(retry);
                assertEquals(1, fixture.committedCount(1));
                assertFalse(fixture.manager.hasTransaction());
            }
        }
    }

    @Test
    public void rejectedAndFailedJoinedScopesMustLeaveTheOuterTransactionUsable() throws Exception {
        try (Fixture fixture = new Fixture()) {
            TransactionStatus outer = fixture.manager.begin(Propagation.REQUIRED, Isolation.READ_COMMITTED);
            ConnectionHolder holder = DataSourceUtils.getHolder(fixture.dataSource);
            fixture.insert(1);
            try {
                fixture.manager.begin(Propagation.NEVER, Isolation.SERIALIZABLE);
                fail("NEVER must reject the existing transaction.");
            } catch (SQLException failure) {
                assertTrue(failure.getMessage().contains("existing transaction"));
            }
            assertSame(holder, DataSourceUtils.getHolder(fixture.dataSource));
            assertEquals(1, holder.getRefCount());
            assertTrue(fixture.manager.isTopTransaction(outer));
            assertEquals(0, fixture.connections.get(0).isolationChanges);
            assertEquals(0, fixture.committedCount(1));

            SQLException failure = fixture.failNext(FailurePoint.SAVEPOINT);
            assertBeginFails(fixture, Propagation.NESTED, Isolation.READ_COMMITTED, failure);
            assertSame(holder, DataSourceUtils.getHolder(fixture.dataSource));
            assertEquals(1, holder.getRefCount());
            assertTrue(fixture.manager.isTopTransaction(outer));
            fixture.insert(2);
            fixture.manager.rollBack(outer);
            assertEquals(0, fixture.committedCount(1));
            assertEquals(0, fixture.committedCount(2));
        }
    }

    private void assertBeginFails(Fixture fixture, Propagation propagation, Isolation isolation, SQLException expected) throws Exception {
        try {
            fixture.manager.begin(propagation, isolation);
            fail("The driver failure must reach the caller.");
        } catch (SQLException actual) {
            assertSame(expected, actual);
        }
    }

    private enum FailurePoint {
        OPEN, ISOLATION, BEGIN, SAVEPOINT
    }

    private static class ConnectionState {
        private final Connection connection;
        private       int        isolationChanges;

        private ConnectionState(Connection connection) {
            this.connection = connection;
        }
    }

    private static class Fixture implements AutoCloseable {
        private final Connection               observer;
        private final DataSource               dataSource;
        private final LocalTransactionManager  manager;
        private final JdbcTemplate             jdbc;
        private final List<ConnectionState>    connections = new ArrayList<>();
        private       FailurePoint             failurePoint;
        private       SQLException             failure;

        private Fixture() throws Exception {
            String url = "jdbc:h2:mem:isolation_" + UUID.randomUUID();
            this.observer = DriverManager.getConnection(url, "sa", "");
            new JdbcTemplate(this.observer).execute("CREATE TABLE sample (id INT PRIMARY KEY)");
            this.dataSource = mock(DataSource.class);
            when(this.dataSource.getConnection()).thenAnswer(invocation -> {
                failIfRequested(FailurePoint.OPEN);
                Connection connection = DriverManager.getConnection(url, "sa", "");
                ConnectionState state = new ConnectionState(connection);
                this.connections.add(state);
                return Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] { Connection.class }, (proxy, method, args) -> {
                    if ("setTransactionIsolation".equals(method.getName())) {
                        state.isolationChanges++;
                        failIfRequested(FailurePoint.ISOLATION);
                    }
                    if ("setAutoCommit".equals(method.getName()) && Boolean.FALSE.equals(args[0])) {
                        failIfRequested(FailurePoint.BEGIN);
                    }
                    if ("setSavepoint".equals(method.getName())) {
                        failIfRequested(FailurePoint.SAVEPOINT);
                    }
                    try {
                        return method.invoke(connection, args);
                    } catch (InvocationTargetException error) {
                        throw error.getCause();
                    }
                });
            });
            this.manager = new LocalTransactionManager(this.dataSource);
            this.jdbc = new JdbcTemplate(this.dataSource);
        }

        private SQLException failNext(FailurePoint point) {
            this.failurePoint = point;
            this.failure = new SQLException("expected " + point + " failure");
            return this.failure;
        }

        private void failIfRequested(FailurePoint point) throws SQLException {
            if (this.failurePoint == point) {
                this.failurePoint = null;
                throw this.failure;
            }
        }

        private void insert(int id) throws SQLException {
            this.jdbc.executeUpdate("INSERT INTO sample (id) VALUES (?)", id);
        }

        private int committedCount(int id) throws SQLException {
            return new JdbcTemplate(this.observer).queryForInt("SELECT COUNT(*) FROM sample WHERE id = ?", id);
        }

        @Override
        public void close() throws Exception {
            try {
                while (this.manager.hasTransaction()) {
                    this.manager.rollBack();
                }
            } finally {
                try {
                    for (ConnectionState state : this.connections) {
                        state.connection.close();
                    }
                } finally {
                    this.observer.close();
                }
            }
        }
    }
}
