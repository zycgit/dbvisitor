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
import java.sql.SQLFeatureNotSupportedException;
import java.util.UUID;
import javax.sql.DataSource;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.transaction.support.LocalTransactionManager;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** Nested transaction behavior when the driver cannot release savepoints. */
public class NestedSavepointTest {
    @Test
    public void nestedCommitShouldWaitForOuterCommit() throws Exception {
        try (Fixture fixture = new Fixture(new SQLFeatureNotSupportedException("releaseSavepoint"))) {
            TransactionStatus outer = fixture.begin();
            fixture.insert(1);
            TransactionStatus nested = fixture.begin();
            fixture.insert(2);

            fixture.manager.commit(nested);
            assertTrue(nested.isCompleted());
            assertFalse(outer.isCompleted());
            assertEquals(0, fixture.committedCount());
            assertEquals(Integer.valueOf(2), fixture.jdbc.queryForInt("SELECT COUNT(*) FROM sample"));

            fixture.manager.commit(outer);
            assertEquals(2, fixture.committedCount());
            assertEquals(1, fixture.releaseAttempts);
        }
    }

    @Test
    public void outerRollbackShouldUndoCommittedNestedWrites() throws Exception {
        try (Fixture fixture = new Fixture(new SQLFeatureNotSupportedException("releaseSavepoint"))) {
            TransactionStatus outer = fixture.begin();
            fixture.insert(1);
            TransactionStatus nested = fixture.begin();
            fixture.insert(2);
            fixture.manager.commit(nested);

            fixture.manager.rollBack(outer);
            assertEquals(0, fixture.committedCount());
        }
    }

    @Test
    public void nestedRollbackShouldPreserveOuterWrites() throws Exception {
        try (Fixture fixture = new Fixture(new SQLFeatureNotSupportedException("releaseSavepoint"))) {
            TransactionStatus outer = fixture.begin();
            fixture.insert(1);
            TransactionStatus nested = fixture.begin();
            fixture.insert(2);
            fixture.manager.rollBack(nested);
            fixture.insert(3);

            fixture.manager.commit(outer);
            assertEquals(2, fixture.committedCount());
            assertEquals(0, fixture.releaseAttempts);
        }
    }

    @Test
    public void parentSavepointShouldRollbackCommittedChild() throws Exception {
        try (Fixture fixture = new Fixture(new SQLFeatureNotSupportedException("releaseSavepoint"))) {
            TransactionStatus outer = fixture.begin();
            fixture.insert(1);
            TransactionStatus parent = fixture.begin();
            fixture.insert(2);
            TransactionStatus child = fixture.begin();
            fixture.insert(3);
            fixture.manager.commit(child);
            fixture.manager.rollBack(parent);

            // A later savepoint must not collide with the retained child savepoint.
            TransactionStatus sibling = fixture.begin();
            fixture.insert(4);
            fixture.manager.commit(sibling);
            fixture.manager.commit(outer);
            assertEquals(2, fixture.committedCount());
            assertEquals(2, fixture.releaseAttempts);
        }
    }

    @Test
    public void outerCommitShouldCompleteOpenNestedTransactions() throws Exception {
        try (Fixture fixture = new Fixture(new SQLFeatureNotSupportedException("releaseSavepoint"))) {
            TransactionStatus outer = fixture.begin();
            fixture.insert(1);
            TransactionStatus nested = fixture.begin();
            fixture.insert(2);
            fixture.manager.commit(outer);

            assertTrue(nested.isCompleted());
            assertFalse(fixture.manager.hasTransaction());
            assertEquals(2, fixture.committedCount());
        }
    }

    @Test
    public void supportedReleaseShouldStillBeCalled() throws Exception {
        try (Fixture fixture = new Fixture(null)) {
            TransactionStatus outer = fixture.begin();
            TransactionStatus nested = fixture.begin();
            fixture.insert(1);
            fixture.manager.commit(nested);
            assertEquals(1, fixture.releaseAttempts);
            assertEquals(0, fixture.committedCount());
            fixture.manager.commit(outer);
            assertEquals(1, fixture.committedCount());
        }
    }

