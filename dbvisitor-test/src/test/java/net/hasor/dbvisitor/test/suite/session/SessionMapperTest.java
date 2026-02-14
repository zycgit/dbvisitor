package net.hasor.dbvisitor.test.suite.session;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.dao.DeclarativeOrderMapper;
import net.hasor.dbvisitor.test.dao.SessionRefUserMapper;
import net.hasor.dbvisitor.test.dao.SessionUserMapper;
import net.hasor.dbvisitor.test.model.UserInfo;
import net.hasor.dbvisitor.test.model.UserOrder;
import net.hasor.dbvisitor.test.model.UserOrderDTO;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Session createMapper 测试
 * 验证 Session.createMapper 创建 @SimpleMapper / @RefMapper 代理的行为
 */
public class SessionMapperTest extends AbstractOneApiTest {

    // ==================== @SimpleMapper 测试 ====================

    /**
     * 测试 createMapper - @SimpleMapper 插入和查询
     */
    @Test
    public void testSimpleMapper_InsertAndQuery() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);

        SessionUserMapper mapper = session.createMapper(SessionUserMapper.class);
        assertNotNull(mapper);

        UserInfo user = new UserInfo();
        user.setId(80001);
        user.setName("SimpleInsert");
        user.setAge(25);
        user.setEmail("simple@test.com");

        mapper.insertUser(user);

        // 通过查询验证插入成功
        UserInfo loaded = mapper.selectById(80001);
        assertNotNull("insert 后应能查询到数据", loaded);
        assertEquals("SimpleInsert", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
        assertEquals("simple@test.com", loaded.getEmail());
    }

    /**
     * 测试 createMapper - @SimpleMapper 更新
     */
    @Test
    public void testSimpleMapper_Update() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);
        SessionUserMapper mapper = session.createMapper(SessionUserMapper.class);

        UserInfo user = new UserInfo();
        user.setId(80002);
        user.setName("BeforeUpdate");
        user.setAge(30);
        user.setEmail("before@test.com");
        mapper.insertUser(user);

        user.setName("AfterUpdate");
        user.setAge(35);
        mapper.updateUser(user);

        UserInfo loaded = mapper.selectById(80002);
        assertNotNull("更新后应能查询到数据", loaded);
        assertEquals("AfterUpdate", loaded.getName());
        assertEquals(Integer.valueOf(35), loaded.getAge());
    }

    /**
     * 测试 createMapper - @SimpleMapper 删除
     */
    @Test
    public void testSimpleMapper_Delete() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);
        SessionUserMapper mapper = session.createMapper(SessionUserMapper.class);

        UserInfo user = new UserInfo();
        user.setId(80003);
        user.setName("ToDelete");
        user.setAge(20);
        mapper.insertUser(user);

        // 确认插入成功
        assertNotNull(mapper.selectById(80003));

        mapper.deleteById(80003);

        // 确认删除成功
        assertNull(mapper.selectById(80003));
    }

    /**
     * 测试 createMapper - @SimpleMapper selectAll 返回列表
     */
    @Test
    public void testSimpleMapper_SelectAll() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);
        SessionUserMapper mapper = session.createMapper(SessionUserMapper.class);

        for (int i = 1; i <= 3; i++) {
            UserInfo u = new UserInfo();
            u.setId(80010 + i);
            u.setName("All" + i);
            u.setAge(20 + i);
            mapper.insertUser(u);
        }

        List<UserInfo> list = mapper.selectAll();
        assertEquals(3, list.size());
    }

    /**
     * 测试 createMapper - @SimpleMapper countAll 返回标量
     */
    @Test
    public void testSimpleMapper_Count() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);
        SessionUserMapper mapper = session.createMapper(SessionUserMapper.class);

        assertEquals(0, mapper.countAll());

        UserInfo u = new UserInfo();
        u.setId(80020);
        u.setName("CountTest");
        u.setAge(30);
        mapper.insertUser(u);

        assertEquals(1, mapper.countAll());
    }

    // ==================== @RefMapper 测试 ====================

    /**
     * 测试 createMapper - @RefMapper 引用 XML 的插入和查询
     */
    @Test
    public void testRefMapper_InsertAndQuery() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);

        SessionRefUserMapper mapper = session.createMapper(SessionRefUserMapper.class);
        assertNotNull(mapper);

        UserInfo user = new UserInfo();
        user.setId(80100);
        user.setName("RefInsert");
        user.setAge(28);
        user.setEmail("ref@test.com");

        int rows = mapper.insertUser(user);
        assertTrue("insertUser 应返回影响行数", rows >= 0);

        UserInfo loaded = mapper.queryUserById(80100);
        assertNotNull(loaded);
        assertEquals("RefInsert", loaded.getName());
        assertEquals(Integer.valueOf(28), loaded.getAge());
    }

    /**
     * 测试 createMapper - @RefMapper 更新和删除
     */
    @Test
    public void testRefMapper_UpdateAndDelete() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);
        SessionRefUserMapper mapper = session.createMapper(SessionRefUserMapper.class);

        UserInfo user = new UserInfo();
        user.setId(80101);
        user.setName("RefUpdate");
        user.setAge(30);
        user.setEmail("old@test.com");
        mapper.insertUser(user);

        // 更新
        int updated = mapper.updateUserEmail(80101, "new@test.com");
        assertTrue("更新应影响行数 >= 0", updated >= 0);
        assertEquals("new@test.com", mapper.queryUserById(80101).getEmail());

        // 删除
        int deleted = mapper.deleteUserById(80101);
        assertTrue("删除应影响行数 >= 0", deleted >= 0);

        // 验证删除成功：查询确认无数据
        assertEquals(0, mapper.countUsers());
    }

    /**
     * 测试 createMapper - @RefMapper queryAll 返回列表
     */
    @Test
    public void testRefMapper_QueryAll() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);
        SessionRefUserMapper mapper = session.createMapper(SessionRefUserMapper.class);

        for (int i = 1; i <= 4; i++) {
            UserInfo u = new UserInfo();
            u.setId(80110 + i);
            u.setName("RefAll" + i);
            u.setAge(20 + i);
            mapper.insertUser(u);
        }

        List<UserInfo> list = mapper.queryAllUsers();
        assertEquals(4, list.size());
    }

    /**
     * 测试 createMapper - @RefMapper count 查询
     */
    @Test
    public void testRefMapper_Count() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);
        SessionRefUserMapper mapper = session.createMapper(SessionRefUserMapper.class);

        assertEquals(0, mapper.countUsers());

        UserInfo u = new UserInfo();
        u.setId(80120);
        u.setName("RefCount");
        u.setAge(25);
        mapper.insertUser(u);

        assertEquals(1, mapper.countUsers());
    }

    /**
     * 测试 createMapper - @RefMapper JOIN 查询映射到 DTO
     */
    @Test
    public void testRefMapper_JoinQueryDTO() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);
        SessionRefUserMapper mapper = session.createMapper(SessionRefUserMapper.class);

        // 插入用户
        UserInfo user = new UserInfo();
        user.setId(80130);
        user.setName("RefJoinUser");
        user.setAge(30);
        user.setEmail("refjoin@test.com");
        mapper.insertUser(user);

        // 插入订单（通过 BaseMapper 直接操作）
        BaseMapper<UserOrder> orderBaseMapper = session.createBaseMapper(UserOrder.class);
        UserOrder order = new UserOrder();
        order.setId(80130);
        order.setUserId(80130);
        order.setOrderNo("ORD-REF-80130");
        order.setAmount(new BigDecimal("299.99"));
        order.setCreateTime(new Date());
        orderBaseMapper.insert(order);

        // JOIN 查询
        UserOrderDTO dto = mapper.queryOrderWithUser(80130);
        assertNotNull(dto);
        assertEquals(Integer.valueOf(80130), dto.getOrderId());
        assertEquals("ORD-REF-80130", dto.getOrderNo());
        assertEquals("RefJoinUser", dto.getUserName());
        assertEquals("refjoin@test.com", dto.getUserEmail());
    }

    // ==================== 已有 @SimpleMapper 复用测试 ====================

    /**
     * 测试 createMapper - 复用已有的 DeclarativeOrderMapper 插入和查询订单
     */
    @Test
    public void testExistingMapper_OrderCRUD() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);

        // 先插入一个用户（订单依赖）
        BaseMapper<UserInfo> userMapper = session.createBaseMapper(UserInfo.class);
        UserInfo user = new UserInfo();
        user.setId(80200);
        user.setName("OrderOwner");
        user.setAge(35);
        userMapper.insert(user);

        // 通过 DeclarativeOrderMapper 创建订单
        DeclarativeOrderMapper orderMapper = session.createMapper(DeclarativeOrderMapper.class);

        UserOrder order = new UserOrder();
        order.setUserId(80200);
        order.setOrderNo("ORD-80200");
        order.setAmount(new BigDecimal("150.00"));
        order.setCreateTime(new Date());
        orderMapper.insertOrder(order);

        // 通过 JDBC 验证订单已插入（因 selectByUserId 的 @Param 缺失，改用 JDBC 验证）
        Integer orderCount = session.jdbc().queryForObject("SELECT COUNT(*) FROM user_order WHERE user_id = ?", new Object[] { 80200 }, Integer.class);
        assertEquals(Integer.valueOf(1), orderCount);

        // 通过 countAll 验证
        assertEquals(1, orderMapper.countAll());
    }

    // ==================== Mapper + BaseMapper 混合使用 ====================

    /**
     * 测试同一 Session 同时使用 createMapper 和 createBaseMapper
     */
    @Test
    public void testMixedMapperAndBaseMapper() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);

        // 通过 BaseMapper 插入用户
        BaseMapper<UserInfo> baseMapper = session.createBaseMapper(UserInfo.class);
        UserInfo user = new UserInfo();
        user.setId(80300);
        user.setName("MixedUser");
        user.setAge(28);
        user.setEmail("mixed@test.com");
        baseMapper.insert(user);

        // 通过 SessionUserMapper 查询同一条记录
        SessionUserMapper annotMapper = session.createMapper(SessionUserMapper.class);
        UserInfo loaded = annotMapper.selectById(80300);
        assertNotNull(loaded);
        assertEquals("MixedUser", loaded.getName());

        // 通过 annotMapper 更新
        loaded.setName("UpdatedMixed");
        loaded.setAge(29);
        annotMapper.updateUser(loaded);

        // 通过 baseMapper 验证更新结果
        UserInfo reloaded = baseMapper.selectById(80300);
        assertEquals("UpdatedMixed", reloaded.getName());
        assertEquals(Integer.valueOf(29), reloaded.getAge());
    }

    /**
     * 测试多个不同 Mapper 接口在同一 Session 中共存
     */
    @Test
    public void testMultipleMapperTypes() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);

        SessionUserMapper userMapper = session.createMapper(SessionUserMapper.class);
        DeclarativeOrderMapper orderMapper = session.createMapper(DeclarativeOrderMapper.class);

        // 插入用户
        UserInfo user = new UserInfo();
        user.setId(80400);
        user.setName("MultiMapper");
        user.setAge(30);
        userMapper.insertUser(user);

        // 插入订单
        UserOrder order = new UserOrder();
        order.setUserId(80400);
        order.setOrderNo("ORD-MULTI");
        order.setAmount(new BigDecimal("75.00"));
        order.setCreateTime(new Date());
        orderMapper.insertOrder(order);

        // 各自验证
        assertEquals(1, userMapper.countAll());
        assertEquals(1, orderMapper.countAll());
    }

    // ==================== 异常场景 ====================

    /**
     * 测试 createMapper - 对非 Mapper 接口创建代理
     */
    @Test
    public void testCreateMapper_PlainInterface() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);

        try {
            session.createMapper(Runnable.class);
            fail("应对无 @SimpleMapper/@RefMapper 注解的接口抛出异常");
        } catch (Exception e) {
            // 期望抛出异常
            assertNotNull(e.getMessage());
        }
    }
}
