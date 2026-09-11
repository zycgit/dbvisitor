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

    protected void insertPositionalFixture() throws SQLException {
        for (int i = 0; i < 3; i++) {
            jdbcTemplate.executeUpdate("INSERT INTO basic_types_test (id, string_value) VALUES (?, ?)",
                    new Object[] { baseId() + i + 1, "NXN-Batch-Pos-" + i });
        }
    }

    protected void assertSuccessfulBatchCounts(int[] counts) {
        for (int count : counts) {
            if (count != Statement.SUCCESS_NO_INFO) {
                assertMutationRows(1, count);
            }
        }
    }
}
