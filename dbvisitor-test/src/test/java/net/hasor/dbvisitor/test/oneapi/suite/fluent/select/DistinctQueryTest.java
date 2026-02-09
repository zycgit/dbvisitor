package net.hasor.dbvisitor.test.oneapi.suite.fluent.select;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * DISTINCT Query Test
 * 验证 DISTINCT 去重查询功能
 * 注意: Lambda API 可能需要通过 applySelect("DISTINCT ...") 实现
 */
public class DistinctQueryTest extends AbstractOneApiTest {

    /**
     * 测试 DISTINCT 单列查询
     * SQL: SELECT DISTINCT age FROM user_info
     */
    @Test
    public void testDistinctSingleColumn() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入重复年龄的数据
        lambda.insert(UserInfo.class).applyEntity(createUser(9001, "User1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9002, "User2", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9003, "User3", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9004, "User4", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9005, "User5", 30)).executeSumResult();

        // 使用 DISTINCT 查询不重复的年龄
        List<Map<String, Object>> result = lambda.query(UserInfo.class)//
                .applySelect("distinct age")//
                .like(UserInfo::getName, "User%")//
                .orderBy("age")//
                .queryForMapList();

        assertNotNull(result);
        assertEquals("Should have 3 distinct ages", 3, result.size());

        // 验证去重后的值
        assertEquals(20, result.get(0).get("age"));
        assertEquals(25, result.get(1).get("age"));
        assertEquals(30, result.get(2).get("age"));
    }

    /**
     * 测试 DISTINCT 多列查询
     * SQL: SELECT DISTINCT age, email FROM user_info
     */
    @Test
    public void testDistinctMultipleColumns() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入部分重复的数据
        lambda.insert(UserInfo.class).applyEntity(createUser(9101, "Multi1", 20, "same@test.com")).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9102, "Multi2", 20, "same@test.com")).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9103, "Multi3", 20, "diff@test.com")).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9104, "Multi4", 25, "other@test.com")).executeSumResult();

        // DISTINCT 多列
        List<Map<String, Object>> result = lambda.query(UserInfo.class)//
                .applySelect("distinct age, email")//
                .like(UserInfo::getName, "Multi%")//
                .queryForMapList();

        assertNotNull(result);
        assertEquals("Should have 3 distinct (age, email) combinations", 3, result.size());
    }

    /**
     * 测试 DISTINCT + COUNT
     * SQL: SELECT COUNT(DISTINCT age) FROM user_info
     */
    @Test
    public void testDistinctCount() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入数据
        lambda.insert(UserInfo.class).applyEntity(createUser(9201, "Count1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9202, "Count2", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9203, "Count3", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9204, "Count4", 30)).executeSumResult();

        // COUNT(DISTINCT age)
        List<Map<String, Object>> result = lambda.query(UserInfo.class)//
                .applySelect("count(distinct age) as distinct_count")//
                .like(UserInfo::getName, "Count%")//
                .queryForMapList();

        assertNotNull(result);
        assertEquals(1, result.size());

        Object distinctCount = result.get(0)//
                .get("distinct_count");
        assertNotNull(distinctCount);
        assertTrue("Distinct count should be at least 3", ((Number) distinctCount).intValue() >= 3);
    }

    /**
     * 测试 DISTINCT + WHERE 条件
     * SQL: SELECT DISTINCT age FROM user_info WHERE age > 20
     */
    @Test
    public void testDistinctWithWhere() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入数据
        lambda.insert(UserInfo.class).applyEntity(createUser(9301, "Where1", 18)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9302, "Where2", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9303, "Where3", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9304, "Where4", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9305, "Where5", 30)).executeSumResult();

        // DISTINCT age WHERE age > 20
        List<Map<String, Object>> result = lambda.query(UserInfo.class)//
                .applySelect("DISTINCT age")//
                .like(UserInfo::getName, "Where%")//
                .gt(UserInfo::getAge, 20)//
                .orderBy("age")//
                .queryForMapList();

        assertNotNull(result);
        assertEquals("Should have 2 distinct ages > 20", 2, result.size());
        assertEquals(25, result.get(0).get("age"));
        assertEquals(30, result.get(1).get("age"));
    }

    /**
     * 测试 DISTINCT 与 NULL 值
     * SQL: SELECT DISTINCT age FROM user_info (including NULLs)
     */
    @Test
    public void testDistinctWithNull() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入包含 NULL 的数据
        lambda.insert(UserInfo.class).applyEntity(createUser(9401, "Null1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9402, "Null2", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9403, "Null3", null)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(9404, "Null4", null)).executeSumResult();

        // DISTINCT age (NULL 也算一个不同的值)
        List<Map<String, Object>> result = lambda.query(UserInfo.class)//
                .applySelect("DISTINCT age")//
                .like(UserInfo::getName, "Null%")//
                .queryForMapList();

        assertNotNull(result);
        assertEquals("Should have 2 distinct values (20 and NULL)", 2, result.size());
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        return createUser(id, name, age, name.toLowerCase() + "@distinct.com");
    }

    private UserInfo createUser(int id, String name, Integer age, String email) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(email);
        u.setCreateTime(new Date());
        return u;
    }
}
