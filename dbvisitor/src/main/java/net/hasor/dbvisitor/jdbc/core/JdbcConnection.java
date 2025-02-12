/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.jdbc.core;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.DynamicConnection;
import net.hasor.dbvisitor.jdbc.StatementCallback;
import net.hasor.dbvisitor.transaction.ConnectionProxy;
import net.hasor.dbvisitor.transaction.DataSourceUtils;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Objects;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-10-16
 */
public class JdbcConnection extends JdbcAccessor {
    private static final Logger  logger         = LoggerFactory.getLogger(JdbcConnection.class);
    /*JDBC查询和从结果集里面每次取设置行数，循环去取，直到取完。合理设置该参数可以避免内存异常。
     * 如果这个变量被设置为非零值,它将被用于设置 statements 的 fetchSize 属性。*/
    private              int     fetchSize      = 0;
    /*从 JDBC 中可以查询的最大行数。
     * 如果这个变量被设置为非零值,它将被用于设置 statements 的 maxRows 属性。*/
    private              int     maxRows        = 0;
    /*从 JDBC 中可以查询的最大行数。
     * 如果这个变量被设置为非零值,它将被用于设置 statements 的 queryTimeout 属性。*/
    private              int     queryTimeout   = 0;
    /*是否忽略出现的 SQL 警告*/
    private              boolean ignoreWarnings = true;
    /* 当 SQL 执行错误是否打印错误日志 */
    private              boolean printStmtError = false;

    /**
     * Construct a new JdbcConnection for bean usage.
     * <p>Note: The DataSource has to be set before using the instance.
     * @see #setDataSource
     */
    public JdbcConnection() {
    }

    /**
     * Construct a new JdbcConnection, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     */
    public JdbcConnection(final DataSource dataSource) {
        this.setDataSource(dataSource);
    }

    /**
     * Construct a new JdbcConnection, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param conn the JDBC Connection
     */
    public JdbcConnection(final Connection conn) {
        this.setConnection(conn);
    }

    /**
     * Construct a new JdbcConnection, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dynamicConn the JDBC Connection of dynamic
     */
    public JdbcConnection(final DynamicConnection dynamicConn) {
        this.setDynamic(dynamicConn);
    }

    public int getFetchSize() {
        return this.fetchSize;
    }

