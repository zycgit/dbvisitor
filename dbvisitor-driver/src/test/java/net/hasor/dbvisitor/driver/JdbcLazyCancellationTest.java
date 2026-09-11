/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;

import java.sql.*;
import java.util.Collections;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;

public class JdbcLazyCancellationTest {
    private Connection connect(LegacyConnection adapter) throws SQLException {
        String name = "lazy_cancel_test";
        AdapterManager.register(name, new MockAdapterFactory() {
            @Override
            public AdapterConnection createConnection(Connection owner, String url, Properties properties) {
                return adapter;
            }
        });
        Properties props = new Properties();
        props.setProperty(JdbcDriver.P_ADAPTER_NAME, name);
        return new JdbcConnection("jdbc:dbvisitor:" + name + "://localhost", props);
    }

    private static class LegacyConnection extends MockAdapterConnection {
        protected AdapterRequest request;
        protected PendingCursor  cursor;
        protected int            legacyCancels;

        private LegacyConnection() {
            super("jdbc:dbvisitor:lazy_cancel_test://localhost", null);
        }

        @Override
        public void doRequest(AdapterRequest request, AdapterReceive receive) throws SQLException {
            this.request = request;
            this.cursor = new PendingCursor();
            receive.responseResult(request, this.cursor);
            receive.responseFinish(request);
        }

        @Override
        public void cancelRequest() {
            legacyCancels++;
        }
    }

    private static final class TargetedConnection extends LegacyConnection {
        private AdapterRequest cancelled;

        @Override
        public void cancelRequest(AdapterRequest request) {
            cancelled = request;
        }
    }

    private static final class PendingCursor extends AdapterMemoryCursor {
        private boolean pending = true;

        private PendingCursor() {
            super(Collections.singletonList(new JdbcColumn("id", AdapterType.Long, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array)), new Object[][] { { 1L } });
        }

        @Override
        public boolean isPending() {
            return pending && !isClose();
        }
    }

    @Test
    public void legacyAdapterStillReceivesNoArgumentCancellation() throws Exception {
        LegacyConnection adapter = new LegacyConnection();
        try (Connection conn = connect(adapter); Statement statement = conn.createStatement(); ResultSet result = statement.executeQuery("SELECT id")) {
            statement.cancel();
            assertEquals(1, adapter.legacyCancels);
        }
    }

    @Test
    public void targetedAdapterReceivesTheExecutingRequestAfterExecuteReturns() throws Exception {
        TargetedConnection adapter = new TargetedConnection();
        try (Connection conn = connect(adapter); Statement statement = conn.createStatement(); ResultSet result = statement.executeQuery("SELECT id")) {
            statement.cancel();
            assertSame(adapter.request, adapter.cancelled);
            assertEquals(0, adapter.legacyCancels);
        }
    }

    @Test
    public void retainedCursorRemainsCancellableAfterAdvancingPastTheLastResult() throws Exception {
        TargetedConnection adapter = new TargetedConnection();
        try (Connection conn = connect(adapter); Statement statement = conn.createStatement(); ResultSet result = statement.executeQuery("SELECT id")) {
            assertFalse(statement.getMoreResults(Statement.KEEP_CURRENT_RESULT));
            assertFalse(result.isClosed());
            statement.cancel();
            assertSame(adapter.request, adapter.cancelled);
        }
    }

    @Test
    public void finishedOrClosedCursorsDoNotTriggerCancellation() throws Exception {
        LegacyConnection adapter = new LegacyConnection();
        try (Connection conn = connect(adapter); Statement statement = conn.createStatement()) {
            statement.cancel();
            try (ResultSet result = statement.executeQuery("SELECT id")) {
                adapter.cursor.pending = false;
                statement.cancel();
            }
            statement.cancel();
            assertEquals(0, adapter.legacyCancels);
        }
    }
}
