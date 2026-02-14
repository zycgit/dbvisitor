package net.hasor.dbvisitor.test.suite.programmatic;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JOIN 查询测试
 * 验证各种 JOIN 类型的结果集映射（非 Mapping）
 * 仅使用跨数据库兼容的 SQL 语句
 */
public class JoinQueryTest extends AbstractOneApiTest {

    @Override
    protected void cleanTestData() {
        super.cleanTestData();
    }

    /**
     * 准备测试数据
     */
    private void prepareTestData() throws SQLException {
        // Insert users
        for (int i = 6001; i <= 6005; i++) {
            UserInfo user = new UserInfo();
            user.setId(i);
            user.setName("User" + i);
            user.setEmail("user" + i + "@example.com");
            user.setCreateTime(new Date());
            lambdaTemplate.insert(UserInfo.class)//
                    .applyEntity(user)//
                    .executeSumResult();
        }

        // Insert orders using user_order table
        String insertOrderSql = "INSERT INTO user_order (id, user_id, order_no, amount) VALUES (?, ?, ?, ?)";
        for (int i = 1; i <= 10; i++) {
            int userId = 6001 + (i - 1) % 5;
            jdbcTemplate.executeUpdate(insertOrderSql, new Object[] { 7000 + i, userId, "ORDER" + (7000 + i), 100.0 * i });
        }
    }

    /**
     * 测试 INNER JOIN
     * 验证内连接的基本结果映射
     */
    @Test
    public void testInnerJoin() throws SQLException {
        prepareTestData();

        String sql = "SELECT u.id, u.name, o.id as order_id, o.amount " + "FROM user_info u " + "INNER JOIN user_order o ON u.id = o.user_id " + "WHERE u.id = ?";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, 6001);

        assertNotNull(result);
        assertEquals(2, result.size());

        Map<String, Object> firstRow = result.get(0);
        assertEquals(6001, firstRow.get("id"));
        assertEquals("User6001", firstRow.get("name"));
        assertNotNull(firstRow.get("order_id"));
        assertNotNull(firstRow.get("amount"));
    }

    /**
     * 测试 LEFT JOIN
     * 验证左连接的结果映射，包括 NULL 值处理
     */
    @Test
    public void testLeftJoin() throws SQLException {
        prepareTestData();

        // 添加一个没有订单的用户
        UserInfo userNoOrder = new UserInfo();
        userNoOrder.setId(6010);
        userNoOrder.setName("UserNoOrder");
        userNoOrder.setEmail("noorder@example.com");
        userNoOrder.setCreateTime(new Date());
        lambdaTemplate.insert(UserInfo.class).applyEntity(userNoOrder).executeSumResult();

        String sql = "SELECT u.id, u.name, o.id as order_id " + "FROM user_info u " + "LEFT JOIN user_order o ON u.id = o.user_id " + "WHERE u.id >= ?";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, 6001);

        assertNotNull(result);
        assertTrue(result.size() > 10);

        // 验证没有订单的用户，order_id 为 null
        boolean foundNullOrder = result.stream().anyMatch(row -> row.get("id").equals(6010) && row.get("order_id") == null);
        assertTrue(foundNullOrder);
    }

    /**
     * 测试 RIGHT JOIN
     * 验证右连接的结果映射
     */
    @Test
    public void testRightJoin() throws SQLException {
        prepareTestData();

        String sql = "SELECT u.id, u.name, o.id as order_id, o.amount " + "FROM user_info u " + "RIGHT JOIN user_order o ON u.id = o.user_id " + "WHERE o.id >= ?";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, 7001);

        assertNotNull(result);
        assertEquals(10, result.size());

        // 验证所有记录都有 order_id
        for (Map<String, Object> row : result) {
            assertNotNull(row.get("order_id"));
        }
    }

    /**
     * 测试多条件 JOIN
     * 验证 JOIN ON 中包含多个条件的结果映射
     */
    @Test
    public void testJoinWithMultipleConditions() throws SQLException {
        prepareTestData();

        String sql = "SELECT u.id, u.name, o.id as order_id, o.amount " + "FROM user_info u " + "INNER JOIN user_order o ON u.id = o.user_id AND o.amount > ? " + "WHERE u.name LIKE ?";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, new Object[] { 500.0, "User%" });

        assertNotNull(result);
        assertTrue(!result.isEmpty());

        // 验证所有记录的 amount > 500
        for (Map<String, Object> row : result) {
            assertTrue(((Number) row.get("amount")).doubleValue() > 500.0);
        }
    }

    /**
     * 测试 SELF JOIN
     * 验证自连接的结果映射
     */
    @Test
    public void testSelfJoin() throws SQLException {
        prepareTestData();

        // 查找不同的用户对
        String sql = "SELECT u1.id AS user1_id, u1.name AS user1_name, " + "u2.id AS user2_id, u2.name AS user2_name " + "FROM user_info u1 " + "INNER JOIN user_info u2 ON u1.email = u2.email " + "WHERE u1.id < u2.id";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);

        assertNotNull(result);
        // 验证结果结构
        if (!result.isEmpty()) {
            Map<String, Object> firstRow = result.get(0);
            assertNotNull(firstRow.get("user1_id"));
            assertNotNull(firstRow.get("user1_name"));
            assertNotNull(firstRow.get("user2_id"));
            assertNotNull(firstRow.get("user2_name"));
        }
    }

    /**
     * 测试 CROSS JOIN
     * 验证笛卡尔积的结果映射
     */
    @Test
    public void testCrossJoin() throws SQLException {
        // 插入小数据集
        for (int i = 6100; i <= 6102; i++) {
            UserInfo user = new UserInfo();
            user.setId(i);
            user.setName("CrossUser" + i);
            user.setEmail("cross" + i + "@example.com");
            user.setCreateTime(new Date());
            lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();
        }

        String sql = "SELECT u1.id AS id1, u2.id AS id2 " + "FROM user_info u1 " + "CROSS JOIN user_info u2 " + "WHERE u1.id BETWEEN 6100 AND 6102 AND u2.id BETWEEN 6100 AND 6102";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);

        assertNotNull(result);
        assertEquals(9, result.size()); // 3 x 3 = 9

        // 验证结果结构
        Map<String, Object> firstRow = result.get(0);
        assertNotNull(firstRow.get("id1"));
        assertNotNull(firstRow.get("id2"));
    }

    /**
     * 测试 JOIN 结果排序
     * 验证带 ORDER BY 的 JOIN 查询
     */
    @Test
    public void testJoinWithOrderBy() throws SQLException {
        prepareTestData();

        String sql = "SELECT u.id, u.name, o.id as order_id, o.amount " + "FROM user_info u " + "INNER JOIN user_order o ON u.id = o.user_id " + "WHERE u.id = ? " + "ORDER BY o.amount DESC";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, 6001);

        assertNotNull(result);
        assertEquals(2, result.size());

        // 验证排序：第一条记录的 amount 应该大于等于第二条
        double firstAmount = ((Number) result.get(0).get("amount")).doubleValue();
        double secondAmount = ((Number) result.get(1).get("amount")).doubleValue();
        assertTrue(firstAmount >= secondAmount);
    }
}
