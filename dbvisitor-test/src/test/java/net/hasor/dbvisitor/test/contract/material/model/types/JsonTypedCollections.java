/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.model.types;

import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.types.BindTypeHandler;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

/** A scalar JSON value whose bean properties retain nested generic element types. */
@BindTypeHandler(JsonTypeHandler.class)
public class JsonTypedCollections {
    private List<JsonTestBean>              entries;
    private Map<String, List<JsonTestBean>> groups;

    public List<JsonTestBean> getEntries() {
        return this.entries;
    }

    public void setEntries(List<JsonTestBean> entries) {
        this.entries = entries;
    }

    public Map<String, List<JsonTestBean>> getGroups() {
        return this.groups;
    }

    public void setGroups(Map<String, List<JsonTestBean>> groups) {
        this.groups = groups;
    }
}
