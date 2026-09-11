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

import static org.junit.Assert.assertNotNull;

public abstract class JdbcResultHandlingSupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 670000;
    }

    /** Datasource-specific fixtures may supply native SQL while keeping all mapping assertions. */
    protected String selectSql(String columns, String predicate, boolean ordered) {
        return "SELECT " + columns + " FROM user_info WHERE " + predicate + (ordered ? " ORDER BY id" : "");
    }

    protected void insertUser(int id, String name, int age, String email) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)",
                new Object[] { id, name, age, email, new Date() });
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

    protected static class UserNameAge {
        protected final String nameAge;

        protected UserNameAge(String nameAge) {
            this.nameAge = nameAge;
        }
    }
}
