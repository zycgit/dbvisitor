package net.hasor.dbvisitor.test.suite.basemapper;

import java.util.*;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * BaseMapper API - 结果集处理测试
 * 使用 BaseMapper 接口测试结果映射和转换
 * 测试场景模版来源：result 文档
 * API 实现：BaseMapper (通用 Mapper 接口)
 * 参考文档：dbvisitor/dbvisitor-doc/docs/guides/result/
 */
public class ResultHandlingTest extends AbstractOneApiTest {

    private Session              session;
    private BaseMapper<UserInfo> mapper;

    @Before
    public void setUp() throws Exception {
        super.setup();
        this.session = newSession();
        this.mapper = session.createBaseMapper(UserInfo.class);

        // 插入测试数据
        for (int i = 1; i <= 10; i++) {
            UserInfo user = new UserInfo();
            user.setId(96000 + i);
            user.setName("ResultBase" + i);
            user.setAge(20 + i);
            user.setEmail("rb" + i + "@test.com");
            mapper.insert(user);
        }
    }

    // ========== 实体对象映射测试 ==========

    /**
     * 测试单个实体映射 - selectById
     */
    @Test
    public void testEntityMapping_SelectById() {
        UserInfo user = mapper.selectById(96001);

        assertNotNull(user);
        assertEquals(Integer.valueOf(96001), user.getId());
        assertEquals("ResultBase1", user.getName());
        assertEquals(Integer.valueOf(21), user.getAge());
        assertEquals("rb1@test.com", user.getEmail());
    }

    /**
     * 测试实体列表映射 - selectByIds
     */
    @Test
    public void testEntityMapping_SelectByIds() {
        List<UserInfo> users = mapper.selectByIds(Arrays.asList(96002, 96004, 96006));

        assertEquals(3, users.size());
        assertEquals("ResultBase2", users.get(0).getName());
        assertEquals("ResultBase4", users.get(1).getName());
        assertEquals("ResultBase6", users.get(2).getName());
    }

    /**
     * 测试实体列表映射 - listBySample
     */
    @Test
    public void testEntityMapping_ListBySample() {
        UserInfo sample = new UserInfo();
        sample.setAge(25);

        List<UserInfo> users = mapper.listBySample(sample);

        assertTrue("应找到记录", users.size() > 0);
        assertTrue(users.stream().allMatch(u -> u.getAge() == 25));
    }

    // ========== 计数结果测试 ==========

    /**
     * 测试统计 - countBySample
     */
    @Test
    public void testCount_BySample() {
        UserInfo sample = new UserInfo();
        // 不设置任何条件，统计所有

        long count = mapper.countBySample(sample);

        assertTrue("应至少有10条", count >= 10);
    }

    /**
     * 测试统计 - countBySample 传入 Map
     */
    @Test
    public void testCount_ByMap() {
        Map<String, Object> params = new HashMap<>();
        params.put("age", 28);

        int count = mapper.countBySample(params);

        assertEquals(1, count);
    }

    /**
     * 测试统计 - 条件统计
     */
    @Test
    public void testCount_WithConditions() {
        UserInfo sample = new UserInfo();
        sample.setName("ResultBase%");  // LIKE 查询取决于具体实现

        // BaseMapper 的 sample 通常是 = 比较，不支持 LIKE
        // 这里测试精确匹配
        sample.setName("ResultBase3");
        long count = mapper.countBySample(sample);

        assertEquals(1, count);
    }

    // ========== 批量操作结果测试 ==========

    /**
     * 测试批量插入返回
     */
    @Test
    public void testBatch_Insert() {
        List<UserInfo> users = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            UserInfo user = new UserInfo();
            user.setId(96100 + i);
            user.setName("BatchInsert" + i);
            user.setAge(30 + i);
            users.add(user);
        }

        int result = mapper.insert(users);

        assertEquals(3, result);

