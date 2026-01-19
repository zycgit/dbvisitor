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
package net.hasor.dbvisitor.adapter.elastic;
import java.util.Collections;
import java.util.Map;
import net.hasor.dbvisitor.driver.AdapterRequest;

class ElasticOperation {
    //
    private final ElasticHttpMethod   method;
    private final String              endpoint;
    private final String              queryPath;
    private final Map<String, Object> queryParams;
    //
    private final Map<String, Object> hints;
    private final AdapterRequest      request;
    private       boolean             useRefresh = false;

    public ElasticOperation(ElasticHttpMethod method, String endpoint, String queryPath, Map<String, Object> queryParams, //
            Map<String, Object> hints, AdapterRequest request) {
        this.method = method;
        this.endpoint = endpoint;
        this.queryPath = queryPath;
        this.queryParams = (queryParams == null) ? Collections.emptyMap() : Collections.unmodifiableMap(queryParams);

        this.hints = (hints == null) ? Collections.emptyMap() : Collections.unmodifiableMap(hints);
        this.request = request;
    }

    public ElasticHttpMethod getMethod() {
        return this.method;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public String getQueryPath() {
        return this.queryPath;
    }

    public Map<String, Object> getQueryParams() {
        return this.queryParams;
    }

    public Map<String, Object> getHints() {
        return this.hints;
    }

    public AdapterRequest getRequest() {
        return this.request;
    }

    public void setUseRefresh(boolean useRefresh) {
        this.useRefresh = useRefresh;
    }

    public boolean hasRefreshParam() {
        return this.queryParams.containsKey("refresh");
    }

    public String getEndpointWithRefresh() {
        if (!useRefresh || hasRefreshParam()) {
            return endpoint;
        }

        StringBuilder sb = new StringBuilder(queryPath);
        boolean first = queryParams.isEmpty();
        for (Map.Entry<String, Object> e : queryParams.entrySet()) {
            sb.append(first ? "?" : "&").append(e.getKey()).append("=").append(e.getValue());
            first = false;
        }
        sb.append(first ? "?" : "&").append("refresh=true");
        return sb.toString();
    }
}
