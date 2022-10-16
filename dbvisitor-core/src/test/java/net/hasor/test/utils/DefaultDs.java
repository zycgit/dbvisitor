package net.hasor.test.utils;
import net.hasor.cobble.CollectionUtils;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DefaultDs implements DataSource, Closeable {
    private String       url;
    private String       driverClassName;
    private String       username;
    private String       password;
    private List<String> initSql;

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
    //---------------------------------------------------------------------
    // Implementation of JDBC 4.1's getParentLogger method
    //---------------------------------------------------------------------

    @Override
    public void close() throws IOException {

    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.getConnection(this.username, this.password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
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

}
