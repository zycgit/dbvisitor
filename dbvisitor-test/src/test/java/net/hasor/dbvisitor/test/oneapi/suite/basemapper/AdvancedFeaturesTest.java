package net.hasor.dbvisitor.test.oneapi.suite.basemapper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * BaseMapper Advanced Features Test
 * 验证 BaseMapper 的高级特性
 */
public class AdvancedFeaturesTest extends AbstractOneApiTest {

    /**
     * 测试 loadBy - 根据参考对象加载
     */
    @Test
    public void testLoadBy() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入数据
        UserInfo user = new UserInfo();
        user.setId(44601);
        user.setName("LoadBy");
        user.setAge(30);
        mapper.insert(user);

        // 使用参考对象加载（通过主键）
        UserInfo refObj = new UserInfo();
        refObj.setId(44601);

        UserInfo loaded = mapper.loadBy(refObj);
        assertNotNull(loaded);
        assertEquals("LoadBy", loaded.getName());
    }

    /**
     * 测试 loadListBy - 批量加载
     */
    @Test
    public void testLoadListBy() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入数据
        for (int i = 1; i <= 5; i++) {
            UserInfo user = new UserInfo();
            user.setId(44700 + i);
            user.setName("LoadList" + i);
            user.setAge(25 + i);
            mapper.insert(user);
        }

        // 批量加载
        List<Object> refList = new ArrayList<>();
        UserInfo ref1 = new UserInfo();
        ref1.setId(44701);
        UserInfo ref2 = new UserInfo();
        ref2.setId(44703);
        refList.add(ref1);
        refList.add(ref2);

        List<UserInfo> loaded = mapper.loadListBy(refList);
        assertEquals(2, loaded.size());
    }

    /**
     * 测试获取 entityType
     */
    @Test
    public void testEntityType() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        Class<UserInfo> entityType = mapper.entityType();
        assertEquals(UserInfo.class, entityType);
    }

    /**
     * 测试获取 LambdaTemplate
     */
    @Test
    public void testGetLambdaTemplate() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        assertNotNull(mapper.lambda());

        // 验证可以使用 LambdaTemplate
        UserInfo user = new UserInfo();
        user.setId(44801);
        user.setName("LambdaTest");
        user.setAge(28);

        int result = mapper.lambda()//
                .insert(UserInfo.class)//
                .applyEntity(user)//
                .executeSumResult();

        assertEquals(1, result);
    }

    /**
     * 测试获取 JdbcOperations
     */
    @Test
    public void testGetJdbcOperations() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        assertNotNull(mapper.jdbc());

        // 验证可以使用 JDBC 操作
        String sql = "INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        int result = mapper.jdbc()//
                .executeUpdate(sql, new Object[] { 44901, "JdbcTest", 32 });
        assertEquals(1, result);
    }

    /**
     * 测试获取 Session
     */
    @Test
    public void testGetSession() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        assertNotNull(mapper.session());
        assertEquals(session, mapper.session());
    }

    /**
     * 测试混合使用不同操作
     */
    @Test
    public void testMixedOperations() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入
        UserInfo user = createUser(45401, "Mixed", 25);
        mapper.insert(user);

        // 查询
        UserInfo loaded = mapper.selectById(45401);
        assertNotNull(loaded);

        // 更新
        loaded.setAge(30);
        mapper.update(loaded);

        // 再次查询验证
        UserInfo updated = mapper.selectById(45401);
        assertEquals(Integer.valueOf(30), updated.getAge());

        // Upsert
        updated.setAge(35);
        mapper.upsert(updated);

        // 最终查询
        UserInfo final1 = mapper.selectById(45401);
        assertEquals(Integer.valueOf(35), final1.getAge());

        // 删除
        mapper.deleteById(45401);

        // 验证已删除
        UserInfo deleted = mapper.selectById(45401);
        assertNull(deleted);
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@advanced.com");
        u.setCreateTime(new Date());
        return u;
    }
}
