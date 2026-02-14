package net.hasor.dbvisitor.test.suite.fluent;
import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 基础 CRUD 测试
 * 验证 Entity 模式和 Freedom 模式下的增删改查基本操作
 */
public class CrudTest extends AbstractOneApiTest {

    /**
     * Entity 模式 - 基础 CRUD（插入/查询/更新/删除）
     */
    @Test
    public void testEntityCRUD() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 1. Insert
        UserInfo user = new UserInfo();
        user.setName("Guest");
        user.setAge(20);
        user.setEmail("guest@example.com");
        user.setCreateTime(new Date());

        int result = lambda.insert(UserInfo.class)//
                .applyEntity(user)//
                .executeSumResult();
        assertEquals(1, result);

        // 2. Select
        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getName, user.getName())//
                .queryForObject();
        assertNotNull("Loaded user should not be null", loaded);
        assertEquals("Guest", loaded.getName());
        Integer userId = loaded.getId();
        assertNotNull("ID should be generated", userId);

        // 3. Update
        int updateRes = lambda.update(UserInfo.class)//
                .eq(UserInfo::getId, userId)//
                .updateTo(UserInfo::getAge, 21)//
                .doUpdate();
        assertEquals(1, updateRes);

        UserInfo updated = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, userId)//
                .queryForObject();
        assertEquals(Integer.valueOf(21), updated.getAge());

        // 4. Delete
        int delRes = lambda.delete(UserInfo.class)//
                .eq(UserInfo::getId, userId)//
                .doDelete();
        assertEquals(1, delRes);

        int afterDelCount = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, userId)//
                .queryForCount();
        assertEquals(0, afterDelCount);
    }

    /**
     * Freedom 模式 - 基础 CRUD（Map 方式，不依赖实体类）
     */
    @Test
    public void testFreedomCRUD() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        String tableName = "user_info";

        // 1. Insert (Map-based)
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", "FreedomUser");
        userMap.put("age", 30);
        userMap.put("email", "freedom@test.com");
        userMap.put("create_time", new Date());

        int aff = lambda.insertFreedom(tableName)//
                .applyMap(userMap)//
                .executeSumResult();
        assertEquals(1, aff);

        // 2. Select (Map-based)
        Map<String, Object> resultUserInfo = lambda.queryFreedom(tableName)//
                .eq("name", "FreedomUser")//
                .queryForObject();

        assertNotNull(resultUserInfo);
        assertEquals("FreedomUser", getVal(resultUserInfo, "name"));
        assertEquals(30, ((Number) getVal(resultUserInfo, "age"))//
                .intValue());

        // 3. Update (Map-based)
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("email", "freedom_updated@test.com");

        int updateCount = lambda.updateFreedom(tableName)//
                .eq("name", "FreedomUser")//
                .updateToSample(updateMap)//
                .doUpdate();
        assertEquals(1, updateCount);

        Map<String, Object> updated = lambda.queryFreedom(tableName)//
                .eq("name", "FreedomUser")//
                .queryForObject();
        assertEquals("freedom_updated@test.com", getVal(updated, "email"));

        // 4. Delete (Map-based)
        int deleteCount = lambda.deleteFreedom(tableName)//
                .eq("name", "FreedomUser")//
                .doDelete();
        assertEquals(1, deleteCount);

        long count = lambda.queryFreedom(tableName)//
                .eq("name", "FreedomUser")//
                .queryForCount();
        assertEquals(0, count);
    }

    /**
     * Entity 模式 - 批量插入
     */
    @Test
    public void testBatchInsert() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        List<UserInfo> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserInfo u = new UserInfo();
            u.setId(100 + i);
            u.setName("User" + i);
            u.setAge(20 + i);
            u.setEmail("user" + i + "@example.com");
            u.setCreateTime(new Date());
            users.add(u);
        }

        int affected = lambda.insert(UserInfo.class)//
                .applyEntity(users)//
                .executeSumResult();
        assertEquals(10, affected);

        int count = lambda.query(UserInfo.class)//
                .queryForCount();
        assertEquals(10, count);
    }

    private Object getVal(Map<String, Object> map, String key) {
        if (map.containsKey(key))
            return map.get(key);
        if (map.containsKey(key.toUpperCase()))
            return map.get(key.toUpperCase());
        if (map.containsKey(key.toLowerCase()))
            return map.get(key.toLowerCase());
        return null;
    }
}