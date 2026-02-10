package net.hasor.dbvisitor.test.oneapi.suite.programmatic.types;
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
 * JSON 类型测试 - Programmatic API (JdbcTemplate)
 * 测试范围：
 * 1. 使用 JsonTypeHandler 自动序列化/反序列化 JSON（框架自动选择）
 * 2. 测试简单对象、嵌套对象、数组的 JSON 转换
 * 3. 测试 null 值处理
 * 4. 验证 TypeHandler 体系的 JSON 支持
 * 数据库初始化：通过 /oneapi/sql/{dialect}/init.sql 脚本自动创建 json_types_explicit_test 表
 * 重点：通过 #{field, typeHandler=...} 语法利用 TypeHandler 进行 JSON 序列化和反序列化
 * 支持的 JSON 框架（由 JsonTypeHandler 自动选择）：
 * - Jackson (com.fasterxml.jackson.databind.ObjectMapper)
 * - Gson (com.google.gson.Gson)
 * - Fastjson (com.alibaba.fastjson.JSON)
 * - Fastjson2 (com.alibaba.fastjson2.JSON)
 * 后续扩展计划：
 * - MongoDB BSON 类型支持 (BsonTypeHandler, BsonListTypeHandler)
 * - PostgreSQL JSONB 类型支持（数据库特定功能）
 * - JSONB 是 PostgreSQL 特有的二进制 JSON 类型，提供更高效的存储和索引
 * - 计划在数据库方言层面添加 JSONB 专用支持
 */
public class JsonTypesJdbcTest extends AbstractOneApiTest {

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
     * 测试简单对象的 JSON 序列化和反序列化
     * 使用 JsonTypeHandler 自动处理 JSON 转换
     */
    @Test
    public void testJsonWrite_Object() throws SQLException {
        // 创建测试对象
        JsonTestBean bean = new JsonTestBean("Alice", 30, true);

        // 使用 #{} 语法配合 TypeHandler 进行 JSON 序列化
        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{bean, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";

        Map<String, Object> params = CollectionUtils.asMap("id", 1, "bean", bean);

        jdbcTemplate.executeUpdate(insertSql, params);

        // 读取 JSON 字符串验证序列化成功
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        String jsonString = jdbcTemplate.queryForObject(selectSql, new Object[] { 1 }, String.class);

        assertNotNull(jsonString);
        // 验证 JSON 包含预期的字段
        assertTrue(jsonString.contains("Alice"));
        assertTrue(jsonString.contains("30"));
    }

    /**
     * 测试 null 值处理 - 数据库 NULL
     */
    @Test
    public void testJsonWrite_NullValue() throws SQLException {
        // 使用位置参数插入 NULL 值（避免 typeHandler 处理 NULL 时的问题）
        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar, json_mysql, nested_json) VALUES (?, ?, ?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 3, null, null, null });

