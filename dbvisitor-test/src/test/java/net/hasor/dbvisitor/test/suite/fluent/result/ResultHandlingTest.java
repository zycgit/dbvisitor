package net.hasor.dbvisitor.test.suite.fluent.result;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.extractor.FilterResultSetExtractor;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Fluent API - 结果集处理扩展测试
 * 测试 ResultSetExtractor 和 RowCallbackHandler
 * 补充 RowMapperTest，专注于高级结果集处理
 * 参考文档：dbvisitor/dbvisitor-doc/docs/guides/result/
 */
public class ResultHandlingTest extends AbstractOneApiTest {

    @Before
    public void setUp() throws Exception {
        super.setup();
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);

        // 插入测试数据
        for (int i = 1; i <= 10; i++) {
            UserInfo user = new UserInfo();
            user.setId(95000 + i);
            user.setName("ResultFluent" + i);
            user.setAge(20 + i);
            user.setEmail("rf" + i + "@test.com");
            lambda.insert(UserInfo.class)//
                    .applyEntity(user)//
                    .executeSumResult();
        }
    }

    // ========== List/Map 结果集测试 ==========

    /**
     * 测试 queryForMapList - 返回 Map 列表
     */
    @Test
    public void testListMap_QueryForMapList() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        List<Map<String, Object>> results = lambda.query(UserInfo.class)//
                .between("age", 23, 26)//
                .orderBy("age")//
                .queryForMapList();

        assertTrue("应找到记录", results.size() >= 4);
        for (Map<String, Object> map : results) {
            int age = (Integer) map.get("age");
            assertTrue(age >= 23 && age <= 26);
        }
    }

    /**
     * 测试 queryForMap - 返回单条 Map
     */
    @Test
    public void testListMap_QueryForMap() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        Map<String, Object> result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 95001)//
                .queryForMap();

        assertNotNull(result);
        assertEquals(95001, result.get("id"));
        assertEquals("ResultFluent1", result.get("name"));
    }

    /**
     * 测试 queryForCount - 返回记录数
     */
    @Test
    public void testListMap_QueryForCount() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        long count = lambda.query(UserInfo.class)//
                .gt(UserInfo::getAge, 25)//
                .queryForCount();

        assertTrue("应找到记录", count > 0);
    }

    /**
     * 测试 queryForObject - 返回单列值
     */
    @Test
    public void testListMap_QueryForSingleValue() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        // 使用 applySelect 选择单列
        Integer maxAge = lambda.query(UserInfo.class)//
                .between("id", 95001, 95005)//
                .applySelect("MAX(age)")//
                .queryForObject(Integer.class);

        assertEquals(Integer.valueOf(25), maxAge);
    }

    // ========== RowCallbackHandler 测试 ==========

    /**
     * 测试 RowCallbackHandler - 基本用法（收集数据）
     */
    @Test
    public void testRowCallback_BasicCollection() throws SQLException {
        List<String> collectedNames = new ArrayList<>();

        RowCallbackHandler handler = (rs, rowNum) -> {
            collectedNames.add(rs.getString("name"));
        };

        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        lambda.query(UserInfo.class)//
                .lt(UserInfo::getAge, 25)//
                .query(handler);

        assertTrue("应收集到记录", !collectedNames.isEmpty());
        assertTrue(collectedNames.stream()//
                .allMatch(n -> n.startsWith("ResultFluent")));
    }

    /**
     * 测试 RowCallbackHandler - 流式聚合计算
     */
    @Test
    public void testRowCallback_StreamAggregation() throws SQLException {
        AtomicInteger totalAge = new AtomicInteger(0);
        AtomicInteger count = new AtomicInteger(0);

        RowCallbackHandler handler = (rs, rowNum) -> {
            totalAge.addAndGet(rs.getInt("age"));
            count.incrementAndGet();
        };

        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        lambda.query(UserInfo.class)//
                .between("id", 95001, 95005)//
                .query(handler);

        assertEquals(5, count.get());

        double avgAge = (double) totalAge.get() / count.get();
        assertEquals(23.0, avgAge, 0.1);  // (21+22+23+24+25)/5 = 23
    }

    /**
     * 测试 RowCallbackHandler - 条件过滤
     */
    @Test
    public void testRowCallback_ConditionalProcessing() throws SQLException {
        List<Integer> evenAges = new ArrayList<>();
        List<Integer> oddAges = new ArrayList<>();

        RowCallbackHandler handler = (rs, rowNum) -> {
            int age = rs.getInt("age");
            if (age % 2 == 0) {
                evenAges.add(age);
            } else {
                oddAges.add(age);
            }
        };

        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        lambda.query(UserInfo.class)//
                .between("id", 95001, 95010)//
                .query(handler);

        assertTrue("应有偶数年龄", evenAges.size() > 0);
        assertTrue("应有奇数年龄", oddAges.size() > 0);
    }

    /**
     * 测试 RowCallbackHandler - 无返回值处理
     */
    @Test
    public void testRowCallback_NoReturn() throws SQLException {
        AtomicInteger processedCount = new AtomicInteger(0);

        // 直接处理，不返回结果
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        lambda.query(UserInfo.class)//
                .gt(UserInfo::getAge, 20)//
                .query((rs, rowNum) -> {
                    // 模拟处理逻辑（如发送通知、写日志等）
                    processedCount.incrementAndGet();
                });

        assertTrue("应处理记录", processedCount.get() > 0);
    }

    // ========== ResultSetExtractor 测试 ==========

    /**
     * 测试自定义 ResultSetExtractor - 构建 Map<ID, Name>
     */
    @Test
    public void testExtractor_CustomMap() throws SQLException {
        ResultSetExtractor<Map<Integer, String>> extractor = rs -> {
            Map<Integer, String> map = new LinkedHashMap<>();
            while (rs.next()) {
                map.put(rs.getInt("id"), rs.getString("name"));
            }
            return map;
        };

        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        Map<Integer, String> result = lambda.query(UserInfo.class)//
                .between("id", 95001, 95003)//
                .orderBy("id")//
                .query(extractor);

        assertEquals(3, result.size());
        assertEquals("ResultFluent1", result.get(95001));
        assertEquals("ResultFluent2", result.get(95002));
        assertEquals("ResultFluent3", result.get(95003));
    }

    /**
     * 测试 ResultSetExtractor - 自定义聚合结构
     */
    @Test
    public void testExtractor_CustomAggregation() throws SQLException {
        class AgeStatistics {
            int min   = Integer.MAX_VALUE;
            int max   = Integer.MIN_VALUE;
            int sum   = 0;
            int count = 0;

            double getAverage() {
                return count > 0 ? (double) sum / count : 0;
            }
        }

        ResultSetExtractor<AgeStatistics> extractor = rs -> {
            AgeStatistics stats = new AgeStatistics();
            while (rs.next()) {
                int age = rs.getInt("age");
                stats.min = Math.min(stats.min, age);
                stats.max = Math.max(stats.max, age);
                stats.sum += age;
                stats.count++;
            }
            return stats;
        };

        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        AgeStatistics stats = lambda.query(UserInfo.class)//
                .between("id", 95001, 95010)//
                .query(extractor);

        assertEquals(10, stats.count);
        assertEquals(21, stats.min);
        assertEquals(30, stats.max);
        assertEquals(25.5, stats.getAverage(), 0.1);
    }

    /**
     * 测试 queryForPairs 方法 (String 参数)
     */
    @Test
    public void testExtractor_Pairs() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        Map<Integer, String> result = lambda.query(UserInfo.class)//
                .between("id", 95004, 95006)//
                .queryForPairs("id", "name", Integer.class, String.class);

        assertEquals(3, result.size());
        assertEquals("ResultFluent4", result.get(95004));
        assertEquals("ResultFluent5", result.get(95005));
        assertEquals("ResultFluent6", result.get(95006));
    }

    /**
     * 测试 queryForPairs 方法 (Lambda 参数)
     */
    @Test
    public void testLambda_Pairs() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        Map<Integer, String> result = lambda.query(UserInfo.class)//
                .between("id", 95004, 95006)//
                .queryForPairs(UserInfo::getId, UserInfo::getName, Integer.class, String.class);

        assertEquals(3, result.size());
        assertEquals("ResultFluent4", result.get(95004));
        assertEquals("ResultFluent5", result.get(95005));
        assertEquals("ResultFluent6", result.get(95006));
    }

    /**
     * 测试内置 ResultSetExtractor - FilterResultSetExtractor
     */
    @Test
    public void testExtractor_Filter() throws SQLException {
        ResultSetExtractor<List<UserInfo>> extractor = new FilterResultSetExtractor<>((rs, rowNum) -> {
            UserInfo u = new UserInfo();
            u.setId(rs.getInt("id"));
            u.setName(rs.getString("name"));
            u.setAge(rs.getInt("age"));
            return u;
        }, user -> user.getAge() % 2 == 0  // 只保留偶数年龄
        );

        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        List<UserInfo> results = lambda.query(UserInfo.class)//
                .between("id", 95001, 95010)//
                .query(extractor);

        assertTrue("应找到记录", results.size() > 0);
        assertTrue("都应是偶数年龄", results.stream()//
                .allMatch(u -> u.getAge() % 2 == 0));
    }

    // ========== 复杂场景测试 ==========

    /**
     * 测试混合使用 - Lambda 查询 + 自定义 Extractor
     */
    @Test
    public void testComplex_LambdaWithExtractor() throws SQLException {
        // 构建复杂的结果结构：按年龄分组
        ResultSetExtractor<Map<Integer, List<String>>> extractor = rs -> {
            Map<Integer, List<String>> groupByAge = new HashMap<>();
            while (rs.next()) {
                int age = rs.getInt("age");
                String name = rs.getString("name");
                groupByAge.computeIfAbsent(age, k -> new ArrayList<>())//
                        .add(name);
            }
            return groupByAge;
        };

        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        Map<Integer, List<String>> result = lambda.query(UserInfo.class)//
                .between("id", 95001, 95010)//
                .applySelect("age, name")//
                .orderBy("age")//
                .query(extractor);

        assertTrue("应有多个年龄组", result.size() > 0);
        result.forEach((age, names) -> {
            assertTrue("年龄应在范围内", age >= 21 && age <= 30);
            assertFalse("每个年龄组应有名字", names.isEmpty());
        });
    }

    /**
     * 测试结果转换 - Map 到 Bean
     */
    @Test
    public void testComplex_MapToBean() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(jdbcTemplate);
        List<Map<String, Object>> maps = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 95007)//
                .queryForMapList();

        assertEquals(1, maps.size());

        // 手动转换 Map 到 Bean
        Map<String, Object> map = maps.get(0);
        UserInfo user = new UserInfo();
        user.setId((Integer) map.get("id"));
        user.setName((String) map.get("name"));
        user.setAge((Integer) map.get("age"));

        assertEquals(Integer.valueOf(95007), user.getId());
        assertEquals("ResultFluent7", user.getName());
    }
}
