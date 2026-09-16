/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class XmlMapperStatementAttributeSupport extends AbstractNxnContractTest {
    protected Session session;
    private   String  observedStatementFactory;
    private   Integer observedResultSetType;
    private   Integer observedQueryTimeout;
    private   Integer observedFetchSize;

    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration config = newConfiguration();
        config.loadMapper("/mapper/XmlStatementAttrMapper.xml");
        this.session = config.newSession(dataSource);
    }

    @Override
    protected void initData() throws SQLException {
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", //
                    new Object[] { baseId() + i, "StmtAttr" + i, 20 + i, "attr" + i + "@nxn.test" });
        }
    }

    protected int baseId() {
        return 955000;
    }

    protected int expectedQueryTimeout() {
        return 30;
    }

    protected int expectedFetchSize() {
        return 2;
    }

    protected int expectedCombinedFetchSize() {
        return 10;
    }

    protected <T> List<T> queryWithObservedOptions(String statementId, Object parameters) throws SQLException {
        clearObservedOptions();
        return this.session.jdbc().execute((ConnectionCallback<List<T>>) connection -> {
            Session observed = this.session.getConfiguration().newSession(observeConnection(connection));
            return observed.queryStatement(statementId, parameters);
        });
    }

    protected Object executeWithObservedOptions(String statementId, Object parameters) throws SQLException {
        clearObservedOptions();
        return this.session.jdbc().execute((ConnectionCallback<Object>) connection -> {
            Session observed = this.session.getConfiguration().newSession(observeConnection(connection));
            return observed.executeStatement(statementId, parameters);
        });
    }

    protected void assertStatementFactory(String expected) {
        assertEquals("XML statementType must select the JDBC statement factory", expected, this.observedStatementFactory);
    }

    protected void assertResultSetType(int expected) {
        assertEquals("XML resultSetType must reach the JDBC connection", Integer.valueOf(expected), this.observedResultSetType);
    }

    protected void assertQueryTimeout(int expected) {
        assertEquals("XML timeout must reach the JDBC statement", Integer.valueOf(expected), this.observedQueryTimeout);
    }

    protected void assertFetchSize(int expected) {
        assertEquals("XML fetchSize must reach the JDBC statement", Integer.valueOf(expected), this.observedFetchSize);
    }

    private void clearObservedOptions() {
        this.observedStatementFactory = null;
        this.observedResultSetType = null;
        this.observedQueryTimeout = null;
        this.observedFetchSize = null;
    }

    /** Observe successful calls while delegating all database work to the real JDBC objects. */
    private Connection observeConnection(Connection connection) {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] { Connection.class }, (proxy, method, args) -> {
            Object result = invoke(connection, method, args);
            if (result instanceof Statement && ("createStatement".equals(method.getName()) || "prepareStatement".equals(method.getName()) || "prepareCall".equals(method.getName()))) {
                this.observedStatementFactory = method.getName();
                int typeIndex = "createStatement".equals(method.getName()) ? 0 : 1;
                if (args != null && args.length >= typeIndex + 2) {
                    this.observedResultSetType = (Integer) args[typeIndex];
                }
                return observeStatement((Statement) result);
            }
            return result;
        });
    }

    private Statement observeStatement(Statement statement) {
        Class<?> statementType = statement instanceof CallableStatement ? CallableStatement.class : statement instanceof PreparedStatement ? PreparedStatement.class : Statement.class;
        return (Statement) Proxy.newProxyInstance(Statement.class.getClassLoader(), new Class<?>[] { statementType }, (proxy, method, args) -> {
            Object result = invoke(statement, method, args);
            if ("setQueryTimeout".equals(method.getName())) {
                this.observedQueryTimeout = (Integer) args[0];
            } else if ("setFetchSize".equals(method.getName())) {
                this.observedFetchSize = (Integer) args[0];
            }
            return result;
        });
    }

    private Object invoke(Object target, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    protected Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
    }

    protected void assertAscendingById(List<UserInfo> list) {
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getId() < list.get(i).getId());
        }
    }
}
