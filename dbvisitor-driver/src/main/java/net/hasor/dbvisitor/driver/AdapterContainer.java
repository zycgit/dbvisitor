/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.io.IOUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;

class AdapterContainer implements AdapterReceive {
    private static final Logger                      logger             = LoggerFactory.getLogger(AdapterContainer.class);
    private final        JdbcConnection              jdbcConn;
    private volatile     AdapterReceiveState         state;
    private volatile     AdapterRequest              request;
    private final        Map<String, JdbcColumn>     parameterDefs;
    private final        Map<String, Object>         parameterValues;
    private final        LinkedList<AdapterResponse> response;
    private final        List<AdapterResponse>       retained           = new ArrayList<>();
    private volatile     boolean                     closed;
    private              Runnable                    completionListener = () -> {
    };

    public AdapterContainer(JdbcConnection jdbcConn) {
        this.jdbcConn = jdbcConn;
        this.state = AdapterReceiveState.Ready;
        this.parameterDefs = new HashMap<>();
        this.parameterValues = new HashMap<>();
        this.response = new LinkedList<>();
    }

    public AdapterReceiveState getState() {
        return this.state;
    }

    public synchronized void prepareReceive(AdapterRequest request) throws SQLException {
        if (this.closed) {
            throw new SQLException("Statement is closed.");
        }

        if (this.state != AdapterReceiveState.Ready) {
            throw new SQLException("there is already query in the processing.", JdbcErrorCode.SQL_STATE_QUERY_IS_PENDING);
        }

        synchronized (this) {
            this.closeResults();
            this.state = AdapterReceiveState.Pending;
            this.request = request;
            this.parameterDefs.clear();
            this.parameterValues.clear();
            this.response.clear();
        }
    }

    public AdapterRequest getRequest() {
        return this.request;
    }

    synchronized boolean hasPendingCursor() {
        return this.response.stream().anyMatch(AdapterResponse::hasPendingCursor) || this.retained.stream().anyMatch(AdapterResponse::hasPendingCursor);
    }

    public synchronized AdapterCursor getOutParameters() throws SQLException {
        if (this.state == AdapterReceiveState.Pending) {
            throw new SQLException("the query in progress, result is Pending.", JdbcErrorCode.SQL_STATE_QUERY_IS_PENDING);
        }

        JdbcColumn[] toCols = new JdbcColumn[this.parameterDefs.size()];
        Object[][] toVals = new Object[1][this.parameterDefs.size()];
        AtomicInteger counter = new AtomicInteger(0);
        this.parameterDefs.forEach((k, v) -> {
            int i = counter.getAndIncrement();
            toCols[i] = v;
            toVals[0][i] = parameterValues.get(k);
        });

        return new AdapterMemoryCursor(Arrays.asList(toCols), toVals);
    }

    private boolean ifErrorStatusForResponse(AdapterRequest request) {
        if (this.closed || this.request == null || !StringUtils.equals(this.request.getTraceId(), request.getTraceId())) {
            logger.warn("received an unrelated response, traceId " + request.getTraceId());
            return true;
        }

        if (this.state == AdapterReceiveState.Ready) {
            logger.warn("received error, no query in progress, traceId " + request.getTraceId());
            return true;
        }

        return false;
    }

    @Override
    public synchronized boolean responseFailed(AdapterRequest request, Throwable e) {
        Objects.requireNonNull(e, "received error is null.");
        if (this.ifErrorStatusForResponse(request)) {
            return false;
        }

        this.response.add(AdapterResponse.ofError(e));
        this.onReceive();
        return true;
    }

    @Override
    public boolean responseResult(AdapterRequest request, AdapterCursor cursor) {
        return this.responseResult(request, cursor, null);
    }

    @Override
    public synchronized boolean responseResult(AdapterRequest request, AdapterCursor cursor, AdapterCursor generatedKeys) {
        Objects.requireNonNull(cursor, "received cursor is null.");
        if (this.ifErrorStatusForResponse(request)) {
            IOUtils.closeQuietly(cursor);
            IOUtils.closeQuietly(generatedKeys);
            return false;
        }

        this.response.add(AdapterResponse.ofCursor(cursor, generatedKeys));
        this.onReceive();
        return true;
    }

    @Override
    public boolean responseUpdateCount(AdapterRequest request, long updateCount) {
        return this.responseUpdateCount(request, updateCount, null);
    }

    @Override
    public synchronized boolean responseUpdateCount(AdapterRequest request, long updateCount, AdapterCursor generatedKeys) {
        if (this.ifErrorStatusForResponse(request)) {
            IOUtils.closeQuietly(generatedKeys);
            return false;
        }

        this.response.add(AdapterResponse.ofUpdateCount(updateCount, generatedKeys));
        this.onReceive();
        return true;
    }

