package net.hasor.dbvisitor.test.oneapi.suite.basemapper;

import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * BaseMapper Delete Operations Test
 * 验证 BaseMapper 的删除操作
 */
public class DeleteOperationsTest extends AbstractOneApiTest {

    /**
     * 测试根据主键删除 - deleteById(Serializable id)
     */
    @Test
    public void testDeleteById() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 先插入
        UserInfo user = new UserInfo();
        user.setId(42701);
        user.setName("DeleteById");
        user.setAge(30);
        user.setEmail("delete@test.com");
        mapper.insert(user);

        // 删除
        int result = mapper.deleteById(42701);
        assertEquals(1, result);

        // 验证已删除
        UserInfo loaded = mapper.selectById(42701);
        assertNull(loaded);
    }

    /**
     * 测试批量主键删除 - deleteByIds(List)
     */
    @Test
    public void testDeleteByIds() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入多条数据
        for (int i = 1; i <= 5; i++) {
            UserInfo user = new UserInfo();
            user.setId(42800 + i);
            user.setName("DeleteBatch" + i);
            user.setAge(25 + i);
            user.setEmail("batch" + i + "@test.com");
            mapper.insert(user);
        }

        // 批量删除
        List<Integer> ids = Arrays.asList(42801, 42802, 42803);
        int result = mapper.deleteByIds(ids);
        assertEquals(3, result);

        // 验证
        UserInfo user1 = mapper.selectById(42801);
        assertNull(user1);
        UserInfo user4 = mapper.selectById(42804);
        assertNotNull("Should not be deleted", user4);
    }

    /**
     * 测试根据实体删除 - delete(T entity)
     */
    @Test
    public void testDeleteByEntity() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 先插入
        UserInfo user = new UserInfo();
        user.setId(42901);
        user.setName("DeleteEntity");
        user.setAge(28);
        mapper.insert(user);

        // 使用实体删除（根据主键）
        UserInfo deleteObj = new UserInfo();
        deleteObj.setId(42901);

        int result = mapper.delete(deleteObj);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(42901);
        assertNull(loaded);
    }

    /**
     * 测试使用 Map 删除 - deleteByMap
     */
    @Test
    public void testDeleteByMap() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 先插入
        UserInfo user = new UserInfo();
        user.setId(43001);
        user.setName("DeleteMap");
        user.setAge(32);
        mapper.insert(user);

        // 使用 Map 删除
        Map<String, Object> deleteMap = new HashMap<>();
        deleteMap.put("id", 43001);

        int result = mapper.deleteByMap(deleteMap);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(43001);
        assertNull(loaded);
    }

    /**
     * 测试批量删除实体列表 - deleteList(List)
     */
    @Test
    public void testDeleteEntityList() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入数据
        List<UserInfo> users = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            UserInfo user = new UserInfo();
            user.setId(43200 + i);
            user.setName("DeleteList" + i);
            user.setAge(25);
            mapper.insert(user);
            users.add(user);
        }

        // 批量删除（只删除前 3 个）
        List<UserInfo> toDelete = users.subList(0, 3);
        int result = mapper.deleteList(toDelete);
        assertEquals(3, result);

        // 验证
        assertNull(mapper.selectById(43201));
        assertNotNull(mapper.selectById(43204));
    }

    /**
     * 测试删除不存在的记录
     */
    @Test
    public void testDeleteNonExistentRecord() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 删除不存在的 ID
        int result = mapper.deleteById(99999);
        assertEquals(0, result);
    }

    /**
     * 测试删除后插入相同主键
     */
    @Test
    public void testDeleteThenInsertSameId() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入
        UserInfo user1 = new UserInfo();
        user1.setId(43401);
        user1.setName("First");
        user1.setAge(30);
        mapper.insert(user1);

        // 删除
        mapper.deleteById(43401);

        // 再次插入相同 ID
        UserInfo user2 = new UserInfo();
        user2.setId(43401);
        user2.setName("Second");
        user2.setAge(35);
        int result = mapper.insert(user2);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(43401);
        assertEquals("Second", loaded.getName());
        assertEquals(Integer.valueOf(35), loaded.getAge());
    }

    /**
     * 测试批量删除空列表
     */
    @Test
    public void testDeleteEmptyList() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        List<Integer> emptyIds = new ArrayList<>();
        int result = mapper.deleteByIds(emptyIds);
        assertEquals(0, result);
    }

    /**
     * 测试使用 NULL 主键删除
     */
    @Test
    public void testDeleteWithNullId() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        try {
            mapper.deleteById(null);
            fail("Should throw exception for NULL id");
        } catch (Exception e) {
            // 预期异常
            assertTrue(e.getMessage().contains("null")//
                    || e.getMessage().contains("NULL"));
        }
    }
}
