package net.hasor.dbvisitor.test.suite.xmlmapper;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import net.hasor.dbvisitor.test.model.UserOrderDTO;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * XML Mapper JOIN 查询测试
 * <p>测试要点：
 * <ul>
 *   <li>INNER JOIN + resultMap 映射到 DTO</li>
 *   <li>INNER JOIN + resultType="map" 映射到 Map</li>
 *   <li>LEFT JOIN 无匹配行时 null 处理</li>
 *   <li>聚合函数 COUNT/SUM + GROUP BY</li>
 *   <li>自连接（self-join）</li>
 *   <li>子查询（subquery IN）</li>
 * </ul>
 */
public class XmlJoinQueryTest extends AbstractOneApiTest {

    private Session session;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Configuration config = new Configuration();
        config.loadMapper("/mapper/XmlJoinQueryMapper.xml");
        this.session = config.newSession(dataSource);
    }

    @Override
    protected void initData() throws SQLException {
        // user_info 数据
        Object[][] users = {//
                { 57701, "JoinQAlice", 25, "alice_j@test.com" },//
                { 57702, "JoinQBob", 30, "bob_j@test.com" },//
                { 57703, "JoinQCarol", 25, "carol_j@test.com" }, // 与 Alice 同 age，用于自连接
                { 57704, "JoinQDave", 40, "dave_j@test.com" },   // 无订单，用于 LEFT JOIN null
        };
        for (Object[] u : users) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",//
                    new Object[] { u[0], u[1], u[2], u[3] });
        }

        // user_order 数据
        Object[][] orders = {//
                { 57701, 57701, "ORD-001", new BigDecimal("100.50") },//
                { 57702, 57701, "ORD-002", new BigDecimal("200.75") },//
                { 57703, 57702, "ORD-003", new BigDecimal("50.00") },//
        };
        for (Object[] o : orders) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_order (id, user_id, order_no, amount, create_time) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",//
                    new Object[] { o[0], o[1], o[2], o[3] });
        }

    }

    // ========== INNER JOIN → DTO ==========

    /** INNER JOIN + resultMap → UserOrderDTO */
    @Test
    public void testInnerJoinToDTO() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", 57701);

        List<UserOrderDTO> list = session.queryStatement("xmltest.JoinQueryMapper.selectUserOrderDTO", params);
        assertEquals(2, list.size());

        UserOrderDTO first = list.get(0);
        assertEquals(Integer.valueOf(57701), first.getOrderId());
        assertEquals("ORD-001", first.getOrderNo());
        assertEquals(0, new BigDecimal("100.50").compareTo(first.getAmount()));
        assertEquals(Integer.valueOf(57701), first.getUserId());
        assertEquals("JoinQAlice", first.getUserName());
        assertEquals("alice_j@test.com", first.getUserEmail());
        assertNotNull(first.getCreateTime());

        UserOrderDTO second = list.get(1);
        assertEquals("ORD-002", second.getOrderNo());
    }

    // ========== INNER JOIN → Map ==========

    /** INNER JOIN + resultType="map" */
    @Test
    public void testInnerJoinToMap() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", 57702);

        List<Map<String, Object>> list = session.queryStatement("xmltest.JoinQueryMapper.selectUserOrderMap", params);
        assertEquals(1, list.size());

        Map<String, Object> row = list.get(0);
        assertEquals("ORD-003", row.get("order_no"));
        assertEquals("JoinQBob", row.get("user_name"));
    }

    // ========== LEFT JOIN null 处理 ==========

    /** LEFT JOIN：有订单的用户 */
    @Test
    public void testLeftJoin_WithOrders() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", 57701);

        List<Map<String, Object>> list = session.queryStatement("xmltest.JoinQueryMapper.selectUserOrderLeftJoin", params);
        assertEquals(2, list.size());
        for (Map<String, Object> row : list) {
            assertNotNull(row.get("order_id"));
        }
    }

    /** LEFT JOIN：无订单的用户 */
    @Test
    public void testLeftJoin_NoOrders() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", 57704);

        List<Map<String, Object>> list = session.queryStatement("xmltest.JoinQueryMapper.selectUserOrderLeftJoin", params);
        assertEquals(1, list.size());
        assertEquals("JoinQDave", list.get(0).get("user_name"));
        assertNull(list.get(0).get("order_id")); // LEFT JOIN null
    }

    // ========== 聚合函数 ==========

    /** COUNT + SUM + GROUP BY：有订单的用户 */
    @Test
    public void testAggregate_WithOrders() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", 57701);

        List<Map<String, Object>> list = session.queryStatement("xmltest.JoinQueryMapper.selectUserOrderAggregate", params);
        assertEquals(1, list.size());

        Map<String, Object> row = list.get(0);
        assertEquals("JoinQAlice", row.get("user_name"));
        assertEquals(2L, ((Number) row.get("order_count")).longValue());
        assertTrue(new BigDecimal("301.25").compareTo(new BigDecimal(row.get("total_amount").toString())) == 0);
    }

    /** COUNT + SUM：无订单的用户 */
    @Test
    public void testAggregate_NoOrders() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", 57704);

        List<Map<String, Object>> list = session.queryStatement("xmltest.JoinQueryMapper.selectUserOrderAggregate", params);
        assertEquals(1, list.size());

        Map<String, Object> row = list.get(0);
        assertEquals(0L, ((Number) row.get("order_count")).longValue());
        assertEquals(0, new BigDecimal("0").compareTo(new BigDecimal(row.get("total_amount").toString())));
    }

    // ========== 自连接 ==========

    /** self-join：查找同龄用户对 */
    @Test
    public void testSelfJoin() throws Exception {
        List<Map<String, Object>> list = session.queryStatement("xmltest.JoinQueryMapper.selectSameAgeUsers", null);
        assertTrue(list.size() >= 1); // Alice(25) + Carol(25) 至少一对

        Map<String, Object> pair = list.get(0);
        assertEquals(25, ((Number) pair.get("common_age")).intValue());
        assertNotNull(pair.get("user1_name"));
        assertNotNull(pair.get("user2_name"));
    }

    // ========== 子查询 ==========

    /** 子查询 IN：查有订单的用户 */
    @Test
    public void testSubquery() throws Exception {
        List<UserInfo> list = session.queryStatement("xmltest.JoinQueryMapper.selectUsersWithOrders", null);
        // Alice(57701) 和 Bob(57702) 有订单
        assertEquals(2, list.size());
        for (UserInfo u : list) {
            assertTrue(u.getName().startsWith("JoinQ"));
        }
    }
}
