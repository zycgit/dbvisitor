package net.hasor.dbvisitor.test.oneapi.suite.fluent.query;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * BETWEEN Query Test
 * 验证 BETWEEN 和 NOT BETWEEN 查询的各种场景
 */
public class BetweenQueryTest extends AbstractOneApiTest {

    /**
     * 测试基础 BETWEEN 查询（闭区间）
     * SQL: SELECT * FROM user_info WHERE age BETWEEN 20 AND 30
     */
    @Test
    public void testBasicBetweenQuery() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入数据（age: 15, 20, 25, 30, 35）
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17001, "Between1", 15))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17002, "Between2", 20))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17003, "Between3", 25))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17004, "Between4", 30))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17005, "Between5", 35))//
                .executeSumResult();

        // BETWEEN 20 AND 30（闭区间，包含边界）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Between%")//
                .rangeBetween(UserInfo::getAge, 20, 30)//
                .orderBy("age")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 3 users (20, 25, 30)", 3, result.size());
        assertEquals(Integer.valueOf(20), result.get(0).getAge());
        assertEquals(Integer.valueOf(25), result.get(1).getAge());
        assertEquals(Integer.valueOf(30), result.get(2).getAge());
    }

    /**
     * 测试 NOT BETWEEN 查询
     * SQL: SELECT * FROM user_info WHERE age NOT BETWEEN 20 AND 30
     */
    @Test
    public void testNotBetweenQuery() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入数据
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17101, "NotBtw1", 15))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17102, "NotBtw2", 20))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17103, "NotBtw3", 25))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17104, "NotBtw4", 30))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17105, "NotBtw5", 35))//
                .executeSumResult();

        // NOT BETWEEN 20 AND 30
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NotBtw%")//
                .rangeNotBetween(UserInfo::getAge, 20, 30)//
                .orderBy("age")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users (15, 35)", 2, result.size());
        assertEquals(Integer.valueOf(15), result.get(0).getAge());
        assertEquals(Integer.valueOf(35), result.get(1).getAge());
    }

    /**
     * 测试 BETWEEN 边界值（相等的边界）
     * SQL: SELECT * FROM user_info WHERE age BETWEEN 25 AND 25
     */
    @Test
    public void testBetweenWithEqualBounds() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17201, "Equal1", 24))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17202, "Equal2", 25))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17203, "Equal3", 26))//
                .executeSumResult();

        // BETWEEN 25 AND 25（只匹配 25）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Equal%")//
                .rangeBetween(UserInfo::getAge, 25, 25)//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find exactly 1 user with age=25", 1, result.size());
        assertEquals(Integer.valueOf(25), result.get(0).getAge());
    }

    /**
     * 测试 BETWEEN 反向边界（min > max）
     * SQL: SELECT * FROM user_info WHERE age BETWEEN 30 AND 20
     * 标准 SQL 行为: 通常不返回结果
     */
    @Test
    public void testBetweenWithReversedBounds() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17301, "Rev1", 20))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17302, "Rev2", 25))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17303, "Rev3", 30))//
                .executeSumResult();

        // BETWEEN 30 AND 20（反向，标准 SQL 行为是空结果）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Rev%")//
                .rangeBetween(UserInfo::getAge, 30, 20)//
                .queryForList();

        assertNotNull(result);
        // 预期: 空结果（SQL 标准行为）
        assertEquals("Reversed BETWEEN should return no results", 0, result.size());
    }

    /**
     * 测试 BETWEEN 与 NULL 值
     * SQL: SELECT * FROM user_info WHERE age BETWEEN 20 AND 30 (age 为 NULL 不匹配)
     */
    @Test
    public void testBetweenWithNullValues() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17401, "Null1", 20))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17402, "Null2", null))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17403, "Null3", 30))//
                .executeSumResult();

        // BETWEEN 20 AND 30（NULL 值不应匹配）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Null%")//
                .rangeBetween(UserInfo::getAge, 20, 30)//
                .queryForList();

        assertNotNull(result);
        assertEquals("NULL values should not match BETWEEN", 2, result.size());
    }

    /**
     * 测试 BETWEEN 与负数
     * SQL: SELECT * FROM user_info WHERE age BETWEEN -10 AND 10
     */
    @Test
    public void testBetweenWithNegativeNumbers() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17501, "Neg1", -5))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17502, "Neg2", 0))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17503, "Neg3", 5))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17504, "Neg4", 15))//
                .executeSumResult();

        // BETWEEN -10 AND 10
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Neg%")//
                .rangeBetween(UserInfo::getAge, -10, 10)//
                .orderBy("age")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 3 users (-5, 0, 5)", 3, result.size());
        assertEquals(Integer.valueOf(-5), result.get(0).getAge());
        assertEquals(Integer.valueOf(0), result.get(1).getAge());
        assertEquals(Integer.valueOf(5), result.get(2).getAge());
    }

    /**
     * 测试 BETWEEN 与其他条件组合
     * SQL: SELECT * FROM user_info WHERE name LIKE 'Combo%' AND age BETWEEN 20 AND 30
     */
    @Test
    public void testBetweenWithOtherConditions() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17601, "Combo1", 20))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17602, "Combo2", 25))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17603, "Other1", 25))//
                .executeSumResult();

        // LIKE + BETWEEN
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Combo%")//
                .rangeBetween(UserInfo::getAge, 20, 30)//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users (Combo1, Combo2)", 2, result.size());
    }

    /**
     * 测试 rangeOpenOpen (开区间: a < x < b)
     * 注意: ComplexQueryTest 中已有测试，这里补充边界情况
     */
    @Test
    public void testRangeOpenOpenBoundary() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17701, "Open1", 20))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17702, "Open2", 21))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17703, "Open3", 29))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17704, "Open4", 30))//
                .executeSumResult();

        // 20 < age < 30（不含边界）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Open%")//
                .rangeOpenOpen(UserInfo::getAge, 20, 30)//
                .orderBy("age")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users (21, 29)", 2, result.size());
        assertEquals(Integer.valueOf(21), result.get(0).getAge());
        assertEquals(Integer.valueOf(29), result.get(1).getAge());
    }

    /**
     * 测试 rangeOpenClosed (左开右闭: a < x <= b)
     */
    @Test
    public void testRangeOpenClosedBoundary() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17801, "OC1", 20))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17802, "OC2", 21))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17803, "OC3", 30))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17804, "OC4", 31))//
                .executeSumResult();

        // 20 < age <= 30
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "OC%")//
                .rangeOpenClosed(UserInfo::getAge, 20, 30)//
                .orderBy("age")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users (21, 30)", 2, result.size());
        assertEquals(Integer.valueOf(21), result.get(0).getAge());
        assertEquals(Integer.valueOf(30), result.get(1).getAge());
    }

    /**
     * 测试 rangeClosedOpen (左闭右开: a <= x < b)
     */
    @Test
    public void testRangeClosedOpenBoundary() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17901, "CO1", 19))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17902, "CO2", 20))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17903, "CO3", 29))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(17904, "CO4", 30))//
                .executeSumResult();

        // 20 <= age < 30
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "CO%")//
                .rangeClosedOpen(UserInfo::getAge, 20, 30)//
                .orderBy("age")//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should find 2 users (20, 29)", 2, result.size());
        assertEquals(Integer.valueOf(20), result.get(0).getAge());
        assertEquals(Integer.valueOf(29), result.get(1).getAge());
    }

    /**
     * 测试 BETWEEN 与 ORDER BY 组合
     */
    @Test
    public void testBetweenWithOrderBy() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(18001, "Order1", 30))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(18002, "Order2", 20))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(18003, "Order3", 25))//
                .executeSumResult();

        // BETWEEN + ORDER BY DESC
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Order%")//
                .rangeBetween(UserInfo::getAge, 20, 30)//
                .desc("age")//
                .queryForList();

        assertNotNull(result);
        assertEquals(3, result.size());
        // 验证降序
        assertEquals(Integer.valueOf(30), result.get(0).getAge());
        assertEquals(Integer.valueOf(25), result.get(1).getAge());
        assertEquals(Integer.valueOf(20), result.get(2).getAge());
    }

    // ==================== 补充：缺失的 Range 变体 ====================

    /**
     * 测试 rangeClosedClosed (v1 <= col <= v2)
     * 等价于 BETWEEN
     */
    @Test
    public void testRangeClosedClosed() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(UserInfo.class).applyEntity(createUser(18001, "CC1", 15)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18002, "CC2", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18003, "CC3", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18004, "CC4", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18005, "CC5", 35)).executeSumResult();

        // [20, 30] => 20, 25, 30
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "CC%")//
                .rangeClosedClosed(UserInfo::getAge, 20, 30)//
                .queryForCount();
        assertEquals(3, count);
    }

    /**
     * 测试 rangeNotOpenClosed - NOT (v1 < col <= v2)
     */
    @Test
    public void testRangeNotOpenClosed() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(UserInfo.class).applyEntity(createUser(18011, "NOC1", 15)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18012, "NOC2", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18013, "NOC3", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18014, "NOC4", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18015, "NOC5", 35)).executeSumResult();

        // rangeOpenClosed(20,30) => (20,30] => 25,30 即 2 条
        // rangeNotOpenClosed(20,30) => NOT (20,30] => 15,20,35 即 3 条
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NOC%")//
                .rangeNotOpenClosed(UserInfo::getAge, 20, 30)//
                .queryForCount();
        assertEquals(3, count);
    }

    /**
     * 测试 rangeNotClosedOpen - NOT (v1 <= col < v2)
     */
    @Test
    public void testRangeNotClosedOpen() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(UserInfo.class).applyEntity(createUser(18021, "NCO1", 15)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18022, "NCO2", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18023, "NCO3", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18024, "NCO4", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18025, "NCO5", 35)).executeSumResult();

        // rangeClosedOpen(20,30) => [20,30) => 20,25 即 2 条
        // rangeNotClosedOpen(20,30) => NOT [20,30) => 15,30,35 即 3 条
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NCO%")//
                .rangeNotClosedOpen(UserInfo::getAge, 20, 30)//
                .queryForCount();
        assertEquals(3, count);
    }

    /**
     * 测试 rangeNotClosedClosed - NOT (v1 <= col <= v2)
     * 等价于 NOT BETWEEN
     */
    @Test
    public void testRangeNotClosedClosed() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(UserInfo.class).applyEntity(createUser(18031, "NCC1", 15)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18032, "NCC2", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18033, "NCC3", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18034, "NCC4", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18035, "NCC5", 35)).executeSumResult();

        // rangeClosedClosed(20,30) => [20,30] => 20,25,30 即 3 条
        // rangeNotClosedClosed(20,30) => NOT [20,30] => 15,35 即 2 条
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NCC%")//
                .rangeNotClosedClosed(UserInfo::getAge, 20, 30)//
                .queryForCount();
        assertEquals(2, count);
    }

    /**
     * 测试 rangeBetween 动态版本 (boolean test)
     */
    @Test
    public void testRangeBetween_Dynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(UserInfo.class).applyEntity(createUser(18041, "DRB1", 15)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18042, "DRB2", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18043, "DRB3", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18044, "DRB4", 30)).executeSumResult();

        // test=true => rangeBetween 生效
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DRB%")//
                .rangeBetween(true, UserInfo::getAge, 20, 25)//
                .queryForCount();
        assertEquals(2, count); // 20, 25

        // test=false => rangeBetween 跳过
        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DRB%")//
                .rangeBetween(false, UserInfo::getAge, 20, 25)//
                .queryForCount();
        assertEquals(4, count); // 全部
    }

    /**
     * 测试 rangeNotBetween 动态版本 (boolean test)
     */
    @Test
    public void testRangeNotBetween_Dynamic() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(UserInfo.class).applyEntity(createUser(18051, "DNRB1", 15)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18052, "DNRB2", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18053, "DNRB3", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(18054, "DNRB4", 30)).executeSumResult();

        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DNRB%")//
                .rangeNotBetween(true, UserInfo::getAge, 20, 25)//
                .queryForCount();
        assertEquals(2, count); // 15, 30

        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DNRB%")//
                .rangeNotBetween(false, UserInfo::getAge, 20, 25)//
                .queryForCount();
        assertEquals(4, count); // 全部
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@between.com");
        u.setCreateTime(new Date());
        return u;
    }
}
