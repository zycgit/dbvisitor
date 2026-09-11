/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class JdbcQuerySupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 630000;
    }

    protected String selectSql(String columns, String predicate, boolean ordered) {
        return "SELECT " + columns + " FROM user_info WHERE " + predicate + (ordered ? " ORDER BY id" : "");
    }

    protected String selectById(String columns, String parameter) throws SQLException {
        return selectSql(columns, "id = " + parameter, false);
    }

    protected String selectRange(String columns, String lower, String upper, boolean ordered) throws SQLException {
        return selectSql(columns, "id >= " + lower + " AND id <= " + upper, ordered);
    }

    protected String countRange(String lower, String upper) throws SQLException {
        return selectRange("COUNT(*)", lower, upper, false);
    }

    protected void assertNumericField(int expected, Object actual) {
        assertEquals(expected, ((Number) actual).intValue());
    }

    protected void seedUsers() throws SQLException {
        for (int i = 1; i <= 3; i++) {
            insertUser(baseId() + i, "NXN-JDBC-Query-" + i, 60 + i, "nxn-jdbc-query-" + i + "@test.com", new Date());
        }
    }

    protected void insertUser(int id, String name, int age, String email, Date createTime) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)",
                new Object[] { id, name, age, email, createTime });
    }

    protected Map<String, Object> params(int minId, int maxId) {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("minId", minId);
        params.put("maxId", maxId);
        return params;
    }

    protected Object value(Map<String, Object> row, String key) {
        assertNotNull(row);
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
