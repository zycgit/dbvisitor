/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.mongo.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLFeatureNotSupportedException;
import net.hasor.dbvisitor.adapter.mongo.AbstractJdbcTest;
import net.hasor.dbvisitor.adapter.mongo.MongoCommandInterceptor;
import org.junit.Test;
import static org.junit.Assert.*;

public class JdbcSupportTest extends AbstractJdbcTest {
    @Test
    public void transactionMetadataMatchesActualAdapterBehavior() throws Exception {
        MongoCommandInterceptor.resetInterceptor();
        try (Connection connection = redisConnection("test")) {
            assertFalse(connection.getMetaData().supportsTransactions());
            assertFalse(connection.getMetaData().supportsSavepoints());
            assertEquals(Connection.TRANSACTION_NONE, connection.getTransactionIsolation());
            assertTrue(connection.getAutoCommit());
            try {
                connection.setAutoCommit(false);
                fail("Mongo JDBC must not claim transactional execution without ClientSession support");
            } catch (SQLFeatureNotSupportedException expected) {
                assertTrue(connection.getAutoCommit());
            }
        }
    }

    @Test
    public void scrollableResultsAreExplicitlyUnsupported() throws Exception {
        MongoCommandInterceptor.resetInterceptor();
        try (Connection connection = redisConnection("test")) {
            assertFalse(connection.getMetaData().supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE));
            try {
                connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                fail("Mongo JDBC does not support scrollable results");
            } catch (SQLFeatureNotSupportedException expected) {
                assertNotNull(expected.getMessage());
            }
        }
    }
}
