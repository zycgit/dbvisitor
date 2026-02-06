package net.hasor.dbvisitor.test.oneapi.suite.programmatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.jdbc.extractor.*;
import net.hasor.dbvisitor.jdbc.mapper.BeanMappingRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.SingleColumnRowMapper;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Programmatic API - 结果集处理测试
 * 使用 JdbcTemplate 编程式 API 测试各种结果集处理方式
 * 测试场景模版来源：result 文档
 * API 实现：JdbcTemplate (编程式)
 * 参考文档：dbvisitor/dbvisitor-doc/docs/guides/result/
 */
public class ResultHandlingTest extends AbstractOneApiTest {

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() throws Exception {
        super.setup();
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        // 清理可能存在的测试数据（94001-94010范围）
        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id >= 94001 AND id <= 94010");

        // 插入测试数据
        for (int i = 1; i <= 10; i++) {
            jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email) VALUES (?, ?, ?, ?)", new Object[] { 94000 + i, "Result" + i, 20 + i, "result" + i + "@test.com" });
        }
    }

    // ========== RowMapper 测试 ==========

    /**
     * 测试自定义 RowMapper
     */
    @Test
    public void testRowMapper_Custom() throws Exception {
        RowMapper<UserInfo> customMapper = (rs, rowNum) -> {
            UserInfo user = new UserInfo();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name")//
                    .toUpperCase());  // 转大写
            user.setAge(rs.getInt("age"));
            return user;
        };

        List<UserInfo> results = jdbcTemplate.queryForList("SELECT * FROM user_info WHERE id BETWEEN ? AND ?",//
                new Object[] { 94001, 94003 }, customMapper);

        assertEquals(3, results.size());
        assertEquals("RESULT1", results.get(0).getName());
    }

    /**
     * 测试 Lambda 风格 RowMapper
     */
    @Test
    public void testRowMapper_Lambda() throws Exception {
        RowMapper<String> nameMapper = (rs, rowNum) -> //
                rs.getString("name") + "-" + rs.getInt("age");

        List<String> results = jdbcTemplate.queryForList("SELECT * FROM user_info WHERE id = ?",//
                new Object[] { 94001 }, nameMapper);

        assertEquals(1, results.size());
        assertEquals("Result1-21", results.get(0));
    }

    /**
     * 测试内置 RowMapper - ColumnMapRowMapper
     */
    @Test
    public void testRowMapper_ColumnMap() throws Exception {
        RowMapper<Map<String, Object>> mapper = new ColumnMapRowMapper();

        List<Map<String, Object>> results = jdbcTemplate.queryForList("SELECT * FROM user_info WHERE age > ?",//
                new Object[] { 25 }, mapper);

        assertTrue("应找到记录", results.size() > 0);
        Map<String, Object> first = results.get(0);
        assertNotNull(first.get("id"));
        assertNotNull(first.get("name"));
        assertNotNull(first.get("age"));
    }

    /**
     * 测试内置 RowMapper - SingleColumnRowMapper
     */
    @Test
    public void testRowMapper_SingleColumn() throws Exception {
        RowMapper<String> mapper = new SingleColumnRowMapper<>(String.class);

        List<String> names = jdbcTemplate.queryForList("SELECT name FROM user_info WHERE age < ?",//
                new Object[] { 25 }, mapper);

        assertTrue("应找到记录", !names.isEmpty());
        assertTrue(names.stream().allMatch(n -> n.startsWith("Result")));
    }

    /**
     * 测试内置 RowMapper - MappingRowMapper (Bean映射)
     */
    @Test
    public void testRowMapper_BeanMapping() throws Exception {
        RowMapper<UserInfo> mapper = new BeanMappingRowMapper<>(UserInfo.class);

        List<UserInfo> results = jdbcTemplate.queryForList("SELECT * FROM user_info WHERE id = ?",//
                new Object[] { 94002 }, mapper);

        assertEquals(1, results.size());
        UserInfo user = results.get(0);
        assertEquals(Integer.valueOf(94002), user.getId());
        assertEquals("Result2", user.getName());
        assertEquals(Integer.valueOf(22), user.getAge());
    }

    /**
     * 测试内置 RowMapper - MapMappingRowMapper
     */
    @Test
    public void testRowMapper_MapMapping() throws Exception {
        RowMapper<Map<String, Object>> mapper = new ColumnMapRowMapper();

        List<Map<String, Object>> results = jdbcTemplate.queryForList("SELECT * FROM user_info WHERE id = ?",//
                new Object[] { 94003 }, mapper);

        assertEquals(1, results.size());
        Map<String, Object> map = results.get(0);
        assertEquals(94003, map.get("id"));
        assertEquals("Result3", map.get("name"));
    }

    // ========== List/Map 结果集测试 ==========

    /**
     * 测试 queryForMap - 返回单条 Map
     */
    @Test
    public void testListMap_QueryForMap() throws Exception {
        Map<String, Object> result = jdbcTemplate.queryForMap("SELECT * FROM user_info WHERE id = ?",//
                new Object[] { 94004 });

        assertNotNull(result);
        assertEquals(94004, result.get("id"));
        assertEquals("Result4", result.get("name"));
        assertEquals(24, result.get("age"));
    }

    /**
     * 测试 queryForList - 返回 List<Map>
     */
    @Test
    public void testListMap_QueryForList() throws Exception {
        List<Map<String, Object>> results = jdbcTemplate.queryForList("SELECT * FROM user_info WHERE age BETWEEN ? AND ?",//
                new Object[] { 23, 26 });

        assertTrue("应找到记录", results.size() >= 4);
        for (Map<String, Object> map : results) {
            int age = (Integer) map.get("age");
            assertTrue(age >= 23 && age <= 26);
        }
    }

    /**
     * 测试 queryForObject - 返回单个值
     */
    @Test
    public void testListMap_QueryForObject() throws Exception {
        Integer age = jdbcTemplate.queryForObject("SELECT age FROM user_info WHERE id = ?",//
                new Object[] { 94005 }, Integer.class);

        assertEquals(Integer.valueOf(25), age);

        String name = jdbcTemplate.queryForObject("SELECT name FROM user_info WHERE id = ?",//
                new Object[] { 94005 }, String.class);

        assertEquals("Result5", name);
    }

    /**
     * 测试 queryForList - 返回单列值列表
     */
    @Test
    public void testListMap_QueryForSingleColumnList() throws Exception {
        List<Integer> ages = jdbcTemplate.queryForList("SELECT age FROM user_info WHERE age > ?",//
                new Object[] { 25 }, Integer.class);

        assertTrue("应找到记录", ages.size() > 0);
        assertTrue(ages.stream()//
                .allMatch(age -> age > 25));
    }

    // ========== RowCallbackHandler 测试 ==========

    /**
     * 测试 RowCallbackHandler - 基本用法
     */
    @Test
    public void testRowCallback_Basic() throws Exception {
        List<String> collectedNames = new ArrayList<>();

        RowCallbackHandler handler = (rs, rowNum) -> {
            collectedNames.add(rs.getString("name"));
        };

        jdbcTemplate.query("SELECT * FROM user_info WHERE age < ?",//
                new Object[] { 25 }, handler);

        assertTrue("应收集到记录", !collectedNames.isEmpty());
        assertTrue(collectedNames.stream()//
                .allMatch(n -> n.startsWith("Result")));
    }

    /**
     * 测试 RowCallbackHandler - 流式处理大数据
     */
    @Test
    public void testRowCallback_StreamProcessing() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        List<Integer> evenAges = new ArrayList<>();

        RowCallbackHandler handler = (rs, rowNum) -> {
            counter.incrementAndGet();
            int age = rs.getInt("age");
            if (age % 2 == 0) {
                evenAges.add(age);
            }
        };

        jdbcTemplate.query("SELECT * FROM user_info WHERE id BETWEEN ? AND ?",//
                new Object[] { 94001, 94010 }, handler);

        assertEquals(10, counter.get());
        assertTrue("应找到偶数年龄", !evenAges.isEmpty());
    }

    /**
     * 测试 RowCallbackHandler - 聚合计算
     */
    @Test
    public void testRowCallback_Aggregation() throws Exception {
        AtomicInteger totalAge = new AtomicInteger(0);
        AtomicInteger count = new AtomicInteger(0);

        RowCallbackHandler handler = (rs, rowNum) -> {
            totalAge.addAndGet(rs.getInt("age"));
            count.incrementAndGet();
        };

        jdbcTemplate.query("SELECT * FROM user_info WHERE id BETWEEN ? AND ?",//
                new Object[] { 94001, 94005 }, handler);

        assertEquals(5, count.get());
        int avgAge = totalAge.get() / count.get();
        assertEquals(23, avgAge);  // (21+22+23+24+25)/5 = 23
    }

    // ========== ResultSetExtractor 测试 ==========

    /**
     * 测试自定义 ResultSetExtractor - 构建 Map
     */
    @Test
    public void testExtractor_CustomMap() throws Exception {
        ResultSetExtractor<Map<Integer, String>> extractor = rs -> {
            Map<Integer, String> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getInt("id"), rs.getString("name"));
            }
            return map;
        };

        Map<Integer, String> result = jdbcTemplate.query("SELECT id, name FROM user_info WHERE id BETWEEN ? AND ?",//
                new Object[] { 94001, 94003 }, extractor);

        assertEquals(3, result.size());
        assertEquals("Result1", result.get(94001));
        assertEquals("Result2", result.get(94002));
        assertEquals("Result3", result.get(94003));
    }

    /**
     * 测试内置 ResultSetExtractor - ColumnMapResultSetExtractor
     */
    @Test
    public void testExtractor_ColumnMap() throws Exception {
        ResultSetExtractor<List<Map<String, Object>>> extractor = new ColumnMapResultSetExtractor();

        List<Map<String, Object>> results = jdbcTemplate.query("SELECT * FROM user_info WHERE age > ?",//
                new Object[] { 26 }, extractor);

        assertTrue("应找到记录", !results.isEmpty());
    }

    /**
     * 测试内置 ResultSetExtractor - RowMapperResultSetExtractor
     */
    @Test
    public void testExtractor_RowMapper() throws Exception {
        RowMapper<UserInfo> rowMapper = new BeanMappingRowMapper<>(UserInfo.class);
        ResultSetExtractor<List<UserInfo>> extractor = new RowMapperResultSetExtractor<>(rowMapper);

        List<UserInfo> results = jdbcTemplate.query("SELECT * FROM user_info WHERE id = ?",//
                new Object[] { 94006 }, extractor);

        assertEquals(1, results.size());
        assertEquals("Result6", results.get(0).getName());
    }

    /**
     * 测试内置 ResultSetExtractor - FilterResultSetExtractor
     */
    @Test
    public void testExtractor_Filter() throws Exception {
        RowMapper<UserInfo> rowMapper = new BeanMappingRowMapper<>(UserInfo.class);
        // 只保留年龄大于 24 的记录
        ResultSetExtractor<List<UserInfo>> extractor = //
                new FilterResultSetExtractor<>(rowMapper, user -> user.getAge() > 24);

        List<UserInfo> results = jdbcTemplate.query("SELECT * FROM user_info WHERE id BETWEEN ? AND ?",//
                new Object[] { 94001, 94010 }, extractor);

        assertTrue("应找到记录", !results.isEmpty());
        assertTrue(results.stream().allMatch(u -> u.getAge() > 24));
    }

    /**
     * 测试内置 ResultSetExtractor - PairsResultSetExtractor
     */
    @Test
    public void testExtractor_Pairs() throws Exception {
        ResultSetExtractor<Map<Integer, String>> extractor = //
                new PairsResultSetExtractor<>(TypeHandlerRegistry.DEFAULT, Integer.class, String.class);

        Map<Integer, String> result = jdbcTemplate.query("SELECT id, name FROM user_info WHERE id BETWEEN ? AND ?",//
                new Object[] { 94007, 94009 }, extractor);

        assertEquals(3, result.size());
        assertEquals("Result7", result.get(94007));
        assertEquals("Result8", result.get(94008));
        assertEquals("Result9", result.get(94009));
    }

    /**
     * 测试内置 ResultSetExtractor - BeanMappingResultSetExtractor
     */
    @Test
    public void testExtractor_BeanMapping() throws Exception {
        ResultSetExtractor<List<UserInfo>> extractor = //
                new BeanMappingResultSetExtractor<>(UserInfo.class, MappingRegistry.DEFAULT);

        List<UserInfo> results = jdbcTemplate.query("SELECT * FROM user_info WHERE age = ?",//
                new Object[] { 25 }, extractor);

        assertEquals(1, results.size());
        assertEquals("Result5", results.get(0).getName());
    }

    /**
     * 测试内置 ResultSetExtractor - MapMappingResultSetExtractor
     */
    @Test
    public void testExtractor_MapMapping() throws Exception {
        ResultSetExtractor<List<Map<String, Object>>> extractor = //
                new MapMappingResultSetExtractor(UserInfo.class, MappingRegistry.DEFAULT);

        List<Map<String, Object>> results = jdbcTemplate.query("SELECT * FROM user_info WHERE id = ?",//
                new Object[] { 94010 }, extractor);

        assertEquals(1, results.size());
        assertEquals(94010, results.get(0).get("id"));
    }

    // ========== 复杂场景测试 ==========

    /**
     * 测试混合使用 - RowMapper + queryForList
     */
    @Test
    public void testComplex_MixedUsage() throws Exception {
        // 自定义 DTO
        class UserDTO {
            String nameAge;

            String getNameAge() {
                return nameAge;
            }

            void setNameAge(String nameAge) {
                this.nameAge = nameAge;
            }
        }

        RowMapper<UserDTO> mapper = (rs, rowNum) -> {
            UserDTO dto = new UserDTO();
            dto.setNameAge(rs.getString("name") + ":" + rs.getInt("age"));
            return dto;
        };

        List<UserDTO> results = jdbcTemplate.queryForList("SELECT * FROM user_info WHERE id BETWEEN ? AND ?",//
                new Object[] { 94001, 94003 }, mapper);

        assertEquals(3, results.size());
        assertEquals("Result1:21", results.get(0).getNameAge());
    }
}
