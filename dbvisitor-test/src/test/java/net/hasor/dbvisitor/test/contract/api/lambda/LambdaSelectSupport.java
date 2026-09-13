/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public abstract class LambdaSelectSupport extends LambdaResultHandlingSupport {
    protected int baseId() {
        return 816000;
    }

    protected void insert(int id, String name, Integer age, String email) throws SQLException {
        insertByJdbc(id, name, age, email);
    }

    protected Map<String, Object> findByInt(List<Map<String, Object>> rows, String key, int value) {
        for (Map<String, Object> row : rows) {
            Object rowValue = getVal(row, key);
            if (rowValue instanceof Number && ((Number) rowValue).intValue() == value) {
                return row;
            }
        }
        return null;
    }

    protected boolean containsKey(Map<String, Object> map, String key) {
        return map.containsKey(key) || map.containsKey(key.toUpperCase()) || map.containsKey(key.toLowerCase());
    }

    protected Object getVal(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        if (map.containsKey(key.toUpperCase())) {
            return map.get(key.toUpperCase());
        }
        if (map.containsKey(key.toLowerCase())) {
            return map.get(key.toLowerCase());
        }
        return null;
    }
}
