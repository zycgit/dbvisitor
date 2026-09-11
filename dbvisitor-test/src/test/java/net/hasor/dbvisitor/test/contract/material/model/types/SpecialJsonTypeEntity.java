/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.model.types;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

@Table(value = "test_special_types")
public class SpecialJsonTypeEntity {
    @Column(primary = true)
    private int                 id;
    @Column(value = "json_map", typeHandler = JsonTypeHandler.class, specialJavaType = LinkedHashMap.class)
    private Map<String, Object> jsonMap;
    @Column(value = "json_list", typeHandler = JsonTypeHandler.class, specialJavaType = java.util.LinkedList.class)
    private List<String>        jsonList;
    @Column(value = "json_set", typeHandler = JsonTypeHandler.class, specialJavaType = HashSet.class)
    private Set<String>         jsonSet;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<String, Object> getJsonMap() {
        return jsonMap;
    }

    public void setJsonMap(Map<String, Object> jsonMap) {
        this.jsonMap = jsonMap;
    }

    public List<String> getJsonList() {
        return jsonList;
    }

    public void setJsonList(List<String> jsonList) {
        this.jsonList = jsonList;
    }

    public Set<String> getJsonSet() {
        return jsonSet;
    }

    public void setJsonSet(Set<String> jsonSet) {
        this.jsonSet = jsonSet;
    }
}
