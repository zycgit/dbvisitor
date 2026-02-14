package net.hasor.dbvisitor.test.suite.fluent.query;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * String Property Condition Test
 * 验证使用字符串属性名（而非 Lambda Property 引用）的条件方法
 * 覆盖 EntityQueryCompare 接口中所有 String 参数版本的条件方法
 */
public class StringPropertyConditionTest extends AbstractOneApiTest {

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

    // ==================== 比较条件 String 版本 ====================

    /** eq(String, Object) */
    @Test
    public void testEq_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        UserInfo user = lambda.query(UserInfo.class)//
                .eq("name", "Alice")//
                .queryForObject();
        assertNotNull(user);
        assertEquals("Alice", user.getName());
        assertEquals(Integer.valueOf(18), user.getAge());
    }

    /** ne(String, Object) */
    @Test
    public void testNe_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // name <> 'Alice' => 4 条
        long count = lambda.query(UserInfo.class)//
                .ne("name", "Alice")//
                .queryForCount();
        assertEquals(4, count);
    }

    /** gt(String, Object) */
    @Test
    public void testGt_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // age > 25 => Diana(30), Eve(35)
        long count = lambda.query(UserInfo.class)//
                .gt("age", 25)//
                .queryForCount();
        assertEquals(2, count);
    }

    /** ge(String, Object) */
    @Test
    public void testGe_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // age >= 25 => Charlie(25), Diana(30), Eve(35)
        long count = lambda.query(UserInfo.class)//
                .ge("age", 25)//
                .queryForCount();
        assertEquals(3, count);
    }

    /** lt(String, Object) */
    @Test
    public void testLt_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // age < 25 => Alice(18), Bob(22)
        long count = lambda.query(UserInfo.class)//
                .lt("age", 25)//
                .queryForCount();
        assertEquals(2, count);
    }

    /** le(String, Object) */
    @Test
    public void testLe_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // age <= 25 => Alice(18), Bob(22), Charlie(25)
        long count = lambda.query(UserInfo.class)//
                .le("age", 25)//
                .queryForCount();
        assertEquals(3, count);
    }

    // ==================== 比较条件 String 动态版本 ====================

    /** eq(boolean, String, Object) */
    @Test
    public void testEq_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .eq(true, "name", "Alice")//
                .queryForCount();
        assertEquals(1, count);

        count = lambda.query(UserInfo.class)//
                .eq(false, "name", "Alice")//
                .queryForCount();
        assertEquals(5, count);
    }

    /** ne(boolean, String, Object) */
    @Test
    public void testNe_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .ne(true, "name", "Alice")//
                .queryForCount();
        assertEquals(4, count);

        count = lambda.query(UserInfo.class)//
                .ne(false, "name", "Alice")//
                .queryForCount();
        assertEquals(5, count);
    }

    /** gt(boolean, String, Object) */
    @Test
    public void testGt_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .gt(true, "age", 30)//
                .queryForCount();
        assertEquals(1, count); // Eve(35)

        count = lambda.query(UserInfo.class)//
                .gt(false, "age", 30)//
                .queryForCount();
        assertEquals(5, count);
    }

    /** ge(boolean, String, Object) */
    @Test
    public void testGe_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .ge(true, "age", 30)//
                .queryForCount();
        assertEquals(2, count); // Diana(30), Eve(35)

        count = lambda.query(UserInfo.class)//
                .ge(false, "age", 30)//
                .queryForCount();
        assertEquals(5, count);
    }

    /** lt(boolean, String, Object) */
    @Test
    public void testLt_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .lt(true, "age", 22)//
                .queryForCount();
        assertEquals(1, count); // Alice(18)

        count = lambda.query(UserInfo.class)//
                .lt(false, "age", 22)//
                .queryForCount();
        assertEquals(5, count);
    }

    /** le(boolean, String, Object) */
    @Test
    public void testLe_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .le(true, "age", 22)//
                .queryForCount();
        assertEquals(2, count); // Alice(18), Bob(22)

        count = lambda.query(UserInfo.class)//
                .le(false, "age", 22)//
                .queryForCount();
        assertEquals(5, count);
    }

    // ==================== LIKE String 版本 ====================

    /** like(String, Object) */
    @Test
    public void testLike_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .like("name", "li")// %li%  => Alice, Charlie
                .queryForCount();
        assertEquals(2, count);
    }

    /** notLike(String, Object) */
    @Test
    public void testNotLike_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // name NOT LIKE '%li%' => Bob, Diana, Eve
        long count = lambda.query(UserInfo.class)//
                .notLike("name", "li")//
                .queryForCount();
        assertEquals(3, count);
    }

    /** likeRight(String, Object) */
    @Test
    public void testLikeRight_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // name LIKE 'A%' => Alice
        long count = lambda.query(UserInfo.class)//
                .likeRight("name", "A")//
                .queryForCount();
        assertEquals(1, count);
    }

    /** notLikeRight(String, Object) */
    @Test
    public void testNotLikeRight_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // name NOT LIKE 'A%' => Bob, Charlie, Diana, Eve
        long count = lambda.query(UserInfo.class)//
                .notLikeRight("name", "A")//
                .queryForCount();
        assertEquals(4, count);
    }

    /** likeLeft(String, Object) */
    @Test
    public void testLikeLeft_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // name LIKE '%e' => Alice, Charlie, Eve
        long count = lambda.query(UserInfo.class)//
                .likeLeft("name", "e")//
                .queryForCount();
        assertEquals(3, count);
    }

    /** notLikeLeft(String, Object) */
    @Test
    public void testNotLikeLeft_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // name NOT LIKE '%e' => Bob, Diana
        long count = lambda.query(UserInfo.class)//
                .notLikeLeft("name", "e")//
                .queryForCount();
        assertEquals(2, count);
    }

    /** like(boolean, String, Object) 动态版本 */
    @Test
    public void testLike_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .like(true, "name", "li")//
                .queryForCount();
        assertEquals(2, count);

        count = lambda.query(UserInfo.class)//
                .like(false, "name", "li")//
                .queryForCount();
        assertEquals(5, count);
    }

    /** notLike(boolean, String, Object) 动态版本 */
    @Test
    public void testNotLike_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .notLike(true, "name", "li")//
                .queryForCount();
        assertEquals(3, count);

        count = lambda.query(UserInfo.class)//
                .notLike(false, "name", "li")//
                .queryForCount();
        assertEquals(5, count);
    }

    /** likeRight(boolean, String, Object) 动态版本 */
    @Test
    public void testLikeRight_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .likeRight(true, "name", "Ch")//
                .queryForCount();
        assertEquals(1, count); // Charlie

        count = lambda.query(UserInfo.class)//
                .likeRight(false, "name", "Ch")//
                .queryForCount();
        assertEquals(5, count);
    }

    /** notLikeRight(boolean, String, Object) 动态版本 */
    @Test
    public void testNotLikeRight_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .notLikeRight(true, "name", "Ch")//
                .queryForCount();
        assertEquals(4, count);

        count = lambda.query(UserInfo.class)//
                .notLikeRight(false, "name", "Ch")//
                .queryForCount();
        assertEquals(5, count);
    }

    /** likeLeft(boolean, String, Object) 动态版本 */
    @Test
    public void testLikeLeft_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .likeLeft(true, "name", "e")//
                .queryForCount();
        assertEquals(3, count);

        count = lambda.query(UserInfo.class)//
                .likeLeft(false, "name", "e")//
                .queryForCount();
        assertEquals(5, count);
    }

    /** notLikeLeft(boolean, String, Object) 动态版本 */
    @Test
    public void testNotLikeLeft_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .notLikeLeft(true, "name", "e")//
                .queryForCount();
        assertEquals(2, count);

        count = lambda.query(UserInfo.class)//
                .notLikeLeft(false, "name", "e")//
                .queryForCount();
        assertEquals(5, count);
    }

    // ==================== IN/NOT IN String 版本 ====================

    /** in(String, Collection) */
    @Test
    public void testIn_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .in("age", Arrays.asList(18, 25, 35))//
                .queryForCount();
        assertEquals(3, count);
    }

    /** notIn(String, Collection) */
    @Test
    public void testNotIn_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .notIn("age", Arrays.asList(18, 25, 35))//
                .queryForCount();
        assertEquals(2, count); // Bob(22), Diana(30)
    }

    /** in(boolean, String, Collection) */
    @Test
    public void testIn_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .in(true, "age", Arrays.asList(18, 25))//
                .queryForCount();
        assertEquals(2, count);

        count = lambda.query(UserInfo.class)//
                .in(false, "age", Arrays.asList(18, 25))//
                .queryForCount();
        assertEquals(5, count);
    }

    /** notIn(boolean, String, Collection) */
    @Test
    public void testNotIn_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .notIn(true, "age", Arrays.asList(18, 35))//
                .queryForCount();
        assertEquals(3, count);

        count = lambda.query(UserInfo.class)//
                .notIn(false, "age", Arrays.asList(18, 35))//
                .queryForCount();
        assertEquals(5, count);
    }

    // ==================== NULL String 版本 ====================

    /** isNull(String) */
    @Test
    public void testIsNull_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        UserInfo u = new UserInfo();
        u.setName("NullEmail");
        u.setAge(20);
        // email 不赋值 -> null
        lambda.insert(UserInfo.class).applyEntity(u).executeSumResult();

        insertUsers(lambda); // 这 5 条 email 非空

        long count = lambda.query(UserInfo.class)//
                .isNull("email")//
                .queryForCount();
        assertEquals(1, count);
    }

    /** isNotNull(String) */
    @Test
    public void testIsNotNull_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        UserInfo u = new UserInfo();
        u.setName("NullEmail");
        u.setAge(20);
        lambda.insert(UserInfo.class).applyEntity(u).executeSumResult();

        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .isNotNull("email")//
                .queryForCount();
        assertEquals(5, count);
    }

    /** isNull(boolean, String) */
    @Test
    public void testIsNull_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        UserInfo u = new UserInfo();
        u.setName("NullEmail");
        u.setAge(20);
        lambda.insert(UserInfo.class).applyEntity(u).executeSumResult();
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .isNull(true, "email")//
                .queryForCount();
        assertEquals(1, count);

        count = lambda.query(UserInfo.class)//
                .isNull(false, "email")//
                .queryForCount();
        assertEquals(6, count);
    }

    /** isNotNull(boolean, String) */
    @Test
    public void testIsNotNull_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        UserInfo u = new UserInfo();
        u.setName("NullEmail");
        u.setAge(20);
        lambda.insert(UserInfo.class).applyEntity(u).executeSumResult();
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .isNotNull(true, "email")//
                .queryForCount();
        assertEquals(5, count);

        count = lambda.query(UserInfo.class)//
                .isNotNull(false, "email")//
                .queryForCount();
        assertEquals(6, count);
    }

    // ==================== BETWEEN String 版本 ====================

    /** between(String, Object, Object) */
    @Test
    public void testBetween_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // age BETWEEN 20 AND 30 => Bob(22), Charlie(25), Diana(30)
        long count = lambda.query(UserInfo.class)//
                .between("age", 20, 30)//
                .queryForCount();
        assertEquals(3, count);
    }

    /** notBetween(String, Object, Object) */
    @Test
    public void testNotBetween_StringProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // age NOT BETWEEN 20 AND 30 => Alice(18), Eve(35)
        long count = lambda.query(UserInfo.class)//
                .notBetween("age", 20, 30)//
                .queryForCount();
        assertEquals(2, count);
    }

    /** between(boolean, String, Object, Object) */
    @Test
    public void testBetween_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .between(true, "age", 20, 30)//
                .queryForCount();
        assertEquals(3, count);

        count = lambda.query(UserInfo.class)//
                .between(false, "age", 20, 30)//
                .queryForCount();
        assertEquals(5, count);
    }

    /** notBetween(boolean, String, Object, Object) */
    @Test
    public void testNotBetween_StringDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        long count = lambda.query(UserInfo.class)//
                .notBetween(true, "age", 20, 30)//
                .queryForCount();
        assertEquals(2, count);

        count = lambda.query(UserInfo.class)//
                .notBetween(false, "age", 20, 30)//
                .queryForCount();
        assertEquals(5, count);
    }

    // ==================== 组合场景：String + Property 混用 ====================

    /** 测试 String 和 Property 属性名方式混合使用 */
    @Test
    public void testMixed_StringAndProperty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        insertUsers(lambda);

        // age >= 22 (String) AND name LIKE 'C%' (Property)
        List<UserInfo> users = lambda.query(UserInfo.class)//
                .ge("age", 22)//
                .likeRight(UserInfo::getName, "C")//
                .queryForList();

        assertEquals(1, users.size()); // Charlie(25)
        assertEquals("Charlie", users.get(0).getName());
    }
}
