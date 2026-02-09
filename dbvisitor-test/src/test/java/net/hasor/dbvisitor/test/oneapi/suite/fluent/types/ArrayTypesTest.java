package net.hasor.dbvisitor.test.oneapi.suite.fluent.types;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.types.ArrayTypesModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 数组类型测试 - Fluent API (LambdaTemplate)
 * 验证 H2 和 PostgreSQL 的数组类型支持
 * 测试范围：
 * 1. Integer[] 数组
 * 2. String[] 数组
 * 3. Float[] 数组
 * 4. 空数组、单元素数组、NULL 处理
 * Note: MySQL 不支持原生数组，需要降级为 JSON 或 VARCHAR
 */
public class ArrayTypesTest extends AbstractOneApiTest {

    @Override
    protected void cleanTestData() {
        try {
            jdbcTemplate.executeUpdate("DELETE FROM array_types_test");
        } catch (Exception e) {
            // Ignore
        }
        super.cleanTestData();
    }

    /**
     * 测试 Integer 数组 - 基本操作
     * 场景1：使用 LambdaTemplate 自动映射（依赖 TypeHandlerRegistry 中注册的 ArrayTypeHandler）
     * 场景2：使用 JdbcTemplate 手动处理（不依赖 ArrayTypeHandler）
     */
    @Test
    public void testIntegerArray_BasicOperations() throws SQLException {
        Integer[] intArray = { 1, 2, 3, 4, 5 };

        // ========== 场景1：使用 LambdaTemplate 自动映射 ==========
        System.out.println("[TEST] 场景1：使用 LambdaTemplate 自动映射");

        ArrayTypesModel model1 = new ArrayTypesModel();
        model1.setId(1);
        model1.setIntArray(intArray);

        int inserted1 = lambdaTemplate.insert(ArrayTypesModel.class)//
                .applyEntity(model1)//
                .executeSumResult();

        ArrayTypesModel loaded1 = lambdaTemplate//
                .query(ArrayTypesModel.class)//
                .eq(ArrayTypesModel::getId, 1)//
                .queryForObject();

        assertNotNull("Scenario 1: Loaded object should not be null", loaded1);
        assertNotNull("Scenario 1: IntArray should not be null", loaded1.getIntArray());
        assertEquals("Scenario 1: Array length should match", 5, loaded1.getIntArray().length);
        assertArrayEquals("Scenario 1: Array contents should match", intArray, loaded1.getIntArray());

        // 清理场景1的数据
        lambdaTemplate.delete(ArrayTypesModel.class).eq(ArrayTypesModel::getId, 1).doDelete();

        // ========== 场景2：使用 JdbcTemplate 手动处理（不依赖 ArrayTypeHandler）==========
        System.out.println("[TEST] 场景2：使用 JdbcTemplate 手动处理");

        ArrayTypesModel model2 = new ArrayTypesModel();
        model2.setId(2);
        model2.setIntArray(intArray);

        // 使用 JDBC 原始方式插入
        String insertSql = "INSERT INTO array_types_test (id, int_array, string_array, float_array) VALUES (?, ?, ?, ?)";
        int inserted2 = jdbcTemplate.executeUpdate(insertSql, new Object[] { model2.getId(), model2.getIntArray(), model2.getStringArray(), model2.getFloatArray() });

        System.out.println("[DEBUG] Scenario 2 - Inserted rows: " + inserted2);

        // 使用 JdbcTemplate 手动处理 Array 类型
        String querySql = "SELECT id, int_array, string_array, float_array FROM array_types_test WHERE id = 2";
        ArrayTypesModel loaded2 = jdbcTemplate.queryForList(querySql, (rs, rowNum) -> {
            ArrayTypesModel m = new ArrayTypesModel();
            m.setId(rs.getInt("id"));

            // 手动从 JDBC Array 中提取数据
            java.sql.Array sqlArray = rs.getArray("int_array");
            if (sqlArray != null) {
                Object[] objectArray = (Object[]) sqlArray.getArray();
                Integer[] intArr = new Integer[objectArray.length];
                for (int i = 0; i < objectArray.length; i++) {
                    intArr[i] = (Integer) objectArray[i];
                }
                m.setIntArray(intArr);
                sqlArray.free();
            }

            return m;
        }).get(0);

        assertNotNull("Scenario 2: Loaded object should not be null", loaded2);
        assertNotNull("Scenario 2: IntArray should not be null", loaded2.getIntArray());
        assertEquals("Scenario 2: Array length should match", 5, loaded2.getIntArray().length);
        assertArrayEquals("Scenario 2: Array contents should match", intArray, loaded2.getIntArray());
    }

