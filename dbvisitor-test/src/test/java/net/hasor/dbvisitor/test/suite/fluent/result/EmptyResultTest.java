package net.hasor.dbvisitor.test.suite.fluent.result;
import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Empty Result Handling Test
 * 验证空结果的处理行为
 */
public class EmptyResultTest extends AbstractOneApiTest {

    /**
     * 测试 queryForList 返回空列表
     * 应返回空列表而不是 null
     */
    @Test
    public void testQueryForListReturnsEmptyList() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        List<UserInfo> result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 99999999)//
                .queryForList();

        assertNotNull("Should return empty list, not null", result);
        assertEquals("Empty list should have size 0", 0, result.size());
        assertTrue("Should be empty", result.isEmpty());
    }

    /**
     * 测试 queryForObject 无结果
     * 应返回 null 或抛出异常
     */
    @Test
    public void testQueryForObjectNoResult() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 99999999)//
                .queryForObject();

        // 如果返回 null，验证之
        assertNull("Should return null when no result", result);
    }

    /**
     * 测试 queryForCount 返回 0
     * 空结果集应返回 0
     */
    @Test
    public void testQueryForCountReturnsZero() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        long count = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 99999999)//
                .queryForCount();

        assertEquals("Empty query should return count 0", 0, count);
    }

    /**
     * 测试空分页结果
     * 应返回空页面对象，不是 null
     */
    @Test
    public void testQueryForPageReturnsEmptyPage() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        Page pageInfo = PageObject.of(1, 10);
        EntityQuery<UserInfo> query = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 99999999)//
                .usePage(pageInfo);
        List<UserInfo> pageData = query.queryForList();
        Page page = query.pageInfo();

        assertNotNull("Page should not be null", page);
        assertNotNull("Page data should not be null", pageData);
        assertEquals("Page data should be empty", 0, pageData.size());
        assertEquals("Total count should be 0", 0, page.getTotalCount());
        assertEquals("Total page should be 0", 0, page.getTotalPage());
    }

    /**
     * 测试空字符串 vs NULL 的区分
     * 场景: WHERE name = '' vs WHERE name IS NULL
     */
    @Test
    public void testEmptyStringVsNull() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入空字符串
        UserInfo u1 = new UserInfo();
        u1.setId(24501);
        u1.setName(""); // 空字符串
        u1.setAge(25);
        lambda.insert(UserInfo.class)//
                .applyEntity(u1)//
                .executeSumResult();

        // 插入 NULL
        UserInfo u2 = new UserInfo();
        u2.setId(24502);
        u2.setName(null); // NULL
        u2.setAge(26);
        lambda.insert(UserInfo.class)//
                .applyEntity(u2)//
                .executeSumResult();

        // 查询空字符串
        List<UserInfo> emptyStrResult = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(24501, 24502))//
                .eq(UserInfo::getName, "")//
                .queryForList();

        assertEquals("Should find empty string record", 1, emptyStrResult.size());
        assertEquals(Integer.valueOf(24501), emptyStrResult.get(0)//
                .getId());

        // 查询 NULL
        List<UserInfo> nullResult = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(24501, 24502))//
                .isNull(UserInfo::getName)//
                .queryForList();

        assertEquals("Should find NULL record", 1, nullResult.size());
        assertEquals(Integer.valueOf(24502), nullResult.get(0)//
                .getId());
    }

    /**
     * 测试 WHERE 条件匹配空字符串
     * LIKE '' 匹配所有非 NULL 的 name 记录（因为任何字符串都包含空字符串）
     */
    @Test
    public void testLikeEmptyString() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(24601, "Like1", 25))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(24602, "Like2", 26))//
                .executeSumResult();

        // LIKE '' 应匹配所有非 NULL 记录
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(24601, 24602))//
                .like(UserInfo::getName, "")//
                .queryForList();

        assertEquals("Empty LIKE should match all non-NULL", 2, result.size());
    }

    /**
     * 测试 IN 空列表
     * 框架不允许空 IN 列表，会抛出 IllegalArgumentException
     */
    @Test
    public void testInEmptyList() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(24701, "In1", 25))//
                .executeSumResult();

        try {
            lambda.query(UserInfo.class)//
                    .in(UserInfo::getId, Collections.emptyList())//
                    .queryForList();
            fail("Should throw IllegalArgumentException for empty IN list");
        } catch (IllegalArgumentException e) {
            // 预期行为：框架拒绝空 IN 列表
            assertTrue("Should mention empty", e.getMessage().contains("empty"));
        }
    }

    /**
     * 测试空 GROUP BY 结果
     * 无数据时 GROUP BY 应返回空列表
     */
    @Test
    public void testGroupByWithNoData() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        List<Map<String, Object>> result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 99999999)//
                .applySelect("age, count(*) as cnt")//
                .groupBy("age")//
                .queryForMapList();

        assertNotNull("Should return empty list", result);
        assertEquals("Empty GROUP BY should return no groups", 0, result.size());
    }

    /**
     * 测试 MAX/MIN/AVG 在空表上的行为
     * 聚合函数在空结果集上应返回 NULL
     */
    @Test
    public void testAggregationOnEmptyTable() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // COUNT 应返回 0
        Long countResult = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 99999999)//
                .applySelect("count(*)")//
                .queryForObject(Long.class);

        assertNotNull("COUNT should return 0, not null", countResult);
        assertEquals("COUNT on empty should be 0", Long.valueOf(0), countResult);

        // MAX 应返回 NULL
        Integer maxResult = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 99999999)//
                .applySelect("max(age)")//
                .queryForObject(Integer.class);

        assertNull("MAX on empty should return null", maxResult);
    }

    /**
     * 测试 DISTINCT 空结果
     * 无数据时 DISTINCT 应返回空列表
     */
    @Test
    public void testDistinctWithNoData() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        List<Integer> result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 99999999)//
                .applySelect("distinct id")//
                .queryForList(Integer.class);

        assertNotNull("Should return empty list", result);
        assertEquals("DISTINCT on empty should return empty", 0, result.size());
    }

    /**
     * 测试空迭代器
     * iteratorForLimit 应返回空迭代器
     */
    @Test
    public void testIteratorWithNoData() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        int count = 0;
        java.util.Iterator<UserInfo> iterator = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 99999999)//
                .iteratorForLimit(10);
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }

        assertEquals("Empty iterator should not execute callback", 0, count);
    }

    /**
     * 测试 UPDATE 不存在的记录
     * 应返回 0（受影响行数）
     */
    @Test
    public void testUpdateNonExistentRecord() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        int updated = lambda.update(UserInfo.class)//
                .eq(UserInfo::getId, 99999999)//
                .updateTo(UserInfo::getAge, 100)//
                .doUpdate();

        assertEquals("Update non-existent should return 0", 0, updated);
    }

    /**
     * 测试 DELETE 不存在的记录
     * 应返回 0（受影响行数）
     */
    @Test
    public void testDeleteNonExistentRecord() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        int deleted = lambda.delete(UserInfo.class)//
                .eq(UserInfo::getId, 99999999)//
                .doDelete();

        assertEquals("Delete non-existent should return 0", 0, deleted);
    }

    /**
     * 测试 LIMIT 0（pageSize=0 表示不分页）
     * 框架中 initPage(0, 0) 的 pageSize=0 表示不分页，返回所有匹配记录
     */
    @Test
    public void testLimitZero() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(24901, "Limit0", 25))//
                .executeSumResult();

        // pageSize=0 在框架中表示不分页，返回所有匹配记录
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getName, "Limit0")//
                .initPage(0, 0)//
                .queryForList();

        assertNotNull("Should return list", result);
        assertEquals("pageSize=0 means no paging, returns all matching", 1, result.size());
    }

    /**
     * 测试空 BETWEEN 范围
     * WHERE age BETWEEN 100 AND 50 (反向) 应返回空
     */
    @Test
    public void testEmptyBetweenRange() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(25001, "Between1", 25))//
                .executeSumResult();

        List<UserInfo> result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getName, "Between1")//
                .rangeBetween(UserInfo::getAge, 100, 50) // 反向范围
                .queryForList();

        assertNotNull("Should return empty list", result);
        assertEquals("Reversed BETWEEN should return empty", 0, result.size());
    }

    /**
     * 测试查询空表
     * 新表应返回空结果
     */
    @Test
    public void testQueryEmptyTable() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 假设表刚创建或已清空（实际测试中可能有其他数据）
        // 查询一个不可能存在的条件
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getName, "IMPOSSIBLE_NAME_" + System.currentTimeMillis())//
                .queryForList();

        assertNotNull("Should return empty list", result);
        assertEquals("Non-matching query should return empty", 0, result.size());
    }

    /**
     * 测试 queryForMapList 空结果
     * 应返回空列表
     */
    @Test
    public void testQueryForMapListEmpty() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        List<Map<String, Object>> result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 99999999)//
                .queryForMapList();

        assertNotNull("Should return empty list", result);
        assertEquals("Empty queryForMapList should return empty", 0, result.size());
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@empty.com");
        u.setCreateTime(new Date());
        return u;
    }
}
