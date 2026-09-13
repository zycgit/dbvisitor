/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.sql.Statement;

import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

public abstract class JdbcBatchSupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 640000;
    }

    protected String insertCommand() {
        return "INSERT INTO basic_types_test (id, string_value) VALUES (?, ?)";
    }

    protected String namedInsertCommand() {
        return "INSERT INTO basic_types_test (id, string_value) VALUES (:id, :val)";
    }

    protected String updateCommand() {
        return "UPDATE basic_types_test SET string_value = ? WHERE id = ?";
    }

    protected Object[] updateArguments(String value, int id) {
        return new Object[] { value, id };
    }

    protected String deleteCommand() {
        return "DELETE FROM basic_types_test WHERE id = ?";
    }

    protected String valueCommand() {
        return "SELECT string_value FROM basic_types_test WHERE id = ?";
    }

    protected String countCommand() {
        return "SELECT COUNT(*) FROM basic_types_test WHERE id = ?";
    }

    protected String countRangeCommand(boolean inclusiveEnd) {
        return inclusiveEnd ? "SELECT COUNT(*) FROM basic_types_test WHERE id BETWEEN ? AND ?"
                : "SELECT COUNT(*) FROM basic_types_test WHERE id >= ? AND id < ?";
    }

    protected String literalInsertCommand(int id, String value) {
        return "INSERT INTO basic_types_test (id, string_value) VALUES (" + id + ", '" + value + "')";
    }

    protected String literalUpdateCommand(int id, String value) {
        return "UPDATE basic_types_test SET string_value = '" + value + "' WHERE id = " + id;
    }

    protected String invalidCommand() {
        return "INSERT INTO nxn_batch_missing_table (id) VALUES (1)";
    }

    protected void insertPositionalFixture() throws SQLException {
        for (int i = 0; i < 3; i++) {
            jdbcTemplate.executeUpdate(insertCommand(),
                    new Object[] { baseId() + i + 1, "NXN-Batch-Pos-" + i });
        }
    }

    protected void assertSuccessfulBatchCounts(int[] counts) {
        assertSuccessfulBatchCounts(counts, 1);
    }

    protected int expectedUpdateCount() {
        return 1;
    }

    protected void assertSuccessfulBatchCounts(int[] counts, int expected) {
        for (int count : counts) {
            if (count != Statement.SUCCESS_NO_INFO) {
                assertMutationRows(expected, count);
            }
        }
    }
}
