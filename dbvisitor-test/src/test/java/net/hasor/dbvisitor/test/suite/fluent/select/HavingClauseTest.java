//package net.hasor.dbvisitor.test.suite.fluent.select;
//
//import java.sql.SQLException;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import net.hasor.dbvisitor.lambda.LambdaTemplate;
//import net.hasor.dbvisitor.test.AbstractOneApiTest;
//import net.hasor.dbvisitor.test.model.UserInfo;
//import org.junit.Test;
//import static org.junit.Assert.*;
//
/// **
// * HAVING Clause Test
// * 验证分组后的条件过滤（HAVING 子句）
// * 注意: Lambda API 可能需要通过原生 SQL 或特殊方法实现 HAVING
// */
//public class HavingClauseTest extends AbstractOneApiTest {
//
//    /**
//     * 测试 GROUP BY + COUNT(*) + HAVING
//     * SQL: SELECT age, COUNT(*) as cnt FROM user_info GROUP BY age HAVING cnt > 1
//     */
//    @Test
//    public void testGroupByWithHavingCount() throws SQLException {
//        LambdaTemplate lambda = new LambdaTemplate(dataSource);
//
//        // 插入测试数据: 多个用户同年龄
//        lambda.insert(UserInfo.class)//
//                .applyEntity(createUser(6001, "User1", 20))//
//                .executeSumResult();
//        lambda.insert(UserInfo.class)//
//                .applyEntity(createUser(6002, "User2", 20))//
//                .executeSumResult();
//        lambda.insert(UserInfo.class)//
//                .applyEntity(createUser(6003, "User3", 20))//
//                .executeSumResult();
//        lambda.insert(UserInfo.class)//
//                .applyEntity(createUser(6004, "User4", 25))//
//                .executeSumResult();
//        lambda.insert(UserInfo.class)//
//                .applyEntity(createUser(6005, "User5", 30))//
//                .executeSumResult();
//
//        // 使用 applySelect 构建 GROUP BY + HAVING（通过原生 SQL 片段）
//        // 注意：Lambda API 可能不直接支持 HAVING，这里验证能否通过 Map 结果处理
//        List<Map<String, Object>> result = lambda.query(UserInfo.class)//
//                .applySelect("age, COUNT(*) as cnt")//
//                .groupBy("age")//
//                .orderBy("age")//
//                .queryForMapList();
//
//        assertNotNull(result);
//        assertTrue("Should have grouped results", result.size() >= 3);
//
//        // 手动过滤 HAVING cnt > 1 的逻辑（因为 Lambda API 可能不支持 HAVING）
//        long countOver1 = result.stream()//
//                .filter(row -> {
//                    Object cntObj = row.get("cnt");
//                    if (cntObj instanceof Number) {
//                        return ((Number) cntObj)//
//                                .intValue() > 1;
//                    }
//                    return false;
//                })//
//                .count();
//
//        assertEquals("Should find 1 age group with count > 1 (age=20)", 1, countOver1);
//    }
//
//    /**
//     * 测试 GROUP BY + SUM + HAVING
//     * 模拟场景: 查询订单总金额 > 100 的用户
//     * 注意：由于 Lambda API 主要针对单表，这里简化为统计 age 总和
//     * SQL: SELECT name, SUM(age) as total_age FROM user_info GROUP BY name HAVING total_age > 50
//     */
//    @Test
//    public void testGroupByWithHavingSum() throws SQLException {
//        LambdaTemplate lambda = new LambdaTemplate(dataSource);
//
//        // 插入同名用户（实际场景较少，这里仅为演示）
//        lambda.insert(UserInfo.class)//
//                .applyEntity(createUser(7001, "Alice", 20))//
//                .executeSumResult();
//        lambda.insert(UserInfo.class)//
//                .applyEntity(createUser(7002, "Alice", 35))//
//                .executeSumResult();
//        lambda.insert(UserInfo.class)//
//                .applyEntity(createUser(7003, "Bob", 15))//
//                .executeSumResult();
//        lambda.insert(UserInfo.class)//
//                .applyEntity(createUser(7004, "Bob", 25))//
//                .executeSumResult();
//
//        // 使用原生 SQL 片段进行聚合
//        List<Map<String, Object>> result = lambda.query(UserInfo.class)//
//                .applySelect("name, SUM(age) as total_age")//
//                .like(UserInfo::getName, "%") // 添加条件确保查询范围
//                .groupBy("name")//
//                .orderBy("name")//
//                .queryForMapList();
//
//        assertNotNull(result);
//
//        // 验证结果包含分组数据
//        assertTrue("Should have at least 2 groups", result.size() >= 2);
//
//        // 手动验证 HAVING 逻辑（total_age > 50）
//        long countOver50 = result.stream()//
//                .filter(row -> {
//                    Object totalObj = row.get("total_age");
//                    if (totalObj instanceof Number) {
//                        return ((Number) totalObj)//
//                                .intValue() > 50;
//                    }
//                    return false;
//                })//
//                .count();
//
//        assertEquals("Should find 1 name with total_age > 50 (Alice=55)", 1, countOver50);
//    }
//
//    /**
//     * 测试 GROUP BY + AVG + HAVING
//     * SQL: SELECT age, AVG(age) FROM user_info GROUP BY age HAVING AVG(age) >= 25
//     */
//    @Test
//    public void testGroupByWithHavingAvg() throws SQLException {
//        LambdaTemplate lambda = new LambdaTemplate(dataSource);
//
//        // 插入数据
//        lambda.insert(UserInfo.class)//
//                .applyEntity(createUser(8001, "Test1", 20))//
//                .executeSumResult();
//        lambda.insert(UserInfo.class)//
//                .applyEntity(createUser(8002, "Test2", 25))//
//                .executeSumResult();
//        lambda.insert(UserInfo.class)//
//                .applyEntity(createUser(8003, "Test3", 30))//
//                .executeSumResult();
//
//        // 使用 AVG 聚合
//        List<Map<String, Object>> result = lambda.query(UserInfo.class)//
//                .applySelect("age, AVG(age) as avg_age")//
//                .like(UserInfo::getName, "Test%")//
//                .groupBy("age")//
//                .queryForMapList();
//
//        assertNotNull(result);
//        assertTrue("Should have grouped by age", result.size() >= 3);
//
//        // 手动过滤 AVG(age) >= 25
//        long countAvgOver25 = result.stream()//
//                .filter(row -> {
//                    Object avgObj = row.get("avg_age");
//                    if (avgObj instanceof Number) {
//                        return ((Number) avgObj)//
//                                .doubleValue() >= 25.0;
//                    }
//                    return false;
//                })//
//                .count();
//
//        assertTrue("Should find groups with avg >= 25", countAvgOver25 >= 2);
//    }
//
//    /**
//     * Helper: 创建 UserInfo
//     */
//    private UserInfo createUser(int id, String name, Integer age) {
//        UserInfo u = new UserInfo();
//        u.setId(id);
//        u.setName(name);
//        u.setAge(age);
//        u.setEmail(name.toLowerCase() + "@having.com");
//        u.setCreateTime(new Date());
//        return u;
//    }
//}
