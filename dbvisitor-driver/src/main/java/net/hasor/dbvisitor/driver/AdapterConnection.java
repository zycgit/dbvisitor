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
package net.hasor.dbvisitor.driver;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.hasor.cobble.concurrent.timer.HashedWheelTimer;
import net.hasor.cobble.concurrent.timer.Timeout;
import net.hasor.cobble.concurrent.timer.TimerTask;

public abstract class AdapterConnection implements Closeable {
    private final static HashedWheelTimer     TIMER;
    private final        String               objectId;
    private final        AdapterInfo          adapterInfo;
    private final        AdapterFeatures      features;
    private final        Map<String, Timeout> timeout;

    static {
        TIMER = new HashedWheelTimer();
    }

    public AdapterConnection(String jdbcUrl, String userName) {
        this.objectId = UUID.randomUUID().toString().replace("-", "");
        this.adapterInfo = new AdapterInfo();
        this.adapterInfo.setUrl(jdbcUrl);
        this.adapterInfo.setUserName(userName);
        this.adapterInfo.setDbVersion(this.dbVersion());
        this.adapterInfo.setDriverVersion(this.driverVersion());
        this.features = new AdapterFeatures();
        this.timeout = new ConcurrentHashMap<>();
    }

    public String getObjectId() {
        return this.objectId;
    }

    public AdapterFeatures getFeatures() {
        return this.features;
    }

    public int getDefaultGeneratedKeys() {
        return this.features.boolFeatureVal(AdapterFeatureKey.ReturnGeneratedKeys) ?//
                Statement.RETURN_GENERATED_KEYS ://
                Statement.NO_GENERATED_KEYS;
    }

    protected AdapterVersion driverVersion() {
        AdapterVersion jdbc = new AdapterVersion();
        jdbc.setName(JdbcDriver.NAME);
        jdbc.setVersion(JdbcDriver.VERSION);
        jdbc.setMajorVersion(JdbcDriver.VERSION_MAJOR);
        jdbc.setMinorVersion(JdbcDriver.VERSION_MINOR);
        return jdbc;
    }

    protected AdapterVersion dbVersion() {
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
        T value = this.findWrapper(iface, target);
        if (value == null) {
            throw new SQLException("Not a wrapper for " + iface.getName(), JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }
        return value;
    }

    public final boolean isWrapperFor(Class<?> iface, Object target) throws SQLException {
        return this.findWrapper(iface, target) != null;
    }

    private <T> T findWrapper(Class<T> iface, Object target) throws SQLException {
        if (iface == null) {
            throw new SQLException("Wrapper type must not be null.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }
        if (iface.isInstance(target)) {
            return iface.cast(target);
        }

        // find JdbcConnection.
        JdbcConnection jdbcConn;
        Statement jdbcStatement;
        ResultSet jdbcResultSet;
        if (target instanceof JdbcConnection c) {
            jdbcResultSet = null;
            jdbcStatement = null;
            jdbcConn = c;
        } else if (target instanceof JdbcDatabaseMetaData m) {
            jdbcResultSet = null;
            jdbcStatement = null;
            jdbcConn = (JdbcConnection) m.getConnection();
        } else if (target instanceof JdbcParameterMetaData m) {
            jdbcResultSet = null;
            jdbcStatement = m.getStatement();
            jdbcConn = (JdbcConnection) jdbcStatement.getConnection();
        } else if (target instanceof JdbcStatement s) {
            jdbcResultSet = null;
            jdbcStatement = s;
            jdbcConn = (JdbcConnection) jdbcStatement.getConnection();
        } else if (target instanceof JdbcResultSet r) {
            jdbcResultSet = r;
            jdbcStatement = r.getStatement();
            jdbcConn = (JdbcConnection) jdbcStatement.getConnection();
        } else if (target instanceof JdbcResultSetMetaData m) {
            jdbcResultSet = m.getResultSet();
            jdbcStatement = m.getStatement();
            jdbcConn = (JdbcConnection) jdbcStatement.getConnection();
        } else {
            jdbcResultSet = null;
            jdbcStatement = null;
            jdbcConn = null;
        }

        //
        Object candidate = null;
        if (jdbcConn != null && TransactionSupport.class.isAssignableFrom(iface)) {
            candidate = jdbcConn.txSupport();
        } else if (jdbcConn != null && TypeSupport.class.isAssignableFrom(iface)) {
            candidate = jdbcConn.typeSupport();
        } else if (Connection.class.isAssignableFrom(iface)) {
            candidate = jdbcConn;
        } else if (Statement.class.isAssignableFrom(iface)) {
            candidate = jdbcStatement;
        } else if (ResultSet.class.isAssignableFrom(iface)) {
            candidate = jdbcResultSet;
        }
        if (iface.isInstance(candidate)) {
            return iface.cast(candidate);
        }

        Object unwrapped = this.unwrap(iface);
        return iface.isInstance(unwrapped) ? iface.cast(unwrapped) : null;
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

    protected <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    public abstract AdapterRequest newRequest(String sql);

    public abstract void doRequest(AdapterRequest request, AdapterReceive receive) throws SQLException;

    public abstract void cancelRequest();

    /** Cancel one execution, including lazy cursors; legacy adapters retain their existing cancellation behavior. */
    public void cancelRequest(AdapterRequest request) {
        this.cancelRequest();
    }

    @Override
    public final void close() throws IOException {
        try {
            this.doClose();
        } finally {
            AdapterConnManager.removeConnection(this);
        }
    }

    protected abstract void doClose() throws IOException;
}
