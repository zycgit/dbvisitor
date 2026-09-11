/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.driver;
import java.util.Map;
import java.util.UUID;

public abstract class AdapterRequest {
    private final String               traceId    = UUID.randomUUID().toString().replace("-", "");
    protected     boolean              generatedKeys;
    protected     long                 maxRows    = 0;
    protected     int                  fetchSize  = 0;
    protected     int                  timeoutSec = 0;
    private       Map<String, JdbcArg> argMap;

    public String getTraceId() {
        return traceId;
    }

    public long getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(long maxRows) {
        this.maxRows = maxRows;
    }

    public boolean isGeneratedKeys() {
        return this.generatedKeys;
    }

    public void setGeneratedKeys(boolean generatedKeys) {
        this.generatedKeys = generatedKeys;
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
