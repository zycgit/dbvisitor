/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLFeatureNotSupportedException;
import net.hasor.dbvisitor.error.UncategorizedSQLException;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class CallableQueryTest {
    @Test
    public void callableQuery_shouldNotRequireStoredProcedureMetadataSupport() throws Exception {
        try (Connection connection = DsUtils.h2Conn()) {
            assertFalse(connection.getMetaData().supportsStoredProcedures());
            JdbcTemplate jdbc = new JdbcTemplate(connection);
            Integer result = jdbc.call("SELECT ? + ?", statement -> {
                statement.setInt(1, 10);
                statement.setInt(2, 20);
            }, statement -> {
                try (ResultSet rows = statement.executeQuery()) {
                    return rows.next() ? rows.getInt(1) : null;
                }
            });
            assertEquals(Integer.valueOf(30), result);
        }
    }

    @Test
    public void callableQuery_shouldPreservePrepareCallFailure() throws Exception {
        try (Connection physical = DsUtils.h2Conn()) {
            SQLFeatureNotSupportedException unsupported = new SQLFeatureNotSupportedException("prepareCall is unavailable");
            Connection connection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] { Connection.class }, (proxy, method, args) -> {
                if ("prepareCall".equals(method.getName())) {
                    throw unsupported;
                }
                try {
                    return method.invoke(physical, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            });
            JdbcTemplate jdbc = new JdbcTemplate(connection);
            try {
                jdbc.call("CALL unsupported()", null, statement -> null);
                fail("Expected the JDBC driver to reject prepareCall");
            } catch (UncategorizedSQLException e) {
                assertSame(unsupported, e.getCause());
            }
        }
    }
}
