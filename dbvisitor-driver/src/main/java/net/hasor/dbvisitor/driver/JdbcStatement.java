package net.hasor.dbvisitor.driver;

import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;

import java.io.Closeable;
import java.sql.*;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

class JdbcStatement implements Statement, Closeable {
    private static final Logger               logger = LoggerFactory.getLogger(JdbcStatement.class);
    protected final      JdbcConnection       jdbcConn;
    protected final      AdapterDataContainer container;

    /** Maximum number of rows to return, 0 = unlimited. */
    protected long    maxRows           = 0;
    /** Number of rows to get in a batch. */
    protected int     fetchSize         = 0;
    /** Timeout (in seconds) for a query. */
    protected int     timeoutSec        = 0;
    private   boolean closed            = false;
    private   boolean closeOnCompletion = false;

    JdbcStatement(JdbcConnection jdbcConn) {
        this.jdbcConn = Objects.requireNonNull(jdbcConn, "jdbcConn is null.");
        this.container = new AdapterDataContainer();
    }

    @Override
    public void close() {
        this.closed = true;
    }

    @Override
    public boolean isClosed() {
        return this.closed || this.jdbcConn.isClosed();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        this.checkOpen();
        this.closeOnCompletion = true;
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        this.checkOpen();
        return this.closeOnCompletion;
    }

    protected void checkOpen() throws SQLException {
        if (this.isClosed()) {
            throw new SQLException("Statement is closed", JdbcErrorCode.SQL_STATE_IS_CLOSED);
        }
    }

