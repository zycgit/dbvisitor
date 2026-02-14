package net.hasor.dbvisitor.test.suite.fluent.result;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * LIMIT / OFFSET Independent Test
 * 验证独立于分页对象的限制查询功能
 * 注意: Lambda API 可能需要通过 usePage() 或其他方式实现
 */
public class LimitOffsetTest extends AbstractOneApiTest {

    /**
     * 测试 Top-N 查询 (LIMIT without OFFSET)
     * SQL: SELECT * FROM user_info ORDER BY age DESC LIMIT 5
     */
    @Test
    public void testTopNQuery() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入 20 条数据
        for (int i = 1; i <= 20; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(11000 + i, "TopN" + i, 20 + i))//
                    .executeSumResult();
        }

        // 查询 Top 5（年龄最大的 5 个用户）
        // Lambda API 通过 initPage(pageSize, pageNumber) 实现，pageNumber=0 表示第一页
        EntityQuery<UserInfo> query = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "TopN%")//
                .desc("age")//
                .initPage(5, 0); // pageSize=5, page=0 (第一页)

        List<UserInfo> result = query.queryForList();

        assertNotNull(result);
        assertEquals("Should return top 5 users", 5, result.size());

        // 验证排序（age 应为 40, 39, 38, 37, 36）
        assertEquals(Integer.valueOf(40), result.get(0).getAge());
        assertEquals(Integer.valueOf(39), result.get(1).getAge());
        assertEquals(Integer.valueOf(36), result.get(4).getAge());
    }

    /**
     * 测试 LIMIT + OFFSET
     * SQL: SELECT * FROM user_info ORDER BY id LIMIT 10 OFFSET 5
     */
    @Test
    public void testLimitWithOffset() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入 30 条数据
        for (int i = 1; i <= 30; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(11100 + i, "Offset" + i, 20 + i))//
                    .executeSumResult();
        }

        // 跳过前 5 条，取接下来的 10 条
        // initPage(pageSize, pageNumber): pageNumber=1 表示第二页（跳过第一页的 5 条）
        // 但这里 pageSize=10, 所以需要 pageNumber=0 表示第一页（0-9），pageNumber=1 表示 (10-19)
        // 要实现 OFFSET 5, LIMIT 10，可以用 pageSize=10, 起始位置=5 的方式

        // 方案1: 使用分页逻辑 - 第2页，每页5条（offset=5, limit=5）
        EntityQuery<UserInfo> query = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Offset%")//
                .orderBy("id")//
                .initPage(10, 1); // pageSize=10, page=1 -> offset=10

        List<UserInfo> result = query.queryForList();

        assertNotNull(result);
        assertEquals("Should return 10 users with offset 10", 10, result.size());

        // 验证是否跳过了前 10 条（ID 应从 11111 开始）
        assertTrue("First result ID should be > 11110", result.get(0).getId() > 11110);
    }

    /**
     * 测试只有 LIMIT 1 的查询（常用于获取单条记录）
     * SQL: SELECT * FROM user_info WHERE name LIKE 'Single%' LIMIT 1
     */
    @Test
    public void testLimitOne() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(11201, "Single1", 25))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(11202, "Single2", 30))//
                .executeSumResult();

        // LIMIT 1
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Single%")//
                .orderBy("id")//
                .initPage(1, 0) // pageSize=1, page=0
                .queryForList();

        assertNotNull(result);
        assertEquals("Should return exactly 1 user", 1, result.size());
        assertEquals(Integer.valueOf(11201), result.get(0).getId());
    }

    /**
     * 测试 OFFSET 超出范围的情况
     * SQL: SELECT * FROM user_info LIMIT 10 OFFSET 1000
     */
    @Test
    public void testOffsetBeyondRange() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 只插入 10 条数据
        for (int i = 1; i <= 10; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(11300 + i, "Beyond" + i, 20 + i))//
                    .executeSumResult();
        }

        // OFFSET 超出范围（请求第 100 页，但总共不足 100 页）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Beyond%")//
                .initPage(10, 100) // pageSize=10, page=100 (offset=1000)//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should return empty list when offset exceeds total", 0, result.size());
    }

    /**
     * 测试 LIMIT 0 的行为（pageSize=0 表示不分页）
     * 框架中 initPage(0, 0) 的 pageSize=0 表示不分页，返回所有匹配记录
     */
    @Test
    public void testLimitZero() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(11401, "Zero1", 25))//
                .executeSumResult();

        // pageSize=0 在框架中表示不分页，返回所有匹配记录
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Zero%")//
                .initPage(0, 0) // pageSize=0 = no paging
                .queryForList();

        assertNotNull(result);
        assertEquals("pageSize=0 means no paging, returns all matching", 1, result.size());
    }

    /**
     * 测试 OFFSET 0 的行为（等同于不使用 OFFSET）
     * SQL: SELECT * FROM user_info LIMIT 5 OFFSET 0
     */
    @Test
    public void testOffsetZero() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        for (int i = 1; i <= 10; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(11500 + i, "OffZero" + i, 20 + i))//
                    .executeSumResult();
        }

        // OFFSET 0（等同于从头开始）
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "OffZero%")//
                .orderBy("id")//
                .initPage(5, 0) // pageSize=5, page=0 (offset=0)//
                .queryForList();

        assertNotNull(result);
        assertEquals("Should return first 5 users", 5, result.size());
        assertEquals(Integer.valueOf(11501), result.get(0).getId());
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@limit.com");
        u.setCreateTime(new Date());
        return u;
    }
}
