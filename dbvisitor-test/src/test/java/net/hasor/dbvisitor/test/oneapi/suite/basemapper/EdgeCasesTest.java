package net.hasor.dbvisitor.test.oneapi.suite.basemapper;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * BaseMapper Edge Cases Test
 * 验证 BaseMapper 的边界条件和异常场景
 */
public class EdgeCasesTest extends AbstractOneApiTest {

    /**
     * 测试分页边界 - 页码为 0
     */
    @Test
    public void testPaginationPageZero() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入数据
        for (int i = 1; i <= 10; i++) {
            UserInfo user = new UserInfo();
            user.setId(46800 + i);
            user.setName("PageZero" + i);
            user.setAge(25);
            mapper.insert(user);
        }

        // 页码为 0（第一页）
        UserInfo sample = new UserInfo();
        sample.setAge(25);

        PageObject page = new PageObject();
        page.setPageSize(5);
        page.setCurrentPage(0); // 第一页

        List<UserInfo> loaded = mapper.pageBySample(sample, page).getData();
        assertEquals(5, loaded.size());
        for (UserInfo u : loaded) {
            assertEquals(Integer.valueOf(25), u.getAge());
        }
    }

    /**
     * 测试分页边界 - 超大页码
     */
    @Test
    public void testPaginationLargePageNumber() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入 5 条数据
        for (int i = 1; i <= 5; i++) {
            UserInfo user = new UserInfo();
            user.setId(46900 + i);
            user.setName("LargePage" + i);
            user.setAge(30);
            mapper.insert(user);
        }

        // 请求不存在的页码
        UserInfo sample = new UserInfo();
        sample.setAge(30);

        PageObject page = new PageObject();
        page.setPageSize(10);
        page.setCurrentPage(100); // 超大页码

        List<UserInfo> loaded = mapper.pageBySample(sample, page).getData();
        // 应返回空列表
        assertEquals(0, loaded.size());
    }

    /**
     * 测试分页边界 - 页大小为 0（无效参数，应返回空数据）
     */
    @Test
    public void testPaginationPageSizeZero() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        UserInfo sample = new UserInfo();
        sample.setAge(25);

        PageObject page = new PageObject();
        page.setPageSize(0); // 无效页大小
        page.setCurrentPage(0);

        List<UserInfo> loaded = mapper.pageBySample(sample, page).getData();
        assertEquals(0, loaded.size());
    }

    /**
     * 测试查询所有数据的分页
     */
    @Test
    public void testPaginationWithEmptySample() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入测试数据
        for (int i = 1; i <= 10; i++) {
            UserInfo user = new UserInfo();
            user.setId(47000 + i);
            user.setName("EmptySample" + i);
            user.setAge(25 + i % 3);
            mapper.insert(user);
        }

        // 空样本（查询所有）
        UserInfo emptySample = new UserInfo();

        PageObject page = new PageObject();
        page.setPageSize(5);
        page.setCurrentPage(0);

        List<UserInfo> loaded = mapper.pageBySample(emptySample, page).getData();
        // 应返回数据（可能包括其他测试的数据）
        assertTrue(!loaded.isEmpty());
    }

    /**
     * 测试并发插入相同主键（模拟并发冲突）
     */
    @Test
    public void testConcurrentInsert() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 先插入一条
        UserInfo user1 = new UserInfo();
        user1.setId(47101);
        user1.setName("Concurrent1");
        user1.setAge(25);
        mapper.insert(user1);

        // 尝试插入相同主键
        try {
            UserInfo user2 = new UserInfo();
            user2.setId(47101);
            user2.setName("Concurrent2");
            user2.setAge(26);
            mapper.insert(user2);
            fail("Should throw exception for duplicate key");
        } catch (Exception e) {
            // 预期的重复键异常
            assertTrue(e.getMessage().toLowerCase().contains("duplicate") //
                    || e.getMessage().toLowerCase().contains("unique")//
                    || e.getMessage().toLowerCase().contains("constraint"));
        }
    }

    /**
     * 测试主键为 NULL 的插入（SERIAL 列自动生成 ID，插入应成功）
     */
    @Test
    public void testInsertWithNullPrimaryKey() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        UserInfo user = new UserInfo();
        user.setId(null); // NULL 主键，SERIAL 列会自动生成
        user.setName("NullPK");
        user.setAge(25);

        int result = mapper.insert(user);
        assertEquals(1, result);
    }

    /**
     * 测试所有字段为 NULL 的查询样本
     */
    @Test
    public void testQueryWithAllNullSample() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入数据
        for (int i = 1; i <= 5; i++) {
            UserInfo user = new UserInfo();
            user.setId(47200 + i);
            user.setName("AllNull" + i);
            user.setAge(30);
            mapper.insert(user);
        }

        // 所有字段为 NULL 的样本
        UserInfo nullSample = new UserInfo();

        List<UserInfo> loaded = mapper.listBySample(nullSample);
        // 应返回所有数据或至少包含测试数据
        assertTrue(loaded.size() >= 5);
    }

    /**
     * 测试超长字符串（name 列为 VARCHAR(100)，1000 字符必超限抛异常）
     */
    @Test
    public void testInsertVeryLongString() throws Exception {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 创建 1000 字符超长字符串，超过 VARCHAR(100) 限制
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longName.append("A");
        }

        UserInfo user = new UserInfo();
        user.setId(47301);
        user.setName(longName.toString());
        user.setAge(25);

        try {
            mapper.insert(user);
            fail("Should throw exception for value too long");
        } catch (Exception e) {
            String msg = e.getMessage().toLowerCase();
            assertTrue("Expected value-too-long error but got: " + e.getMessage(), msg.contains("too long") || msg.contains("value too long") || msg.contains("truncat") || msg.contains("length"));
        }
    }

    /**
     * 测试负数主键
     */
    @Test
    public void testNegativePrimaryKey() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        UserInfo user = new UserInfo();
        user.setId(-1);
        user.setName("NegativePK");
        user.setAge(25);

        int result = mapper.insert(user);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(-1);
        assertNotNull(loaded);
        assertEquals("NegativePK", loaded.getName());
    }

    /**
     * 测试批量操作中部分失败（事务回滚）
     */
    @Test
    public void testBatchPartialFailure() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 先插入一条，稍后会造成冲突
        UserInfo existing = new UserInfo();
        existing.setId(47401);
        existing.setName("Existing");
        existing.setAge(25);
        mapper.insert(existing);

        List<UserInfo> batchUsers = new ArrayList<>();

        UserInfo user1 = new UserInfo();
        user1.setId(47402);
        user1.setName("Batch1");
        user1.setAge(26);
        batchUsers.add(user1);

        UserInfo user2 = new UserInfo();
        user2.setId(47401); // 重复主键
        user2.setName("Batch2");
        user2.setAge(27);
        batchUsers.add(user2);

        try {
            mapper.insert(batchUsers);
            fail("Should throw exception for duplicate key in batch");
        } catch (Exception e) {
            String msg = e.getMessage().toLowerCase();
            assertTrue(msg.contains("duplicate") || msg.contains("unique") || msg.contains("constraint"));
        }
    }

    /**
     * 测试空样本的统计
     */
    @Test
    public void testCountWithEmptySample() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入数据
        for (int i = 1; i <= 5; i++) {
            UserInfo user = new UserInfo();
            user.setId(47500 + i);
            user.setName("CountEmpty" + i);
            user.setAge(28);
            mapper.insert(user);
        }

        // 空样本
        UserInfo emptySample = new UserInfo();
        int count = mapper.countBySample(emptySample);

        // 应返回所有记录数
        assertTrue(count >= 5);
    }

    /**
     * 测试特殊 SQL 字符转义
     */
    @Test
    public void testSpecialSqlCharacters() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        UserInfo user = new UserInfo();
        user.setId(47901);
        user.setName("Test'Quote\"Double\\Slash");
        user.setAge(28);

        int result = mapper.insert(user);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(47901);
        assertNotNull(loaded);
        assertEquals("Test'Quote\"Double\\Slash", loaded.getName());
    }
}
