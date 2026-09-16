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
import net.hasor.dbvisitor.lambda.core.OrderNullsStrategy;
import net.hasor.dbvisitor.lambda.core.OrderType;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.env.DataSourceId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaNullSortCase extends AbstractNxnContractTest {
    // 能力归属：构造器 API / 排序。
    @Test
    @Capability(value = CapabilityId.LAMBDA_SORT_NULL_DEFAULT_ASC, column = "builder/grouping-and-ordering/ordering")
    public void defaultAscending() throws SQLException {
        assertOrdering(OrderNullsStrategy.DEFAULT, OrderType.ASC);
    }

    // 能力归属：构造器 API / 排序。
    @Test
    @Capability(value = CapabilityId.LAMBDA_SORT_NULL_ORDERING, column = "builder/grouping-and-ordering/ordering")
    public void defaultDescending() throws SQLException {
        assertOrdering(OrderNullsStrategy.DEFAULT, OrderType.DESC);
    }

    // 能力归属：构造器 API / 排序。
    @Test
    @Capability(value = CapabilityId.LAMBDA_SORT_NULL_FIRST_ASC, column = "builder/grouping-and-ordering/ordering")
    public void firstAscending() throws SQLException {
        assertOrdering(OrderNullsStrategy.FIRST, OrderType.ASC);
    }

    // 能力归属：构造器 API / 排序。
    @Test
    @Capability(value = CapabilityId.LAMBDA_SORT_NULL_FIRST_DESC, column = "builder/grouping-and-ordering/ordering")
    public void firstDescending() throws SQLException {
        assertOrdering(OrderNullsStrategy.FIRST, OrderType.DESC);
    }

    // 能力归属：构造器 API / 排序。
    @Test
    @Capability(value = CapabilityId.LAMBDA_SORT_NULL_LAST_ASC, column = "builder/grouping-and-ordering/ordering")
    public void lastAscending() throws SQLException {
        assertOrdering(OrderNullsStrategy.LAST, OrderType.ASC);
    }

    // 能力归属：构造器 API / 排序。
    @Test
    @Capability(value = CapabilityId.LAMBDA_SORT_NULL_LAST_DESC, column = "builder/grouping-and-ordering/ordering")
    public void lastDescending() throws SQLException {
        assertOrdering(OrderNullsStrategy.LAST, OrderType.DESC);
    }

    private void assertOrdering(OrderNullsStrategy strategy, OrderType direction) throws SQLException {
        insertUser(851001, "Alpha", 25, "null-a@test.com");
        insertUser(851002, "Beta", null, "null-b@test.com");
        insertUser(851003, "Gamma", 30, "null-c@test.com");
        insertUser(851004, "Delta", null, "null-d@test.com");

        List<UserInfo> rows = lambdaTemplate.query(UserInfo.class).in(UserInfo::getId, Arrays.asList(851001, 851002, 851003, 851004)).orderBy(direction, strategy, UserInfo::getAge).asc(UserInfo::getName).queryForList();

        boolean nullsFirst = strategy == OrderNullsStrategy.FIRST || strategy == OrderNullsStrategy.DEFAULT && defaultNullsFirst(direction);
        String[] values = direction == OrderType.ASC ? new String[] { "Alpha", "Gamma" } : new String[] { "Gamma", "Alpha" };
        List<String> expected = nullsFirst ? Arrays.asList("Beta", "Delta", values[0], values[1]) : Arrays.asList(values[0], values[1], "Beta", "Delta");
        assertEquals(expected, rows.stream().map(UserInfo::getName).collect(Collectors.toList()));
        assertEquals(2L, rows.stream().filter(row -> row.getAge() == null).count());
    }

    private boolean defaultNullsFirst(OrderType direction) {
        DataSourceId id = profile().id();
        if (id == DataSourceId.ELASTIC6 || id == DataSourceId.ELASTIC7 || id == DataSourceId.CLICKHOUSE) {
            return false;
        }
        boolean nullsHigh = id == DataSourceId.PG || id == DataSourceId.ORACLE || id == DataSourceId.DB2;
        return nullsHigh == (direction == OrderType.DESC);
    }

    protected void insertUser(int id, String name, Integer age, String email) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", new Object[] { id, name, age, email, new Date() });
    }
}
