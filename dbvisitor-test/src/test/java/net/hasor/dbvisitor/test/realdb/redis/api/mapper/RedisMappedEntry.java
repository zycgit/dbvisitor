/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

/** Bean mapped directly from the FIELD and VALUE columns returned by HGETALL. */
public class RedisMappedEntry {
    private Integer field;
    private String  value;

    public Integer getField() {
        return this.field;
    }

    public void setField(Integer field) {
        this.field = field;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