        // 查询 null 字段
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        String loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 3 }, String.class);

        assertNull(loaded);
    }

    /**
     * 测试 JSON 内部的 null 字段
     */
    @Test
    public void testJsonWrite_NullFields() throws SQLException {
        // 创建包含 null 字段的对象
        JsonTestBean bean = new JsonTestBean();
        bean.setName("Charlie");
        bean.setAge(null); // age 为 null
        bean.setActive(false);
        // tags 和 address 为 null

        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{bean, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";

        Map<String, Object> params = CollectionUtils.asMap("id", 4, "bean", bean);

        jdbcTemplate.executeUpdate(insertSql, params);

        // 查询 JSON 字符串验证
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        String jsonString = jdbcTemplate.queryForObject(selectSql, new Object[] { 4 }, String.class);

        assertNotNull(jsonString);
        assertTrue(jsonString.contains("Charlie"));
        // 验证包含 false 值
        assertTrue(jsonString.contains("false") || jsonString.contains("active"));
    }

    /**
     * 测试特殊字符和 Unicode
     */
    @Test
    public void testJsonWrite_SpecialCharacters() throws SQLException {
        JsonTestBean bean = new JsonTestBean();
        bean.setName("中文名字");
        bean.setAge(28);
        bean.setActive(true);
        bean.setTags(Arrays.asList("emoji😀", "引号\"test\"", "反斜杠\\path"));

        Address address = new Address("上海", "南京路123号", "200000");
        bean.setAddress(address);

        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{bean, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";

        Map<String, Object> params = CollectionUtils.asMap("id", 5, "bean", bean);

        jdbcTemplate.executeUpdate(insertSql, params);

        // 查询 JSON 字符串验证特殊字符处理
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        String jsonString = jdbcTemplate.queryForObject(selectSql, new Object[] { 5 }, String.class);

        assertNotNull(jsonString);
        // 验证中文和特殊字符
        assertTrue(jsonString.contains("中文名字") || jsonString.contains("\\u4e2d"));
        assertTrue(jsonString.contains("上海") || jsonString.contains("\\u4e0a"));
        assertTrue(jsonString.contains("28"));
    }

    /**
     * 测试空对象的 JSON 处理
     */
    @Test
    public void testJsonWrite_EmptyObject() throws SQLException {
        // 创建空对象（所有字段都是默认值）
        JsonTestBean bean = new JsonTestBean();

        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{bean, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";

        Map<String, Object> params = CollectionUtils.asMap("id", 6, "bean", bean);

        jdbcTemplate.executeUpdate(insertSql, params);

        // 查询 JSON 字符串
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        String jsonString = jdbcTemplate.queryForObject(selectSql, new Object[] { 6 }, String.class);

        assertNotNull(jsonString);
        // 验证是有效的 JSON（至少是 {} 或包含 null  值）
        assertTrue(jsonString.contains("{") && jsonString.contains("}"));
    }

    /**
     * 测试查询为 Map 时的 JSON 字符串格式
     */
    @Test
    public void testJsonRead_AsString() throws SQLException {
        JsonTestBean bean = new JsonTestBean("Eve", 22, false);

        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{bean, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";

        Map<String, Object> params = CollectionUtils.asMap("id", 8, "bean", bean);

        jdbcTemplate.executeUpdate(insertSql, params);

        // 查询为 String 查看 JSON 格式
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        String jsonString = jdbcTemplate.queryForObject(selectSql, new Object[] { 8 }, String.class);

        assertNotNull(jsonString);
        // 验证是有效的 JSON 格式
        assertTrue(jsonString.contains("\"name\"") || jsonString.contains("\"Eve\""));
        assertTrue(jsonString.contains("22") || jsonString.contains("\"age\""));
    }

    /**
     * 测试读取为 JsonHashMap 结构
     * 使用 JsonHashMap 包装类，支持直接通过 queryForObject 读取 JSON 为 Map
     */
    @Test
    public void testJsonRead_AsMap() throws SQLException {
        JsonTestBean bean = new JsonTestBean("Frank", 40, true);
        Address address = new Address("Shenzhen", "Futian Road", "518000");
        bean.setAddress(address);

        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{bean, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";

        Map<String, Object> params = CollectionUtils.asMap("id", 9, "bean", bean);
        jdbcTemplate.executeUpdate(insertSql, params);

        // 读取为 JsonHashMap
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        Map loadedMap = jdbcTemplate.queryForObject(selectSql, new Object[] { 9 }, JsonType.jsonMap());

        assertTrue(loadedMap instanceof HashMap);
        assertNotNull(loadedMap);
        assertEquals("Frank", loadedMap.get("name"));
        // age 可能是 Integer 或 Double，取决于 JSON 库的解析方式
        Object ageValue = loadedMap.get("age");
        assertNotNull(ageValue);
        assertTrue(ageValue instanceof Number);
        assertEquals(40, ((Number) ageValue).intValue());
        assertEquals(Boolean.TRUE, loadedMap.get("active"));

        // 验证嵌套的 address Map
        Object addressObj = loadedMap.get("address");
        assertNotNull(addressObj);
        assertTrue(addressObj instanceof Map);
        @SuppressWarnings("unchecked") Map<String, Object> addressMap = (Map<String, Object>) addressObj;
        assertEquals("Shenzhen", addressMap.get("city"));
        assertEquals("Futian Road", addressMap.get("street"));
    }

    /**
     * 测试读取为 JsonArrayList 结构
     * 使用 JsonArrayList 包装类，支持直接通过 queryForObject 读取 JSON 数组为 List
     */
    @Test
    public void testJsonRead_AsList() throws SQLException {
        // 准备 List<Map> 数据
        List<Map<String, Object>> list = Arrays.asList(CollectionUtils.asMap("id", 1, "name", "Alice", "score", 95), CollectionUtils.asMap("id", 2, "name", "Bob", "score", 88), CollectionUtils.asMap("id", 3, "name", "Charlie", "score", 92));

        // 插入 List 数据
        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{list, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";

        Map<String, Object> params = CollectionUtils.asMap("id", 10, "list", list);
        jdbcTemplate.executeUpdate(insertSql, params);

        // 读取为 JsonArrayList
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        List loadedList = jdbcTemplate.queryForObject(selectSql, new Object[] { 10 }, JsonType.jsonList());

        assertTrue(loadedList instanceof ArrayList);
        assertNotNull(loadedList);
        assertEquals(3, loadedList.size());

        // 验证第一个元素
        Object firstElement = loadedList.get(0);
        assertNotNull(firstElement);
        assertTrue(firstElement instanceof Map);
        @SuppressWarnings("unchecked") Map<String, Object> firstMap = (Map<String, Object>) firstElement;
        assertEquals("Alice", firstMap.get("name"));

        // 验证第二个元素
        @SuppressWarnings("unchecked") Map<String, Object> secondMap = (Map<String, Object>) loadedList.get(1);
        assertEquals("Bob", secondMap.get("name"));
        Object scoreValue = secondMap.get("score");
        assertTrue(scoreValue instanceof Number);
        assertEquals(88, ((Number) scoreValue).intValue());
    }

    /**
     * 测试读取为 JsonHashSet 结构
     * 使用 JsonHashSet 包装类，支持直接通过 queryForObject 读取 JSON 数组为 Set
     */
    @Test
    public void testJsonRead_AsSet() throws SQLException {
        // 准备 List<Map> 数据
        List<Map<String, Object>> list = Arrays.asList(CollectionUtils.asMap("id", 1, "name", "Alice", "score", 95), CollectionUtils.asMap("id", 2, "name", "Bob", "score", 88), CollectionUtils.asMap("id", 3, "name", "Charlie", "score", 92));

        // 插入 List 数据
        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{list, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";

        Map<String, Object> params = CollectionUtils.asMap("id", 12, "list", list);
        jdbcTemplate.executeUpdate(insertSql, params);

        // 读取为 JsonArrayList
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        Set loadedSet = jdbcTemplate.queryForObject(selectSql, new Object[] { 12 }, JsonType.jsonSet());

        assertNotNull(loadedSet);
        assertEquals(3, loadedSet.size());

        // 验证包含元素（根据 name 字段匹配）
        boolean foundAlice = false;
        boolean foundBob = false;
        for (Object elem : loadedSet) {
            assertNotNull(elem);
            assertTrue(elem instanceof Map);
            @SuppressWarnings("unchecked") Map<String, Object> map = (Map<String, Object>) elem;
            Object name = map.get("name");
            if ("Alice".equals(name)) {
                foundAlice = true;
            } else if ("Bob".equals(name)) {
                foundBob = true;
                Object scoreValue = map.get("score");
                assertTrue(scoreValue instanceof Number);
                assertEquals(88, ((Number) scoreValue).intValue());
            }
        }
        assertTrue(foundAlice);
        assertTrue(foundBob);
    }

    /**
     * 测试读取为 List<Bean> 结构
     */
    @Test
    public void testJsonRead_AsBean() throws SQLException {
        // 准备 List<JsonTestBean> 数据
        List<JsonTestBean> list = Arrays.asList(//
                new JsonTestBean("George", 29, true), //
                new JsonTestBean("Helen", 31, false), //
                new JsonTestBean("Ivan", 27, true));

        // 插入 List 数据
        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) " +//
                "VALUES (#{id}, #{list, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})";

        Map<String, Object> params = CollectionUtils.asMap("id", 11, "list", list);
        jdbcTemplate.executeUpdate(insertSql, params);

        // 读取为 List - 使用 JsonArrayList 作为具体类型
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        List loadedList = jdbcTemplate.queryForObject(selectSql, new Object[] { 11 }, JsonType.jsonList());

        assertTrue(loadedList instanceof ArrayList);
        assertNotNull(loadedList);
        assertEquals(3, loadedList.size());

        // 验证元素类型和内容
        Object firstElement = loadedList.get(0);
        assertNotNull(firstElement);
        // 可能是 JsonTestBean 或 Map，取决于 TypeHandler 的实现
        if (firstElement instanceof JsonTestBean) {
            JsonTestBean firstBean = (JsonTestBean) firstElement;
            assertEquals("George", firstBean.getName());
            assertEquals(Integer.valueOf(29), firstBean.getAge());
        } else if (firstElement instanceof Map) {
            @SuppressWarnings("unchecked") Map<String, Object> firstMap = (Map<String, Object>) firstElement;
            assertEquals("George", firstMap.get("name"));
        } else {
            fail("Unexpected element type: " + firstElement.getClass());
        }
    }

    /**
     * 测试使用 @BindTypeHandler 注解的 Bean - 自动 JSON 序列化/反序列化
     * <p>场景：通过在 Bean 类上添加 @BindTypeHandler(JsonTypeHandler.class) 注解，
     * 无需在 SQL 中手动指定 typeHandler，框架会自动使用 JsonTypeHandler 进行转换。</p>
     * <p>优势：</p>
     * <ul>
     *   <li>简化 SQL 编写，无需 #{bean, typeHandler=...} 语法</li>
     *   <li>代码更简洁，TypeHandler 配置集中在 Bean 定义处</li>
     *   <li>提高可维护性，Bean 的序列化方式一目了然</li>
     * </ul>
     */
    @Test
    public void testJsonTypeHandler_WithBindAnnotation() throws SQLException {
        // 创建带有 @BindTypeHandler 注解的测试对象
        JsonAnnotatedBean product = new JsonAnnotatedBean("Laptop", 5999.99, 10, "Electronics");

        // 插入数据 - 无需在 SQL 中指定 typeHandler，注解会自动生效
        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 100, product });

        // 查询 JSON 字符串，验证序列化成功
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";

        // 读取为 Bean 对象 - 自动反序列化
        JsonAnnotatedBean loadedProduct = jdbcTemplate.queryForObject(selectSql, new Object[] { 100 }, JsonAnnotatedBean.class);

        assertNotNull("Loaded product should not be null", loadedProduct);
        assertEquals("Product name should match", "Laptop", loadedProduct.getProductName());
        assertEquals("Price should match", Double.valueOf(5999.99), loadedProduct.getPrice());
        assertEquals("Quantity should match", Integer.valueOf(10), loadedProduct.getQuantity());
        assertEquals("Category should match", "Electronics", loadedProduct.getCategory());
    }

    /**
     * 测试 @BindTypeHandler 注解 - 使用命名参数方式
     * <p>展示注解方式与命名参数 #{} 语法的结合使用</p>
     */
    @Test
    public void testJsonTypeHandler_WithBindAnnotation_NamedParams() throws SQLException {
        // 创建测试对象
        JsonAnnotatedBean product = new JsonAnnotatedBean("Smartphone", 3999.0);
        product.setQuantity(20);
        product.setCategory("Mobile");

        // 使用命名参数 - 无需指定 typeHandler
        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (#{id}, #{product})";
        Map<String, Object> params = CollectionUtils.asMap("id", 101, "product", product);
        jdbcTemplate.executeUpdate(insertSql, params);

        // 查询验证
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        JsonAnnotatedBean loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 101 }, JsonAnnotatedBean.class);

        assertNotNull(loaded);
        assertEquals("Smartphone", loaded.getProductName());
        assertEquals(Double.valueOf(3999.0), loaded.getPrice());
        assertEquals(Integer.valueOf(20), loaded.getQuantity());
        assertEquals("Mobile", loaded.getCategory());
    }

    /**
     * 测试 @BindTypeHandler 注解 - NULL 值处理
     * <p>验证带注解的 Bean 在处理 NULL 值时的行为</p>
     */
    @Test
    public void testJsonTypeHandler_WithBindAnnotation_NullHandling() throws SQLException {
        // 测试插入 null
        String insertSql = "INSERT INTO json_types_explicit_test (id, json_varchar) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 102, null });

        // 查询 null
        String selectSql = "SELECT json_varchar FROM json_types_explicit_test WHERE id = ?";
        JsonAnnotatedBean loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 102 }, JsonAnnotatedBean.class);
        assertNull("Null JSON should result in null Bean", loaded);

        // 测试部分字段为 null 的对象
        JsonAnnotatedBean partialProduct = new JsonAnnotatedBean("Tablet", 2999.0);
        // quantity 和 category 为 null

        jdbcTemplate.executeUpdate(insertSql, new Object[] { 103, partialProduct });
        JsonAnnotatedBean loadedPartial = jdbcTemplate.queryForObject(selectSql, new Object[] { 103 }, JsonAnnotatedBean.class);

        assertNotNull(loadedPartial);
        assertEquals("Tablet", loadedPartial.getProductName());
        assertEquals(Double.valueOf(2999.0), loadedPartial.getPrice());
        assertNull("Quantity should be null", loadedPartial.getQuantity());
        assertNull("Category should be null", loadedPartial.getCategory());
    }
}
