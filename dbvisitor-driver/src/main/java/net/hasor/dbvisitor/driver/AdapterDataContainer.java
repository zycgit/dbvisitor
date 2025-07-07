package net.hasor.dbvisitor.driver;

import net.hasor.cobble.StringUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

//
// pending > receive > finish > ready > pending
//
class AdapterDataContainer implements AdapterReceive {
    private static final Logger                logger   = LoggerFactory.getLogger(AdapterDataContainer.class);
    private volatile     AdapterReceiveState   state    = AdapterReceiveState.Ready;
    private volatile     AdapterRequest        request;
    private final        List<AdapterResponse> response = new LinkedList<>();

    public AdapterReceiveState getState() {
        return this.state;
    }

    public synchronized void prepareReceive(AdapterRequest request) throws SQLException {
        switch (this.state) {
            case Finish:
            case Ready:
                break;
            default:
                throw new SQLException("there is already query in the processing.", JdbcErrorCode.SQL_STATE_QUERY_IS_PENDING);
        }

        this.state = AdapterReceiveState.Pending;
        this.request = request;
    }

    public AdapterRequest getRequest() {
        return this.request;
    }

    public boolean emptyResult() {
        return this.response.isEmpty();
    }

    public boolean nextResult() {
        if (!this.response.isEmpty()) {
            this.response.removeFirst();
        }
        return response.isEmpty();
    }

    public AdapterResponse firstResult() {
        if (this.response.isEmpty()) {
            return null;
        } else {
            return this.response.getFirst();
        }
    }

    public AdapterCursor getOutParameters() {
        return null;// TODO
    }

    @Override
    public void responseFailed(AdapterRequest request, Exception e) {
        if (this.request == null || !StringUtils.equals(this.request.getTraceId(), request.getTraceId())) {
            logger.warn("Received an unrelated error " + e.getMessage(), e);
            return;
        }
    }

    @Override
    public void responseResult(AdapterRequest request, AdapterCursor cursor) {
        if (this.request == null || !StringUtils.equals(this.request.getTraceId(), request.getTraceId())) {
            logger.warn("Received an unrelated error " + e.getMessage(), e);
            return;
        }
    }

    @Override
    public void responseUpdateCount(AdapterRequest request, long updateCount) {
        if (this.request == null || !StringUtils.equals(this.request.getTraceId(), request.getTraceId())) {
            logger.warn("Received an unrelated error " + e.getMessage(), e);
            return;
        }
    }

    @Override
    public void responseParameter(AdapterRequest request, String paramName, String paramType) {
        if (this.request == null || !StringUtils.equals(this.request.getTraceId(), request.getTraceId())) {
            logger.warn("Received an unrelated error " + e.getMessage(), e);
            return;
        }
    }

    @Override
    public void responseFinish() {
        this.notifyAll();
    }

    public void waitFor(int timeout, TimeUnit timeUnit) {
        try {
            if (timeout == 0) {
                this.wait();
            } else {
                this.wait(timeUnit.toMillis(timeout));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
