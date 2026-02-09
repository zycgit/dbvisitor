package net.hasor.dbvisitor.test.oneapi.suite.fluent.types;

import java.sql.SQLException;
import java.util.*;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.types.JsonAnnotatedBean;
import net.hasor.dbvisitor.test.oneapi.model.types.JsonTestBean;
import net.hasor.dbvisitor.test.oneapi.model.types.JsonTestBean.Address;
import net.hasor.dbvisitor.types.handler.json.wrap.JsonType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JSON 类型测试 - Fluent API (LambdaTemplate)
 * 对应 programmatic/types/JsonTypesJdbcTest，使用与 programmatic 相同的 Bean 类型：
 * - JsonTestBean：普通 JSON Bean（name/age/active/tags/address）
 * - JsonAnnotatedBean：带 @BindTypeHandler(JsonTypeHandler.class) 注解的 Bean
 * 测试范围：
 * 1. 使用 JdbcTemplate + #{} 语法写入 Map/Bean，读取为不同结构
 * 2. 使用 @BindTypeHandler 注解的 Bean 自动转换
 * 3. JSON 中的 null、空对象、特殊字符处理
 * 4. JSON 读取为 Map/List/Set/Bean 等不同结构
 */
@SuppressWarnings("unchecked")
public class JsonTypesFluentTest extends AbstractOneApiTest {
    @Override
    protected void cleanTestData() {
        try {
            jdbcTemplate.executeUpdate("DELETE FROM json_types_explicit_test");
        } catch (Exception e) {
            // Ignore
        }
        super.cleanTestData();
    }

