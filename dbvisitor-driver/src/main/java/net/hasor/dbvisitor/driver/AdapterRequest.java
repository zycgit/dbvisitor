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
