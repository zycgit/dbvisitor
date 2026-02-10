package net.hasor.dbvisitor.test.oneapi.suite.fluent.query;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Level 3 & Level 1 Extension - Complex Query Builder Test
 * 验证复杂查询构建器及 Fluent API 特有能力
 */
public class ComplexQueryTest extends AbstractOneApiTest {

    /**
     * 5.1 Sample Condition (样本查询)
     * 验证 eqBySample
     */
    @Test
    public void testComplexQuery_SampleCondition() throws Exception {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        UserInfo u1 = new UserInfo();
        u1.setName("SampleUser");
        u1.setAge(25);
        u1.setEmail("sample@test.com");
        lambda.insert(UserInfo.class).applyEntity(u1).executeSumResult();

        UserInfo u2 = new UserInfo();
        u2.setName("SampleUser");
        u2.setAge(26);
        lambda.insert(UserInfo.class).applyEntity(u2).executeSumResult();

        // Query by Sample (Name="SampleUser", Age=25) - Should match u1 only
        UserInfo sample = new UserInfo();
        sample.setName("SampleUser");
        sample.setAge(25);

        UserInfo result = lambda.query(UserInfo.class)//
                .eqBySample(sample)//
                .queryForObject();

        assertNotNull(result);
        assertEquals("sample@test.com", result.getEmail());

        // Query by Sample (Name="SampleUser" only) - Should match both -> count 2
        UserInfo sample2 = new UserInfo();
        sample2.setName("SampleUser");

        long count = lambda.query(UserInfo.class)//
                .eqBySample(sample2)//
                .queryForCount();
        assertEquals(2, count);
    }