    @Test
    public void unchangedNestedIsolationShouldNotCommitAndOuterIsolationShouldRestore() throws Exception {
        try (Fixture fixture = new Fixture(null)) {
            TransactionStatus outer = fixture.manager.begin(Propagation.NESTED, Isolation.REPEATABLE_READ);
            fixture.insert(1);
            TransactionStatus nested = fixture.manager.begin(Propagation.NESTED, Isolation.REPEATABLE_READ);
            fixture.insert(2);
            fixture.manager.commit(nested);

            assertEquals(0, fixture.committedCount());
            assertEquals(1, fixture.isolationChanges);
            fixture.manager.commit(outer);
            assertEquals(2, fixture.committedCount());
            assertEquals(2, fixture.isolationChanges);
            assertEquals(Connection.TRANSACTION_READ_COMMITTED, fixture.lastIsolation);
        }
    }

    @Test
    public void releaseSqlErrorShouldPropagateAndRollback() throws Exception {
        assertReleaseFailurePropagates(new SQLException("Connection lost", "08006"));
    }

    @Test
    public void releaseErrorWithoutSqlStateMustNotBeAssumedUnsupported() throws Exception {
        assertReleaseFailurePropagates(new SQLException("This operation is not supported.", null, 0));
    }

    private void assertReleaseFailurePropagates(SQLException failure) throws Exception {
        try (Fixture fixture = new Fixture(failure)) {
            TransactionStatus outer = fixture.begin();
            fixture.insert(1);
            TransactionStatus nested = fixture.begin();
            fixture.insert(2);
            try {
                fixture.manager.commit(nested);
                fail("A real release error must not be treated as unsupported");
            } catch (SQLException actual) {
                assertSame(failure, actual);
            }
            assertTrue(nested.isCompleted());
            fixture.manager.rollBack(outer);
            assertEquals(0, fixture.committedCount());
        }
    }

    private static class Fixture implements AutoCloseable {
        private final Connection observer;
        private final LocalTransactionManager manager;
        private final JdbcTemplate jdbc;
        private int releaseAttempts;
        private int isolationChanges;
        private int lastIsolation;

        private Fixture(SQLException releaseFailure) throws Exception {
            String url = "jdbc:h2:mem:nested_" + UUID.randomUUID();
            this.observer = DriverManager.getConnection(url, "sa", "");
            new JdbcTemplate(this.observer).execute("CREATE TABLE sample (id INT PRIMARY KEY)");
            DataSource dataSource = mock(DataSource.class);
            when(dataSource.getConnection()).thenAnswer(invocation -> {
                Connection connection = DriverManager.getConnection(url, "sa", "");
                return Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] { Connection.class }, (proxy, method, args) -> {
                    if ("setTransactionIsolation".equals(method.getName())) {
                        this.isolationChanges++;
                        this.lastIsolation = (Integer) args[0];
                    }
                    if ("releaseSavepoint".equals(method.getName())) {
                        this.releaseAttempts++;
                        if (releaseFailure != null) {
                            throw releaseFailure;
                        }
                    }
                    try {
                        return method.invoke(connection, args);
                    } catch (InvocationTargetException error) {
                        throw error.getCause();
                    }
                });
            });
            this.manager = new LocalTransactionManager(dataSource);
            this.jdbc = new JdbcTemplate(dataSource);
        }

        private TransactionStatus begin() throws SQLException {
            return this.manager.begin(Propagation.NESTED, Isolation.READ_COMMITTED);
        }

        private void insert(int id) throws SQLException {
            this.jdbc.executeUpdate("INSERT INTO sample (id) VALUES (?)", id);
        }

        private int committedCount() throws SQLException {
            return new JdbcTemplate(this.observer).queryForInt("SELECT COUNT(*) FROM sample");
        }

        @Override
        public void close() throws Exception {
            try {
                this.manager.close();
            } finally {
                this.observer.close();
            }
        }
    }
}
