package net.hasor.dbvisitor.test.oneapi.suite.basemapper;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * BaseMapper Insert Operations Test
 * 验证 BaseMapper 的插入操作
 */
public class InsertOperationsTest extends AbstractOneApiTest {

    /**
     * 测试单条插入 - insert(T entity)
     */
    @Test
    public void testInsertSingleEntity() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        UserInfo user = new UserInfo();
        user.setId(40901);
        user.setName("SingleInsert");
        user.setAge(30);
        user.setEmail("single@basemapper.com");
        user.setCreateTime(new Date());

        int result = mapper.insert(user);
        assertEquals(1, result);

        // 验证插入成功
        UserInfo loaded = mapper.selectById(40901);
        assertNotNull(loaded);
        assertEquals("SingleInsert", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
    }

    /**
     * 测试批量插入 - insert(List<T> entity)
     */
    @Test
    public void testInsertBatchEntities() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        List<UserInfo> users = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            UserInfo user = new UserInfo();
            user.setId(41000 + i);
            user.setName("BatchUser" + i);
            user.setAge(20 + i);
            user.setEmail("batch" + i + "@basemapper.com");
            user.setCreateTime(new Date());
            users.add(user);
        }

        int result = mapper.insert(users);
        assertEquals(5, result);

        // 验证插入成功
        List<UserInfo> loaded = mapper.selectByIds(Arrays.asList(41001, 41002, 41003, 41004, 41005));
        assertEquals(5, loaded.size());
    }

    /**
     * 测试插入 NULL 值字段
     */
    @Test
    public void testInsertWithNullFields() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        UserInfo user = new UserInfo();
        user.setId(41101);
        user.setName("NullFields");
        user.setAge(null); // NULL
        user.setEmail(null); // NULL

        int result = mapper.insert(user);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(41101);
        assertNotNull(loaded);
        assertEquals("NullFields", loaded.getName());
        assertNull(loaded.getAge());
        assertNull(loaded.getEmail());
    }

    /**
     * 测试插入空列表 —— 空列表直接返回 0，不抛异常
     */
    @Test
    public void testInsertEmptyList() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        List<UserInfo> emptyList = new ArrayList<>();
        int result = mapper.insert(emptyList);
        assertEquals(0, result);
    }

    /**
     * 测试插入重复主键（应失败）
     */
    @Test
    public void testInsertDuplicatePrimaryKey() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        UserInfo user1 = new UserInfo();
        user1.setId(41201);
        user1.setName("DupKey1");
        user1.setAge(25);
        user1.setEmail("dup@basemapper.com");
        mapper.insert(user1);

        try {
            UserInfo user2 = new UserInfo();
            user2.setId(41201); // 重复主键
            user2.setName("DupKey2");
            user2.setAge(26);
            mapper.insert(user2);
            fail("Should throw exception for duplicate key");
        } catch (Exception e) {
            // 预期异常
            assertTrue(e.getMessage().toLowerCase().contains("duplicate")//
                    || e.getMessage().toLowerCase().contains("unique") //
                    || e.getMessage().toLowerCase().contains("constraint"));
        }
    }

    /**
     * 测试插入所有字段为 NULL（除主键外）
     */
    @Test
    public void testInsertAllNullFieldsExceptPK() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        UserInfo user = new UserInfo();
        user.setId(41401);
        // 其他字段都不设置（NULL）

        int result = mapper.insert(user);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(41401);
        assertNotNull(loaded);
        assertNull(loaded.getName());
        assertNull(loaded.getAge());
        assertNull(loaded.getEmail());
    }

    /**
     * 测试插入特殊字符
     */
    @Test
    public void testInsertSpecialCharacters() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        UserInfo user = new UserInfo();
        user.setId(41501);
        user.setName("O'Brien & Co.");
        user.setAge(35);
        user.setEmail("special@test.com");

        int result = mapper.insert(user);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(41501);
        assertEquals("O'Brien & Co.", loaded.getName());
    }

    /**
     * 测试插入 Unicode 字符
     */
    @Test
    public void testInsertUnicodeCharacters() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        UserInfo user = new UserInfo();
        user.setId(41601);
        user.setName("测试用户 テスト");
        user.setAge(28);
        user.setEmail("unicode@test.com");

        int result = mapper.insert(user);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(41601);
        assertEquals("测试用户 テスト", loaded.getName());
    }

    /**
     * 测试大批量插入性能
     */
    @Test
    public void testInsertLargeBatch() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        List<UserInfo> users = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            UserInfo user = new UserInfo();
            user.setId(41700 + i);
            user.setName("LargeBatch" + i);
            user.setAge(20 + (i % 50));
            user.setEmail("large" + i + "@test.com");
            users.add(user);
        }

        int result = mapper.insert(users);
        assertEquals(100, result);

        // 抽查验证
        UserInfo first = mapper.selectById(41701);
        assertNotNull(first);
        UserInfo last = mapper.selectById(41800);
        assertNotNull(last);
    }
}
