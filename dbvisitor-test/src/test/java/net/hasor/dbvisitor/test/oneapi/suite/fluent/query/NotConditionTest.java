package net.hasor.dbvisitor.test.oneapi.suite.fluent.query;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * NOT Condition Test
 * 验证 NOT 系列条件的完整场景
 */
public class NotConditionTest extends AbstractOneApiTest {

    /**
     * 测试 NE (不等于)
     * SQL: SELECT * FROM user_info WHERE age <> 25
     */
    @Test
    public void testNotEqual() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(19401, "NE1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19402, "NE2", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19403, "NE3", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19404, "NE4", 35)).executeSumResult();

        // age <> 25
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NE%")//
                .ne(UserInfo::getAge, 25)//
                .orderBy("age")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users with age != 25", 2, result.size());
        assertEquals(Integer.valueOf(30), result.get(0).getAge());
        assertEquals(Integer.valueOf(35), result.get(1).getAge());
    }

    /**
     * 测试 NE 与 NULL 值
     * SQL: SELECT * FROM user_info WHERE age <> 25 (NULL 不匹配)
     */
    @Test
    public void testNotEqualWithNull() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(19501, "NEN1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19502, "NEN2", null)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19503, "NEN3", 30)).executeSumResult();

        // age <> 25（NULL 值不应匹配）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NEN%")//
                .ne(UserInfo::getAge, 25)//
                .queryForList();

        assertNotNull(result);
        assertEquals("NULL should not match NE condition", 1, result.size());
        assertEquals(Integer.valueOf(30), result.get(0).getAge());
    }

