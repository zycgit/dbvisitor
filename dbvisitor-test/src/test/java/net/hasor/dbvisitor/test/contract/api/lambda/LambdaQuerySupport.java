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

public abstract class LambdaQuerySupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 930000;
    }

    protected void insertUsers(String prefix, int[] ages, int startId) throws SQLException {
        for (int i = 0; i < ages.length; i++) {
            insertByJdbc(startId + i, prefix + (i + 1), ages[i], prefix.toLowerCase() + (i + 1) + "@test.com");
        }
    }

    protected void insertByJdbc(int id, String name, Integer age, String email) throws SQLException {
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, email, new Date() });
    }
}
