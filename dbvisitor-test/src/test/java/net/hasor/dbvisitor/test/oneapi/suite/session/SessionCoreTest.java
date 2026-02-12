package net.hasor.dbvisitor.test.oneapi.suite.session;

import java.util.List;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import net.hasor.dbvisitor.test.oneapi.model.UserOrder;
import net.hasor.dbvisitor.test.oneapi.model.UserRole;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Session 核心功能测试
 * 验证 Session 的生命周期、组件访问、BaseMapper 创建等基本能力
 */
public class SessionCoreTest extends AbstractOneApiTest {

    // ==================== Session 生命周期 ====================

    /**
     * 测试 Session 通过 DataSource 创建
     */
    @Test
    public void testSessionFromDataSource() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);

        assertNotNull(session);
        assertNotNull(session.getConfiguration());
        assertSame(config, session.getConfiguration());

        // session 可以正常工作
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);
        assertNotNull(mapper);
    }

    /**
     * 测试 Session 关闭后资源释放
     */
    @Test
    public void testSessionClose() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);

        // 先正常使用
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);
        UserInfo user = new UserInfo();
        user.setId(60001);
        user.setName("CloseTest");
        user.setAge(25);
        mapper.insert(user);

        UserInfo loaded = mapper.selectById(60001);
        assertNotNull(loaded);

        // 关闭 session
        session.close();
        // close 后不应抛出异常（幂等关闭）
    }

    // ==================== 组件访问 ====================

    /**
     * 测试 session.jdbc() 返回 JdbcTemplate
     */
    @Test
    public void testSessionJdbc() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);

        JdbcTemplate jdbc = session.jdbc();
        assertNotNull(jdbc);

        // 通过 JdbcTemplate 执行原生 SQL
        jdbc.executeUpdate("INSERT INTO user_info (id, name, age) VALUES (60010, 'JdbcTest', 30)");
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = 60010", Integer.class);
        assertEquals(Integer.valueOf(1), count);
    }

    /**
     * 测试 session.lambda() 返回 LambdaTemplate
     */
    @Test
    public void testSessionLambda() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);

        LambdaTemplate lambda = session.lambda();
        assertNotNull(lambda);

        // 通过 LambdaTemplate 构建查询
        UserInfo user = new UserInfo();
        user.setId(60020);
        user.setName("LambdaTest");
        user.setAge(28);
        int rows = lambda.insert(UserInfo.class).applyEntity(user).executeSumResult();
        assertEquals(1, rows);

        // 通过 LambdaTemplate 查询验证
        List<UserInfo> list = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 60020)//
                .queryForList();
        assertEquals(1, list.size());
        assertEquals("LambdaTest", list.get(0).getName());
    }

    /**
     * 测试 session.getConfiguration() 返回关联的 Configuration
     */
    @Test
    public void testSessionConfiguration() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);

        Configuration returned = session.getConfiguration();
        assertNotNull(returned);
        assertSame(config, returned);
    }

    // ==================== createBaseMapper ====================

    /**
     * 测试 createBaseMapper - 基本 CRUD
     */
    @Test
    public void testCreateBaseMapper_CRUD() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // Insert
        UserInfo user = new UserInfo();
        user.setId(60100);
        user.setName("CrudTest");
        user.setAge(25);
        user.setEmail("crud@test.com");
        int rows = mapper.insert(user);
        assertEquals(1, rows);

        // Select
        UserInfo loaded = mapper.selectById(60100);
        assertNotNull(loaded);
        assertEquals("CrudTest", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
        assertEquals("crud@test.com", loaded.getEmail());

        // Update
        loaded.setAge(30);
        int updated = mapper.update(loaded);
        assertEquals(1, updated);

        UserInfo reloaded = mapper.selectById(60100);
        assertEquals(Integer.valueOf(30), reloaded.getAge());

        // Delete
        int deleted = mapper.deleteById(60100);
        assertEquals(1, deleted);
        assertNull(mapper.selectById(60100));
    }

    /**
     * 测试 createBaseMapper - 不同实体类型共享同一 Session
     */
    @Test
    public void testCreateBaseMapper_MultipleEntityTypes() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);

        BaseMapper<UserInfo> userMapper = session.createBaseMapper(UserInfo.class);
        BaseMapper<UserOrder> orderMapper = session.createBaseMapper(UserOrder.class);

        // 插入用户
        UserInfo user = new UserInfo();
        user.setId(60200);
        user.setName("MultiType");
        user.setAge(30);
        userMapper.insert(user);

        // 插入订单
        UserOrder order = new UserOrder();
        order.setId(60200);
        order.setUserId(60200);
        order.setOrderNo("ORD-60200");
        order.setAmount(new java.math.BigDecimal("99.99"));
        orderMapper.insert(order);

        // 验证两种实体互不干扰
        assertNotNull(userMapper.selectById(60200));
        assertNotNull(orderMapper.selectById(60200));

        // 删除
        orderMapper.deleteById(60200);
        userMapper.deleteById(60200);
        assertNull(userMapper.selectById(60200));
        assertNull(orderMapper.selectById(60200));
    }

    /**
     * 测试 createBaseMapper - 复合主键实体
     */
    @Test
    public void testCreateBaseMapper_CompositeKey() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class);

        // 插入
        UserRole role = new UserRole(60300, 1, "Admin");
        mapper.insert(role);

        // 通过 loadBy 查询
        UserRole ref = new UserRole();
        ref.setUserId(60300);
        ref.setRoleId(1);
        UserRole loaded = mapper.loadBy(ref);
        assertNotNull(loaded);
        assertEquals("Admin", loaded.getRoleName());

        // 删除
        int deleted = mapper.delete(loaded);
        assertEquals(1, deleted);
    }

    /**
     * 测试 createBaseMapper(entityType, namespace) - 带命名空间
     * 验证命名空间隔离：同一个实体类可以在不同命名空间中分别注册和查找
     * 注：由于框架内部 LambdaTemplate 对实体有跨命名空间的唯一性检查，
     * 这里仅测试命名空间的注册和查找功能
     */
    @Test
    public void testCreateBaseMapper_WithNamespace() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);

        // 在自定义命名空间下创建 BaseMapper
        BaseMapper<UserRole> mapper = session.createBaseMapper(UserRole.class, "custom.role.namespace");
        assertNotNull(mapper);

        // 自定义命名空间应能找到该映射
        assertNotNull(config.findBySpace("custom.role.namespace", UserRole.class));

        // 默认命名空间不应存在该映射
        assertNull(config.findByEntity(UserRole.class));
    }

    /**
     * 测试 createBaseMapper - 同一实体类多次创建返回不同实例但操作同一张表
     */
    @Test
    public void testCreateBaseMapper_MultipleInstances() throws Exception {
        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);

        BaseMapper<UserInfo> mapper1 = session.createBaseMapper(UserInfo.class);
        BaseMapper<UserInfo> mapper2 = session.createBaseMapper(UserInfo.class);

        // 不同实例
        assertNotSame(mapper1, mapper2);

        // 但操作同一张表
        UserInfo user = new UserInfo();
        user.setId(60500);
        user.setName("MultiInstance");
        user.setAge(35);
        mapper1.insert(user);

        UserInfo loaded = mapper2.selectById(60500);
        assertNotNull(loaded);
        assertEquals("MultiInstance", loaded.getName());
    }

    // ==================== Configuration 多构造器 ====================

    /**
     * 测试 Configuration(Options) 构造
     */
    @Test
    public void testConfigurationWithOptions() throws Exception {
        Options options = Options.of();
        Configuration config = new Configuration(options);
        Session session = config.newSession(dataSource);

        assertNotNull(session);
        assertSame(options, config.options());

        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);
        UserInfo user = new UserInfo();
        user.setId(60600);
        user.setName("OptionsTest");
        user.setAge(40);
        mapper.insert(user);

        assertNotNull(mapper.selectById(60600));
    }

    /**
     * 测试 Configuration 的 Registry 访问器
     */
    @Test
    public void testConfigurationRegistries() throws Exception {
        Configuration config = new Configuration();

        assertNotNull(config.getTypeRegistry());
        assertNotNull(config.getMacroRegistry());
        assertNotNull(config.getRuleRegistry());
        assertNotNull(config.getMapperRegistry());
        assertNotNull(config.getMappingRegistry());
        assertNotNull(config.getClassLoader());
        assertNotNull(config.options());
    }

    /**
     * 测试 Configuration.newJdbc 创建 JdbcTemplate
     */
    @Test
    public void testConfigurationNewJdbc() throws Exception {
        Configuration config = new Configuration();
        JdbcTemplate jdbc = config.newJdbc(dataSource);
        assertNotNull(jdbc);

        jdbc.executeUpdate("INSERT INTO user_info (id, name, age) VALUES (60700, 'NewJdbc', 28)");
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = 60700", Integer.class);
        assertEquals(Integer.valueOf(1), count);
    }

    /**
     * 测试 Configuration.newLambda 创建 LambdaTemplate
     */
    @Test
    public void testConfigurationNewLambda() throws Exception {
        Configuration config = new Configuration();
        LambdaTemplate lambda = config.newLambda(dataSource);
        assertNotNull(lambda);

        // LambdaTemplate 的 jdbc() 应该也可用
        JdbcTemplate jdbc = lambda.jdbc();
        assertNotNull(jdbc);
    }

    /**
     * 测试 Configuration.loadEntityToSpace 注册实体
     */
    @Test
    public void testLoadEntityToSpace() throws Exception {
        Configuration config = new Configuration();

        // 注册实体
        config.loadEntityToSpace(UserInfo.class);

        // 之后 findByEntity 应能找到
        assertNotNull(config.findByEntity(UserInfo.class));
    }

    /**
     * 测试 Configuration.findByEntity - 未注册实体返回 null
     */
    @Test
    public void testFindByEntity_NotRegistered() throws Exception {
        Configuration config = new Configuration();

        // 未注册的实体 findByEntity 返回 null
        assertNull(config.findByEntity(UserInfo.class));
    }

    /**
     * 测试 Configuration.loadClass
     */
    @Test
    public void testConfigurationLoadClass() throws Exception {
        Configuration config = new Configuration();

        Class<?> clazz = config.loadClass("net.hasor.dbvisitor.test.oneapi.model.UserInfo");
        assertEquals(UserInfo.class, clazz);
    }

    /**
     * 测试 Configuration.loadClass - 类不存在时抛异常
     */
    @Test
    public void testConfigurationLoadClass_NotFound() throws Exception {
        Configuration config = new Configuration();

        try {
            config.loadClass("com.nonexistent.SomeClass");
            fail("应抛出 ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            // 期望
        }
    }

    /**
     * 测试同一 Configuration 创建多个 Session
     */
    @Test
    public void testMultipleSessionsFromSameConfig() throws Exception {
        Configuration config = new Configuration();

        Session session1 = config.newSession(dataSource);
        Session session2 = config.newSession(dataSource);

        assertNotSame(session1, session2);
        assertSame(session1.getConfiguration(), session2.getConfiguration());

        // 两个 session 操作同一数据库
        BaseMapper<UserInfo> mapper1 = session1.createBaseMapper(UserInfo.class);
        BaseMapper<UserInfo> mapper2 = session2.createBaseMapper(UserInfo.class);

        UserInfo user = new UserInfo();
        user.setId(60800);
        user.setName("Session1");
        user.setAge(30);
        mapper1.insert(user);

        // session2 能看到 session1 插入的数据
        UserInfo loaded = mapper2.selectById(60800);
        assertNotNull(loaded);
        assertEquals("Session1", loaded.getName());
    }
}