        // 验证插入成功
        UserInfo loaded = mapper.selectById(96101);
        assertEquals("BatchInsert1", loaded.getName());
    }

    /**
     * 测试多次 replace 更新
     */
    @Test
    public void testBatch_Update() {
        // 先插入
        UserInfo user1 = new UserInfo();
        user1.setId(96201);
        user1.setName("BatchUpdate1");
        user1.setAge(35);
        mapper.insert(user1);

        UserInfo user2 = new UserInfo();
        user2.setId(96202);
        user2.setName("BatchUpdate2");
        user2.setAge(36);
        mapper.insert(user2);

        // 批量更新
        user1.setAge(36);
        user2.setAge(37);

        int result = mapper.replace(user1) + mapper.replace(user2);

        assertEquals(2, result);

        // 验证更新
        assertEquals(Integer.valueOf(36), mapper.selectById(96201).getAge());
        assertEquals(Integer.valueOf(37), mapper.selectById(96202).getAge());
    }

    /**
     * 测试批量删除返回
     */
    @Test
    public void testBatch_Delete() {
        // 先插入
        for (int i = 1; i <= 3; i++) {
            UserInfo user = new UserInfo();
            user.setId(96300 + i);
            user.setName("BatchDelete" + i);
            user.setAge(40);
            mapper.insert(user);
        }

        int deleted = mapper.deleteByIds(Arrays.asList(96301, 96302, 96303));

        assertEquals(3, deleted);

        // 验证删除
        assertNull(mapper.selectById(96301));
        assertNull(mapper.selectById(96302));
        assertNull(mapper.selectById(96303));
    }

    // ========== NULL 值处理测试 ==========

    /**
     * 测试 NULL 值映射 - 插入和查询
     */
    @Test
    public void testNullMapping_InsertAndSelect() {
        UserInfo user = new UserInfo();
        user.setId(96401);
        user.setName("NullTest");
        user.setAge(null);  // NULL age
        user.setEmail(null);  // NULL email

        mapper.insert(user);

        UserInfo loaded = mapper.selectById(96401);

        assertNotNull(loaded);
        assertEquals("NullTest", loaded.getName());
        assertNull(loaded.getAge());
        assertNull(loaded.getEmail());
    }

    /**
     * 测试 NULL 值更新
     */
    @Test
    public void testNullMapping_Update() {
        UserInfo user = new UserInfo();
        user.setId(96402);
        user.setName("NullUpdate");
        user.setAge(30);
        user.setEmail("test@test.com");
        mapper.insert(user);

        // 使用 updateByIdSelective 不会更新 NULL 字段
        UserInfo updates = new UserInfo();
        updates.setId(96402);
        updates.setAge(31);
        updates.setEmail(null);  // NULL 不更新

        mapper.update(updates);

        UserInfo loaded = mapper.selectById(96402);
        assertEquals(Integer.valueOf(31), loaded.getAge());
        assertEquals("test@test.com", loaded.getEmail());  // 未改变
    }

    // ========== 空结果集处理测试 ==========

    /**
     * 测试查询不存在的 ID
     */
    @Test
    public void testEmptyResult_SelectById() {
        UserInfo user = mapper.selectById(99999);

        assertNull(user);
    }

    /**
     * 测试查询不存在的条件
     */
    @Test
    public void testEmptyResult_ListBySample() {
        UserInfo sample = new UserInfo();
        sample.setName("NotExist");
        sample.setAge(999);

        List<UserInfo> users = mapper.listBySample(sample);

        assertNotNull(users);
        assertEquals(0, users.size());
    }

    /**
     * 测试空列表查询
     */
    @Test
    public void testEmptyResult_SelectByIds() {
        List<UserInfo> users = mapper.selectByIds(Arrays.asList(99991, 99992, 99993));

        assertNotNull(users);
        assertEquals(0, users.size());
    }

    // ========== 复杂场景测试 ==========

    /**
     * 测试大批量查询
     */
    @Test
    public void testComplex_LargeBatch() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 96001; i <= 96010; i++) {
            ids.add(i);
        }

        List<UserInfo> users = mapper.selectByIds(ids);

        assertEquals(10, users.size());

        // 验证顺序（取决于实现）
        Set<String> names = new HashSet<>();
        for (UserInfo user : users) {
            names.add(user.getName());
        }
        assertTrue(names.contains("ResultBase1"));
        assertTrue(names.contains("ResultBase10"));
    }

    /**
     * 测试样本对象部分字段匹配
     */
    @Test
    public void testComplex_PartialSample() {
        UserInfo sample = new UserInfo();
        // 只设置一个条件
        sample.setAge(26);

        List<UserInfo> users = mapper.listBySample(sample);

        assertEquals(1, users.size());
        assertEquals("ResultBase6", users.get(0).getName());
    }
}