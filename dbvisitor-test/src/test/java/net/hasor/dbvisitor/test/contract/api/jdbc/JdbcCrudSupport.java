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
import java.util.Locale;
import java.util.Map;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class JdbcCrudSupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 610000;
    }

    protected String command(JdbcCrudCommand command) throws SQLException {
        switch (command) {
            case INSERT:
                return "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)";
            case UPDATE_AGE:
                return "UPDATE user_info SET age = ? WHERE id = ?";
            case DELETE:
                return "DELETE FROM user_info WHERE id = ?";
            case SELECT_NAME:
                return "SELECT name FROM user_info WHERE id = ?";
            case SELECT_AGE:
                return "SELECT age FROM user_info WHERE id = ?";
            case COUNT_BY_ID:
                return "SELECT COUNT(*) FROM user_info WHERE id = ?";
            default:
                throw new IllegalArgumentException("Unknown CRUD fixture command: " + command);
        }
    }

    protected int insertUser(int id, String name, int age, String email) throws SQLException {
        return jdbcTemplate.executeUpdate(command(JdbcCrudCommand.INSERT), new Object[] { id, name, age, email, new Date() });
    }

    protected Object field(Map<String, Object> row, String column) {
        if (row.containsKey(column)) {
            return row.get(column);
        }
        if (row.containsKey(column.toUpperCase(Locale.ROOT))) {
            return row.get(column.toUpperCase(Locale.ROOT));
        }
        return row.get(column.toLowerCase(Locale.ROOT));
    }

    protected String storedValue(JdbcCrudCommand command, String column, int id) throws SQLException {
        Map<String, Object> row = jdbcTemplate.queryForMap(command(command), new Object[] { id });
        assertNotNull("Stored row for " + id, row);
        Object value = field(row, column);
        assertNotNull("Stored field " + column, value);
        // Column types differ by source; the scalar contracts assert requested Java types separately.
        return value.toString();
    }

    protected void assertUserAbsent(int id) throws SQLException {
        Map<String, Object> row = jdbcTemplate.queryForMap(command(JdbcCrudCommand.COUNT_BY_ID), new Object[] { id });
        assertNotNull("Count result for " + id, row);
        assertEquals(1, row.size());
        assertEquals("0", row.values().iterator().next().toString());
    }
}
