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
import java.util.Map;

import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

public abstract class LambdaResultHandlingSupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 750000;
    }

    protected EntityQuery<? extends UserInfo> queryRows(String prefix) throws SQLException {
        return lambdaTemplate.query(UserInfo.class).like(UserInfo::getName, prefix + "%");
    }

    protected EntityQuery<? extends UserInfo> orderRows(EntityQuery<? extends UserInfo> query, String field) {
        return query.orderBy(field);
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

    protected Object getVal(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        if (map.containsKey(key.toUpperCase())) {
            return map.get(key.toUpperCase());
        }
        if (map.containsKey(key.toLowerCase())) {
            return map.get(key.toLowerCase());
        }
        return null;
    }

    protected static class AgeGroup {
        protected final Integer age;
        protected final Long    count;

        protected AgeGroup(Integer age, Long count) {
            this.age = age;
            this.count = count;
        }
    }
}
