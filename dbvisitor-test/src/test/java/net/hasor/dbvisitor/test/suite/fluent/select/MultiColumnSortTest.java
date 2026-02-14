package net.hasor.dbvisitor.test.suite.fluent.select;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Multi-Column Sort Priority Test
 * 验证多字段排序的优先级及组合逻辑
 */
public class MultiColumnSortTest extends AbstractOneApiTest {

    /**
     * 测试双字段排序 (ORDER BY age DESC, name ASC)
     */
    @Test
    public void testTwoColumnSort() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入测试数据（相同年龄，不同姓名）
        lambda.insert(UserInfo.class).applyEntity(createUser(12001, "Charlie", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(12002, "Alice", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(12003, "Bob", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(12004, "David", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(12005, "Eve", 20)).executeSumResult();

        // ORDER BY age DESC, name ASC
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(12001, 12002, 12003, 12004, 12005))//
                .desc("age")       // 第一优先级: age 降序
                .asc("name")       // 第二优先级: name 升序
                .queryForList();

        assertNotNull(result);
        assertEquals(5, result.size());

        // 验证排序结果
        // 1. age=30: David
        assertEquals("David", result.get(0).getName());
        assertEquals(Integer.valueOf(30), result.get(0).getAge());

        // 2-4. age=25 (按 name 升序): Alice, Bob, Charlie
        assertEquals("Alice", result.get(1).getName());
        assertEquals(Integer.valueOf(25), result.get(1).getAge());

        assertEquals("Bob", result.get(2).getName());
        assertEquals(Integer.valueOf(25), result.get(2).getAge());

        assertEquals("Charlie", result.get(3).getName());
        assertEquals(Integer.valueOf(25), result.get(3).getAge());

        // 5. age=20: Eve
        assertEquals("Eve", result.get(4).getName());
        assertEquals(Integer.valueOf(20), result.get(4).getAge());
    }

    /**
     * 测试三字段排序 (ORDER BY age DESC, email ASC, name ASC)
     */
    @Test
    public void testThreeColumnSort() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入复杂数据（年龄相同，email 相同，name 不同）
        lambda.insert(UserInfo.class).applyEntity(createUser(12101, "User1", 25, "a@test.com")).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(12102, "User2", 25, "a@test.com")).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(12103, "User3", 25, "b@test.com")).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(12104, "User4", 30, "c@test.com")).executeSumResult();

        // ORDER BY age DESC, email ASC, name ASC
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(12101, 12102, 12103, 12104))//
                .desc("age")//
                .asc("email")//
                .asc("name")//
                .queryForList();

        assertNotNull(result);
        assertEquals(4, result.size());

        // 验证: age=30 先出现
        assertEquals("User4", result.get(0).getName());

        // age=25 内部: email=a@test.com (User1, User2 按 name 排序), 然后 email=b@test.com (User3)
        assertEquals("User1", result.get(1).getName());
        assertEquals("a@test.com", result.get(1).getEmail());

        assertEquals("User2", result.get(2).getName());
        assertEquals("a@test.com", result.get(2).getEmail());

        assertEquals("User3", result.get(3).getName());
        assertEquals("b@test.com", result.get(3).getEmail());
    }

    /**
     * 测试混合升降序 (ORDER BY age ASC, name DESC)
     */
    @Test
    public void testMixedAscDesc() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(12201, "Zara", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(12202, "Anna", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(12203, "Mike", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(12204, "John", 25)).executeSumResult();

        // ORDER BY age ASC, name DESC
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(12201, 12202, 12203, 12204))//
                .asc("age")//
                .desc("name")//
                .queryForList();

        assertNotNull(result);
        assertEquals(4, result.size());

        // age=20 按 name 降序: Zara, Anna
        assertEquals("Zara", result.get(0).getName());
        assertEquals("Anna", result.get(1).getName());

        // age=25 按 name 降序: Mike, John
        assertEquals("Mike", result.get(2).getName());
        assertEquals("John", result.get(3).getName());
    }

    /**
     * 测试 NULL 值在多字段排序中的行为
     * ORDER BY age DESC, name ASC
     * 注意: 数据库默认 DESC 排序时 NULL 排在最前（PG、H2 均如此）
     */
    @Test
    public void testMultiColumnSortWithNull() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(12301, "Alpha", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(12302, "Beta", null)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(12303, "Gamma", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(12304, "Delta", null)).executeSumResult();

        // ORDER BY age DESC, name ASC
        // 数据库默认行为：DESC 时 NULL 排在最前
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(12301, 12302, 12303, 12304))//
                .desc("age")//
                .asc("name")//
                .queryForList();

        assertNotNull(result);
        assertEquals(4, result.size());

        // 默认 DESC 时 NULL 排在最前，NULL 组内按 name ASC: Beta, Delta
        assertNull(result.get(0).getAge());
        assertNull(result.get(1).getAge());
        assertEquals("Beta", result.get(0).getName());
        assertEquals("Delta", result.get(1).getName());

        // 非 NULL 值按 age DESC: 30(Gamma), 25(Alpha)
        assertEquals("Gamma", result.get(2).getName());
        assertEquals(Integer.valueOf(30), result.get(2).getAge());

        assertEquals("Alpha", result.get(3).getName());
        assertEquals(Integer.valueOf(25), result.get(3).getAge());
    }

    /**
     * 测试同一字段多次排序（框架追加而非覆盖，第一个 ASC 决定主排序）
     */
    @Test
    public void testRepeatedSortOnSameColumn() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(12401, "User1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(12402, "User2", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(12403, "User3", 30)).executeSumResult();

        // 先 ASC 后 DESC: 框架为追加模式，生成 ORDER BY age ASC, age DESC
        // 第一个 ASC 已决定排序，第二个 DESC 仅作为 tiebreaker（无效果）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(12401, 12402, 12403))//
                .asc("age")    // 第一次排序（升序）- 主排序
                .desc("age")   // 第二次排序（降序）- 追加，作为 tiebreaker 无实际效果
                .queryForList();

        assertNotNull(result);
        assertEquals(3, result.size());

        // 框架追加模式：ASC 为主排序，结果为升序
        assertTrue("Age should be in ascending order (ASC dominates)", //
                result.get(0).getAge() <= result.get(1).getAge());
        assertTrue("Age should be in ascending order (ASC dominates)", //
                result.get(1).getAge() <= result.get(2).getAge());
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        return createUser(id, name, age, name.toLowerCase() + "@sort.com");
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
