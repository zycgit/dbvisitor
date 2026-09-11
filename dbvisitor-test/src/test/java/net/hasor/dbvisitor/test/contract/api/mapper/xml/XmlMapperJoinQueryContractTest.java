/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.UserOrderDTO;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class XmlMapperJoinQueryContractTest extends AbstractNxnContractTest {
    private Session session;

    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration config = newConfiguration();
        config.loadMapper("/mapper/XmlJoinQueryMapper.xml");
        this.session = config.newSession(dataSource);
    }

    @Override
    protected void initData() throws SQLException {
        // @formatter:off
        Object[][] users = {
            { baseId() + 1, "JoinQAlice", 25, "alice-j@nxn.test" },
            { baseId() + 2, "JoinQBob", 30, "bob-j@nxn.test" },
            { baseId() + 3, "JoinQCarol", 25, "carol-j@nxn.test" },
            { baseId() + 4, "JoinQDave", 40, "dave-j@nxn.test" }
        };
        // @formatter:on
        for (Object[] user : users) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", //
                    user);
        }

        // @formatter:off
        Object[][] orders = {
            { baseId() + 101, baseId() + 1, "ORD-001", new BigDecimal("100.50") },
            { baseId() + 102, baseId() + 1, "ORD-002", new BigDecimal("200.75") },
            { baseId() + 103, baseId() + 2, "ORD-003", new BigDecimal("50.00") }
        };
        // @formatter:on
        for (Object[] order : orders) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_order (id, user_id, order_no, amount, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", //
                    order);
        }
    }

    protected int baseId() {
        return 957000;
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_JOIN_DTO)
    public void joinQuery_shouldMapInnerJoinRowsToDtoResultMap() throws Exception {
        List<UserOrderDTO> list = this.session.queryStatement("xmltest.JoinQueryMapper.selectUserOrderDTO", mapOf("userId", baseId() + 1));

        assertEquals(2, list.size());
        UserOrderDTO first = list.get(0);
        assertEquals(Integer.valueOf(baseId() + 101), first.getOrderId());
        assertEquals("ORD-001", first.getOrderNo());
        assertEquals(0, new BigDecimal("100.50").compareTo(first.getAmount()));
        assertEquals(Integer.valueOf(baseId() + 1), first.getUserId());
        assertEquals("JoinQAlice", first.getUserName());
        assertEquals("alice-j@nxn.test", first.getUserEmail());
        assertNotNull(first.getCreateTime());
        assertEquals("ORD-002", list.get(1).getOrderNo());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_JOIN_MAP)
    public void joinQuery_shouldMapInnerJoinRowsToColumnMap() throws Exception {
        List<Map<String, Object>> list = this.session.queryStatement("xmltest.JoinQueryMapper.selectUserOrderMap", mapOf("userId", baseId() + 2));

        assertEquals(1, list.size());
        Map<String, Object> row = list.get(0);
        assertEquals("ORD-003", value(row, "order_no"));
        assertEquals("JoinQBob", value(row, "user_name"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_JOIN_LEFT_NULL)
    public void joinQuery_shouldRepresentLeftJoinMatchesAndNulls() throws Exception {
        requiresNxnFeature(FeatureId.LEFT_JOIN_NULL_VALUES);

        List<Map<String, Object>> withOrders = this.session.queryStatement("xmltest.JoinQueryMapper.selectUserOrderLeftJoin", mapOf("userId", baseId() + 1));
        List<Map<String, Object>> withoutOrders = this.session.queryStatement("xmltest.JoinQueryMapper.selectUserOrderLeftJoin", mapOf("userId", baseId() + 4));

        assertEquals(2, withOrders.size());
        for (Map<String, Object> row : withOrders) {
            assertNotNull(value(row, "order_id"));
        }
        assertEquals(1, withoutOrders.size());
        assertEquals("JoinQDave", value(withoutOrders.get(0), "user_name"));
        assertNull(value(withoutOrders.get(0), "order_id"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_JOIN_AGGREGATE)
    public void joinQuery_shouldAggregateJoinedRows() throws Exception {
        requiresNxnFeature(FeatureId.LEFT_JOIN_NULL_VALUES);

        List<Map<String, Object>> withOrders = this.session.queryStatement("xmltest.JoinQueryMapper.selectUserOrderAggregate", mapOf("userId", baseId() + 1));
        List<Map<String, Object>> withoutOrders = this.session.queryStatement("xmltest.JoinQueryMapper.selectUserOrderAggregate", mapOf("userId", baseId() + 4));

        assertEquals(1, withOrders.size());
        Map<String, Object> alice = withOrders.get(0);
        assertEquals("JoinQAlice", value(alice, "user_name"));
        assertEquals(2L, ((Number) value(alice, "order_count")).longValue());
        assertEquals(0, new BigDecimal("301.25").compareTo(new BigDecimal(value(alice, "total_amount").toString())));

        assertEquals(1, withoutOrders.size());
        assertEquals(0L, ((Number) value(withoutOrders.get(0), "order_count")).longValue());
        assertEquals(0, BigDecimal.ZERO.compareTo(new BigDecimal(value(withoutOrders.get(0), "total_amount").toString())));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_JOIN_SELF)
    public void joinQuery_shouldSupportSelfJoin() throws Exception {
        requiresNxnFeature(FeatureId.JOIN_NON_EQUI_CONDITION);

        List<Map<String, Object>> list = this.session.queryStatement("xmltest.JoinQueryMapper.selectSameAgeUsers", null);

        assertEquals(1, list.size());
        Map<String, Object> pair = list.get(0);
        assertEquals(25, ((Number) value(pair, "common_age")).intValue());
        assertEquals("JoinQAlice", value(pair, "user1_name"));
        assertEquals("JoinQCarol", value(pair, "user2_name"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_JOIN_SUBQUERY)
    public void joinQuery_shouldSupportSubqueryInPredicate() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.JoinQueryMapper.selectUsersWithOrders", null);

        assertEquals(2, list.size());
        assertEquals("JoinQAlice", list.get(0).getName());
        assertEquals("JoinQBob", list.get(1).getName());
    }

    private Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
    }

    private Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        String lower = key.toLowerCase();
        if (row.containsKey(lower)) {
            return row.get(lower);
        }
        return row.get(key.toUpperCase());
    }
}
