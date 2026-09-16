/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import net.hasor.dbvisitor.test.realdb.redis.api.mapper.RedisNativeMapperSupport.Generated;

public final class RedisKeyFixture implements AutoCloseable {
    private final RedisMapperFixture     fixture     = new RedisMapperFixture();
    private final Map<Object, Generated> records     = new HashMap<>();
    private final Map<Object, Boolean>   listStorage = new HashMap<>();
    private       RedisKeyMapper         mapper;

    public void open() throws Exception {
        fixture.open();
        if (mapper == null) {
            mapper = fixture.session().createMapper(RedisKeyMapper.class);
        }
    }

    public Generated record(Object id, String name) {
        Generated record = new Generated();
        record.setId(id == null ? null : ((Number) id).longValue());
        record.setKey(fixture.key(name));
        record.setCounter(fixture.key(name + ":counter"));
        record.setValue(name);
        return record;
    }

    public int write(String operation, Generated record) throws Exception {
        int count;
        switch (operation) {
            case "BEFORE" -> count = mapper.before(record);
            case "AFTER" -> count = mapper.after(record);
            case "OPTIONS" -> count = mapper.options(record);
            case "RESULT_SET" -> {
                record.setCounter(fixture.key("generated-counter"));
                count = mapper.resultKey(record);
            }
            default -> throw new IllegalArgumentException("Unsupported Redis key source: " + operation);
        }
        records.put(record.getId(), record);
        listStorage.put(record.getId(), "AFTER".equals(operation));
        return count;
    }

    public String readName(Object id) throws SQLException {
        Generated record = records.get(id);
        if (Boolean.TRUE.equals(listStorage.get(id))) {
            return fixture.session().jdbc().queryForString("LINDEX ? ?", new Object[] { record.getKey(), record.getId() - 1 });
        }
        return fixture.session().jdbc().queryForString("HGET ? ?", new Object[] { record.getKey(), record.getId() });
    }

    @Override
    public void close() throws SQLException {
        fixture.close();
    }
}