    @Override
    public synchronized boolean responseParameter(AdapterRequest request, String paramName, String paramType, Object value) {
        if (this.ifErrorStatusForResponse(request)) {
            return false;
        }

        if (StringUtils.isBlank(paramName) || StringUtils.isBlank(paramType)) {
            throw new NullPointerException("received an unrelated data, paramName or paramType is blank.");
        }

        this.parameterDefs.put(paramName, new JdbcColumn(paramName, paramType, "", "", "", ResultSetMetaData.columnNullableUnknown, false, AdapterType.Array));
        this.parameterValues.put(paramName, value);
        return true;
    }

    @Override
    public synchronized boolean responseFinish(AdapterRequest request) {
        if (this.ifErrorStatusForResponse(request)) {
            logger.warn("received an unrelated finish, traceId " + request.getTraceId());
            return false;
        }

        this.onReady();
        this.jdbcConn.adapterConnection().stopTimer(request.getTraceId());
        this.completionListener.run();
        return true;
    }

    protected void onReceive() {
        synchronized (this) {
            this.state = AdapterReceiveState.Receive;
            this.notifyAll();
        }
    }

    protected void onReady() {
        synchronized (this) {
            this.state = AdapterReceiveState.Ready;
            this.notifyAll();
        }
    }

    public synchronized void closeResults() {
        this.response.forEach(AdapterResponse::closeResult);
        this.retained.forEach(AdapterResponse::closeResult);
        this.response.clear();
        this.retained.clear();
    }

    public synchronized void close() {
        this.closed = true;
        this.onReady();
        this.closeResults();
    }

    public synchronized void setCompletionListener(Runnable listener) {
        this.completionListener = listener;
    }

    public synchronized boolean allResultsClosed() {
        // A closed current ResultSet can complete the statement, but queued
        // results and an unread update count/error must remain accessible.
        boolean currentComplete = this.response.isEmpty() || (this.response.size() == 1 && //
                this.response.getFirst().currentResultComplete());
        return this.state == AdapterReceiveState.Ready && //
                currentComplete && //
                this.retained.stream().allMatch(AdapterResponse::resultsClosed);
    }

    public synchronized void failRequest(AdapterRequest request, SQLException error) {
        if (this.ifErrorStatusForResponse(request)) {
            return;
        }

        this.closeResults();
        this.response.add(AdapterResponse.ofError(error));
        this.onReady();
    }

    public synchronized boolean nextResult(int current, int timeout, TimeUnit timeUnit) throws SQLException {
        if (current != Statement.CLOSE_CURRENT_RESULT &&    //
                current != Statement.KEEP_CURRENT_RESULT && //
                current != Statement.CLOSE_ALL_RESULTS) {
            throw new SQLException("Invalid getMoreResults flag: " + current);
        }
        if (this.state == AdapterReceiveState.Pending) {
            throw new SQLException("querying in progress.", JdbcErrorCode.SQL_STATE_QUERY_IS_PENDING);
        }

        if (!this.response.isEmpty()) {
            AdapterResponse res = this.response.removeFirst();
            if (current == Statement.KEEP_CURRENT_RESULT) {
                this.retained.add(res);
            } else {
                res.closeResult();
            }
        }

        if (current == Statement.CLOSE_ALL_RESULTS) {
            this.retained.forEach(AdapterResponse::closeResult);
            this.retained.clear();
        }

        boolean empty = this.response.isEmpty();
        if (empty && this.state == AdapterReceiveState.Receive) {
            this.awaitResult(timeout, timeUnit);
        }

        AdapterResponse next = this.firstResult();
        if (next != null && next.isError()) {
            this.response.removeFirst();
            throw next.toError();
        }
        return next != null && next.isResult();
    }

    public synchronized AdapterResponse firstResult() throws SQLException {
        if (this.state == AdapterReceiveState.Pending) {
            throw new SQLException("querying in progress.", JdbcErrorCode.SQL_STATE_QUERY_IS_PENDING);
        }

        if (this.response.isEmpty()) {
            return null;
        } else {
            return this.response.getFirst();
        }
    }

    public synchronized void waitFor(int timeout, TimeUnit timeUnit) throws SQLException {
        this.awaitResult(timeout, timeUnit);
        if (this.response.isEmpty()) {
            throw new SQLException("no data received.");
        }

        AdapterResponse res = this.response.getFirst();
        if (res.isError()) {
            this.response.removeFirst();
            throw res.toError();
        }
    }

    private void awaitResult(int timeout, TimeUnit unit) throws SQLException {
        long remaining = unit.toNanos(timeout);
        long deadline = System.nanoTime() + remaining;
        while (!this.closed && this.response.isEmpty() && this.state != AdapterReceiveState.Ready) {
            try {
                if (timeout == 0) {
                    this.wait();
                } else {
                    if (remaining <= 0) {
                        throw new SQLTimeoutException("query result wait timeout.");
                    }
                    TimeUnit.NANOSECONDS.timedWait(this, remaining);
                    remaining = deadline - System.nanoTime();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SQLException("Interrupted while waiting for query results.", e);
            }
        }

        if (this.closed || this.jdbcConn.isClosed()) {
            throw new SQLException("Statement or connection is closed.");
        }
    }
}
