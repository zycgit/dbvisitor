/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

public abstract class LambdaPredicateSupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 740000;
    }

    protected void insertUsers(String prefix, int[] ages, int startId) throws SQLException {
        for (int i = 0; i < ages.length; i++) {
            insert(startId + i, prefix + (i + 1), ages[i], prefix.toLowerCase() + (i + 1) + "@test.com");
        }
    }

    protected void insertAgeSet(String prefix, int startId) throws SQLException {
        int[] ages = { 18, 22, 25, 30, 35 };
        for (int i = 0; i < ages.length; i++) {
            insert(startId + i, prefix + ages[i], ages[i], "predicate@nxn.test");
        }
    }

    protected void insertRangeSet(String prefix, int startId) throws SQLException {
        int[] ages = { 15, 20, 25, 30, 35 };
        for (int i = 0; i < ages.length; i++) {
            insert(startId + i, prefix + ages[i], ages[i], "range@nxn.test");
        }
    }

    protected List<Integer> ids(int... offsets) {
        List<Integer> ids = new ArrayList<>();
        for (int offset : offsets) {
            ids.add(baseId() + offset);
        }
        return ids;
    }

    protected void insert(int id, String name, Integer age, String email) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, email, new Date() });
    }
}
