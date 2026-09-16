/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class LambdaSortCase extends AbstractNxnContractTest {
    protected int baseId() {
        return 850000;
    }

    // 能力归属：构造器 API / 排序。
    @Test
    @Capability(value = CapabilityId.LAMBDA_SORT_MULTI_COLUMN, column = "builder/grouping-and-ordering/ordering")
    public void lambdaSort_shouldHonorMultiColumnPriorityAndMixedDirections() throws SQLException {
        insertUser(baseId() + 1, "Charlie", 25, "sort-a@test.com");
        insertUser(baseId() + 2, "Alice", 25, "sort-a@test.com");
        insertUser(baseId() + 3, "Bob", 25, "sort-b@test.com");
        insertUser(baseId() + 4, "David", 30, "sort-c@test.com");
        insertUser(baseId() + 5, "Eve", 20, "sort-d@test.com");

        List<UserInfo> ageDescNameAsc = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(1, 2, 3, 4, 5))//
                .desc("age")//
                .asc("name")//
                .queryForList();
        assertNames(ageDescNameAsc, "David", "Alice", "Bob", "Charlie", "Eve");

        List<UserInfo> ageDescEmailAscNameAsc = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(1, 2, 3, 4))//
                .desc("age")//
                .asc("email")//
                .asc("name")//
                .queryForList();
        assertNames(ageDescEmailAscNameAsc, "David", "Alice", "Charlie", "Bob");

        List<UserInfo> ageAscNameDesc = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(1, 2, 3, 4, 5))//
                .asc("age")//
                .desc("name")//
                .queryForList();
        assertNames(ageAscNameDesc, "Eve", "Charlie", "Bob", "Alice", "David");
    }

    // 能力归属：构造器 API / 排序。
    @Test
    @Capability(value = CapabilityId.LAMBDA_SORT_REPEATED_COLUMN, column = "builder/grouping-and-ordering/ordering")
    public void lambdaSort_shouldAppendRepeatedColumnOrderingWithoutOverridingPrimaryOrder() throws SQLException {
        insertUser(baseId() + 21, "User1", 20, "repeat-sort-1@test.com");
        insertUser(baseId() + 22, "User2", 25, "repeat-sort-2@test.com");
        insertUser(baseId() + 23, "User3", 30, "repeat-sort-3@test.com");

        var query = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(21, 22, 23))//
                .asc("age")//
                .desc("age");

        if (!profile().supportsFeature(FeatureId.REPEATED_ORDER_BY_COLUMN)) {
            try {
                query.queryForList();
                fail("SQL Server must reject repeated ORDER BY columns");
            } catch (SQLException e) {
                assertTrue(e.getMessage(), e.getMessage().contains("more than once"));
            }
            List<UserInfo> rows = lambdaTemplate.query(UserInfo.class)//
                    .in(UserInfo::getId, ids(21, 22, 23)).desc("age").asc("id").queryForList();
            assertEquals(3, rows.size());
            assertEquals(Integer.valueOf(30), rows.get(0).getAge());
            return;
        }

        List<UserInfo> rows = query.queryForList();
        assertEquals(3, rows.size());
        assertTrue(rows.get(0).getAge() <= rows.get(1).getAge());
        assertTrue(rows.get(1).getAge() <= rows.get(2).getAge());
    }

    protected void insertUser(int id, String name, Integer age, String email) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, email, new Date() });
    }

    private List<Integer> ids(int... offsets) {
        return Arrays.stream(offsets).map(offset -> baseId() + offset).boxed().collect(Collectors.toList());
    }

    private void assertNames(List<UserInfo> rows, String... names) {
        assertEquals(names.length, rows.size());
        for (int i = 0; i < names.length; i++) {
            assertEquals(names[i], rows.get(i).getName());
        }
    }

    // 能力归属：构造器 API / 排序。
    @Test
    @Capability(value = CapabilityId.LAMBDA_QUERY_ORDER, column = "builder/grouping-and-ordering/ordering")
    public void lambdaQueryOrder_shouldReturnRowsInRequestedOrder() throws SQLException {
        int[] ages = { 22, 35, 28, 19 };
        for (int i = 0; i < ages.length; i++) {
            insertUser(baseId() + 50 + i, "Ord" + (i + 1), ages[i], "ord" + (i + 1) + "@test.com");
        }

        List<UserInfo> users = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 50, baseId() + 53)//
                .desc("age")//
                .queryForList();

        assertEquals(4, users.size());
        assertEquals(Integer.valueOf(35), users.get(0).getAge());
        assertEquals(Integer.valueOf(19), users.get(3).getAge());
        assertEquals(Arrays.asList(35, 28, 22, 19), users.stream().map(UserInfo::getAge).collect(Collectors.toList()));
    }
}