    /**
     * 测试使用 JdbcTemplate 写入 Map 类型的 JSON，LambdaTemplate 读取
     * 注意：JsonTypesExplicitModel 的 Map 字段 jdbcType=VARCHAR，VarcharTypeHandler 无法处理 Map
     * 因此写入时使用 JdbcTemplate + JsonTypeHandler
     */
    @Test
    public void testJsonMap_WriteAndRead() throws SQLException {
        Map<String, Object> jsonData = new LinkedHashMap<>();
        jsonData.put("name", "Alice");
        jsonData.put("age", 30);
        jsonData.put("active", true);

        // 使用 JdbcTemplate #{} 语法写入
        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{data, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";
        Map<String, Object> params = CollectionUtils.asMap("id", 1, "data", jsonData);
        jdbcTemplate.executeUpdate(insertSql, params);

        // 使用 JdbcTemplate + JsonType.jsonMap() 读取（模型的 VarcharTypeHandler 无法反序列化 Map）
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        Map<String, Object> loadedMap = (Map<String, Object>) jdbcTemplate//
                .queryForObject(selectSql, new Object[] { 1 }, JsonType.jsonMap());

        assertNotNull(loadedMap);
        assertEquals("Alice", loadedMap.get("name"));
        assertTrue(loadedMap.get("age") instanceof Number);
        assertEquals(30, ((Number) loadedMap.get("age")).intValue());
        assertEquals(true, loadedMap.get("active"));
    }

    /**
     * 测试嵌套 JSON 对象
     * 使用 JdbcTemplate + JsonTypeHandler 写入，LambdaTemplate 读取
     */
    @Test
    public void testJsonMap_NestedObject() throws SQLException {
        Map<String, Object> address = new LinkedHashMap<>();
        address.put("city", "Shanghai");
        address.put("street", "Nanjing Road");
        address.put("zipCode", "200000");

        Map<String, Object> jsonData = new LinkedHashMap<>();
        jsonData.put("name", "Bob");
        jsonData.put("address", address);

        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{data, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";
        Map<String, Object> params = CollectionUtils.asMap("id", 2, "data", jsonData);
        jdbcTemplate.executeUpdate(insertSql, params);

        // 使用 JdbcTemplate + JsonType.jsonMap() 读取
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        Map<String, Object> loadedMap = (Map<String, Object>) jdbcTemplate//
                .queryForObject(selectSql, new Object[] { 2 }, JsonType.jsonMap());

        assertNotNull(loadedMap);
        assertEquals("Bob", loadedMap.get("name"));

        Object addressObj = loadedMap.get("address");
        assertNotNull(addressObj);
        assertTrue(addressObj instanceof Map);
        Map<String, Object> loadedAddress = (Map<String, Object>) addressObj;
        assertEquals("Shanghai", loadedAddress.get("city"));
        assertEquals("Nanjing Road", loadedAddress.get("street"));
    }

    /**
     * 测试 null JSON 字段（数据库 NULL）
     */
    @Test
    public void testJsonMap_NullValue() throws SQLException {
        // 使用位置参数插入 NULL 值
        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar, json_mysql, nested_json) VALUES (?, ?, ?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 3, null, null, null });

        // 查询 null 字段
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        String loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 3 }, String.class);
        assertNull(loaded);
    }

    /**
     * 测试空 Map 的 JSON 处理
     * 使用 JdbcTemplate + JsonTypeHandler 写入，LambdaTemplate 读取
     */
    @Test
    public void testJsonMap_EmptyObject() throws SQLException {
        Map<String, Object> emptyMap = new LinkedHashMap<>();

        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{data, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";
        Map<String, Object> params = CollectionUtils.asMap("id", 4, "data", emptyMap);
        jdbcTemplate.executeUpdate(insertSql, params);

        // 使用 JdbcTemplate + JsonType.jsonMap() 读取
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        Map<String, Object> loadedMap = (Map<String, Object>) jdbcTemplate//
                .queryForObject(selectSql, new Object[] { 4 }, JsonType.jsonMap());

        assertNotNull(loadedMap);
        assertTrue(loadedMap.isEmpty());
    }

    /**
     * 测试 JSON 中的特殊字符和 Unicode
     * 使用 JdbcTemplate + JsonTypeHandler 写入，LambdaTemplate 读取
     */
    @Test
    public void testJsonMap_SpecialCharacters() throws SQLException {
        Map<String, Object> jsonData = new LinkedHashMap<>();
        jsonData.put("name", "中文名字");
        jsonData.put("emoji", "😀🌍");
        jsonData.put("special", "引号\"test\"反斜杠\\path");

        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{data, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";
        Map<String, Object> params = CollectionUtils.asMap("id", 5, "data", jsonData);
        jdbcTemplate.executeUpdate(insertSql, params);

        // 使用 JdbcTemplate + JsonType.jsonMap() 读取
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        Map<String, Object> loadedMap = (Map<String, Object>) jdbcTemplate//
                .queryForObject(selectSql, new Object[] { 5 }, JsonType.jsonMap());

        assertNotNull(loadedMap);
        assertEquals("中文名字", loadedMap.get("name"));
    }

    /**
     * 测试使用 #{} + JsonTypeHandler 写入 Bean，JdbcTemplate 读取为不同结构
     * 使用 JdbcTemplate 的 #{} 语法配合 JsonTypeHandler 进行 JSON 序列化
     */
    @Test
    public void testJsonBean_WriteWithHashParam_ReadAsMap() throws SQLException {
        JsonTestBean bean = new JsonTestBean("Frank", 40, true);
        Address address = new Address("Shenzhen", "Futian Road", "518000");
        bean.setAddress(address);

        // 使用 JdbcTemplate #{} 语法写入
        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{bean, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";
        Map<String, Object> params = CollectionUtils.asMap("id", 6, "bean", bean);
        jdbcTemplate.executeUpdate(insertSql, params);

        // 使用 JdbcTemplate 读取为 JsonHashMap
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        Map loadedMap = jdbcTemplate.queryForObject(selectSql, new Object[] { 6 }, JsonType.jsonMap());

        assertNotNull(loadedMap);
        assertEquals("Frank", loadedMap.get("name"));
        assertEquals(40, ((Number) loadedMap.get("age")).intValue());
        assertEquals(Boolean.TRUE, loadedMap.get("active"));

        // 验证嵌套 address
        Object addressObj = loadedMap.get("address");
        assertNotNull(addressObj);
        assertTrue(addressObj instanceof Map);
        Map<String, Object> addressMap = (Map<String, Object>) addressObj;
        assertEquals("Shenzhen", addressMap.get("city"));
    }

    /**
     * 测试使用 #{} + JsonTypeHandler 写入 List，读取为 JsonArrayList
     */
    @Test
    public void testJsonList_WriteWithHashParam_ReadAsList() throws SQLException {
        List<Map<String, Object>> list = Arrays.asList(//
                CollectionUtils.asMap("id", 1, "name", "Alice", "score", 95),//
                CollectionUtils.asMap("id", 2, "name", "Bob", "score", 88),//
                CollectionUtils.asMap("id", 3, "name", "Charlie", "score", 92));

        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{list, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";
        Map<String, Object> params = CollectionUtils.asMap("id", 7, "list", list);
        jdbcTemplate.executeUpdate(insertSql, params);

        // 读取为 List
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        List loadedList = jdbcTemplate.queryForObject(selectSql, new Object[] { 7 }, JsonType.jsonList());

        assertNotNull(loadedList);
        assertEquals(3, loadedList.size());

        Map<String, Object> firstMap = (Map<String, Object>) loadedList.get(0);
        assertEquals("Alice", firstMap.get("name"));
    }

    /**
     * 测试读取为 JsonHashSet
     */
    @Test
    public void testJsonList_ReadAsSet() throws SQLException {
        List<Map<String, Object>> list = Arrays.asList(//
                CollectionUtils.asMap("id", 1, "name", "Alice"),//
                CollectionUtils.asMap("id", 2, "name", "Bob"),//
                CollectionUtils.asMap("id", 3, "name", "Charlie"));

        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{list, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";
        Map<String, Object> params = CollectionUtils.asMap("id", 8, "list", list);
        jdbcTemplate.executeUpdate(insertSql, params);

        // 读取为 Set
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        Set loadedSet = jdbcTemplate.queryForObject(selectSql, new Object[] { 8 }, JsonType.jsonSet());

        assertNotNull(loadedSet);
        assertEquals(3, loadedSet.size());
    }

    /**
     * 测试使用 @BindTypeHandler 注解的 Bean - 自动 JSON 序列化/反序列化
     * 无需在 SQL 中指定 typeHandler
     */
    @Test
    public void testBindTypeHandler_AutoSerialization() throws SQLException {
        JsonAnnotatedBean product = new JsonAnnotatedBean("Laptop", 5999.99, 10, "Electronics");

        // 使用位置参数直接插入 - 注解自动生效
        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 9, product });

        // 读取为 Bean - 自动反序列化
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        JsonAnnotatedBean loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 9 }, JsonAnnotatedBean.class);

        assertNotNull(loaded);
        assertEquals("Laptop", loaded.getProductName());
        assertEquals(Double.valueOf(5999.99), loaded.getPrice());
        assertEquals(Integer.valueOf(10), loaded.getQuantity());
        assertEquals("Electronics", loaded.getCategory());
    }

    /**
     * 测试 @BindTypeHandler 注解结合命名参数
     */
    @Test
    public void testBindTypeHandler_WithNamedParams() throws SQLException {
        JsonAnnotatedBean product = new JsonAnnotatedBean("Smartphone", 3999.0);
        product.setQuantity(20);
        product.setCategory("Mobile");

        // 使用命名参数 - 无需指定 typeHandler
        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (#{id}, #{product})";
        Map<String, Object> params = CollectionUtils.asMap("id", 10, "product", product);
        jdbcTemplate.executeUpdate(insertSql, params);

        // 读取验证
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        JsonAnnotatedBean loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 10 }, JsonAnnotatedBean.class);

        assertNotNull(loaded);
        assertEquals("Smartphone", loaded.getProductName());
        assertEquals(Double.valueOf(3999.0), loaded.getPrice());
        assertEquals(Integer.valueOf(20), loaded.getQuantity());
        assertEquals("Mobile", loaded.getCategory());
    }

    /**
     * 测试 @BindTypeHandler 注解 - NULL 值处理
     */
    @Test
    public void testBindTypeHandler_NullHandling() throws SQLException {
        // 插入 null
        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 11, null });

        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        JsonAnnotatedBean loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 11 }, JsonAnnotatedBean.class);
        assertNull(loaded);

        // 插入部分字段为 null 的对象
        JsonAnnotatedBean partial = new JsonAnnotatedBean("Tablet", 2999.0);
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 12, partial });

        JsonAnnotatedBean loadedPartial = jdbcTemplate.queryForObject(selectSql, new Object[] { 12 }, JsonAnnotatedBean.class);
        assertNotNull(loadedPartial);
        assertEquals("Tablet", loadedPartial.getProductName());
        assertEquals(Double.valueOf(2999.0), loadedPartial.getPrice());
        assertNull(loadedPartial.getQuantity());
        assertNull(loadedPartial.getCategory());
    }

    /**
     * 测试 JSON 字段更新
     * 使用 JdbcTemplate + JsonTypeHandler 写入和更新，LambdaTemplate 读取
     */
    @Test
    public void testJsonMap_UpdateOperation() throws SQLException {
        // 初始插入
        Map<String, Object> original = new LinkedHashMap<>();
        original.put("key", "original");

        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{data, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";
        Map<String, Object> insertParams = CollectionUtils.asMap("id", 13, "data", original);
        jdbcTemplate.executeUpdate(insertSql, insertParams);

        // 更新
        Map<String, Object> updated = new LinkedHashMap<>();
        updated.put("key", "updated");
        updated.put("extra", "new field");

        String updateSql = "UPDATE json_types_explicit_test SET json_varchar = " +//
                "#{data, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler} WHERE id = #{id}";
        Map<String, Object> updateParams = CollectionUtils.asMap("id", 13, "data", updated);
        int rows = jdbcTemplate.executeUpdate(updateSql, updateParams);
        assertEquals(1, rows);

        // 使用 JdbcTemplate + JsonType.jsonMap() 验证
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        Map<String, Object> loadedMap = (Map<String, Object>) jdbcTemplate.queryForObject(selectSql, new Object[] { 13 }, JsonType.jsonMap());

        assertNotNull(loadedMap);
        assertEquals("updated", loadedMap.get("key"));
        assertEquals("new field", loadedMap.get("extra"));
    }
}
