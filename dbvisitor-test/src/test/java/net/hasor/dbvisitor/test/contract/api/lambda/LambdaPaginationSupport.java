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
import java.util.List;

import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import static org.junit.Assert.assertEquals;

public abstract class LambdaPaginationSupport extends AbstractNxnContractTest {
    protected EntityQuery<? extends UserInfo> queryRows() throws SQLException {
        return this.lambdaTemplate.query(UserInfo.class);
    }

    protected EntityQuery<? extends UserInfo> queryRows(String prefix) throws SQLException {
        return queryRows().like(UserInfo::getName, prefix + "%");
    }

    protected EntityQuery<? extends UserInfo> orderRows(EntityQuery<? extends UserInfo> query) {
        return query.orderBy("id");
    }

    protected void assertPageRows(List<? extends UserInfo> rows, String prefix, int start, int size) {
        assertEquals(size, rows.size());
        for (int i = 0; i < size; i++) {
            assertEquals(prefix + (start + i), rows.get(i).getName());
        }
    }

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
