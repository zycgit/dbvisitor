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

import net.hasor.dbvisitor.jdbc.PreparedStatementSetter;

import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class JdbcParameterSupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 660000;
    }

    /** Supplies datasource command material without changing parameter sources or assertions. */
    protected String sql(String statement) {
        return statement;
    }

    protected String command(JdbcParameterCommand command) throws SQLException {
        return sql(command.sql());
    }

    protected String fixtureTable() {
        return sql("user_info");
    }

    protected String fixtureColumn(String name) {
        return name;
    }

    protected String orderFragment() {
        return "age DESC";
    }

    protected void prepareFragmentOrder() throws SQLException {
    }

    // Readback verifies stored parameters; scalar-result conversion has its own contract.
    protected String readEmail(String command, Object args) throws SQLException {
        return jdbcTemplate.queryForObject(command, args, String.class);
    }

    protected String readEmail(String command, PreparedStatementSetter args) throws SQLException {
        return jdbcTemplate.queryForObject(command, args, String.class);
    }

    protected void assertNumericField(int expected, Object actual) {
        assertEquals(expected, ((Number) actual).intValue());
    }

    protected void writeParameters(String command, Object args) throws SQLException {
        jdbcTemplate.executeUpdate(command, args);
    }

    protected void insert(int id, String name, int age, String email) throws SQLException {
        writeParameters(command(JdbcParameterCommand.INSERT_POSITIONAL), //
                new Object[] { id, name, age, email, new Date() });
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
