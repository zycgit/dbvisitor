package net.hasor.dbvisitor.test.suite.programmatic.types;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.array.ArrayTypeHandler;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 数组类型测试 - Programmatic API (JdbcTemplate)
 * 使用 JdbcTemplate 的原生 API 测试数组类型的 CRUD 操作
 * 测试重点：
 * 1. 使用 java.sql.Array 和 Connection.createArrayOf()
 * 2. 使用 SqlArg 和 ArrayTypeHandler 显式控制类型映射
 * 3. 使用 queryForObject 直接获取强类型数组结果
 * 数据库初始化：通过 /sql/{dialect}/init.sql 脚本自动创建 array_types_test 表
 */
public class ArrayTypesJdbcTest extends AbstractOneApiTest {

    /**
     * 测试场景1：使用 Connection.createArrayOf() 创建 SQL Array
     * 这是标准 JDBC 的数组操作方式
     */
    @Test
    public void testIntegerArray_JavaSqlArray() throws SQLException {
        System.out.println("\n[TEST] 场景1：使用 java.sql.Array 进行数组操作");

        Integer[] testArray = { 10, 20, 30, 40, 50 };

        // 使用 Connection 创建 SQL Array
        Connection conn = dataSource.getConnection();
        try {
            Array sqlArray = conn.createArrayOf("INTEGER", testArray);

            // 插入数据 - 使用位置参数
            String insertSql = "INSERT INTO array_types_test (id, int_array) VALUES (?, ?)";
            int rows = jdbcTemplate.executeUpdate(insertSql, new Object[] { 1, sqlArray });
            assertEquals(1, rows);

            // 查询数据 - 使用 queryForObject 直接获取 Integer[] 类型
            String selectSql = "SELECT int_array FROM array_types_test WHERE id = ?";
            Integer[] loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 1 }, Integer[].class);

            assertNotNull(loaded);
            assertEquals(testArray.length, loaded.length);
            assertArrayEquals(testArray, loaded);

