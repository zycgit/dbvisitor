/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.Date;

import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

public abstract class LambdaPaginationSupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 720000;
    }

    protected void insertBatch(String prefix, int count, int startId) throws SQLException {
        for (int i = 0; i < count; i++) {
            insert(startId + i, prefix + i, 20 + i);
        }
    }

    protected void insert(int id, String name, Integer age) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, name.toLowerCase() + "@nxn.test", new Date() });
    }
}