    /**
     * 测试 String 数组 - 包含 Unicode 字符
     */
    @Test
    public void testStringArray_UnicodeCharacters() throws SQLException {
        ArrayTypesModel model = new ArrayTypesModel();
        model.setId(2);

        String[] strArray = { "Hello", "世界", "🌍", "Test" };
        model.setStringArray(strArray);

        lambdaTemplate.insert(ArrayTypesModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        ArrayTypesModel loaded = lambdaTemplate.query(ArrayTypesModel.class)//
                .eq(ArrayTypesModel::getId, 2)//
                .queryForObject();

        assertNotNull(loaded.getStringArray());
        assertEquals(4, loaded.getStringArray().length);
        assertArrayEquals(strArray, loaded.getStringArray());
    }

    /**
     * 测试 Float 数组
     */
    @Test
    public void testFloatArray_DecimalValues() throws SQLException {
        ArrayTypesModel model = new ArrayTypesModel();
        model.setId(3);

        Float[] floatArray = { 3.14f, 2.718f, 1.414f, 0.0f, -1.5f };
        model.setFloatArray(floatArray);

        lambdaTemplate.insert(ArrayTypesModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        ArrayTypesModel loaded = lambdaTemplate.query(ArrayTypesModel.class)//
                .eq(ArrayTypesModel::getId, 3)//
                .queryForObject();

        assertNotNull(loaded.getFloatArray());
        assertEquals(5, loaded.getFloatArray().length);

        for (int i = 0; i < floatArray.length; i++) {
            assertEquals(floatArray[i], loaded.getFloatArray()[i], 0.0001f);
        }
    }

    /**
     * 测试边界情况 - 空数组、单元素、NULL
     * PostgreSQL 完整支持空数组类型，H2 对空数组有类型信息丢失的限制
     */
    @Test
    public void testArrayBoundaryConditions_EmptySingleNull() throws SQLException {
        // Empty array
        ArrayTypesModel model1 = new ArrayTypesModel();
        model1.setId(4);
        model1.setIntArray(new Integer[0]);
        model1.setStringArray(new String[0]);
        model1.setFloatArray(new Float[0]);

        lambdaTemplate.insert(ArrayTypesModel.class)//
                .applyEntity(model1)//
                .executeSumResult();

        ArrayTypesModel loaded1 = lambdaTemplate.query(ArrayTypesModel.class)//
                .eq(ArrayTypesModel::getId, 4)//
                .queryForObject();

        // 调试信息
        System.out.println("[DEBUG] Empty array test - loaded model: " + loaded1);
        System.out.println("[DEBUG] intArray: " + (loaded1.getIntArray() == null ? "null" : ("length=" + loaded1.getIntArray().length)));
        System.out.println("[DEBUG] stringArray: " + (loaded1.getStringArray() == null ? "null" : ("length=" + loaded1.getStringArray().length)));
        System.out.println("[DEBUG] floatArray: " + (loaded1.getFloatArray() == null ? "null" : ("length=" + loaded1.getFloatArray().length)));

        assertNotNull("intArray should not be null", loaded1.getIntArray());
        assertNotNull("stringArray should not be null", loaded1.getStringArray());
        assertNotNull("floatArray should not be null", loaded1.getFloatArray());
        assertEquals(0, loaded1.getIntArray().length);
        assertEquals(0, loaded1.getStringArray().length);
        assertEquals(0, loaded1.getFloatArray().length);
        assertArrayEquals(new Integer[0], loaded1.getIntArray());
        assertArrayEquals(new String[0], loaded1.getStringArray());
        assertArrayEquals(new Float[0], loaded1.getFloatArray());

        // 清理
        jdbcTemplate.executeUpdate("DELETE FROM array_types_test WHERE id = ?", 4);
        // Single element array
        ArrayTypesModel model2 = new ArrayTypesModel();
        model2.setId(5);
        model2.setIntArray(new Integer[] { 42 });
        model2.setStringArray(new String[] { "Single" });
        model2.setFloatArray(new Float[] { 1.23f });

        lambdaTemplate.insert(ArrayTypesModel.class)//
                .applyEntity(model2)//
                .executeSumResult();

        ArrayTypesModel loaded2 = lambdaTemplate.query(ArrayTypesModel.class)//
                .eq(ArrayTypesModel::getId, 5)//
                .queryForObject();

        assertNotNull(loaded2.getIntArray());
        assertNotNull(loaded2.getStringArray());
        assertNotNull(loaded2.getFloatArray());
        assertEquals(1, loaded2.getIntArray().length);
        assertEquals(1, loaded2.getStringArray().length);
        assertEquals(1, loaded2.getFloatArray().length);
        assertEquals(Integer.valueOf(42), loaded2.getIntArray()[0]);
        assertEquals("Single", loaded2.getStringArray()[0]);
        assertEquals(Float.valueOf(1.23f), loaded2.getFloatArray()[0], 0.0001f);
        assertArrayEquals(new Integer[] { 42 }, loaded2.getIntArray());
        assertArrayEquals(new String[] { "Single" }, loaded2.getStringArray());

        // 清理
        jdbcTemplate.executeUpdate("DELETE FROM array_types_test WHERE id = ?", 5);

        // NULL arrays
        ArrayTypesModel model3 = new ArrayTypesModel();
        model3.setId(6);
        model3.setIntArray(null);
        model3.setStringArray(null);
        model3.setFloatArray(null);

        lambdaTemplate.insert(ArrayTypesModel.class)//
                .applyEntity(model3)//
                .executeSumResult();

        ArrayTypesModel loaded3 = lambdaTemplate.query(ArrayTypesModel.class)//
                .eq(ArrayTypesModel::getId, 6)//
                .queryForObject();

        assertNull(loaded3.getIntArray());
        assertNull(loaded3.getStringArray());
        assertNull(loaded3.getFloatArray());
    }
}
