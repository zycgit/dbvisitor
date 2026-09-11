/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.hasor.dbvisitor.driver.AdapterRequest;

public class ElasticRequest extends AdapterRequest {
    private final String       commandBody;
    private final boolean      preRead;
    private final boolean      indexRefresh;
    private       ObjectMapper json;

    public ElasticRequest(String commandBody, boolean preRead, boolean indexRefresh) {
        this.commandBody = commandBody;
        this.preRead = preRead;
        this.indexRefresh = indexRefresh;
    }

    public boolean isPreRead() {
        return this.preRead;
    }

    public String getCommandBody() {
        return commandBody;
    }

    public ObjectMapper getJson() {
        return json;
    }

    public void setJson(ObjectMapper json) {
        this.json = json;
    }

    public boolean isIndexRefresh() {
        return this.indexRefresh;
    }
}
