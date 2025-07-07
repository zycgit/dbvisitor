package net.hasor.dbvisitor.driver;

import net.hasor.cobble.concurrent.timer.HashedWheelTimer;
import net.hasor.cobble.concurrent.timer.Timeout;
import net.hasor.cobble.concurrent.timer.TimerTask;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class AdapterConnection implements Closeable {

    private final static HashedWheelTimer     TIMER;
    private final        AdapterInfo          adapterInfo;
    private final        Map<String, Timeout> timeout;

    static {
        TIMER = new HashedWheelTimer();
    }

    public AdapterConnection(String jdbcUrl, String userName) {
        this.adapterInfo = new AdapterInfo();
        this.adapterInfo.setUrl(jdbcUrl);
        this.adapterInfo.setUserName(userName);
        this.adapterInfo.setDbVersion(this.jdbcVersion());
        this.adapterInfo.setDriverVersion(this.driverVersion());
        this.timeout = new ConcurrentHashMap<>();
    }

    protected AdapterVersion driverVersion() {
        AdapterVersion jdbc = new AdapterVersion();
        jdbc.setName(JdbcDriver.NAME);
        jdbc.setVersion(JdbcDriver.VERSION);
        jdbc.setMajorVersion(JdbcDriver.VERSION_MAJOR);
        jdbc.setMinorVersion(JdbcDriver.VERSION_MINOR);
        return jdbc;
    }

    protected AdapterVersion jdbcVersion() {
        AdapterVersion jdbc = new AdapterVersion();
        jdbc.setName(JdbcDriver.NAME);
        jdbc.setVersion(JdbcDriver.VERSION);
        jdbc.setMajorVersion(JdbcDriver.VERSION_MAJOR);
        jdbc.setMinorVersion(JdbcDriver.VERSION_MINOR);
        return jdbc;
    }

    public AdapterInfo getInfo() {
        return this.adapterInfo;
    }

    public abstract void setCatalog(String catalog) throws SQLException;

    public abstract String getCatalog() throws SQLException;

    public abstract void setSchema(String schema) throws SQLException;

    public abstract String getSchema() throws SQLException;

    public final <T> T unwrap(Class<T> iface, Object target) throws SQLException {
        if (iface.isInstance(target)) {
            return (T) target;
        }

        // find JdbcConnection.
        JdbcConnection jdbcConn;
        Statement jdbcStatement;
        ResultSet jdbcResultSet;
        if (target instanceof JdbcConnection) {
            jdbcResultSet = null;
            jdbcStatement = null;
            jdbcConn = (JdbcConnection) target;
        } else if (target instanceof JdbcDatabaseMetaData) {
            jdbcResultSet = null;
            jdbcStatement = null;
            jdbcConn = (JdbcConnection) ((JdbcDatabaseMetaData) target).getConnection();
        } else if (target instanceof JdbcParameterMetaData) {
            jdbcResultSet = null;
            jdbcStatement = ((JdbcParameterMetaData) target).getStatement();
            jdbcConn = (JdbcConnection) jdbcStatement.getConnection();
        } else if (target instanceof JdbcStatement) {
            jdbcResultSet = null;
            jdbcStatement = (Statement) target;
            jdbcConn = (JdbcConnection) jdbcStatement.getConnection();
        } else if (target instanceof JdbcResultSet) {
            jdbcResultSet = (ResultSet) target;
            jdbcStatement = ((JdbcResultSet) target).getStatement();
            jdbcConn = (JdbcConnection) jdbcStatement.getConnection();
        } else if (target instanceof JdbcResultSetMetaData) {
            jdbcResultSet = ((JdbcResultSetMetaData) target).getResultSet();
            jdbcStatement = ((JdbcResultSetMetaData) target).getStatement();
            jdbcConn = (JdbcConnection) ((JdbcResultSetMetaData) target).getStatement().getConnection();
        } else {
            jdbcResultSet = null;
            jdbcStatement = null;
            jdbcConn = null;
        }

        //
        if (TransactionSupport.class.isAssignableFrom(iface)) {
            return (T) jdbcConn.txSupport();
        } else if (TypeSupport.class.isAssignableFrom(iface)) {
            return (T) jdbcConn.typeSupport();
        } else if (Connection.class.isAssignableFrom(iface)) {
            return (T) jdbcConn;
        } else if (Statement.class.isAssignableFrom(iface)) {
            return (T) jdbcStatement;
        } else if (ResultSet.class.isAssignableFrom(iface)) {
            return (T) jdbcResultSet;
        } else {
            return null;
        }
    }

    //

    public void startTimer(String traceId, int timeoutMs, TimerTask timerTask) {
        this.timeout.put(traceId, TIMER.newTimeout(t -> {
            try {
                timerTask.run(t);
            } finally {
                this.timeout.remove(traceId);
            }
        }, timeoutMs, TimeUnit.MILLISECONDS));
    }

    public void stopTimer(String traceId) {
        Timeout timeout = this.timeout.get(traceId);
        if (timeout != null) {
            timeout.cancel();
            this.timeout.remove(traceId);
        }
    }

    //

    public abstract AdapterRequest newRequest(String sql);

    public abstract void doRequest(AdapterRequest request, AdapterReceive receive) throws SQLException;

    public abstract void cancelQuery(AdapterRequest request);

    @Override
    public abstract void close();
}
