package net.hasor.dbvisitor.test.oneapi.suite.fluent.query;

import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * IN / NOT IN Query Enhanced Test
 * 验证 IN 和 NOT IN 查询的各种边界情况
 */
public class InNotInTest extends AbstractOneApiTest {

    /**
     * 测试基础 IN 查询
     * SQL: SELECT * FROM user_info WHERE age IN (20, 25, 30)
     */
    @Test
    public void testBasicInQuery() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入数据
        lambda.insert(UserInfo.class).applyEntity(createUser(10001, "In1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(10002, "In2", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(10003, "In3", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(10004, "In4", 35)).executeSumResult();

        // IN 查询
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "In%")//
                .in(UserInfo::getAge, Arrays.asList(20, 25, 30))//
                .orderBy("age")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 3 users with age IN (20,25,30)", 3, result.size());
        assertEquals(Integer.valueOf(20), result.get(0).getAge());
        assertEquals(Integer.valueOf(25), result.get(1).getAge());
        assertEquals(Integer.valueOf(30), result.get(2).getAge());
    }

    /**
     * 测试单值 IN 查询（等同于 = 条件）
     * SQL: SELECT * FROM user_info WHERE age IN (25)
     */
    @Test
    public void testSingleValueInQuery() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(10101, "Single1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(10102, "Single2", 30)).executeSumResult();

        // IN 单值
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Single%")//
                .in(UserInfo::getAge, Collections.singletonList(25))//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 1 user with age IN (25)", 1, result.size());
        assertEquals(Integer.valueOf(25), result.get(0).getAge());
    }

    /**
     * 测试空列表 IN 查询
     * SQL: SELECT * FROM user_info WHERE age IN ()
     * 预期行为: 返回空结果或抛出异常（取决于实现）
     */
    @Test
    public void testEmptyListInQuery() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(10201, "Empty1", 25))//
                .executeSumResult();

        // IN 空列表 - 框架应拒绝空集合
        try {
            List<UserInfo> result = lambda.query(UserInfo.class)//
                    .like(UserInfo::getName, "Empty%")//
                    .in(UserInfo::getAge, Collections.emptyList())//
                    .queryForList();
            fail("Should throw exception for empty IN list");
        } catch (Exception e) {
            // 框架拒绝空集合，验证异常消息包含相关信息
            assertNotNull("Exception should have a message", e.getMessage());
            assertTrue("Exception message should indicate empty values, got: " + e.getMessage(), //
                    e.getMessage().toLowerCase().contains("empty"));
        }
    }

    /**
     * 测试大列表 IN 查询 (1000+ 元素)
     * 验证: 某些数据库对 IN 子句有大小限制（如 Oracle 1000），框架是否自动分组处理
     */
    @Test
    public void testLargeListInQuery() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入 50 条数据（ID: 10301-10350）
        for (int i = 1; i <= 50; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(10300 + i, "Large" + i, 20 + i))//
                    .executeSumResult();
        }

        // 构建一个包含 1500 个元素的列表（远超大部分匹配）
        List<Integer> largeList = new ArrayList<>();
        for (int i = 1; i <= 1500; i++) {
            largeList.add(20 + i); // 包含 21-1520
        }

        // IN 大列表（应匹配前 50 条）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Large%")//
                .in(UserInfo::getAge, largeList)//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 50 users (all inserted match)", 50, result.size());
    }

    /**
     * 测试 NOT IN 查询
     * SQL: SELECT * FROM user_info WHERE age NOT IN (20, 30)
     */
    @Test
    public void testBasicNotInQuery() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(10401, "NotIn1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(10402, "NotIn2", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(10403, "NotIn3", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(10404, "NotIn4", 35)).executeSumResult();

        // NOT IN 查询
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NotIn%")//
                .notIn(UserInfo::getAge, Arrays.asList(20, 30))//
                .orderBy("age")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users with age NOT IN (20,30)", 2, result.size());
        assertEquals(Integer.valueOf(25), result.get(0).getAge());
        assertEquals(Integer.valueOf(35), result.get(1).getAge());
    }

    /**
     * 测试 NOT IN 与 NULL 值
     * SQL: SELECT * FROM user_info WHERE age NOT IN (20, NULL)
     * 注意: SQL 语义下，NOT IN 包含 NULL 时会导致整个结果为空
     */
    @Test
    public void testNotInWithNull() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(10501, "NotInNull1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(10502, "NotInNull2", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(10503, "NotInNull3", null)).executeSumResult();

        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NotInNull%")//
                .notIn(UserInfo::getAge, Arrays.asList(20, null))//
                .queryForList();

        assertNotNull(result);
        //SQL 标准中 NOT IN (20, NULL) 的语义是：对每一行检验 age <> 20 AND age <> NULL，
        // 而 age <> NULL 永远返回 UNKNOWN，所以整个 NOT IN 表达式对所有行都返回 UNKNOWN，最终结果为空（0 行）
        assertTrue("Result behavior depends on NULL handling", result.size() == 0);
    }

    /**
     * 测试 IN + 其他条件组合
     * SQL: SELECT * FROM user_info WHERE age IN (20,25,30) AND name LIKE 'Combo%'
     */
    @Test
    public void testInWithMultipleConditions() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(10601, "Combo1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(10602, "Combo2", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(10603, "Other1", 30)).executeSumResult();

        // IN + LIKE
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Combo%")//
                .in(UserInfo::getAge, Arrays.asList(20, 25, 30))//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users (Combo1, Combo2)", 2, result.size());
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@in.com");
        u.setCreateTime(new Date());
        return u;
    }
}
