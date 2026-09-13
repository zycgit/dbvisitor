/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

/** Command material hooks shared by type-handler contracts. */
public abstract class TypeJdbcCommandSupport extends AbstractNxnContractTest {
    protected int executeInsert(String command, Object[] parameters) throws SQLException {
        return this.jdbcTemplate.executeUpdate(command, parameters);
    }

    protected int executeInsert(String command, Map<String, Object> parameters) throws SQLException {
        return this.jdbcTemplate.executeUpdate(command, parameters);
    }

    protected Object[] selectParameters(int id) {
        return new Object[] { id };
    }

    protected String insertCommand(String table, String columns) throws SQLException {
        String[] parameters = new String[columns.split(",").length];
        Arrays.fill(parameters, "?");
        return insertCommand(table, columns, parameters);
    }

    protected String insertCommand(String table, String columns, String... parameters) throws SQLException {
        return "INSERT INTO " + table + " (" + columns + ") VALUES (" + String.join(", ", parameters) + ")";
    }

    protected String selectCommand(String table, String columns) throws SQLException {
        return "SELECT " + columns + " FROM " + table + " WHERE id = ?";
    }
}