    @Override
    public int getResultSetHoldability() {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public int getResultSetConcurrency() {
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public int getResultSetType() {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        this.checkOpen();
        return 0;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        this.checkOpen();
        if (max < 0) {
            throw new SQLException("The maximum field size must be a value greater than or equal to 0.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        this.checkOpen();
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        this.checkOpen();
        return this.timeoutSec;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        this.checkOpen();
        if (seconds < 0) {
            throw new SQLException("The maximum timeout must be a value greater than or equal to 0.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }
        this.timeoutSec = seconds;
    }

    @Override
    public int getFetchDirection() throws SQLException {
        this.checkOpen();
        return ResultSet.FETCH_FORWARD;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        this.checkOpen();
        if (ResultSet.FETCH_FORWARD != direction) {
            throw new SQLFeatureNotSupportedException("ResultSet type can only be FETCH_FORWARD");
        }
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        this.checkOpen();
        if (rows < 0) {
            throw new SQLException("The maximum fetchSize must be a value greater than or equal to 0.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }
        this.fetchSize = rows;
    }

    @Override
    public int getFetchSize() throws SQLException {
        this.checkOpen();
        return this.fetchSize;
    }

    @Override
    public int getMaxRows() throws SQLException {
        long result = getLargeMaxRows();
        if (result > Integer.MAX_VALUE) {
            throw new SQLException("max rows exceeds limit of " + Integer.MAX_VALUE, JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }
        return Math.toIntExact(result);
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        this.setLargeMaxRows(max);
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        this.checkOpen();
        return this.maxRows;
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        this.checkOpen();
        if (max < 0) {
            throw new SQLException("The maximum rows must be a value greater than or equal to 0.", JdbcErrorCode.SQL_STATE_ILLEGAL_ARGUMENT);
        }
        this.maxRows = max;
    }

    protected String getTypeName(int typeNumber) {
        return this.jdbcConn.typeSupport().getTypeName(typeNumber);
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        this.checkOpen();
        if (poolable) {
            throw new SQLFeatureNotSupportedException("poolable must be false");
        }
    }

    @Override
    public boolean isPoolable() throws SQLException {
        this.checkOpen();
        return false;
    }

    //

    @Override
    public Connection getConnection() {
        return this.jdbcConn;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        this.checkOpen();
        return this.jdbcConn.adapterConnection().unwrap(iface, this);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        this.checkOpen();
        return this.jdbcConn.adapterConnection().unwrap(iface, this) != null;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        this.checkOpen();
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        this.checkOpen();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        this.checkOpen();
        throw new SQLFeatureNotSupportedException("cursor not supported");
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        this.checkOpen();
        return null;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        if (Statement.NO_GENERATED_KEYS != autoGeneratedKeys) {
            throw new SQLFeatureNotSupportedException("Auto generated keys must be NO_GENERATED_KEYS");
        }
        return this.executeUpdate(sql);
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        if (Statement.NO_GENERATED_KEYS != autoGeneratedKeys) {
            throw new SQLFeatureNotSupportedException("Auto generated keys must be NO_GENERATED_KEYS");
        }
        return this.executeLargeUpdate(sql);
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        if (Statement.NO_GENERATED_KEYS != autoGeneratedKeys) {
            throw new SQLFeatureNotSupportedException("Auto generated keys must be NO_GENERATED_KEYS");
        }
        return this.execute(sql);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException("columnIndexes not supported");
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException("columnIndexes not supported");
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException("columnIndexes not supported");
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException("columnNames not supported");
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException("columnNames not supported");
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException("columnNames not supported");
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        long updateCount = this.executeLargeUpdate(sql);
        return updateCount <= Integer.MAX_VALUE ? (int) updateCount : SUCCESS_NO_INFO;
    }

    @Override
    public int[] executeBatch() throws SQLException {
        long[] updateCountLong = this.executeLargeBatch();
        int[] updateCountInt = new int[updateCountLong.length];
        for (int i = 0; i < updateCountLong.length; i++) {
            updateCountInt[i] = updateCountLong[i] <= Integer.MAX_VALUE ? (int) updateCountLong[i] : SUCCESS_NO_INFO;
        }
        return updateCountInt;
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        this.checkOpen();
        throw new SQLFeatureNotSupportedException("addBatch not supported");
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        this.checkOpen();
        throw new SQLFeatureNotSupportedException("addBatch not supported");
    }

    @Override
    public void clearBatch() throws SQLException {
        this.checkOpen();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        long updateCount = this.getLargeUpdateCount();
        return updateCount <= Integer.MAX_VALUE ? (int) updateCount : SUCCESS_NO_INFO;
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        if (this.execute(sql)) {
            throw new SQLException("No updateCount were returned by the query.", JdbcErrorCode.SQL_STATE_QUERY_IS_RESULT);
        } else {
            return this.getLargeUpdateCount();
        }
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        if (this.execute(sql)) {
            return this.getResultSet();
        } else {
            throw new SQLException("No results were returned by the query.", JdbcErrorCode.SQL_STATE_QUERY_IS_UPDATE_COUNT);
        }
    }

    @Override
    public synchronized boolean execute(String sql) throws SQLException {
        this.checkOpen();

        switch (this.container.getState()) {
            case Ready:
            case Finish:
                break;
            default:
                throw new SQLException("there is already query in the processing.", JdbcErrorCode.SQL_STATE_QUERY_IS_PENDING);
        }

        try {
            AdapterRequest req = this.jdbcConn.adapterConnection().newRequest(sql);
            this.configRequest(req);

            // request and receive
            this.container.prepareReceive(req); // status set to PENDING

            // timeout
            if (this.timeoutSec > 0) {
                this.jdbcConn.adapterConnection().startTimer(req.getTraceId(), this.timeoutSec * 1000, timeout -> {
                    if (this.container.getState() == AdapterReceiveState.Pending) {
                        try {
                            this.jdbcConn.adapterConnection().cancelQuery(req);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        } finally {
                            this.container.responseFailed(req, new SQLTimeoutException("query timeout.", JdbcErrorCode.SQL_STATE_QUERY_TIMEOUT));
                        }
                    }
                });
            }

            this.beforeExecute(req, this.container);
            this.jdbcConn.adapterConnection().doRequest(req, this.container);
            this.container.waitFor(this.timeoutSec, TimeUnit.SECONDS);
            this.afterExecute(req, this.container);

            return this.container.firstResult().isResult();
        } finally {
            if (this.closeOnCompletion) {
                this.close();
            }
        }
    }

    protected void configRequest(AdapterRequest request) {
        request.setMaxRows(this.maxRows);
        request.setFetchSize(this.fetchSize);
        request.setTimeoutSec(this.timeoutSec);
    }

    protected void beforeExecute(AdapterRequest request, AdapterDataContainer container) {

    }

    protected void afterExecute(AdapterRequest request, AdapterDataContainer container) {

    }

    @Override
    public synchronized void cancel() throws SQLException {
        this.checkOpen();

        if (this.container.getState() != AdapterReceiveState.Pending) {
            throw new SQLException("no pending queries found.", JdbcErrorCode.SQL_STATE_QUERY_IS_FINISH);
        }

        AdapterRequest req = this.container.getRequest();

        try {
            this.jdbcConn.adapterConnection().stopTimer(req.getTraceId());
            this.jdbcConn.adapterConnection().cancelQuery(req);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            this.container.responseFailed(req, new JdbcCancelledSQLException("query cancel.", JdbcErrorCode.SQL_STATE_QUERY_TIMEOUT));
        }
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        this.checkResultSet();
        if (this.container.emptyResult()) {
            throw new SQLException("no search any results found.", JdbcErrorCode.SQL_STATE_QUERY_EMPTY);
        }

        AdapterResponse result = this.container.firstResult();
        if (result.isResult()) {
            throw new SQLException("No updateCount were returned by the query.", JdbcErrorCode.SQL_STATE_QUERY_IS_RESULT);
        } else {
            return result.getUpdateCount();
        }
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        this.checkResultSet();
        if (this.container.emptyResult()) {
            throw new SQLException("no search any results found.", JdbcErrorCode.SQL_STATE_QUERY_EMPTY);
        }

        AdapterResponse result = this.container.firstResult();
        if (result.isResult()) {
            return new JdbcResultSet(this, result.toCursor());
        } else {
            throw new SQLException("No results were returned by the query.", JdbcErrorCode.SQL_STATE_QUERY_IS_UPDATE_COUNT);
        }
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return this.getMoreResults(Statement.CLOSE_ALL_RESULTS);
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        this.checkOpen();
        return this.container.nextResult();
    }

    protected void checkResultSet() throws SQLException {
        this.checkOpen();

        switch (this.container.getState()) {
            case Ready:
            case Pending:
                throw new SQLException("querying in progress.", JdbcErrorCode.SQL_STATE_QUERY_IS_PENDING);
            case Receive:
            case Finish:
                break;
        }
    }
}