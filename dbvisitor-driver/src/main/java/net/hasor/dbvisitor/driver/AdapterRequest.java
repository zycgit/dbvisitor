package net.hasor.dbvisitor.driver;

import java.util.Map;

public abstract class AdapterRequest {
    private   String               traceId;
    protected long                 maxRows    = 0;
    protected int                  fetchSize  = 0;
    protected int                  timeoutSec = 0;
    private   Map<String, JdbcArg> argMap;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public long getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(long maxRows) {
        this.maxRows = maxRows;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public int getTimeoutSec() {
        return timeoutSec;
    }

    public void setTimeoutSec(int timeoutSec) {
        this.timeoutSec = timeoutSec;
    }

    public Map<String, JdbcArg> getArgMap() {
        return argMap;
    }

    public void setArgMap(Map<String, JdbcArg> argMap) {
        this.argMap = argMap;
    }
}
