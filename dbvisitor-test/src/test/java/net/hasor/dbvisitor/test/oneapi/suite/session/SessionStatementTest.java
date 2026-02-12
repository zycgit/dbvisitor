package net.hasor.dbvisitor.test.oneapi.suite.session;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import net.hasor.dbvisitor.test.oneapi.model.UserOrder;
import net.hasor.dbvisitor.test.oneapi.model.UserOrderDTO;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Session Statement 执行测试
 * 验证 Session 直接调用 executeStatement / queryStatement / pageStatement 的行为
 */
public class SessionStatementTest extends AbstractOneApiTest {

    private static final String NS = "oneapi.session.UserSessionMapper";

    private Configuration configuration;
    private Session       session;

    @Before
    public void setUp() throws Exception {
        super.setup();
        configuration = new Configuration();
        configuration.loadMapper("/oneapi/session/UserSessionMapper.xml");
        session = configuration.newSession(dataSource);
    }

    // ==================== executeStatement: Insert ====================

    /**
     * 测试 executeStatement - 插入单条记录
     */
    @Test
    public void testExecuteInsert() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 70001);
        params.put("name", "ExecInsert");
        params.put("age", 25);
        params.put("email", "exec@insert.com");

        Object result = session.executeStatement(NS + ".insertUser", params);
        assertEquals(1, ((Number) result).intValue());

        // 验证
        List<UserInfo> list = session.queryStatement(NS + ".queryUserById", params);
        assertEquals(1, list.size());
        assertEquals("ExecInsert", list.get(0).getName());
    }

    /**
     * 测试 executeStatement - 连续插入多条记录
     */
    @Test
    public void testExecuteInsert_Multiple() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", 70010 + i);
            params.put("name", "Multi" + i);
            params.put("age", 20 + i);
            params.put("email", "multi" + i + "@test.com");
            session.executeStatement(NS + ".insertUser", params);
        }

        List<UserInfo> all = session.queryStatement(NS + ".queryAllUsers", null);
        assertEquals(5, all.size());
    }

    // ==================== executeStatement: Update ====================

    /**
     * 测试 executeStatement - 更新记录
     */
    @Test
    public void testExecuteUpdate() throws Exception {
        // 先插入
        Map<String, Object> insert = new HashMap<>();
        insert.put("id", 70020);
        insert.put("name", "BeforeUpdate");
        insert.put("age", 30);
        insert.put("email", "before@update.com");
        session.executeStatement(NS + ".insertUser", insert);

        // 更新
        Map<String, Object> update = new HashMap<>();
        update.put("id", 70020);
        update.put("email", "after@update.com");
        Object result = session.executeStatement(NS + ".updateUserEmail", update);
        assertEquals(1, ((Number) result).intValue());

        // 验证
        Map<String, Object> q = new HashMap<>();
        q.put("id", 70020);
        List<UserInfo> list = session.queryStatement(NS + ".queryUserById", q);
        assertEquals("after@update.com", list.get(0).getEmail());
    }

    /**
     * 测试 executeStatement - 更新不存在的记录返回 0
     */
    @Test
    public void testExecuteUpdate_NoMatch() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 79999);
        params.put("email", "nope@test.com");

        Object result = session.executeStatement(NS + ".updateUserEmail", params);
        assertEquals(0, ((Number) result).intValue());
    }

    // ==================== executeStatement: Delete ====================

    /**
     * 测试 executeStatement - 删除记录
     */
    @Test
    public void testExecuteDelete() throws Exception {
        // 插入
        Map<String, Object> insert = new HashMap<>();
        insert.put("id", 70030);
        insert.put("name", "ToDelete");
        insert.put("age", 28);
        insert.put("email", "del@test.com");
        session.executeStatement(NS + ".insertUser", insert);

        // 删除
        Map<String, Object> del = new HashMap<>();
        del.put("id", 70030);
        Object result = session.executeStatement(NS + ".deleteUserById", del);
        assertEquals(1, ((Number) result).intValue());

        // 验证已删除
        List<UserInfo> list = session.queryStatement(NS + ".queryUserById", del);
        assertTrue(list.isEmpty());
    }

    /**
     * 测试 executeStatement - 删除不存在的记录返回 0
     */
    @Test
    public void testExecuteDelete_NoMatch() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 79998);

        Object result = session.executeStatement(NS + ".deleteUserById", params);
        assertEquals(0, ((Number) result).intValue());
    }

    // ==================== queryStatement: 基本查询 ====================

    /**
     * 测试 queryStatement - 按 ID 查询（resultMap）
     */
    @Test
    public void testQueryById() throws Exception {
        insertUser(70040, "QueryById", 33, "qid@test.com");

        Map<String, Object> params = new HashMap<>();
        params.put("id", 70040);
        List<UserInfo> list = session.queryStatement(NS + ".queryUserById", params);
        assertEquals(1, list.size());

        UserInfo u = list.get(0);
        assertEquals(Integer.valueOf(70040), u.getId());
        assertEquals("QueryById", u.getName());
        assertEquals(Integer.valueOf(33), u.getAge());
        assertEquals("qid@test.com", u.getEmail());
        assertNotNull(u.getCreateTime());
    }

    /**
     * 测试 queryStatement - 查询不存在的 ID 返回空列表
     */
    @Test
    public void testQueryById_NotFound() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 79997);
        List<UserInfo> list = session.queryStatement(NS + ".queryUserById", params);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    /**
     * 测试 queryStatement - 查询全部
     */
    @Test
    public void testQueryAll() throws Exception {
        insertUser(70050, "All1", 20, "all1@test.com");
        insertUser(70051, "All2", 21, "all2@test.com");
        insertUser(70052, "All3", 22, "all3@test.com");

        List<UserInfo> list = session.queryStatement(NS + ".queryAllUsers", null);
        assertEquals(3, list.size());
        // 按 id 排序
        assertEquals(Integer.valueOf(70050), list.get(0).getId());
        assertEquals(Integer.valueOf(70052), list.get(2).getId());
    }

    /**
     * 测试 queryStatement - 按条件查询（按 age）
     */
    @Test
    public void testQueryByAge() throws Exception {
        insertUser(70060, "Age1", 40, null);
        insertUser(70061, "Age2", 40, null);
        insertUser(70062, "Age3", 50, null);

        Map<String, Object> params = new HashMap<>();
        params.put("age", 40);
        List<UserInfo> list = session.queryStatement(NS + ".queryUsersByAge", params);
        assertEquals(2, list.size());
    }

    /**
     * 测试 queryStatement - count 查询（resultType=int）
     */
    @Test
    public void testQueryCount() throws Exception {
        insertUser(70070, "Count1", 20, null);
        insertUser(70071, "Count2", 20, null);

        List<Integer> list = session.queryStatement(NS + ".countUsers", null);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(2), list.get(0));
    }

    // ==================== queryStatement: 动态条件 ====================

    /**
     * 测试 queryStatement - 动态条件查询（传 name + age，严格匹配）
     */
    @Test
    public void testQueryDynamic_ByNameAndAge() throws Exception {
        insertUser(70080, "DynName", 25, "dyn@test.com");
        insertUser(70081, "Other", 30, "other@test.com");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "DynName");
        params.put("age", 25);
        List<UserInfo> list = session.queryStatement(NS + ".queryUsersByCondition", params);
        assertEquals(1, list.size());
        assertEquals("DynName", list.get(0).getName());
    }

    /**
     * 测试 queryStatement - 动态条件查询（无条件用 queryAllUsers 代替）
     */
    @Test
    public void testQueryDynamic_AllViaQueryAll() throws Exception {
        insertUser(70100, "Dyn1", 20, null);
        insertUser(70101, "Dyn2", 21, null);

        List<UserInfo> list = session.queryStatement(NS + ".queryAllUsers", null);
        assertEquals(2, list.size());
    }

    // ==================== queryStatement: Bean 参数 ====================

    /**
     * 测试 queryStatement - 使用 Bean 对象作为参数
     */
    @Test
    public void testQueryWithBeanParameter() throws Exception {
        insertUser(70110, "BeanParam", 28, "bean@test.com");

        UserInfo param = new UserInfo();
        param.setName("BeanParam");
        param.setAge(28);
        List<UserInfo> list = session.queryStatement(NS + ".queryUserByBean", param);
        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(70110), list.get(0).getId());
    }

    // ==================== queryStatement: 跨表 JOIN ====================

    /**
     * 测试 queryStatement - JOIN 查询映射到 DTO
     */
    @Test
    public void testQueryJoinToDTO() throws Exception {
        // 插入用户
        insertUser(70120, "JoinUser", 30, "join@test.com");

        // 插入订单
        Map<String, Object> orderParams = new HashMap<>();
        orderParams.put("id", 70120);
        orderParams.put("userId", 70120);
        orderParams.put("orderNo", "ORD-70120");
        orderParams.put("amount", new BigDecimal("199.50"));
        session.executeStatement(NS + ".insertOrder", orderParams);

        // JOIN 查询
        Map<String, Object> q = new HashMap<>();
        q.put("orderId", 70120);
        List<UserOrderDTO> list = session.queryStatement(NS + ".queryOrderWithUser", q);
        assertEquals(1, list.size());

        UserOrderDTO dto = list.get(0);
        assertEquals(Integer.valueOf(70120), dto.getOrderId());
        assertEquals("ORD-70120", dto.getOrderNo());
        assertEquals(0, new BigDecimal("199.50").compareTo(dto.getAmount()));
        assertEquals(Integer.valueOf(70120), dto.getUserId());
        assertEquals("JoinUser", dto.getUserName());
        assertEquals("join@test.com", dto.getUserEmail());
    }

    // ==================== queryStatement: 分页 ====================

    /**
     * 测试 queryStatement 带分页 - 第一页
     */
    @Test
    public void testQueryWithPage_FirstPage() throws Exception {
        for (int i = 1; i <= 10; i++) {
            insertUser(70200 + i, "Page" + i, 20 + i, null);
        }

        PageObject page = new PageObject();
        page.setPageSize(3);
        page.setCurrentPage(0);

        List<UserInfo> list = session.queryStatement(NS + ".queryAllUsers", null, page);
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(70201), list.get(0).getId());
    }

    /**
     * 测试 queryStatement 带分页 - 第二页
     */
    @Test
    public void testQueryWithPage_SecondPage() throws Exception {
        for (int i = 1; i <= 10; i++) {
            insertUser(70300 + i, "Page2_" + i, 20 + i, null);
        }

        PageObject page = new PageObject();
        page.setPageSize(4);
        page.setCurrentPage(1);

        List<UserInfo> list = session.queryStatement(NS + ".queryAllUsers", null, page);
        assertEquals(4, list.size());
        // 第二页第一条应为第 5 条记录
        assertEquals(Integer.valueOf(70305), list.get(0).getId());
    }

    /**
     * 测试 queryStatement 带分页 - 最后一页不满页
     */
    @Test
    public void testQueryWithPage_LastPartialPage() throws Exception {
        for (int i = 1; i <= 7; i++) {
            insertUser(70400 + i, "LastPage" + i, 20 + i, null);
        }

        PageObject page = new PageObject();
        page.setPageSize(3);
        page.setCurrentPage(2); // 第三页，7 条记录 → 3 + 3 + 1

        List<UserInfo> list = session.queryStatement(NS + ".queryAllUsers", null, page);
        assertEquals(1, list.size());
    }

    /**
     * 测试 queryStatement 带分页 - 页码超出范围返回空列表
     */
    @Test
    public void testQueryWithPage_BeyondRange() throws Exception {
        for (int i = 1; i <= 3; i++) {
            insertUser(70500 + i, "Beyond" + i, 20, null);
        }

        PageObject page = new PageObject();
        page.setPageSize(3);
        page.setCurrentPage(5); // 远超范围

        List<UserInfo> list = session.queryStatement(NS + ".queryAllUsers", null, page);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    // ==================== pageStatement: 分页结果 ====================

    /**
     * 测试 pageStatement - 返回 PageResult 包含总数
     */
    @Test
    public void testPageStatement() throws Exception {
        for (int i = 1; i <= 8; i++) {
            insertUser(70600 + i, "PageRes" + i, 25, null);
        }

        PageObject page = new PageObject();
        page.setPageSize(3);
        page.setCurrentPage(0);

        PageResult<UserInfo> result = session.pageStatement(NS + ".queryAllUsers", null, page);
        assertNotNull(result);

        // PageResult 应包含数据和分页信息
        List<UserInfo> data = result.getData();
        assertNotNull(data);
        assertEquals(3, data.size());

        // 总记录数
        long totalCount = result.getTotalCount();
        assertEquals(8, totalCount);
    }

    /**
     * 测试 pageStatement - 第二页
     */
    @Test
    public void testPageStatement_SecondPage() throws Exception {
        for (int i = 1; i <= 10; i++) {
            insertUser(70700 + i, "PageRes2_" + i, 30, null);
        }

        PageObject page = new PageObject();
        page.setPageSize(4);
        page.setCurrentPage(1);

        PageResult<UserInfo> result = session.pageStatement(NS + ".queryAllUsers", null, page);
        assertNotNull(result);
        assertEquals(4, result.getData().size());
        assertEquals(10, result.getTotalCount());
    }

    /**
     * 测试 pageStatement - 带条件分页
     */
    @Test
    public void testPageStatement_WithCondition() throws Exception {
        for (int i = 1; i <= 6; i++) {
            insertUser(70800 + i, "CondPage" + i, 35, null);
        }
        insertUser(70810, "OtherAge", 99, null);

        Map<String, Object> params = new HashMap<>();
        params.put("age", 35);

        PageObject page = new PageObject();
        page.setPageSize(2);
        page.setCurrentPage(0);

        PageResult<UserInfo> result = session.pageStatement(NS + ".queryUsersByAge", params, page);
        assertNotNull(result);
        assertEquals(2, result.getData().size());
        assertEquals(6, result.getTotalCount());
    }

    /**
     * 测试 pageStatement - 空结果
     */
    @Test
    public void testPageStatement_EmptyResult() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("age", 9999);

        PageObject page = new PageObject();
        page.setPageSize(10);
        page.setCurrentPage(0);

        PageResult<UserInfo> result = session.pageStatement(NS + ".queryUsersByAge", params, page);
        assertNotNull(result);
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getTotalCount());
    }

    // ==================== 异常场景 ====================

    /**
     * 测试 executeStatement - 引用不存在的 statementId
     */
    @Test
    public void testExecuteStatement_InvalidId() throws Exception {
        try {
            session.executeStatement(NS + ".nonExistent", new HashMap<>());
            fail("应抛出异常：引用不存在的 statementId");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * 测试 queryStatement - 引用不存在的 statementId
     */
    @Test
    public void testQueryStatement_InvalidId() throws Exception {
        try {
            session.queryStatement(NS + ".nonExistent", new HashMap<>());
            fail("应抛出异常：引用不存在的 statementId");
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    // ==================== 跨表操作场景 ====================

    /**
     * 测试 Session 跨表操作 - 用户 + 订单联合操作
     */
    @Test
    public void testCrossTableOperations() throws Exception {
        // 创建用户
        insertUser(70900, "CrossTbl", 30, "cross@test.com");

        // 为用户创建两个订单
        for (int i = 1; i <= 2; i++) {
            Map<String, Object> order = new HashMap<>();
            order.put("id", 70900 + i);
            order.put("userId", 70900);
            order.put("orderNo", "ORD-" + (70900 + i));
            order.put("amount", new BigDecimal("50.00").multiply(new BigDecimal(i)));
            session.executeStatement(NS + ".insertOrder", order);
        }

        // 查询用户的订单
        Map<String, Object> q = new HashMap<>();
        q.put("userId", 70900);
        List<UserOrder> orders = session.queryStatement(NS + ".queryOrdersByUserId", q);
        assertEquals(2, orders.size());

        // 删除一个订单
        Map<String, Object> del = new HashMap<>();
        del.put("id", 70901);
        session.executeStatement(NS + ".deleteOrderById", del);

        orders = session.queryStatement(NS + ".queryOrdersByUserId", q);
        assertEquals(1, orders.size());
    }

    // ==================== 辅助方法 ====================

    private void insertUser(int id, String name, int age, String email) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("name", name);
        params.put("age", age);
        params.put("email", email);
        session.executeStatement(NS + ".insertUser", params);
    }
}
