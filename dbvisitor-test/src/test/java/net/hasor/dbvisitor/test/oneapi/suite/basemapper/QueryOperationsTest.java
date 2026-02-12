package net.hasor.dbvisitor.test.oneapi.suite.basemapper;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * BaseMapper Query Operations Test
 * 验证 BaseMapper 的查询操作
 */
public class QueryOperationsTest extends AbstractOneApiTest {

    /**
     * 测试根据主键查询 - selectById
     */
    @Test
    public void testSelectById() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 先插入
        UserInfo user = new UserInfo();
        user.setId(43501);
        user.setName("SelectById");
        user.setAge(30);
        user.setEmail("select@test.com");
        mapper.insert(user);

        // 查询
        UserInfo loaded = mapper.selectById(43501);
        assertNotNull(loaded);
        assertEquals("SelectById", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
        assertEquals("select@test.com", loaded.getEmail());
    }

    /**
     * 测试批量主键查询 - selectByIds
     */
    @Test
    public void testSelectByIds() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入数据
        for (int i = 1; i <= 5; i++) {
            UserInfo user = new UserInfo();
            user.setId(43600 + i);
            user.setName("SelectBatch" + i);
            user.setAge(20 + i);
            mapper.insert(user);
        }

        // 批量查询（IN 查询不保证返回顺序，按 id 排序后断言）
        List<UserInfo> loaded = mapper.selectByIds(Arrays.asList(43601, 43603, 43605));

        assertEquals(3, loaded.size());
        loaded.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
        assertEquals("SelectBatch1", loaded.get(0).getName());
        assertEquals("SelectBatch3", loaded.get(1).getName());
        assertEquals("SelectBatch5", loaded.get(2).getName());
    }

    /**
     * 测试根据样本查询 - listBySample
     */
    @Test
    public void testListBySample() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入数据
        for (int i = 1; i <= 5; i++) {
            UserInfo user = new UserInfo();
            user.setId(43700 + i);
            user.setName("Sample" + i);
            user.setAge(i % 2 == 0 ? 30 : 25);
            mapper.insert(user);
        }

        // 根据样本查询
        UserInfo sample = new UserInfo();
        sample.setAge(30);

        List<UserInfo> loaded = mapper.listBySample(sample);
        assertEquals(2, loaded.size());
        for (UserInfo u : loaded) {
            assertEquals(Integer.valueOf(30), u.getAge());
        }
    }

    /**
     * 测试分页查询 - pageBySample
     */
    @Test
    public void testQueryByPage() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入 20 条数据
        for (int i = 1; i <= 20; i++) {
            UserInfo user = new UserInfo();
            user.setId(43900 + i);
            user.setName("Page" + i);
            user.setAge(25);
            mapper.insert(user);
        }

        // 分页查询：先通过 pageInitBySample 初始化分页对象（含 totalCount）
        UserInfo sample = new UserInfo();
        sample.setAge(25);

        Page page = mapper.pageInitBySample(sample, 1, 5);
        assertEquals(20, page.getTotalCount());
        assertEquals(4, page.getTotalPage());

        List<UserInfo> loaded = mapper.pageBySample(sample, page).getData();
        assertEquals(5, loaded.size());
    }

    /**
     * 测试统计查询 - countBySample
     */
    @Test
    public void testCountBySample() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入数据
        for (int i = 1; i <= 10; i++) {
            UserInfo user = new UserInfo();
            user.setId(44100 + i);
            user.setName("Count" + i);
            user.setAge(i % 3 == 0 ? 30 : 25);
            mapper.insert(user);
        }

        // 统计
        UserInfo sample = new UserInfo();
        sample.setAge(30);

        int count = mapper.countBySample(sample);
        assertEquals(3, count);
    }

    /**
     * 测试查询所有 - countAll
     */
    @Test
    public void testQueryAll() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入数据
        for (int i = 1; i <= 5; i++) {
            UserInfo user = new UserInfo();
            user.setId(44200 + i);
            user.setName("All" + i);
            user.setAge(28);
            mapper.insert(user);
        }

        // 查询所有
        int totalCount = mapper.countAll();
        assertTrue(totalCount >= 5);

        // 查询列表（需要过滤以避免查到其他测试的数据）
        List<UserInfo> list = mapper.query()//
                .like(UserInfo::getName, "All%")//
                .queryForList();
        assertEquals(5, list.size());
    }

    /**
     * 测试查询不存在的记录
     */
    @Test
    public void testSelectNonExistentRecord() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        UserInfo loaded = mapper.selectById(99999);
        assertNull(loaded);
    }

    /**
     * 测试查询返回空列表
     */
    @Test
    public void testQueryEmptyResult() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        UserInfo sample = new UserInfo();
        sample.setAge(999); // 不存在的 age

        List<UserInfo> loaded = mapper.listBySample(sample);
        assertNotNull(loaded);
        assertEquals(0, loaded.size());
    }
}
