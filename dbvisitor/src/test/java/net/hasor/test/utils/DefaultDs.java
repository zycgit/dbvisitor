package net.hasor.test.utils;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.function.EConsumer;
import net.hasor.cobble.io.IOUtils;
import net.hasor.dbvisitor.transaction.ConnectionProxy;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultDs implements DataSource, Closeable {
    private String                              url;
    private String                              driverClassName;
    private String                              username;
    private String                              password;
    private List<String>                        initSql;
    private EConsumer<Connection, SQLException> consumer;
    private Lock                                syncLock = new ReentrantLock();
    private Queue<Connection>                   freeConn = new LinkedList<>();
    private Queue<Connection>                   usedConn = new LinkedList<>();

    /** Returns 0, indicating the default system timeout is to be used. */
    @Override
    public int getLoginTimeout() {
        return 0;
    }

    /** Setting a login timeout is not supported. */
    @Override
    public void setLoginTimeout(int timeout) {
        throw new UnsupportedOperationException("setLoginTimeout");
    }

    /** LogWriter methods are not supported. */
    @Override
    public PrintWriter getLogWriter() {
        throw new UnsupportedOperationException("getLogWriter");
    }

    /** LogWriter methods are not supported. */
    @Override
    public void setLogWriter(PrintWriter pw) {
        throw new UnsupportedOperationException("setLogWriter");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        throw new SQLException("DataSource of type [" + getClass().getName() + "] cannot be unwrapped as [" + iface.getName() + "]");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(this);
    }

    @Override
    public java.util.logging.Logger getParentLogger() {
        return java.util.logging.Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setConnectionInitSqls(List<String> initSql) {
        this.initSql = initSql;
    }

    public void setConsumer(EConsumer<Connection, SQLException> consumer) {
        this.consumer = consumer;
    }

    //---------------------------------------------------------------------
    // Implementation of JDBC 4.1's getParentLogger method
    //---------------------------------------------------------------------

    @Override
    public void close() throws IOException {
        try {
            this.syncLock.lock();

            for (Connection c : this.usedConn) {
                IOUtils.closeQuietly(c);
            }
            for (Connection c : this.freeConn) {
                IOUtils.closeQuietly(c);
            }

            this.usedConn.clear();
            this.freeConn.clear();
        } finally {
            this.syncLock.unlock();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.getConnection(this.username, this.password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        try {
            this.syncLock.lock();

            Connection conn = null;
            if (this.freeConn.isEmpty()) {
                conn = newConnection();
            } else {
                conn = this.freeConn.poll();
            }

            this.usedConn.offer(conn);
            return newProxyConnection(conn);
        } finally {
            this.syncLock.unlock();
        }
    }

    private Connection newConnection() throws SQLException {
        try {
            Class.forName(this.driverClassName);
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }

        Connection conn = DriverManager.getConnection(url, username, password);

        if (CollectionUtils.isNotEmpty(this.initSql)) {
            try (Statement s = conn.createStatement()) {
                for (String script : this.initSql) {
                    s.execute(script);
                }
            }
        }

        return conn;
    }

    private ConnectionProxy newProxyConnection(final Connection target) {
        Objects.requireNonNull(target, "Connection is null.");
        CloseSuppressingInvocationHandler handler = new CloseSuppressingInvocationHandler(target);
        return (ConnectionProxy) Proxy.newProxyInstance(ConnectionProxy.class.getClassLoader(), new Class[] { ConnectionProxy.class }, handler);
    }

    private class CloseSuppressingInvocationHandler implements InvocationHandler {
        private final Connection target;
        private       boolean    close;

        public CloseSuppressingInvocationHandler(final Connection target) {
            this.target = target;
            this.close = false;
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
                    if (this.close) {
                        throw new SQLException("conn is closed.");
                    } else {
                        this.close = true;
                        try {
                            syncLock.lock();
                            usedConn.remove(this.target);
                            freeConn.offer(this.target);
                            return null;
                        } finally {
                            syncLock.unlock();
                        }
                    }
            }
            // Invoke method on target Connection.
            try {
                return method.invoke(this.target, args);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }
}
