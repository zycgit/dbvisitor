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
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import static org.junit.Assert.assertNotNull;

public abstract class JdbcResultHandlingSupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 670000;
    }

    /** Datasource-specific fixtures may supply native SQL while keeping all mapping assertions. */
    protected String selectSql(String columns, String predicate, boolean ordered) throws SQLException {
        return "SELECT " + columns + " FROM user_info WHERE " + predicate + (ordered ? " ORDER BY id" : "");
    }

    protected Object[] selectArguments(String columns, String predicate, Object... values) {
        return values;
    }

    protected String emptyResultSql() throws SQLException {
        return selectSql("*", "id = ?", false);
    }

    protected Object[] emptyResultArguments() {
        return selectArguments("*", "id = ?", baseId() + 999);
    }

    protected Class<?> resultBeanType() {
        return UserInfo.class;
    }

    protected final Object beanProperty(Object bean, String property) {
        String getter = "get" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
        try {
            return bean.getClass().getMethod(getter).invoke(bean);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Unable to read mapped property " + property, e);
        }
    }

    protected void insertUser(int id, String name, int age, String email) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", new Object[] { id, name, age, email, new Date() });
    }

    protected void seedUsers() throws SQLException {
        for (int i = 1; i <= 10; i++) {
            insertUser(baseId() + i, "NXN-Result-" + i, 20 + i, "nxn-result-" + i + "@test.com");
        }
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