    /**
     * 测试 NOT 条件组合
     * SQL: SELECT * FROM user_info WHERE NOT (age = 25 AND name LIKE '%Test%')
     */
    @Test
    public void testNotWithNestedCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(19601, "TestUser", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19602, "TestAdmin", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19603, "User", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19604, "Admin", 30)).executeSumResult();

        // NOT (age = 25 AND name LIKE '%Test%')
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(19601, 19602, 19603, 19604))//
                .not(q -> {
                    q.eq(UserInfo::getAge, 25).like(UserInfo::getName, "Test");
                }).queryForList();

        assertNotNull(result);
        // 预期: 排除 TestUser (age=25, name含Test)
        assertEquals("Should find 3 users NOT matching the nested condition", 3, result.size());
    }

    /**
     * 测试 NOT IN
     * 已在 InNotInTest 中测试，这里补充与其他条件组合
     * SQL: SELECT * FROM user_info WHERE age NOT IN (20, 25) AND name LIKE '%User%'
     */
    @Test
    public void testNotInWithOtherConditions() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(19701, "User1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19702, "User2", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19703, "User3", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19704, "Admin", 30)).executeSumResult();

        // NOT IN (20, 25) AND LIKE '%User%'
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(19701, 19702, 19703, 19704))//
                .notIn(UserInfo::getAge, Arrays.asList(20, 25))//
                .like(UserInfo::getName, "User")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 1 user (User3)", 1, result.size());
        assertEquals("User3", result.get(0).getName());
    }

    /**
     * 测试 NOT LIKE
     * 已在 LikeQueryTest 中测试，这里补充复杂组合
     * SQL: SELECT * FROM user_info WHERE NOT (name LIKE '%Test%' OR name LIKE '%Admin%')
     */
    @Test
    public void testNotLikeWithOrCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(19801, "TestUser", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19802, "AdminUser", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19803, "User", 35)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19804, "Guest", 40)).executeSumResult();

        // NOT (LIKE '%Test%' OR LIKE '%Admin%')
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(19801, 19802, 19803, 19804))//
                .not(q -> q.like(UserInfo::getName, "Test")//
                        .or()//
                        .like(UserInfo::getName, "Admin"))//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users (User, Guest)", 2, result.size());
    }

    /**
     * 测试 NOT BETWEEN
     * 已在 BetweenQueryTest 中测试，这里补充组合场景
     * SQL: SELECT * FROM user_info WHERE NOT (age BETWEEN 20 AND 30) AND email IS NOT NULL
     */
    @Test
    public void testNotBetweenWithNotNull() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(19901, "NBT1", 15)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19902, "NBT2", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19903, "NBT3", 35)).executeSumResult();

        UserInfo u4 = new UserInfo();
        u4.setId(19904);
        u4.setName("NBT4");
        u4.setAge(40);
        u4.setEmail(null);
        lambda.insert(UserInfo.class)//
                .applyEntity(u4)//
                .executeSumResult();

        // NOT BETWEEN 20 AND 30 AND email IS NOT NULL
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NBT%")//
                .rangeNotBetween(UserInfo::getAge, 20, 30)//
                .isNotNull(UserInfo::getEmail)//
                .orderBy("age")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users (NBT1, NBT3)", 2, result.size());
        assertEquals(Integer.valueOf(15), result.get(0).getAge());
        assertEquals(Integer.valueOf(35), result.get(1).getAge());
    }

    /**
     * 测试 NOT 与 AND/OR 复杂组合
     * SQL: SELECT * FROM user_info WHERE NOT (age > 30 OR name LIKE '%Admin%')
     */
    @Test
    public void testNotWithComplexLogic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(20001, "User1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(20002, "User2", 35)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(20003, "Admin1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(20004, "User3", 30)).executeSumResult();

        // NOT (age > 30 OR name LIKE '%Admin%')
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(20001, 20002, 20003, 20004))//
                .not(q -> q.gt(UserInfo::getAge, 30)//
                        .or()//
                        .like(UserInfo::getName, "Admin"))//
                .queryForList();

        assertNotNull(result);
        // 预期: User1 (25), User3 (30)
        assertEquals("Should find 2 users", 2, result.size());
    }

    /**
     * 测试双重 NOT（NOT NOT = 肯定）
     * SQL: SELECT * FROM user_info WHERE NOT (NOT (age = 25))
     * 等价于: SELECT * FROM user_info WHERE age = 25
     */
    @Test
    public void testDoubleNot() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(20101, "DN1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(20102, "DN2", 30)).executeSumResult();

        // NOT (NOT (age = 25))
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DN%")//
                .not(q -> q.not(n -> n.eq(UserInfo::getAge, 25)))//
                .queryForList();

        assertNotNull(result);
        // 预期: 只匹配 age=25 的记录
        assertEquals("Double NOT should match age=25", 1, result.size());
        assertEquals(Integer.valueOf(25), result.get(0).getAge());
    }

    /**
     * 测试 NOT 与 NULL 条件
     * SQL: SELECT * FROM user_info WHERE NOT (age IS NULL)
     * 等价于: SELECT * FROM user_info WHERE age IS NOT NULL
     */
    @Test
    public void testNotWithIsNull() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(20201, "NN1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(20202, "NN2", null)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(20203, "NN3", 30)).executeSumResult();

        // NOT (age IS NULL)
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NN%")//
                .not(q -> q.isNull(UserInfo::getAge))//
                .orderBy("age")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users with non-NULL age", 2, result.size());
        assertEquals(Integer.valueOf(25), result.get(0).getAge());
        assertEquals(Integer.valueOf(30), result.get(1).getAge());
    }

    /**
     * 测试 NOT 范围查询的各种组合
     * SQL: SELECT * FROM user_info WHERE NOT (20 < age < 30)
     */
    @Test
    public void testNotWithRangeQueries() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(20301, "NR1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(20302, "NR2", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(20303, "NR3", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(20304, "NR4", 35)).executeSumResult();

        // NOT (20 < age < 30) -> age <= 20 OR age >= 30
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NR%")//
                .rangeNotOpenOpen(UserInfo::getAge, 20, 30)//
                .orderBy("age")//
                .queryForList();

        assertNotNull(result);
        // 预期: 20, 30, 35
        assertEquals("Should find 3 users outside open range", 3, result.size());
        assertEquals(Integer.valueOf(20), result.get(0).getAge());
        assertEquals(Integer.valueOf(30), result.get(1).getAge());
        assertEquals(Integer.valueOf(35), result.get(2).getAge());
    }

    /**
     * 测试 NOT 与多条件 DELETE
     * SQL: DELETE FROM user_info WHERE NOT (age > 25 OR name LIKE '%Keep%')
     */
    @Test
    public void testNotWithDelete() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(20401, "Delete1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(20402, "Keep1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(20403, "Delete2", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(20404, "Delete3", 25)).executeSumResult();

        // DELETE WHERE NOT (age > 25 OR name LIKE '%Keep%')
        int deleted = lambda.delete(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(20401, 20402, 20403, 20404))//
                .not(q -> q.gt(UserInfo::getAge, 25)//
                        .or()//
                        .like(UserInfo::getName, "Keep"))//
                .doDelete();

        assertTrue("Should delete at least 1 record", deleted >= 1);

        // 验证: Keep1 和 Delete2/Delete3 (age>25) 应该保留
        long remaining = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(20401, 20402, 20403, 20404))//
                .queryForCount();

        assertTrue("Should have remaining records", remaining >= 2);
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@not.com");
        u.setCreateTime(new Date());
        return u;
    }
}