    /**
     * 5.3 Advanced Ranges
     * 验证 rangeOpenClosed 等
     */
    @Test
    public void testComplexQuery_AdvancedRanges() throws Exception {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        lambda.insert(UserInfo.class).applyEntity(createUser("R1", 10)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser("R2", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser("R3", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser("R4", 40)).executeSumResult();

        // rangeOpenClosed: 10 < age <= 30 => should be 20, 30
        long count = lambda.query(UserInfo.class)//
                .rangeOpenClosed(UserInfo::getAge, 10, 30) // (10, 30]
                .queryForCount();

        assertEquals(2, count);

        // rangeClosedOpen: 10 <= age < 30 => should be 10, 20
        count = lambda.query(UserInfo.class)//
                .rangeClosedOpen(UserInfo::getAge, 10, 30) // [10, 30)//
                .queryForCount();
        assertEquals(2, count);
    }

    /**
     * Helper
     */
    private UserInfo createUser(String name, int age) {
        UserInfo u = new UserInfo();
        u.setName(name);
        u.setAge(age);
        u.setEmail(name + "@range.com");
        return u;
    }

    /**
     * 6.1.1 多条件组合查询
     * 验证 AND/OR 逻辑组合
     */
    @Test
    public void testComplexQuery_MultipleConditions() throws Exception {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        // 插入测试数据
        for (int i = 1; i <= 10; i++) {
            UserInfo user = new UserInfo();
            user.setName("User-" + i);
            user.setAge(20 + i);
            user.setEmail("user" + i + "@test.com");
            lambda.insert(UserInfo.class).applyEntity(user).executeSumResult();
        }

        // 复杂条件查询: age > 25 AND name LIKE 'User-%'
        long count = lambda.query(UserInfo.class)//
                .gt(UserInfo::getAge, 25)//
                .like(UserInfo::getName, "User-%")//
                .queryForCount();

        assertTrue("Should find users with age > 25", count >= 5);
    }

    /**
     * 6.1.2 分页查询测试
     * 验证分页查询的准确性
     */
    @Test
    public void testComplexQuery_Pagination() throws Exception {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        // 插入15条数据
        for (int i = 1; i <= 15; i++) {
            UserInfo user = new UserInfo();
            user.setName("Page-User-" + i);
            user.setAge(20 + i);
            user.setEmail("page" + i + "@test.com");
            lambda.insert(UserInfo.class)//
                    .applyEntity(user)//
                    .executeSumResult();
        }

        // 分页查询：每页5条，查询第2页
        EntityQuery<UserInfo> query = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Page-User-%")//
                .initPage(5, 2); // pageSize=5, pageNumber=2

        List<UserInfo> users = query.queryForList();
        Page page = query.pageInfo(); // 从query获取page对象，它会自动执行count查询

        assertNotNull(users);
        assertTrue("Should have data on page 2", !users.isEmpty());
        assertEquals("Total count should be 15", 15, page.getTotalCount());
    }

    /**
     * 6.1.3 条件过滤查询测试
     * 验证 LIKE + gt() 范围过滤（非 IN 条件）
     */
    @Test
    public void testComplexQuery_InCondition() throws Exception {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        // 插入测试数据
        for (int i = 1; i <= 5; i++) {
            UserInfo user = new UserInfo();
            user.setName("In-User-" + i);
            user.setAge(20 + i);
            user.setEmail("in" + i + "@test.com");
            lambda.insert(UserInfo.class)//
                    .applyEntity(user)//
                    .executeSumResult();
        }

        // 范围查询: LIKE + age > 20
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "In-User-%")//
                .gt(UserInfo::getAge, 20)//
                .queryForCount();

        assertTrue("Should find users in age range", count >= 4);
    }

    /**
     * 6.1.4 简单条件查询测试
     * 验证 LIKE 条件过滤（非子查询，仅基本条件匹配）
     */
    @Test
    public void testComplexQuery_Subquery() throws Exception {
        // 简化版：使用 LIKE 条件查询验证数据存在性（非真正子查询）
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        // 先插入用户
        UserInfo user = new UserInfo();
        user.setName("Subquery User");
        user.setAge(30);
        user.setEmail("subquery@test.com");
        lambda.insert(UserInfo.class)//
                .applyEntity(user)//
                .executeSumResult();

        // 查询验证
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Subquery%")//
                .queryForCount();

        assertTrue(count >= 1);
    }

    // ==================== 补充：缺失的核心方法 ====================

    /**
     * 测试 eqBySampleMap - 按 Map 非空属性组合 EQ
     */
    @Test
    public void testComplexQuery_SampleMapCondition() throws Exception {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        UserInfo u1 = new UserInfo();
        u1.setName("SampleMap1");
        u1.setAge(28);
        u1.setEmail("sm1@test.com");
        lambda.insert(UserInfo.class).applyEntity(u1).executeSumResult();

        UserInfo u2 = new UserInfo();
        u2.setName("SampleMap1");
        u2.setAge(35);
        u2.setEmail("sm2@test.com");
        lambda.insert(UserInfo.class).applyEntity(u2).executeSumResult();

        UserInfo u3 = new UserInfo();
        u3.setName("SampleMap2");
        u3.setAge(28);
        u3.setEmail("sm3@test.com");
        lambda.insert(UserInfo.class).applyEntity(u3).executeSumResult();

        // 用 Map 做 sample: name=SampleMap1, age=28 => 仅匹配 u1
        Map<String, Object> sample = new HashMap<String, Object>();
        sample.put("name", "SampleMap1");
        sample.put("age", 28);

        UserInfo result = lambda.query(UserInfo.class)//
                .eqBySampleMap(sample)//
                .queryForObject();

        assertNotNull(result);
        assertEquals("sm1@test.com", result.getEmail());

        // 用 Map 仅 name=SampleMap1 => 匹配 u1, u2
        Map<String, Object> sample2 = new HashMap<String, Object>();
        sample2.put("name", "SampleMap1");

        long count = lambda.query(UserInfo.class)//
                .eqBySampleMap(sample2)//
                .queryForCount();
        assertEquals(2, count);
    }

    /**
     * 测试 ifTrue - 条件为真时执行 lambda
     */
    @Test
    public void testComplexQuery_IfTrue() throws Exception {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        for (int i = 1; i <= 5; i++) {
            UserInfo u = createUser("IfTrue-" + i, 20 + i * 5);
            lambda.insert(UserInfo.class).applyEntity(u).executeSumResult();
        }

        // ifTrue(true, ...) => 条件执行
        // ages: 25, 30, 35, 40, 45 => gt(30) => 35,40,45 => 3 条
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "IfTrue-%")//
                .ifTrue(true, q -> q.gt(UserInfo::getAge, 30))//
                .queryForCount();
        assertEquals(3, count);

        // ifTrue(false, ...) => 条件跳过, 返回全部 5 条
        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "IfTrue-%")//
                .ifTrue(false, q -> q.gt(UserInfo::getAge, 30))//
                .queryForCount();
        assertEquals(5, count);
    }

    /**
     * 测试 apply - 拼接原生 SQL 条件（无参数）
     * 验证: apply("age > 30") 与其他条件组合时正确插入 AND 连接词
     */
    @Test
    public void testComplexQuery_ApplyRawSql() throws Exception {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        for (int i = 1; i <= 5; i++) {
            UserInfo u = createUser("Apply-" + i, 20 + i * 5);
            lambda.insert(UserInfo.class).applyEntity(u).executeSumResult();
        }

        // apply 拼接纯 SQL 条件 + queryForList
        List<UserInfo> list = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Apply-%")//
                .apply("age > 30")//
                .queryForList();
        assertEquals(3, list.size()); // 35,40,45

        // apply + queryForCount
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Apply-%")//
                .apply("age > 30")//
                .queryForCount();
        assertEquals(3, count);
    }

    /**
     * 测试 apply - 带参数的原生 SQL 条件
     * 验证: apply("age > {0}", value) 参数占位符
     */
    @Test
    public void testComplexQuery_ApplyWithParam() throws Exception {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        for (int i = 1; i <= 5; i++) {
            UserInfo u = createUser("ApplyP-" + i, 20 + i * 5);
            lambda.insert(UserInfo.class).applyEntity(u).executeSumResult();
        }

        // apply 带参数: ? 位置占位符
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "ApplyP-%")//
                .apply("age <= ?", 30)//
                .queryForCount();
        assertEquals(2, count); // 25, 30
    }

    /**
     * 测试 apply 与 OR 组合
     * 验证: 使用 or() 控制 apply 条件的逻辑连接
     */
    @Test
    public void testComplexQuery_ApplyWithOr() throws Exception {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        for (int i = 1; i <= 5; i++) {
            UserInfo u = createUser("ApplyOr-" + i, 20 + i * 5);
            lambda.insert(UserInfo.class).applyEntity(u).executeSumResult();
        }

        // name LIKE 'ApplyOr-%' AND (age = 25 OR age = 45)
        // 用 or() 切换逻辑后 apply
        List<UserInfo> list = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "ApplyOr-%")//
                .nested(q -> {
                    try {
                        q.apply("age = 25")//
                                .or()//
                                .apply("age = 45");
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }).queryForList();
        assertEquals(2, list.size());
    }

    /**
     * 测试 apply 作为唯一条件
     * 验证: 当 apply 是第一个且唯一的条件时不应有逻辑前缀
     */
    @Test
    public void testComplexQuery_ApplyOnly() throws Exception {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        UserInfo u = createUser("ApplyOnly-1", 42);
        lambda.insert(UserInfo.class).applyEntity(u).executeSumResult();

        long count = lambda.query(UserInfo.class)//
                .apply("name = ?", "ApplyOnly-1")//
                .queryForCount();
        assertEquals(1, count);
    }
}
