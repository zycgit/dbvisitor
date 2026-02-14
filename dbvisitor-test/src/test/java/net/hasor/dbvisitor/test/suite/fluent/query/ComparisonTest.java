package net.hasor.dbvisitor.test.suite.fluent.query;

import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Comparison Condition Test
 * 验证 ge, lt, le 比较条件（Property 版本和动态版本）
 * 补充 eq, ne, gt 已在其他测试中覆盖的情况下缺失的比较运算符
 */
public class ComparisonTest extends AbstractOneApiTest {

    private void insertUsers(LambdaTemplate lambda) throws SQLException {
        String[] names = { "Alice", "Bob", "Charlie", "Diana", "Eve" };
        int[] ages = { 18, 22, 25, 30, 35 };
        for (int i = 0; i < names.length; i++) {
            UserInfo u = new UserInfo();
            u.setName(names[i]);
            u.setAge(ages[i]);
            u.setEmail(names[i].toLowerCase() + "@test.com");
            lambda.insert(UserInfo.class).applyEntity(u).executeSumResult();
        }
    }

    // ==================== ge (>=) ====================

    /** 测试 ge (>=) Property 版本 */
    @Test
    public void testGe() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // age >= 25 => Charlie(25), Diana(30), Eve(35)
        long count = lambda.query(UserInfo.class)//
                .ge(UserInfo::getAge, 25)//
                .queryForCount();
        assertEquals(3, count);
    }

    /** 测试 ge 边界值 - 刚好等于 */
    @Test
    public void testGe_BoundaryExact() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // age >= 18 => 全部 5 条
        long count = lambda.query(UserInfo.class)//
                .ge(UserInfo::getAge, 18)//
                .queryForCount();
        assertEquals(5, count);
    }

    /** 测试 ge 动态版本 */
    @Test
    public void testGe_Dynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // test=true => ge 生效, age >= 30 => Diana(30), Eve(35)
        long count = lambda.query(UserInfo.class)//
                .ge(true, UserInfo::getAge, 30)//
                .queryForCount();
        assertEquals(2, count);

        // test=false => ge 跳过, 返回全部
        count = lambda.query(UserInfo.class)//
                .ge(false, UserInfo::getAge, 30)//
                .queryForCount();
        assertEquals(5, count);
    }

    // ==================== lt (<) ====================

    /** 测试 lt (<) Property 版本 */
    @Test
    public void testLt() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // age < 25 => Alice(18), Bob(22)
        long count = lambda.query(UserInfo.class)//
                .lt(UserInfo::getAge, 25)//
                .queryForCount();
        assertEquals(2, count);
    }

    /** 测试 lt 边界值 - 刚好不满足 */
    @Test
    public void testLt_BoundaryExact() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // age < 18 => 无人满足
        long count = lambda.query(UserInfo.class)//
                .lt(UserInfo::getAge, 18)//
                .queryForCount();
        assertEquals(0, count);
    }

    /** 测试 lt 动态版本 */
    @Test
    public void testLt_Dynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // test=true => lt 生效, age < 22 => Alice(18)
        long count = lambda.query(UserInfo.class)//
                .lt(true, UserInfo::getAge, 22)//
                .queryForCount();
        assertEquals(1, count);

        // test=false => lt 跳过, 返回全部
        count = lambda.query(UserInfo.class)//
                .lt(false, UserInfo::getAge, 22)//
                .queryForCount();
        assertEquals(5, count);
    }

    // ==================== le (<=) ====================

    /** 测试 le (<=) Property 版本 */
    @Test
    public void testLe() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // age <= 25 => Alice(18), Bob(22), Charlie(25)
        long count = lambda.query(UserInfo.class)//
                .le(UserInfo::getAge, 25)//
                .queryForCount();
        assertEquals(3, count);
    }

    /** 测试 le 边界值 - 包含等于 */
    @Test
    public void testLe_BoundaryExact() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // age <= 35 => 全部 5 条
        long count = lambda.query(UserInfo.class)//
                .le(UserInfo::getAge, 35)//
                .queryForCount();
        assertEquals(5, count);
    }

    /** 测试 le 动态版本 */
    @Test
    public void testLe_Dynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // test=true => le 生效, age <= 22 => Alice(18), Bob(22)
        long count = lambda.query(UserInfo.class)//
                .le(true, UserInfo::getAge, 22)//
                .queryForCount();
        assertEquals(2, count);

        // test=false => le 跳过, 返回全部
        count = lambda.query(UserInfo.class)//
                .le(false, UserInfo::getAge, 22)//
                .queryForCount();
        assertEquals(5, count);
    }

    // ==================== ne 动态版本 ====================

    /** 测试 ne 动态版本 (boolean test) */
    @Test
    public void testNe_Dynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // test=true => ne 生效, age <> 25 => 4 条
        long count = lambda.query(UserInfo.class)//
                .ne(true, UserInfo::getAge, 25)//
                .queryForCount();
        assertEquals(4, count);

        // test=false => ne 跳过, 返回全部 5 条
        count = lambda.query(UserInfo.class)//
                .ne(false, UserInfo::getAge, 25)//
                .queryForCount();
        assertEquals(5, count);
    }

    // ==================== 组合场景 ====================

    /** 测试 ge + le 组合 (等效 BETWEEN) */
    @Test
    public void testGeLe_Combination() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // age >= 20 AND age <= 30 => Bob(22), Charlie(25), Diana(30)
        long count = lambda.query(UserInfo.class)//
                .ge(UserInfo::getAge, 20)//
                .le(UserInfo::getAge, 30)//
                .queryForCount();
        assertEquals(3, count);
    }

    /** 测试 gt + lt 组合 (开区间) */
    @Test
    public void testGtLt_Combination() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // age > 18 AND age < 35 => Bob(22), Charlie(25), Diana(30)
        long count = lambda.query(UserInfo.class)//
                .gt(UserInfo::getAge, 18)//
                .lt(UserInfo::getAge, 35)//
                .queryForCount();
        assertEquals(3, count);
    }

    /** 测试 lt + le 动态组合 */
    @Test
    public void testDynamicCombination() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        Integer minAge = 20;
        Integer maxAge = null; // 不设上限

        // ge 生效, le 跳过 => age >= 20 => Bob(22), Charlie(25), Diana(30), Eve(35)
        long count = lambda.query(UserInfo.class)//
                .ge(minAge != null, UserInfo::getAge, minAge)//
                .le(maxAge != null, UserInfo::getAge, maxAge)//
                .queryForCount();
        assertEquals(4, count);
    }

    /** 测试多种比较运算符组合 */
    @Test
    public void testMixedComparison() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // age >= 22 AND age < 35 AND name != "Bob"
        List<UserInfo> users = lambda.query(UserInfo.class)//
                .ge(UserInfo::getAge, 22)//
                .lt(UserInfo::getAge, 35)//
                .ne(UserInfo::getName, "Bob")//
                .queryForList();

        assertEquals(2, users.size()); // Charlie(25), Diana(30)
    }
}
