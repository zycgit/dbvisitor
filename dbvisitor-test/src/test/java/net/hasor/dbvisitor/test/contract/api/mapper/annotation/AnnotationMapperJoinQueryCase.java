/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.JoinMappingMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.UserOrderDTO;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class AnnotationMapperJoinQueryCase extends AbstractNxnContractTest {
    private JoinMappingMapper mapper;

    @Before
    public void createJoinMapper() throws Exception {
        Configuration configuration = newConfiguration();
        Session session = configuration.newSession(dataSource);
        this.mapper = session.createMapper(JoinMappingMapper.class);
        prepareJoinRows();
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_JOIN_DTO)
    public void annotationJoin_shouldMapInnerJoinToDto() throws SQLException {
        List<UserOrderDTO> results = this.mapper.selectUserOrderDTO(userId(1));

        assertEquals(2, results.size());
        UserOrderDTO dto = results.get(0);
        assertEquals(Integer.valueOf(orderId(1)), dto.getOrderId());
        assertEquals("NXN-ORDER-1", dto.getOrderNo());
        assertEquals(0, new BigDecimal("100.50").compareTo(dto.getAmount()));
        assertEquals(Integer.valueOf(userId(1)), dto.getUserId());
        assertEquals("AnnoJoinUser1", dto.getUserName());
        assertEquals("anno-join1@nxn.test", dto.getUserEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_JOIN_MAP)
    public void annotationJoin_shouldMapInnerJoinToMap() throws SQLException {
        List<Map<String, Object>> results = this.mapper.selectUserOrderMap(userId(2));

        assertEquals(1, results.size());
        Map<String, Object> row = results.get(0);
        assertEqualsNumber(orderId(3), row.get("order_id"));
        assertEquals("NXN-ORDER-3", row.get("order_no"));
        assertEquals("AnnoJoinUser2", row.get("user_name"));
        assertEquals("anno-join2@nxn.test", row.get("user_email"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_JOIN_ALIAS)
    public void annotationJoin_shouldMapSqlAliasesToDtoProperties() throws SQLException {
        List<UserOrderDTO> results = this.mapper.selectUserOrderWithAlias(userId(1));

        assertEquals(2, results.size());
        UserOrderDTO dto = results.get(0);
        assertEquals(Integer.valueOf(orderId(1)), dto.getOrderId());
        assertEquals("NXN-ORDER-1", dto.getOrderNo());
        assertEquals(Integer.valueOf(userId(1)), dto.getUserId());
        assertEquals("AnnoJoinUser1", dto.getUserName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_JOIN_LEFT_NULL)
    public void annotationJoin_shouldKeepNullRightSideValuesForLeftJoin() throws SQLException {
        requiresNxnFeature(FeatureId.LEFT_JOIN_NULL_VALUES);

        List<UserOrderDTO> dtoResults = this.mapper.selectUserOrderLeftJoinDTO(userId(3));
        assertEquals(1, dtoResults.size());
        UserOrderDTO dto = dtoResults.get(0);
        assertEquals(Integer.valueOf(userId(3)), dto.getUserId());
        assertEquals("AnnoJoinUser3", dto.getUserName());
        assertNull(dto.getOrderId());
        assertNull(dto.getOrderNo());
        assertNull(dto.getAmount());

        List<Map<String, Object>> mapResults = this.mapper.selectUserOrderLeftJoin(userId(3));
        assertEquals(1, mapResults.size());
        Map<String, Object> row = mapResults.get(0);
        assertEquals("AnnoJoinUser3", row.get("user_name"));
        assertNull(row.get("order_id"));
        assertNull(row.get("order_no"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_JOIN_ONE_TO_MANY)
    public void annotationJoin_shouldReturnOneRowPerMatchingChild() throws SQLException {
        List<UserOrderDTO> results = this.mapper.selectUserOrderDTO(userId(1));

        assertEquals(2, results.size());
        assertEquals(results.get(0).getUserId(), results.get(1).getUserId());
        assertEquals(results.get(0).getUserName(), results.get(1).getUserName());
        assertNotEquals(results.get(0).getOrderId(), results.get(1).getOrderId());
        assertNotEquals(results.get(0).getOrderNo(), results.get(1).getOrderNo());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_JOIN_AGGREGATE)
    public void annotationJoin_shouldMapAggregateJoinResult() throws SQLException {
        requiresNxnFeature(FeatureId.LEFT_JOIN_NULL_VALUES);

        Map<String, Object> withOrders = this.mapper.selectUserOrderAggregate(userId(1));
        assertEquals("AnnoJoinUser1", withOrders.get("user_name"));
        assertEquals(2L, ((Number) withOrders.get("order_count")).longValue());
        assertEquals(0, new BigDecimal("301.25").compareTo(new BigDecimal(withOrders.get("total_amount").toString())));

        Map<String, Object> noOrders = this.mapper.selectUserOrderAggregate(userId(3));
        assertEquals("AnnoJoinUser3", noOrders.get("user_name"));
        assertEquals(0L, ((Number) noOrders.get("order_count")).longValue());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_JOIN_MIXED_TYPES)
    public void annotationJoin_shouldMapMixedScalarTypesFromJoinedRows() throws SQLException {
        List<Map<String, Object>> results = this.mapper.selectUserOrderMixedTypes(userId(2));

        assertEquals(1, results.size());
        Map<String, Object> row = results.get(0);
        assertEqualsNumber(22, row.get("age"));
        assertEquals("AnnoJoinUser2", row.get("name"));
        assertEquals(0, new BigDecimal("150.00").compareTo(new BigDecimal(row.get("amount").toString())));
        assertEquals("NXN-ORDER-3", row.get("order_no"));
    }

    private void prepareJoinRows() throws SQLException {
        for (int i = 1; i <= 3; i++) {
            UserInfo user = new UserInfo();
            user.setId(userId(i));
            user.setName("AnnoJoinUser" + i);
            user.setAge(20 + i);
            user.setEmail("anno-join" + i + "@nxn.test");
            user.setCreateTime(new Date());
            lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();
        }

        insertOrder(orderId(1), userId(1), "NXN-ORDER-1", "100.50");
        insertOrder(orderId(2), userId(1), "NXN-ORDER-2", "200.75");
        insertOrder(orderId(3), userId(2), "NXN-ORDER-3", "150.00");
    }

    private void insertOrder(int id, int userId, String orderNo, String amount) throws SQLException {
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_order (id, user_id, order_no, amount) VALUES (?, ?, ?, ?)",//
                new Object[] { id, userId, orderNo, new BigDecimal(amount) });
    }

    private int userId(int index) {
        return 71100 + index;
    }

    private int orderId(int index) {
        return 81100 + index;
    }

    private void assertEqualsNumber(long expected, Object actual) {
        assertNotNull(actual);
        assertEquals(expected, ((Number) actual).longValue());
    }
}