            System.out.println("[SUCCESS] 场景1 通过：java.sql.Array 操作成功");
        } finally {
            conn.close();
        }
    }

    /**
     * 测试场景2：使用 SqlArg 和 ArrayTypeHandler
     * 这是 dbVisitor 推荐的方式，自动处理类型转换
     */
    @Test
    public void testIntegerArray_WithSqlArg() throws SQLException {
        System.out.println("\n[TEST] 场景2：使用 SqlArg + ArrayTypeHandler");

        Integer[] testArray = { 100, 200, 300 };

        // 插入 - 使用 SqlArg 包装数组
        String insertSql = "INSERT INTO array_types_test (id, int_array) VALUES (?, ?)";
        SqlArg arrayArg = new SqlArg(testArray, Types.ARRAY, new ArrayTypeHandler());

        int rows = jdbcTemplate.executeUpdate(insertSql, new Object[] { 2, arrayArg });
        assertEquals(1, rows);

        // 查询 - 使用 queryForObject 直接获取 Integer[] 类型
        String selectSql = "SELECT int_array FROM array_types_test WHERE id = ?";
        Integer[] loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 2 }, Integer[].class);

        assertNotNull(loaded);
        assertEquals(testArray.length, loaded.length);
        assertArrayEquals(testArray, loaded);

        System.out.println("[SUCCESS] 场景2 通过：SqlArg 自动类型转换成功");
    }

    /**
     * 测试场景3：使用命名参数（Map）
     * 结合 dbVisitor 的命名参数支持和 SqlArg
     */
    @Test
    public void testStringArray_WithNamedParameters() throws SQLException {
        System.out.println("\n[TEST] 场景3：使用命名参数 + 数组");

        String[] testArray = { "Hello", "World", "Array", "Test" };

        // 插入 - 使用命名参数
        String insertSql = "INSERT INTO array_types_test (id, string_array) VALUES (:id, :array)";
        Map<String, Object> params = new HashMap<>();
        params.put("id", 3);
        params.put("array", new SqlArg(testArray, Types.ARRAY, new ArrayTypeHandler()));

        int rows = jdbcTemplate.executeUpdate(insertSql, params);
        assertEquals(1, rows);

        // 查询 - 使用命名参数和 queryForObject
        String selectSql = "SELECT string_array FROM array_types_test WHERE id = :id";
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("id", 3);

        String[] loaded = jdbcTemplate.queryForObject(selectSql, queryParams, String[].class);

        assertNotNull(loaded);
        assertEquals(testArray.length, loaded.length);
        assertArrayEquals(testArray, loaded);

        System.out.println("[SUCCESS] 场景3 通过：命名参数数组操作成功");
    }

    /**
     * 测试场景4：浮点数数组
     * 验证不同数值类型的数组支持
     */
    @Test
    public void testFloatArray_NumericArrays() throws SQLException {
        System.out.println("\n[TEST] 场景4：浮点数数组");

        Float[] testArray = { 1.1f, 2.2f, 3.3f, 4.4f };

        // 插入
        String insertSql = "INSERT INTO array_types_test (id, float_array) VALUES (?, ?)";
        SqlArg arrayArg = new SqlArg(testArray, Types.ARRAY, new ArrayTypeHandler());
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 4, arrayArg });

        // 查询 - 使用 queryForObject 直接获取 Float[] 类型
        String selectSql = "SELECT float_array FROM array_types_test WHERE id = ?";
        Float[] loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 4 }, Float[].class);

        assertNotNull(loaded);
        assertEquals(testArray.length, loaded.length);

        // 浮点数比较需要考虑精度
        for (int i = 0; i < testArray.length; i++) {
            assertEquals(testArray[i], loaded[i], 0.0001f);
        }

        System.out.println("[SUCCESS] 场景4 通过：浮点数数组操作成功");
    }

    /**
     * 测试场景5：更新数组
     * 验证数组字段的更新操作
     */
    @Test
    public void testUpdateArray_ModifyArrayField() throws SQLException {
        System.out.println("\n[TEST] 场景5：更新数组字段");

        // 初始插入
        Integer[] originalArray = { 1, 2, 3 };
        String insertSql = "INSERT INTO array_types_test (id, int_array) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 5, new SqlArg(originalArray, Types.ARRAY, new ArrayTypeHandler()) });

        // 更新数组
        Integer[] updatedArray = { 10, 20, 30, 40, 50 };
        String updateSql = "UPDATE array_types_test SET int_array = :array WHERE id = :id";
        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("id", 5);
        updateParams.put("array", new SqlArg(updatedArray, Types.ARRAY, new ArrayTypeHandler()));

        int rows = jdbcTemplate.executeUpdate(updateSql, updateParams);
        assertEquals(1, rows);

        // 验证更新结果 - 使用 queryForObject
        Integer[] loaded = jdbcTemplate.queryForObject("SELECT int_array FROM array_types_test WHERE id = ?", new Object[] { 5 }, Integer[].class);

        assertNotNull(loaded);
        assertEquals(updatedArray.length, loaded.length);
        assertArrayEquals(updatedArray, loaded);

        System.out.println("[SUCCESS] 场景5 通过：数组更新操作成功");
    }

    /**
     * 测试场景6：NULL 数组处理
     * 验证 NULL 值的正确处理
     */
    @Test
    public void testNullArray_NullHandling() throws SQLException {
        System.out.println("\n[TEST] 场景6：NULL 数组处理");

        // 插入 NULL 数组
        String insertSql = "INSERT INTO array_types_test (id, int_array) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 6, null });

        // 查询 NULL 数组 - 使用 queryForObject
        Integer[] loaded = jdbcTemplate.queryForObject("SELECT int_array FROM array_types_test WHERE id = ?", new Object[] { 6 }, Integer[].class);

        assertNull(loaded);

        System.out.println("[SUCCESS] 场景6 通过：NULL 数组处理正确");
    }

    /**
     * 测试场景7：批量插入数组
     * 验证批量操作中的数组处理
     */
    @Test
    public void testBatchInsert_MultipleArrays() throws SQLException {
        System.out.println("\n[TEST] 场景7：批量插入数组");

        String insertSql = "INSERT INTO array_types_test (id, int_array, string_array) VALUES (?, ?, ?)";

        // 准备批量数据
        Object[][] batchData = new Object[][] {//
                { 10, new SqlArg(new Integer[] { 1, 2 }, Types.ARRAY, new ArrayTypeHandler()), new SqlArg(new String[] { "A", "B" }, Types.ARRAY, new ArrayTypeHandler()) },//
                { 11, new SqlArg(new Integer[] { 3, 4 }, Types.ARRAY, new ArrayTypeHandler()), new SqlArg(new String[] { "C", "D" }, Types.ARRAY, new ArrayTypeHandler()) },//
                { 12, new SqlArg(new Integer[] { 5, 6 }, Types.ARRAY, new ArrayTypeHandler()), new SqlArg(new String[] { "E", "F" }, Types.ARRAY, new ArrayTypeHandler()) }//
        };

        // 执行批量插入
        int[] results = jdbcTemplate.executeBatch(insertSql, batchData);

        assertEquals(3, results.length);
        for (int result : results) {
            assertEquals(1, result);
        }

        // 验证数据
        String countSql = "SELECT COUNT(*) FROM array_types_test WHERE id >= 10 AND id <= 12";
        int count = jdbcTemplate.queryForObject(countSql, Integer.class);
        assertEquals(3, count);

        System.out.println("[SUCCESS] 场景7 通过：批量数组插入成功");
    }

    /**
     * 测试场景8：Unicode 字符串数组
     * 验证特殊字符和多语言支持
     */
    @Test
    public void testUnicodeStringArray_Internationalization() throws SQLException {
        System.out.println("\n[TEST] 场景8：Unicode 字符串数组");

        String[] testArray = { "你好", "世界", "こんにちは", "🎉", "Emoji🚀Test" };

        // 插入
        String insertSql = "INSERT INTO array_types_test (id, string_array) VALUES (:id, :array)";
        Map<String, Object> params = new HashMap<>();
        params.put("id", 8);
        params.put("array", new SqlArg(testArray, Types.ARRAY, new ArrayTypeHandler()));

        jdbcTemplate.executeUpdate(insertSql, params);

        // 查询 - 使用 queryForObject
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("id", 8);
        String[] loaded = jdbcTemplate.queryForObject("SELECT string_array FROM array_types_test WHERE id = :id", queryParams, String[].class);

        assertNotNull(loaded);
        assertEquals(testArray.length, loaded.length);
        assertArrayEquals(testArray, loaded);

        System.out.println("[SUCCESS] 场景8 通过：Unicode 数组处理成功");
    }
}
