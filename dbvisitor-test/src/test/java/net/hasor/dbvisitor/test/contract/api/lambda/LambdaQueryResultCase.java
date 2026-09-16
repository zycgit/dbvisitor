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
import java.util.Map;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class LambdaQueryResultCase extends AbstractNxnContractTest {
    protected int baseId() {
        return 710000;
    }

    // 能力归属：构造器 API / 查询操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_EMPTY_LIST, column = "builder/queries/query")
    public void lambdaQueryForList_shouldReturnNonNullEmptyListWhenNoRowsMatch() throws SQLException {
        List<UserInfo> result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 1)//
                .queryForList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // 能力归属：构造器 API / 查询操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_EMPTY_MAP_LIST, column = "builder/queries/query")
    public void lambdaQueryForMapList_shouldReturnNonNullEmptyListWhenNoRowsMatch() throws SQLException {
        List<Map<String, Object>> result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 2)//
                .queryForMapList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // 能力归属：构造器 API / 查询操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_EDGE_QUERY_NO_RESULT, column = "builder/queries/query")
    public void lambdaQueryForObject_shouldReturnNullWhenNoRowMatches() throws SQLException {
        UserInfo result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 1)//
                .queryForObject();

        assertNull(result);
    }

    // 能力归属：构造器 API / 查询操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_EDGE_QUERY_MULTI_RESULT, column = "builder/queries/query")
    public void lambdaQueryForObject_shouldReturnFirstRowWhenMultipleRowsMatch() throws SQLException {
        insertUser(baseId() + 11, "NXN-Lambda-Edge-Multi-1", 25);
        insertUser(baseId() + 12, "NXN-Lambda-Edge-Multi-2", 26);

        var query = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(baseId() + 11, baseId() + 12));
        boolean ordered = profile().supportsFeature(FeatureId.SCALAR_ORDER_BY);
        if (ordered) {
            query.orderBy("id");
        }
        UserInfo result = query.queryForObject();

        assertNotNull(result);
        if (ordered) {
            assertEquals(Integer.valueOf(baseId() + 11), result.getId());
            assertEquals("NXN-Lambda-Edge-Multi-1", result.getName());
        } else {
            assertTrue(Arrays.asList(baseId() + 11, baseId() + 12).contains(result.getId()));
            assertEquals("NXN-Lambda-Edge-Multi-" + (result.getId() - baseId() - 10), result.getName());
        }
    }

    protected void insertUser(int id, String name, Integer age) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, name.toLowerCase() + "@nxn.test", new Date() });
    }
}
