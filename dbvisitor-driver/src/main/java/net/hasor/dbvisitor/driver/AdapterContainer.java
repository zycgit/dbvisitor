package net.hasor.dbvisitor.driver;

import net.hasor.cobble.StringUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class AdapterContainer implements AdapterReceive {
    private static final Logger                  logger = LoggerFactory.getLogger(AdapterContainer.class);
    private final        JdbcConnection          jdbcConn;
    private              AdapterReceiveState     state;
    private              AdapterRequest          request;
    private final        Map<String, JdbcColumn> parameterDefs;
    private final        Map<String, Object>     parameterValues;
    private final        List<AdapterResponse>   response;
    private final        Object                  syncObj;

    public AdapterContainer(JdbcConnection jdbcConn) {
        this.jdbcConn = jdbcConn;
        this.state = AdapterReceiveState.Ready;
        this.parameterDefs = new HashMap<>();
        this.parameterValues = new HashMap<>();
        this.response = new LinkedList<>();
        this.syncObj = new Object();
    }

    public AdapterReceiveState getState() {
        return this.state;
    }

    public void prepareReceive(AdapterRequest request) throws SQLException {
        if (this.state != AdapterReceiveState.Ready) {
            throw new SQLException("there is already query in the processing.", JdbcErrorCode.SQL_STATE_QUERY_IS_PENDING);
        }

        this.state = AdapterReceiveState.Pending;
        this.request = request;
        this.parameterDefs.clear();
        this.parameterValues.clear();
        this.response.clear();
    }

    public AdapterRequest getRequest() {
        return this.request;
    }

    public boolean nextResult() {
        if (this.state == AdapterReceiveState.Pending) {
            return false;
        }

        if (!this.response.isEmpty()) {
            this.response.removeFirst();
        }
        return response.isEmpty();
    }


    public AdapterResponse firstResult() throws SQLException {
        if (this.state == AdapterReceiveState.Pending) {
            throw new SQLException("the query in progress, result is Pending.", JdbcErrorCode.SQL_STATE_QUERY_IS_PENDING);
        }

        if (this.response.isEmpty()) {
            return null;
        } else {
            return this.response.getFirst();
        }
    }

    public AdapterCursor getOutParameters() throws SQLException {
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

    private boolean ifStatusForResponse(AdapterRequest request) {
        if (this.request == null || !StringUtils.equals(this.request.getTraceId(), request.getTraceId())) {
            return false;
        }

        // TODO
        return true;
    }

    @Override
    public boolean responseFailed(AdapterRequest request, Exception e) {
        Objects.requireNonNull(e, "received error is null.");
        if (!this.ifStatusForResponse(request)) {
            logger.warn("received an unrelated error, traceId " + request.getTraceId() + ", error " + e.getMessage(), e);
            return false;
        }

        this.response.add(AdapterResponse.ofError(e));
        this.state = AdapterReceiveState.Receive;
        return true;
    }

    @Override
    public boolean responseResult(AdapterRequest request, AdapterCursor cursor) {
        Objects.requireNonNull(cursor, "received cursor is null.");
        if (!this.ifStatusForResponse(request)) {
            logger.warn("received an unrelated data, traceId " + request.getTraceId() + ", cursor " + cursor);
            return false;
        }

        this.response.add(AdapterResponse.ofCursor(cursor));
        this.state = AdapterReceiveState.Receive;
        return true;
    }

    @Override
    public boolean responseUpdateCount(AdapterRequest request, long updateCount) {
        if (!this.ifStatusForResponse(request)) {
            logger.warn("received an unrelated data, traceId " + request.getTraceId() + ", updateCount " + updateCount);
            return false;
        }

        this.response.add(AdapterResponse.ofUpdateCount(updateCount));
        this.state = AdapterReceiveState.Receive;
        return true;
    }

    @Override
    public boolean responseParameter(AdapterRequest request, String paramName, String paramType, Object value) {
        if (!this.ifStatusForResponse(request)) {
            logger.warn("received an unrelated data, traceId " + request.getTraceId() + ", paramName " + paramName);
            return false;
        }

        if (StringUtils.isBlank(paramName) || StringUtils.isBlank(paramType)) {
            throw new NullPointerException("received an unrelated data, paramName or paramType is blank.");
        }

        this.parameterDefs.put(paramName, new JdbcColumn(paramName, paramType, "", "", ""));
        this.parameterValues.put(paramName, value);
        this.state = AdapterReceiveState.Receive;
        return true;
    }

    @Override
    public boolean responseNotify(AdapterRequest request) {
        if (this.state != AdapterReceiveState.Receive) {
            logger.warn("received an unrelated notify, traceId " + request.getTraceId());
            return false;
        }

        this.syncObj.notifyAll();
        return true;
    }

    @Override
    public boolean responseFinish(AdapterRequest request) {
        if (!this.ifStatusForResponse(request)) {
            logger.warn("received an unrelated finish, traceId " + request.getTraceId());
            return false;
        }

        this.state = AdapterReceiveState.Ready;
        this.syncObj.notifyAll();
        return true;
    }

    @Override
    public void responseFinish() {
        this.state = AdapterReceiveState.Ready;
        this.syncObj.notifyAll();
    }

    public void waitFor(int timeout, TimeUnit timeUnit) throws SQLException {
        try {
            if (timeout == 0) {
                this.syncObj.wait();
            } else {
                this.syncObj.wait(timeUnit.toMillis(timeout));
            }

            // TODO 继续判断 结果集中的临近数据是否为异常需要抛出。
        } catch (InterruptedException e) {
            if (this.jdbcConn.isClosed()) {
                throw new SQLException("connection closed.");
            }
        }
    }
}