    public void setFetchSize(final int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public int getMaxRows() {
        return this.maxRows;
    }

    public void setMaxRows(final int maxRows) {
        this.maxRows = maxRows;
    }

    public int getQueryTimeout() {
        return this.queryTimeout;
    }

    public void setQueryTimeout(final int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public boolean isIgnoreWarnings() {
        return this.ignoreWarnings;
    }

    public void setIgnoreWarnings(final boolean ignoreWarnings) {
        this.ignoreWarnings = ignoreWarnings;
    }

    public boolean isPrintStmtError() {
        return this.printStmtError;
    }

    public void setPrintStmtError(boolean printStmtError) {
        this.printStmtError = printStmtError;
    }

    public <T> T execute(final ConnectionCallback<T> action) throws SQLException {
        Objects.requireNonNull(action, "Callback object must not be null");

        Connection localConn = this.getConnection();
        DataSource localDS = this.getDataSource();
        DynamicConnection localDynamic = this.getDynamic();
        boolean usingConn = (localConn != null);
        boolean usingDynamic = !usingConn && (localDynamic != null);
        boolean usingDS = !usingConn && !usingDynamic && (localDS != null);

        if (!usingConn && !usingDynamic && !usingDS) {
            throw new IllegalArgumentException("Connection unavailable, any of (Connection/DynamicConnection/DataSource) is required.");
        }
        if (logger.isDebugEnabled()) {
            logger.trace("database connection using " + (usingConn ? "connection" : usingDynamic ? "dynamic" : "dataSource"));
        }

        Connection oriConn = localConn;
        Connection useConn;
        if (usingConn) {
            useConn = this.newProxyConnection(localConn);// 代理连接,忽略 close
        } else if (usingDynamic) {
            oriConn = localDynamic.getConnection();
            useConn = this.newProxyConnection(oriConn);// 代理连接,忽略 close
        } else {
            oriConn = DataSourceUtils.getConnection(localDS);// 通过资源管理器创建 Connection
            useConn = oriConn;
        }

        try {
            return action.doInConnection(useConn);
        } finally {
            if (usingDynamic) {
                localDynamic.releaseConnection(oriConn);
            } else if (usingDS) {
                oriConn.close();
            } else {
                // don't do anything
            }
        }
    }

    public <T> T execute(final StatementCallback<T> action) throws SQLException {
        Objects.requireNonNull(action, "Callback object must not be null");
        return this.execute((ConnectionCallback<T>) con -> {
            String stmtSQL = "";
            try (Statement stmt = con.createStatement()) {
                applyStatementSettings(stmt);
                stmtSQL = stmt.toString();
                T result = action.doInStatement(stmt);
                handleWarnings(stmt);
                return result;
            } catch (SQLException ex) {
                if (this.printStmtError) {
                    logger.error(stmtSQL, ex);
                }
                throw ex;
            }
        });
    }

    /** 对Statement的属性进行设置。设置 JDBC Statement 对象的 fetchSize、maxRows、Timeout等参数。 */
    protected void applyStatementSettings(final Statement stmt) throws SQLException {
        int fetchSize = this.getFetchSize();
        if (fetchSize > 0) {
            stmt.setFetchSize(fetchSize);
        }
        int maxRows = this.getMaxRows();
        if (maxRows > 0) {
            stmt.setMaxRows(maxRows);
        }
        int timeout = this.getQueryTimeout();
        if (timeout > 0) {
            stmt.setQueryTimeout(timeout);
        }
    }

    /** 处理潜在的 SQL 警告。当要求不忽略 SQL 警告时，检测到 SQL 警告抛出 SQL 异常。 */
    protected void handleWarnings(final Statement stmt) throws SQLException {
        if (this.isIgnoreWarnings()) {
            if (logger.isDebugEnabled()) {
                SQLWarning warningToLog = stmt.getWarnings();
                while (warningToLog != null) {
                    logger.trace("SQLWarning ignored: SQL state '" + warningToLog.getSQLState() + "', "//
                            + "error code '" + warningToLog.getErrorCode() + "', " //
                            + "message [" + warningToLog.getMessage() + "]."//
                    );
                    warningToLog = warningToLog.getNextWarning();
                }
            }
        } else {
            SQLWarning warning = stmt.getWarnings();
            if (warning != null) {
                throw new SQLException("Warning not ignored", warning);
            }
        }
    }

    /** 获取与本地线程绑定的数据库连接，JDBC 框架会维护这个连接的事务。开发者不必关心该连接的事务管理，以及资源释放操作。 */
    private ConnectionProxy newProxyConnection(final Connection target) {
        Objects.requireNonNull(target, "Connection is null.");
        CloseSuppressingInvocationHandler handler = new CloseSuppressingInvocationHandler(target);
        return (ConnectionProxy) Proxy.newProxyInstance(ConnectionProxy.class.getClassLoader(), new Class[] { ConnectionProxy.class }, handler);
    }

    /** Connection 接口代理，目的是为了控制一些方法的调用。同时进行一些特殊类型的处理。 */
    private class CloseSuppressingInvocationHandler implements InvocationHandler {
        private final Connection target;

        public CloseSuppressingInvocationHandler(final Connection target) {
            this.target = target;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            // Invocation on ConnectionProxy interface coming in...
            switch (method.getName()) {
                case "getTargetConnection":
                    // Handle getTargetConnection method: return underlying Connection.
                    return this.target;
                case "equals":
                    // Only consider equal when proxies are identical.
                    return proxy == args[0];
                case "hashCode":
                    // Use hashCode of PersistenceManager proxy.
                    return System.identityHashCode(proxy);
                case "close":
                    return null;
            }
            // Invoke method on target Connection.
            try {
                Object retVal = method.invoke(this.target, args);
                // If return value is a JDBC Statement, apply statement settings (fetch size, max rows, transaction timeout).
                if (retVal instanceof Statement) {
                    JdbcConnection.this.applyStatementSettings((Statement) retVal);
                }
                return retVal;
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }
}