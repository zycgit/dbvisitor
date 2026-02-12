package net.hasor.dbvisitor.test.oneapi.suite.declarative;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.dao.declarative.JoinMappingMapper;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import net.hasor.dbvisitor.test.oneapi.model.UserOrderDTO;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for JOIN query result mapping via @Query:
 * - INNER JOIN → DTO (flat structure)
 * - INNER JOIN → Map (dynamic keys)
 * - LEFT JOIN with null values
 * - Column alias mapping
 * - One-to-many (multiple rows per user)
 * - JOIN with aggregation (GROUP BY)
 * - Mixed data types in JOIN results
 */
public class JoinMappingTest extends AbstractOneApiTest {

    private JoinMappingMapper mapper;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Configuration configuration = new Configuration();
        Session session = configuration.newSession(dataSource);
        mapper = session.createMapper(JoinMappingMapper.class);

        prepareJoinTestData();
    }

    private void prepareJoinTestData() throws SQLException {
        // Insert users
        for (int i = 1; i <= 3; i++) {
            UserInfo user = new UserInfo();
            user.setId(71000 + i);
            user.setName("JoinUser" + i);
            user.setAge(20 + i);
            user.setEmail("join" + i + "@test.com");
            user.setCreateTime(new Date());
            lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();
        }

        // Insert orders (user 71001 has 2 orders, user 71002 has 1, user 71003 has none)
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_order (id, user_id, order_no, amount) VALUES (?, ?, ?, ?)",//
                new Object[] { 81001, 71001, "ORDER001", new BigDecimal("100.50") });
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_order (id, user_id, order_no, amount) VALUES (?, ?, ?, ?)",//
                new Object[] { 81002, 71001, "ORDER002", new BigDecimal("200.75") });
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_order (id, user_id, order_no, amount) VALUES (?, ?, ?, ?)",//
                new Object[] { 81003, 71002, "ORDER003", new BigDecimal("150.00") });
    }

    // ==================== INNER JOIN → DTO ====================

    /** INNER JOIN maps multi-table columns to flat DTO fields */
    @Test
    public void testInnerJoin_ToDTO() throws SQLException {
        List<UserOrderDTO> results = mapper.selectUserOrderDTO(71001);

        assertNotNull(results);
        assertEquals(2, results.size());

        UserOrderDTO dto = results.get(0);
        assertNotNull(dto.getOrderId());
        assertEquals("ORDER001", dto.getOrderNo());
        assertEquals(0, new BigDecimal("100.50").compareTo(dto.getAmount()));
        assertEquals(Integer.valueOf(71001), dto.getUserId());
        assertEquals("JoinUser1", dto.getUserName());
        assertEquals("join1@test.com", dto.getUserEmail());
    }

    // ==================== INNER JOIN → Map ====================

    /** INNER JOIN maps columns to Map with column names as keys */
    @Test
    public void testInnerJoin_ToMap() throws SQLException {
        List<Map<String, Object>> results = mapper.selectUserOrderMap(71002);

        assertNotNull(results);
        assertEquals(1, results.size());

        Map<String, Object> row = results.get(0);
        assertNotNull(row.get("order_id"));
        assertEquals("ORDER003", row.get("order_no"));
        assertEquals("JoinUser2", row.get("user_name"));
        assertEquals("join2@test.com", row.get("user_email"));
    }

    // ==================== Column alias mapping ====================

    /** SQL column aliases (AS) correctly map to DTO fields */
    @Test
    public void testAlias_DTOMapping() throws SQLException {
        List<UserOrderDTO> results = mapper.selectUserOrderWithAlias(71001);

        assertNotNull(results);
        assertTrue(results.size() > 0);

        UserOrderDTO dto = results.get(0);
        assertNotNull(dto.getOrderId());  // o.id AS orderId
        assertNotNull(dto.getOrderNo());  // o.order_no AS orderNo
        assertNotNull(dto.getUserId());   // u.id AS userId
        assertNotNull(dto.getUserName()); // u.name AS userName
    }

    // ==================== LEFT JOIN with nulls ====================

    /** LEFT JOIN — user without orders has null order fields */
    @Test
    public void testLeftJoin_NullMapping() throws SQLException {
        // User 71003 has no orders
        List<UserOrderDTO> results = mapper.selectUserOrderLeftJoinDTO(71003);

        assertNotNull(results);
        assertEquals(1, results.size());

        UserOrderDTO dto = results.get(0);
        assertEquals(Integer.valueOf(71003), dto.getUserId());
        assertEquals("JoinUser3", dto.getUserName());
        // Order fields should be null (no matching orders)
        assertNull(dto.getOrderId());
        assertNull(dto.getOrderNo());
        assertNull(dto.getAmount());
    }

    /** LEFT JOIN → Map — null values for unmatched right side */
    @Test
    public void testLeftJoin_MapNullValues() throws SQLException {
        List<Map<String, Object>> results = mapper.selectUserOrderLeftJoin(71003);

        assertNotNull(results);
        assertEquals(1, results.size());

        Map<String, Object> row = results.get(0);
        assertNotNull(row.get("user_name"));
        assertNull(row.get("order_id"));
        assertNull(row.get("order_no"));
    }

    // ==================== One-to-many ====================

    /** One user with multiple orders — each order is a separate row */
    @Test
    public void testOneToMany_MultipleRows() throws SQLException {
        List<UserOrderDTO> results = mapper.selectUserOrderDTO(71001);

        assertNotNull(results);
        assertEquals(2, results.size());

        // Same user info in both rows
        assertEquals(results.get(0).getUserId(), results.get(1).getUserId());
        assertEquals(results.get(0).getUserName(), results.get(1).getUserName());

        // Different order info
        assertNotEquals(results.get(0).getOrderId(), results.get(1).getOrderId());
        assertNotEquals(results.get(0).getOrderNo(), results.get(1).getOrderNo());
    }

    // ==================== Aggregate JOIN ====================

    /** JOIN with GROUP BY and aggregate functions (COUNT, SUM) */
    @Test
    public void testAggregate_JoinGroupBy() throws SQLException {
        Map<String, Object> result = mapper.selectUserOrderAggregate(71001);

        assertNotNull(result);
        assertEquals("JoinUser1", result.get("user_name"));
        // User 71001 has 2 orders
        assertEquals(2L, ((Number) result.get("order_count")).longValue());
        // Total = 100.50 + 200.75 = 301.25
        assertTrue(new BigDecimal("301.25").compareTo(
                new BigDecimal(result.get("total_amount").toString())) == 0);
    }

    /** Aggregate JOIN for user with no orders — count=0, sum=0 */
    @Test
    public void testAggregate_NoOrders() throws SQLException {
        Map<String, Object> result = mapper.selectUserOrderAggregate(71003);

        assertNotNull(result);
        assertEquals("JoinUser3", result.get("user_name"));
        assertEquals(0L, ((Number) result.get("order_count")).longValue());
    }

    // ==================== Mixed data types ====================

    /** JOIN result contains multiple data types (Integer, String, BigDecimal) */
    @Test
    public void testMixed_DataTypes() throws SQLException {
        List<Map<String, Object>> results = mapper.selectUserOrderMixedTypes(71002);

        assertNotNull(results);
        assertTrue(results.size() > 0);

        Map<String, Object> row = results.get(0);
        assertNotNull(row.get("age"));
        assertNotNull(row.get("name"));
        assertNotNull(row.get("amount"));
        assertNotNull(row.get("order_no"));
    }
}
