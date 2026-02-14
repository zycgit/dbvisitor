package net.hasor.dbvisitor.test.suite.basemapper;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * BaseMapper Update Operations Test
 * 验证 BaseMapper 的更新操作
 */
public class UpdateOperationsTest extends AbstractOneApiTest {

    /**
     * 测试局部更新 - update(T sample)
     * 只更新非 NULL 字段
     */
    @Test
    public void testPartialUpdate() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 先插入
        UserInfo user = new UserInfo();
        user.setId(41901);
        user.setName("PartialUpdate");
        user.setAge(30);
        user.setEmail("partial@test.com");
        mapper.insert(user);

        // 局部更新（只更新 age）
        UserInfo updateObj = new UserInfo();
        updateObj.setId(41901);
        updateObj.setAge(35); // 只设置 age
        // name 和 email 不设置，应保持原值

        int result = mapper.update(updateObj);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(41901);
        assertEquals("PartialUpdate", loaded.getName()); // 未变
        assertEquals(Integer.valueOf(35), loaded.getAge()); // 已更新
        assertEquals("partial@test.com", loaded.getEmail()); // 未变
    }

    /**
     * 测试替换更新 - replace(T entity)
     * 更新所有字段，NULL 值也会被更新
     */
    @Test
    public void testReplaceUpdate() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 先插入
        UserInfo user = new UserInfo();
        user.setId(42001);
        user.setName("ReplaceUpdate");
        user.setAge(28);
        user.setEmail("replace@test.com");
        mapper.insert(user);

        // 替换更新（包含 NULL 值）
        UserInfo replaceObj = new UserInfo();
        replaceObj.setId(42001);
        replaceObj.setName("Replaced");
        replaceObj.setAge(null); // NULL 值也会被更新
        // email 不设置，会被当作 NULL 处理

        int result = mapper.replace(replaceObj);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(42001);
        assertEquals("Replaced", loaded.getName());
        assertNull("Age should be NULL", loaded.getAge());
    }

    /**
     * 测试 Upsert - upsert(T entity)
     * 存在则更新，不存在则插入
     */
    @Test
    public void testUpsertInsert() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // Upsert 不存在的记录（应插入）
        UserInfo user = new UserInfo();
        user.setId(42101);
        user.setName("UpsertNew");
        user.setAge(32);
        user.setEmail("upsert@test.com");

        int result = mapper.upsert(user);
        assertTrue(result >= 1);

        UserInfo loaded = mapper.selectById(42101);
        assertNotNull(loaded);
        assertEquals("UpsertNew", loaded.getName());
    }

    /**
     * 测试 Upsert 更新
     */
    @Test
    public void testUpsertUpdate() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 先插入
        UserInfo user = new UserInfo();
        user.setId(42201);
        user.setName("UpsertExist");
        user.setAge(25);
        user.setEmail("upsert2@test.com");
        mapper.insert(user);

        // Upsert 已存在的记录（应更新）
        UserInfo upsertObj = new UserInfo();
        upsertObj.setId(42201);
        upsertObj.setName("UpsertUpdated");
        upsertObj.setAge(26);
        upsertObj.setEmail("upsert2@test.com");

        int result = mapper.upsert(upsertObj);
        assertTrue(result >= 1);

        UserInfo loaded = mapper.selectById(42201);
        assertEquals("UpsertUpdated", loaded.getName());
        assertEquals(Integer.valueOf(26), loaded.getAge());
    }

    /**
     * 测试使用 Map 更新 - updateByMap
     */
    @Test
    public void testUpdateByMap() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 先插入
        UserInfo user = new UserInfo();
        user.setId(42301);
        user.setName("MapUpdate");
        user.setAge(30);
        user.setEmail("map@test.com");
        mapper.insert(user);

        // 使用 Map 更新
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("id", 42301);
        updateMap.put("age", 35);

        int result = mapper.updateByMap(updateMap);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(42301);
        assertEquals(Integer.valueOf(35), loaded.getAge());
    }

    /**
     * 测试更新不存在的记录
     */
    @Test
    public void testUpdateNonExistentRecord() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        UserInfo user = new UserInfo();
        user.setId(99999); // 不存在的 ID
        user.setName("NonExistent");
        user.setAge(30);

        int result = mapper.update(user);
        assertEquals(0, result); // 应返回 0
    }

    /**
     * 测试将字段更新为 NULL
     */
    @Test
    public void testUpdateToNull() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 先插入
        UserInfo user = new UserInfo();
        user.setId(42501);
        user.setName("ToNull");
        user.setAge(30);
        user.setEmail("tonull@test.com");
        mapper.insert(user);

        // 使用 replace 将字段更新为 NULL
        UserInfo replaceObj = new UserInfo();
        replaceObj.setId(42501);
        replaceObj.setName("ToNull");
        replaceObj.setAge(null); // 更新为 NULL

        mapper.replace(replaceObj);

        UserInfo loaded = mapper.selectById(42501);
        assertNull(loaded.getAge());
    }

    /**
     * 测试更新时主键为 NULL —— 生成 WHERE id IS NULL 条件，匹配 0 行
     */
    @Test
    public void testUpdateWithNullPrimaryKey() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        UserInfo user = new UserInfo();
        user.setId(null); // NULL 主键
        user.setName("NullPK");
        user.setAge(30);

        int result = mapper.update(user);
        assertEquals(0, result); // 无匹配行，影响 0 条
    }
}
