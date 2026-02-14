package net.hasor.dbvisitor.test.suite.basemapper;

import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * BaseMapper API - 参数传递测试
 * 使用 BaseMapper 接口测试各种参数传递方式
 * 测试场景模版来源：args 套件
 * API 实现：BaseMapper (通用 Mapper 接口)
 */
public class ParameterPassingTest extends AbstractOneApiTest {

    private Session              session;
    private BaseMapper<UserInfo> mapper;

    @Before
    public void setUp() throws java.io.IOException, SQLException {
        super.setup();
        this.session = newSession();
        this.mapper = session.createBaseMapper(UserInfo.class);
    }

    // ========== 主键参数测试 ==========

    /**
     * 测试主键参数 - selectById 单个主键
     */
    @Test
    public void testPrimaryKey_SelectById() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(92001);
        user.setName("PKUser");
        user.setAge(25);
        mapper.insert(user);

        UserInfo loaded = mapper.selectById(92001);

        assertNotNull(loaded);
        assertEquals("PKUser", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
    }

    /**
     * 测试主键参数 - selectByIds 批量主键
     */
    @Test
    public void testPrimaryKey_SelectByIds() throws SQLException {
        for (int i = 1; i <= 5; i++) {
            UserInfo user = new UserInfo();
            user.setId(92010 + i);
            user.setName("PKBatch" + i);
            user.setAge(20 + i);
            mapper.insert(user);
        }

        List<UserInfo> loaded = mapper.selectByIds(Arrays.asList(92011, 92013, 92015));

        assertEquals(3, loaded.size());
        assertEquals("PKBatch1", loaded.get(0).getName());
        assertEquals("PKBatch3", loaded.get(1).getName());
        assertEquals("PKBatch5", loaded.get(2).getName());
    }

    /**
     * 测试主键参数 - deleteById 删除
     */
    @Test
    public void testPrimaryKey_DeleteById() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(92020);
        user.setName("PKDelete");
        user.setAge(30);
        mapper.insert(user);

        int deleted = mapper.deleteById(92020);

