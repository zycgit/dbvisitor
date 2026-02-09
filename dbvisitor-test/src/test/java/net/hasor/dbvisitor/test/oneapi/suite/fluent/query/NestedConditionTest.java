package net.hasor.dbvisitor.test.oneapi.suite.fluent.query;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class NestedConditionTest extends AbstractOneApiTest {

    @Test
    public void testNestedAndOr() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        insertUser(lambda, "A", 10, "g1");
        insertUser(lambda, "B", 10, "g2");
        insertUser(lambda, "C", 20, "g1");
        insertUser(lambda, "D", 20, "g2");

        // Target: (Age=10 AND Group=g1) OR (Age=20 AND Group=g2)
        // Should find A and D

        long count = lambda.query(UserInfo.class)//
                .nested(q -> {
                    q.eq(UserInfo::getAge, 10)//
                            .eq(UserInfo::getEmail, "g1@test.com");
                }).or(q -> {
                    q.eq(UserInfo::getAge, 20)//
                            .eq(UserInfo::getEmail, "g2@test.com");
                }).queryForCount();

        assertEquals(2, count);
    }

    @Test
    public void testDeepNested() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertUser(lambda, "X", 30, "admin");

        // Target: Name=X AND (Age=30 OR Age=40)

        UserInfo result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getName, "X")//
                .and(q -> {
                    q.eq(UserInfo::getAge, 30)//
                            .or()//
                            .eq(UserInfo::getAge, 40);
                }).queryForObject();

        assertEquals("X", result.getName());
    }

    private void insertUser(LambdaTemplate lambda, String name, int age, String group) throws SQLException {
        UserInfo u = new UserInfo();
        u.setName(name);
        u.setAge(age);
        u.setEmail(group + "@test.com"); // Using email as group marker
        u.setCreateTime(new Date());
        lambda.insert(UserInfo.class).applyEntity(u).executeSumResult();
    }

    // ==================== 补充：缺失的逻辑分组方法 ====================

    /**
     * 测试 and() 无参版本 - 显式声明下一个条件为 AND
     */
    @Test
    public void testAndNoArgs() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertUser(lambda, "AndNA1", 25, "grp1");
        insertUser(lambda, "AndNA2", 30, "grp2");
        insertUser(lambda, "AndNA3", 25, "grp2");

        // and() 标记下一个条件为 AND (默认行为，显式调用确认)
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "AndNA%")//
                .and()//
                .eq(UserInfo::getAge, 25)//
                .queryForCount();
        assertEquals(2, count); // AndNA1, AndNA3
    }

    /**
     * 测试 not() 无参版本 - 标记下一个条件 NOT
     */
    @Test
    public void testNotNoArgs() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertUser(lambda, "NotNA1", 25, "grp1");
        insertUser(lambda, "NotNA2", 30, "grp2");
        insertUser(lambda, "NotNA3", 35, "grp3");

        // not() 标记下一个条件取反: NOT (age = 25)
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NotNA%")//
                .not()//
                .eq(UserInfo::getAge, 25)//
                .queryForCount();
        assertEquals(2, count); // NotNA2, NotNA3
    }

    /**
     * 测试 or(boolean, Consumer) 动态版本
     */
    @Test
    public void testOrDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertUser(lambda, "OrD1", 20, "grp1");
        insertUser(lambda, "OrD2", 25, "grp2");
        insertUser(lambda, "OrD3", 30, "grp3");

        // test=true => or 分组生效
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "OrD%")//
                .eq(UserInfo::getAge, 20)//
                .or(true, q -> q.eq(UserInfo::getAge, 30))//
                .queryForCount();
        assertEquals(2, count); // OrD1, OrD3

        // test=false => or 分组跳过, 仅 age=20
        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "OrD%")//
                .eq(UserInfo::getAge, 20)//
                .or(false, q -> q.eq(UserInfo::getAge, 30))//
                .queryForCount();
        assertEquals(1, count); // OrD1
    }

    /**
     * 测试 and(boolean, Consumer) 动态版本
     */
    @Test
    public void testAndDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertUser(lambda, "AndD1", 20, "grp1");
        insertUser(lambda, "AndD2", 25, "grp1");
        insertUser(lambda, "AndD3", 30, "grp2");

        // test=true => and 分组生效, 必须同时满足 email 条件
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "AndD%")//
                .and(true, q -> q.eq(UserInfo::getEmail, "grp1@test.com"))//
                .queryForCount();
        assertEquals(2, count); // AndD1, AndD2

        // test=false => and 分组跳过, 返回全部
        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "AndD%")//
                .and(false, q -> q.eq(UserInfo::getEmail, "grp1@test.com"))//
                .queryForCount();
        assertEquals(3, count);
    }

    /**
     * 测试 not(boolean, Consumer) 动态版本
     */
    @Test
    public void testNotDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertUser(lambda, "NotD1", 20, "grp1");
        insertUser(lambda, "NotD2", 25, "grp2");
        insertUser(lambda, "NotD3", 30, "grp3");

        // test=true => not 分组生效, NOT (age = 25)
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NotD%")//
                .not(true, q -> q.eq(UserInfo::getAge, 25))//
                .queryForCount();
        assertEquals(2, count); // NotD1, NotD3

        // test=false => not 分组跳过, 返回全部
        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NotD%")//
                .not(false, q -> q.eq(UserInfo::getAge, 25))//
                .queryForCount();
        assertEquals(3, count);
    }

    /**
     * 测试 nested(boolean, Consumer) 动态版本
     */
    @Test
    public void testNestedDynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertUser(lambda, "NestD1", 20, "grp1");
        insertUser(lambda, "NestD2", 25, "grp1");
        insertUser(lambda, "NestD3", 30, "grp2");

        // test=true => nested 生效, (email='grp1@test.com' AND age >= 25)
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NestD%")//
                .nested(true, q -> q.eq(UserInfo::getEmail, "grp1@test.com")//
                        .ge(UserInfo::getAge, 25))//
                .queryForCount();
        assertEquals(1, count); // NestD2

        // test=false => nested 跳过, 返回全部
        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NestD%")//
                .nested(false, q -> {
                    q.eq(UserInfo::getEmail, "grp1@test.com").ge(UserInfo::getAge, 25);
                }).queryForCount();
        assertEquals(3, count);
    }

    /**
     * 测试复杂逻辑分组组合:
     * not() + or() + nested() + and()
     */
    @Test
    public void testComplexLogicalGrouping() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertUser(lambda, "CLG1", 20, "admin");
        insertUser(lambda, "CLG2", 25, "user");
        insertUser(lambda, "CLG3", 30, "admin");
        insertUser(lambda, "CLG4", 35, "user");

        // (email='admin@test.com') AND NOT (age >= 30)
        // => CLG1 (admin, age 20)
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "CLG%")//
                .eq(UserInfo::getEmail, "admin@test.com")//
                .not(q -> q.ge(UserInfo::getAge, 30))//
                .queryForCount();
        assertEquals(1, count);
    }
}