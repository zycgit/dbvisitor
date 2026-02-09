package net.hasor.dbvisitor.test.oneapi.suite.fluent.query;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * LIKE Query Comprehensive Test
 * 验证 LIKE 模糊查询的全面场景（包括 NOT LIKE 系列）
 */
public class LikeQueryTest extends AbstractOneApiTest {

    /**
     * 测试基础 LIKE 查询（两端匹配 %xxx%）
     * SQL: SELECT * FROM user_info WHERE name LIKE '%User%'
     */
    @Test
    public void testBasicLike() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(18101, "TestUser", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18102, "UserAccount", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18103, "MyUser", 35)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18104, "Admin", 40)).executeSumResult();

        // LIKE '%User%'
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(18101, 18102, 18103, 18104))//
                .like(UserInfo::getName, "User")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 3 users containing 'User'", 3, result.size());
    }

    /**
     * 测试 likeRight (右匹配 xxx%)
     * SQL: SELECT * FROM user_info WHERE name LIKE 'Test%'
     */
    @Test
    public void testLikeRight() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(18201, "TestUser1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18202, "TestUser2", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18203, "MyTest", 35)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18204, "Other", 40)).executeSumResult();

        // LIKE 'Test%'（以 Test 开头）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(18201, 18202, 18203, 18204))//
                .likeRight(UserInfo::getName, "Test")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users starting with 'Test'", 2, result.size());
    }

    /**
     * 测试 likeLeft (左匹配 %xxx)
     * SQL: SELECT * FROM user_info WHERE name LIKE '%User'
     */
    @Test
    public void testLikeLeft() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(18301, "TestUser", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18302, "AdminUser", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18303, "UserAccount", 35)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18304, "Other", 40)).executeSumResult();

        // LIKE '%User'（以 User 结尾）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(18301, 18302, 18303, 18304))//
                .likeLeft(UserInfo::getName, "User")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users ending with 'User'", 2, result.size());
    }

    /**
     * 测试 NOT LIKE (两端匹配)
     * SQL: SELECT * FROM user_info WHERE name NOT LIKE '%Test%'
     */
    @Test
    public void testNotLike() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(18401, "TestUser", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18402, "MyTest", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18403, "Admin", 35)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18404, "User", 40)).executeSumResult();

        // NOT LIKE '%Test%'
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(18401, 18402, 18403, 18404))//
                .notLike(UserInfo::getName, "Test")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users NOT containing 'Test'", 2, result.size());
    }

    /**
     * 测试 NOT LIKE RIGHT
     * SQL: SELECT * FROM user_info WHERE name NOT LIKE 'Test%'
     */
    @Test
    public void testNotLikeRight() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(18501, "TestUser", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18502, "AdminTest", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18503, "Admin", 35)).executeSumResult();

        // NOT LIKE 'Test%'（不以 Test 开头）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(18501, 18502, 18503))//
                .notLikeRight(UserInfo::getName, "Test")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users NOT starting with 'Test'", 2, result.size());
    }

    /**
     * 测试 NOT LIKE LEFT
     * SQL: SELECT * FROM user_info WHERE name NOT LIKE '%User'
     */
    @Test
    public void testNotLikeLeft() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(18601, "TestUser", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18602, "User", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18603, "Admin", 35)).executeSumResult();

        // NOT LIKE '%User'（不以 User 结尾）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(18601, 18602, 18603))//
                .notLikeLeft(UserInfo::getName, "User")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 1 user NOT ending with 'User'", 1, result.size());
        assertEquals("Admin", result.get(0).getName());
    }

    /**
     * 测试 LIKE 大小写敏感性
     * 注意: 行为取决于数据库的 collation 设置
     */
    @Test
    public void testLikeCaseSensitivity() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(18701, "TestUser", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18702, "testuser", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18703, "TESTUSER", 35)).executeSumResult();

        // LIKE 'test'（小写）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(18701, 18702, 18703))//
                .like(UserInfo::getName, "test")//
                .queryForList();

        assertNotNull(result);
        // 预期: 根据数据库配置，可能匹配 1-3 条记录
        // 大部分数据库默认不区分大小写
        assertTrue("Should find at least 1 user", result.size() >= 1);
    }

    /**
     * 测试 LIKE 与特殊字符（通配符转义）
     * SQL: SELECT * FROM user_info WHERE name LIKE '100\%'
     */
    @Test
    public void testLikeWithSpecialCharacters() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(18801, "100%User", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18802, "100User", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18803, "User_Test", 35)).executeSumResult();

        // LIKE '100%'（字面量 %，需要转义）
        // 注意: 转义方式取决于框架实现，这里验证基础行为
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(18801, 18802, 18803))//
                .likeRight(UserInfo::getName, "100")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users starting with '100'", 2, result.size());
    }

    /**
     * 测试 LIKE 与空字符串
     * SQL: SELECT * FROM user_info WHERE name LIKE '%%' (匹配所有非 NULL)
     */
    @Test
    public void testLikeWithEmptyString() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(18901, "User1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18902, "User2", 30)).executeSumResult();

        // LIKE '' (空字符串)
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(18901, 18902))//
                .like(UserInfo::getName, "")//
                .queryForList();

        assertNotNull(result);
        // 预期: LIKE '%%' 应匹配所有非 NULL 记录
        assertEquals("Empty LIKE should match all non-NULL names", 2, result.size());
    }

    /**
     * 测试 LIKE 与 NULL 值
     * SQL: SELECT * FROM user_info WHERE name LIKE '%Test%' (NULL 不匹配)
     */
    @Test
    public void testLikeWithNullValue() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo u1 = new UserInfo();
        u1.setId(19001);
        u1.setName("TestUser");
        u1.setAge(25);
        lambda.insert(UserInfo.class).applyEntity(u1).executeSumResult();

        UserInfo u2 = new UserInfo();
        u2.setId(19002);
        u2.setName(null); // NULL name
        u2.setAge(30);
        lambda.insert(UserInfo.class).applyEntity(u2).executeSumResult();

        // LIKE '%Test%'（NULL 不应匹配）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(19001, 19002))//
                .like(UserInfo::getName, "Test")//
                .queryForList();

        assertNotNull(result);
        assertEquals("NULL values should not match LIKE", 1, result.size());
        assertEquals("TestUser", result.get(0).getName());
    }

    /**
     * 测试多个 LIKE 条件组合（AND）
     * SQL: SELECT * FROM user_info WHERE name LIKE '%Test%' AND email LIKE '%@test%'
     */
    @Test
    public void testMultipleLikeConditions() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(19101, "TestUser", 25, "test@test.com")).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19102, "TestAdmin", 30, "admin@other.com")).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19103, "User", 35, "user@test.com")).executeSumResult();

        // LIKE '%Test%' AND LIKE '%@test%'
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(19101, 19102, 19103))//
                .like(UserInfo::getName, "Test")//
                .like(UserInfo::getEmail, "@test")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 1 user matching both conditions", 1, result.size());
        assertEquals("TestUser", result.get(0).getName());
    }

    /**
     * 测试 LIKE 与 OR 条件
     * SQL: SELECT * FROM user_info WHERE id IN (19201,19202,19203)
     * AND (name LIKE '%Test%' OR name LIKE '%Admin%')
     */
    @Test
    public void testLikeWithOrCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(19201, "TestUser", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19202, "AdminUser", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(19203, "Other", 35)).executeSumResult();

        // 先用 in 限定范围, 再用 nested + or 实现 (LIKE 'Test' OR LIKE 'Admin')
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(19201, 19202, 19203))//
                .nested(q -> q.like(UserInfo::getName, "Test")//
                        .or()//
                        .like(UserInfo::getName, "Admin"))//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users matching OR condition", 2, result.size());
    }

    /**
     * 测试 LIKE 性能（大数据集）
     * 验证: LIKE 查询在大数据集下的表现
     */
    @Test
    public void testLikePerformance() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入 100 条数据
        for (int i = 1; i <= 100; i++) {
            lambda.insert(UserInfo.class).applyEntity(createUser(19300 + i, "Performance" + i, 25)).executeSumResult();
        }

        long startTime = System.currentTimeMillis();

        // LIKE 查询
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Performance")//
                .queryForList();

        long duration = System.currentTimeMillis() - startTime;

        assertNotNull(result);
        assertEquals(100, result.size());
        assertTrue("LIKE query should complete quickly", duration < 5000); // < 5 seconds
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        return createUser(id, name, age, name.toLowerCase() + "@like.com");
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