        assertEquals(1, deleted);
        assertNull(mapper.selectById(92020));
    }

    /**
     * 测试主键参数 - deleteByIds 批量删除
     */
    @Test
    public void testPrimaryKey_DeleteByIds() throws SQLException {
        for (int i = 1; i <= 3; i++) {
            UserInfo user = new UserInfo();
            user.setId(92030 + i);
            user.setName("PKDelBatch" + i);
            user.setAge(25 + i);
            mapper.insert(user);
        }

        int deleted = mapper.deleteByIds(Arrays.asList(92031, 92032, 92033));

        assertEquals(3, deleted);
        assertTrue(mapper.selectByIds(Arrays.asList(92031, 92032, 92033)).isEmpty());
    }

    // ========== 样本对象参数测试 ==========

    /**
     * 测试样本对象 - listBySample 查询
     */
    @Test
    public void testSample_ListBySample() throws SQLException {
        UserInfo user1 = new UserInfo();
        user1.setId(92101);
        user1.setName("Sample");
        user1.setAge(25);
        mapper.insert(user1);

        UserInfo user2 = new UserInfo();
        user2.setId(92102);
        user2.setName("Sample");
        user2.setAge(30);
        mapper.insert(user2);

        // 样本：name = "Sample"
        UserInfo sample = new UserInfo();
        sample.setName("Sample");

        List<UserInfo> results = mapper.listBySample(sample);

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(u -> "Sample".equals(u.getName())));
    }

    /**
     * 测试样本对象 - listBySample 多属性匹配
     */
    @Test
    public void testSample_MultipleProperties() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(92103);
        user.setName("MultiSample");
        user.setAge(28);
        user.setEmail("multi@test.com");
        mapper.insert(user);

        // 样本：name = "MultiSample" AND age = 28
        UserInfo sample = new UserInfo();
        sample.setName("MultiSample");
        sample.setAge(28);

        List<UserInfo> results = mapper.listBySample(sample);

        assertEquals(1, results.size());
        assertEquals("multi@test.com", results.get(0).getEmail());
    }

    /**
     * 测试样本对象 - countBySample 计数
     */
    @Test
    public void testSample_CountBySample() throws SQLException {
        for (int i = 1; i <= 5; i++) {
            UserInfo user = new UserInfo();
            user.setId(92110 + i);
            user.setName("CountSample");
            user.setAge(25 + i);
            mapper.insert(user);
        }

        UserInfo sample = new UserInfo();
        sample.setName("CountSample");

        long count = mapper.countBySample(sample);

        assertEquals(5, count);
    }

    // ========== Map 参数测试 ==========

    /**
     * 测试 Map 参数 - countBySample 传入 Map 计数
     */
    @Test
    public void testMap_CountByMap() throws SQLException {
        for (int i = 1; i <= 4; i++) {
            UserInfo user = new UserInfo();
            user.setId(92210 + i);
            user.setName("MapCount");
            user.setAge(25);
            mapper.insert(user);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("name", "MapCount");

        int count = mapper.countBySample(params);

        assertEquals(4, count);
    }

    /**
     * 测试 Map 参数 - deleteByMap 按主键删除（Map 必须包含主键字段）
     */
    @Test
    public void testMap_DeleteByMap() throws SQLException {
        for (int i = 1; i <= 2; i++) {
            UserInfo user = new UserInfo();
            user.setId(92230 + i);
            user.setName("MapDelete");
            user.setAge(25 + i);
            mapper.insert(user);
        }

        Map<String, Object> params1 = new HashMap<>();
        params1.put("id", 92231);
        int d1 = mapper.deleteByMap(params1);
        assertEquals(1, d1);

        Map<String, Object> params2 = new HashMap<>();
        params2.put("id", 92232);
        int d2 = mapper.deleteByMap(params2);
        assertEquals(1, d2);

        assertEquals(0, mapper.countAll());
    }

    // ========== 实体对象参数测试 ==========

    /**
     * 测试实体对象 - insert 插入
     */
    @Test
    public void testEntity_Insert() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(92301);
        user.setName("EntityInsert");
        user.setAge(27);
        user.setEmail("entity@test.com");

        int inserted = mapper.insert(user);

        assertEquals(1, inserted);

        UserInfo loaded = mapper.selectById(92301);
        assertEquals("EntityInsert", loaded.getName());
    }

    /**
     * 测试实体对象 - insert 插入（NULL 字段也会被插入）
     */
    @Test
    public void testEntity_InsertSelective() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(92302);
        user.setName("Selective");
        user.setAge(25);
        // email 为 NULL，不应插入

        int inserted = mapper.insert(user);

        assertEquals(1, inserted);

        UserInfo loaded = mapper.selectById(92302);
        assertNull(loaded.getEmail());
    }

    /**
     * 测试实体对象 - replace 替换更新（全字段覆盖）
     */
    @Test
    public void testEntity_UpdateById() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(92303);
        user.setName("OldName");
        user.setAge(28);
        mapper.insert(user);

        user.setName("NewName");
        user.setAge(29);

        int updated = mapper.replace(user);

        assertEquals(1, updated);

        UserInfo loaded = mapper.selectById(92303);
        assertEquals("NewName", loaded.getName());
        assertEquals(Integer.valueOf(29), loaded.getAge());
    }

    /**
     * 测试实体对象 - update 局部更新（NULL 字段不更新）
     */
    @Test
    public void testEntity_UpdateByIdSelective() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(92304);
        user.setName("Original");
        user.setAge(30);
        user.setEmail("original@test.com");
        mapper.insert(user);

        UserInfo updates = new UserInfo();
        updates.setId(92304);
        updates.setAge(31);
        // name 和 email 为 NULL，不应更新

        int updated = mapper.update(updates);

        assertEquals(1, updated);

        UserInfo loaded = mapper.selectById(92304);
        assertEquals("Original", loaded.getName());  // 未改变
        assertEquals(Integer.valueOf(31), loaded.getAge());
        assertEquals("original@test.com", loaded.getEmail());  // 未改变
    }

    // ========== 复杂参数场景 ==========

    /**
     * 测试复杂场景 - 样本对象 NULL 值处理
     */
    @Test
    public void testComplex_SampleWithNull() throws SQLException {
        UserInfo user1 = new UserInfo();
        user1.setId(92401);
        user1.setName("NullTest");
        user1.setAge(25);
        user1.setEmail("test1@test.com");
        mapper.insert(user1);

        UserInfo user2 = new UserInfo();
        user2.setId(92402);
        user2.setName("NullTest");
        user2.setAge(25);
        // email 为 NULL
        mapper.insert(user2);

        // 样本只匹配 name 和 age，email 为 NULL 不参与匹配
        UserInfo sample = new UserInfo();
        sample.setName("NullTest");
        sample.setAge(25);

        long count = mapper.countBySample(sample);

        assertEquals(2, count);  // 应匹配两条
    }

    /**
     * 测试复杂场景 - 批量操作与参数
     */
    @Test
    public void testComplex_BatchOperations() throws SQLException {
        List<Integer> ids = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            UserInfo user = new UserInfo();
            user.setId(92410 + i);
            user.setName("Batch" + i);
            user.setAge(20 + i);
            mapper.insert(user);
            if (i % 2 == 0) {
                ids.add(user.getId());
            }
        }

        // 批量查询偶数 ID
        List<UserInfo> loaded = mapper.selectByIds(ids);

        assertEquals(5, loaded.size());
        assertTrue(loaded.stream().allMatch(u -> u.getName().startsWith("Batch")));

        // 批量删除偶数 ID
        int deleted = mapper.deleteByIds(ids);

        assertEquals(5, deleted);

        // 验证剩余奇数 ID
        UserInfo sample = new UserInfo();
        sample.setName("Batch1");

        assertNotNull(mapper.listBySample(sample));
    }
}
