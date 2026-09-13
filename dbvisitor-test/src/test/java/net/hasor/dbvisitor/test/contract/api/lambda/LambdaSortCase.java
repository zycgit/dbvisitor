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

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class LambdaSortCase extends AbstractNxnContractTest {
    protected int baseId() {
        return 850000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SORT_MULTI_COLUMN)
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

    @Test
    @Capability(CapabilityId.LAMBDA_SORT_NULL_ORDERING)
    public void lambdaSort_shouldExposeDatasourceNullOrderingForMultiColumnSorts() throws SQLException {
        insertUser(baseId() + 11, "Alpha", 25, "null-sort-a@test.com");
        insertUser(baseId() + 12, "Beta", null, "null-sort-b@test.com");
        insertUser(baseId() + 13, "Gamma", 30, "null-sort-c@test.com");
        insertUser(baseId() + 14, "Delta", null, "null-sort-d@test.com");

        List<UserInfo> rows = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(11, 12, 13, 14))//
                .desc("age")//
                .asc("name")//
                .queryForList();
        assertEquals(4, rows.size());

        if (profile().id() == DataSourceId.PG || profile().id() == DataSourceId.ORACLE || profile().id() == DataSourceId.DB2) {
            assertNull(rows.get(0).getAge());
            assertNull(rows.get(1).getAge());
            assertNames(rows, "Beta", "Delta", "Gamma", "Alpha");
        } else {
            assertNames(rows, "Gamma", "Alpha", "Beta", "Delta");
            assertNull(rows.get(2).getAge());
            assertNull(rows.get(3).getAge());
        }
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SORT_REPEATED_COLUMN)
    public void lambdaSort_shouldAppendRepeatedColumnOrderingWithoutOverridingPrimaryOrder() throws SQLException {
        insertUser(baseId() + 21, "User1", 20, "repeat-sort-1@test.com");
        insertUser(baseId() + 22, "User2", 25, "repeat-sort-2@test.com");
        insertUser(baseId() + 23, "User3", 30, "repeat-sort-3@test.com");

        List<UserInfo> rows = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(21, 22, 23))//
                .asc("age")//
                .desc("age")//
                .queryForList();

        assertEquals(3, rows.size());
        assertTrue(rows.get(0).getAge() <= rows.get(1).getAge());
        assertTrue(rows.get(1).getAge() <= rows.get(2).getAge());
    }

    private void insertUser(int id, String name, Integer age, String email) throws SQLException {
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

    @Test
    @Capability(CapabilityId.LAMBDA_QUERY_ORDER)
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
