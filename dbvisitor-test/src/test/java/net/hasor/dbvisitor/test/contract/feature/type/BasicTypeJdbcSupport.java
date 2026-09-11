/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import java.util.Map;
import java.util.Collections;

import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

public abstract class BasicTypeJdbcSupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 650000;
    }

    protected String insertCommand(String table, String columns) {
        int fields = columns.split(",").length;
        return "INSERT INTO " + table + " (" + columns + ") VALUES (" + String.join(", ", Collections.nCopies(fields, "?")) + ")";
    }

    protected String selectCommand(String table, String columns) throws SQLException {
        return "SELECT " + columns + " FROM " + table + " WHERE id = ?";
    }

    protected boolean booleanValue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    protected Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
