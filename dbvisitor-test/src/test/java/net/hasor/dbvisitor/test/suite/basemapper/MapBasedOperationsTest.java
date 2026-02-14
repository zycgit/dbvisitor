package net.hasor.dbvisitor.test.suite.basemapper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * BaseMapper Map-Based Operations Test
 * 验证 BaseMapper 基于 Map 的所有操作
 */
public class MapBasedOperationsTest extends AbstractOneApiTest {

    /**
     * 测试使用 Map 更新 - updateByMap
     */
    @Test
    public void testUpdateByMap() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 先插入
        UserInfo user = new UserInfo();
        user.setId(45701);
        user.setName("MapUpdate");
        user.setAge(25);
        user.setEmail("mapupdate@test.com");
        mapper.insert(user);

        // 使用 Map 更新
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("id", 45701);
        updateData.put("age", 30);
        updateData.put("name", "MapUpdated");

        int result = mapper.updateByMap(updateData);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(45701);
        assertEquals("MapUpdated", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
    }

    /**
     * 测试使用 Map 替换 - replaceByMap
     */
    @Test
    public void testReplaceByMap() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 先插入
        UserInfo user = new UserInfo();
        user.setId(45801);
        user.setName("MapReplace");
        user.setAge(28);
        user.setEmail("mapreplace@test.com");
        mapper.insert(user);

        // 使用 Map 替换（包含 NULL）
        Map<String, Object> replaceData = new HashMap<>();
        replaceData.put("id", 45801);
        replaceData.put("name", "Replaced");
        replaceData.put("age", null); // 会被设置为 NULL

        int result = mapper.replaceByMap(replaceData);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(45801);
        assertEquals("Replaced", loaded.getName());
        assertNull("Age should be NULL", loaded.getAge());
    }

    /**
     * 测试使用 Map Upsert - upsertByMap
     * 不存在时插入
     */
    @Test
    public void testUpsertByMapInsert() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        Map<String, Object> data = new HashMap<>();
        data.put("id", 45901);
        data.put("name", "MapUpsertNew");
        data.put("age", 32);
        data.put("email", "mapupsert@test.com");

        int result = mapper.upsertByMap(data);
        assertTrue(result >= 1);

        UserInfo loaded = mapper.selectById(45901);
        assertNotNull(loaded);
        assertEquals("MapUpsertNew", loaded.getName());
    }

    /**
     * 测试使用 Map Upsert - upsertByMap
     * 存在时更新
     */
    @Test
    public void testUpsertByMapUpdate() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 先插入
        UserInfo user = new UserInfo();
        user.setId(46001);
        user.setName("MapUpsertExist");
        user.setAge(25);
        mapper.insert(user);

        // Upsert 已存在的记录
        Map<String, Object> upsertData = new HashMap<>();
        upsertData.put("id", 46001);
        upsertData.put("name", "MapUpsertUpdated");
        upsertData.put("age", 30);

        int result = mapper.upsertByMap(upsertData);
        assertTrue(result >= 1);

        UserInfo loaded = mapper.selectById(46001);
        assertEquals("MapUpsertUpdated", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
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
        user.setId(46101);
        user.setName("MapDelete");
        user.setAge(28);
        mapper.insert(user);

        // 使用 Map 删除
        Map<String, Object> deleteData = new HashMap<>();
        deleteData.put("id", 46101);

        int result = mapper.deleteByMap(deleteData);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(46101);
        assertNull(loaded);
    }

    /**
     * 测试批量 Map 删除 - deleteListByMap
     */
    @Test
    public void testDeleteListByMap() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入多条数据
        for (int i = 1; i <= 5; i++) {
            UserInfo user = new UserInfo();
            user.setId(46200 + i);
            user.setName("MapBatchDelete" + i);
            user.setAge(25 + i);
            mapper.insert(user);
        }

        // 批量删除（使用 Map 列表）
        List<Map<String, Object>> deleteList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", 46200 + i);
            deleteList.add(map);
        }

        int result = mapper.deleteListByMap(deleteList);
        assertEquals(3, result);

        // 验证
        assertNull(mapper.selectById(46201));
        assertNull(mapper.selectById(46202));
        assertNull(mapper.selectById(46203));
        assertNotNull(mapper.selectById(46204)); // 未删除
    }

    /**
     * 测试 Map 缺少主键 —— Map 中无 id 键时，WHERE id IS NULL 匹配 0 行
     */
    @Test
    public void testUpdateMapWithoutPrimaryKey() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        Map<String, Object> data = new HashMap<>();
        data.put("name", "NoPK");
        data.put("age", 28);
        // 缺少 id 字段

        int result = mapper.updateByMap(data);
        assertEquals(0, result); // 无匹配行，影响 0 条
    }

    /**
     * 测试 Map 混合操作（插入、更新、删除）
     */
    @Test
    public void testMapMixedOperations() throws SQLException {
        Session session = newSession();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入
        UserInfo insertUser = new UserInfo();
        insertUser.setId(46701);
        insertUser.setName("MapMixed");
        insertUser.setAge(25);
        mapper.insert(insertUser);

        // 更新
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("id", 46701);
        updateData.put("age", 30);
        mapper.updateByMap(updateData);

        // 验证更新
        UserInfo loaded = mapper.selectById(46701);
        assertEquals(Integer.valueOf(30), loaded.getAge());

        // 删除
        Map<String, Object> deleteData = new HashMap<>();
        deleteData.put("id", 46701);
        mapper.deleteByMap(deleteData);

        // 验证删除
        UserInfo deleted = mapper.selectById(46701);
        assertNull(deleted);
    }
}
