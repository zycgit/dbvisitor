package net.hasor.dbvisitor.test.suite.fluent.types;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.types.ArrayTypesAnnotationModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 数组类型注解测试 - Fluent API (LambdaTemplate)
 * 测试不同 @Column 注解配置对数组映射的影响
 * 测试场景：
 * 1. @Column 不指定任何属性 - 依赖 TypeHandlerRegistry 默认注册
 * 2. @Column 指定 jdbcType = Types.ARRAY
 * 3. @Column 指定 typeHandler = ArrayTypeHandler.class
 * 4. Number[] 类型指定 specialJavaType = Integer[].class
 * 5. 同时指定 jdbcType 和 specialJavaType（完整配置）
 */
public class ArrayTypesAnnotationTest extends AbstractOneApiTest {

    @Override
    protected void cleanTestData() {
        try {
            jdbcTemplate.executeUpdate("DELETE FROM array_types_annotation_test");
        } catch (Exception e) {
            // Ignore
        }
        super.cleanTestData();
    }

    /**
     * 测试场景1：@Column 不指定任何属性
     * 依赖 TypeHandlerRegistry 中注册的 Integer[].class -> ArrayTypeHandler 映射
     */
    @Test
    public void testArrayNoAnnotation_WithTypeRegistry() throws SQLException {
        System.out.println("\n[TEST] 场景1：@Column 不指定任何属性，依赖 TypeHandlerRegistry 默认注册");

        Integer[] testArray = { 10, 20, 30, 40, 50 };

        ArrayTypesAnnotationModel model = new ArrayTypesAnnotationModel();
        model.setId(1);
        model.setArrayNoAnnotation(testArray);

        // 插入
        int rows = lambdaTemplate.insert(ArrayTypesAnnotationModel.class)//
                .applyEntity(model).executeSumResult();

        assertEquals(1, rows);

        // 查询
        ArrayTypesAnnotationModel loaded = lambdaTemplate.query(ArrayTypesAnnotationModel.class)//
                .eq(ArrayTypesAnnotationModel::getId, 1)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getArrayNoAnnotation());
        assertEquals(testArray.length, loaded.getArrayNoAnnotation().length);
        assertArrayEquals(testArray, loaded.getArrayNoAnnotation());

        System.out.println("[SUCCESS] 场景1 通过：不指定注解属性时，依赖 TypeHandlerRegistry 成功映射");
    }

    /**
     * 测试场景2：@Column 指定 jdbcType = Types.ARRAY
     * 通过 jdbcType 和 Java 类型的组合查找 TypeHandler
     */
    @Test
    public void testArrayJdbcType_ExplicitMapping() throws SQLException {
        System.out.println("\n[TEST] 场景2：@Column 指定 jdbcType = Types.ARRAY");

        Integer[] testArray = { 100, 200, 300 };

        ArrayTypesAnnotationModel model = new ArrayTypesAnnotationModel();
        model.setId(2);
        model.setArrayJdbcType(testArray);

        // 插入
        int rows = lambdaTemplate.insert(ArrayTypesAnnotationModel.class)//
                .applyEntity(model).executeSumResult();

        assertEquals(1, rows);

        // 查询
        ArrayTypesAnnotationModel loaded = lambdaTemplate.query(ArrayTypesAnnotationModel.class)//
                .eq(ArrayTypesAnnotationModel::getId, 2).queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getArrayJdbcType());
        assertEquals(testArray.length, loaded.getArrayJdbcType().length);
        assertArrayEquals(testArray, loaded.getArrayJdbcType());

        System.out.println("[SUCCESS] 场景2 通过：显式指定 jdbcType 成功映射");
    }

    /**
     * 测试场景3：@Column 指定 typeHandler = ArrayTypeHandler.class
     * 直接指定使用的 TypeHandler 类型
     */
    @Test
    public void testArrayTypeHandler_DirectSpecification() throws SQLException {
        System.out.println("\n[TEST] 场景3：@Column 指定 typeHandler = ArrayTypeHandler.class");

        Integer[] testArray = { 7, 14, 21, 28 };

        ArrayTypesAnnotationModel model = new ArrayTypesAnnotationModel();
        model.setId(3);
        model.setArrayTypeHandler(testArray);

        // 插入
        int rows = lambdaTemplate.insert(ArrayTypesAnnotationModel.class)//
                .applyEntity(model).executeSumResult();

        assertEquals(1, rows);

        // 查询
        ArrayTypesAnnotationModel loaded = lambdaTemplate.query(ArrayTypesAnnotationModel.class)//
                .eq(ArrayTypesAnnotationModel::getId, 3).queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getArrayTypeHandler());
        assertEquals(testArray.length, loaded.getArrayTypeHandler().length);
        assertArrayEquals(testArray, loaded.getArrayTypeHandler());

        System.out.println("[SUCCESS] 场景3 通过：直接指定 typeHandler 成功映射");
    }

    /**
     * 测试场景4：Number[] 类型指定 specialJavaType = Integer[].class
     * 字段声明为 Number[]，但通过 specialJavaType 映射到 Integer[]
     */
    @Test
    public void testArrayNumberSpecial_JavaTypeMapping() throws SQLException {
        System.out.println("\n[TEST] 场景4：Number[] 类型指定 specialJavaType = Integer[].class");

        Integer[] testArray = { 111, 222, 333 };

        ArrayTypesAnnotationModel model = new ArrayTypesAnnotationModel();
        model.setId(4);
        model.setArrayNumberSpecial(testArray);  // 设置为 Number[] 但实际值为 Integer[]

        // 插入
        int rows = lambdaTemplate.insert(ArrayTypesAnnotationModel.class)//
                .applyEntity(model).executeSumResult();

        assertEquals(1, rows);

        // 查询
        ArrayTypesAnnotationModel loaded = lambdaTemplate.query(ArrayTypesAnnotationModel.class)//
                .eq(ArrayTypesAnnotationModel::getId, 4).queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getArrayNumberSpecial());
        assertEquals(testArray.length, loaded.getArrayNumberSpecial().length);

        // 验证数组内容
        Number[] loadedArray = loaded.getArrayNumberSpecial();
        for (int i = 0; i < testArray.length; i++) {
            assertEquals(testArray[i].intValue(), loadedArray[i].intValue());
        }

        System.out.println("[SUCCESS] 场景4 通过：Number[] 通过 specialJavaType 成功映射到 Integer[]");
    }

    /**
     * 测试场景5：同时指定 jdbcType 和 specialJavaType
     * 完整配置：jdbcType = Types.ARRAY, specialJavaType = Integer[].class
     */
    @Test
    public void testArrayFullAnnotated_CompleteConfiguration() throws SQLException {
        System.out.println("\n[TEST] 场景5：同时指定 jdbcType 和 specialJavaType（完整配置）");

        Integer[] testArray = { 5, 10, 15, 20, 25 };

        ArrayTypesAnnotationModel model = new ArrayTypesAnnotationModel();
        model.setId(5);
        model.setArrayFullAnnotated(testArray);

        // 插入
        int rows = lambdaTemplate.insert(ArrayTypesAnnotationModel.class)//
                .applyEntity(model).executeSumResult();

        assertEquals(1, rows);

        // 查询
        ArrayTypesAnnotationModel loaded = lambdaTemplate.query(ArrayTypesAnnotationModel.class)//
                .eq(ArrayTypesAnnotationModel::getId, 5).queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getArrayFullAnnotated());
        assertEquals(testArray.length, loaded.getArrayFullAnnotated().length);

        // 验证数组内容
        Number[] loadedArray = loaded.getArrayFullAnnotated();
        for (int i = 0; i < testArray.length; i++) {
            assertEquals(testArray[i].intValue(), loadedArray[i].intValue());
        }

        System.out.println("[SUCCESS] 场景5 通过：完整的注解配置（jdbcType + specialJavaType）成功映射");
    }

    /**
     * 综合测试：同时测试所有5种场景
     * 验证不同注解配置可以在同一个 Model 中共存并正常工作
     */
    @Test
    public void testAllAnnotationScenarios_Together() throws SQLException {
        System.out.println("\n[TEST] 综合测试：所有5种注解场景同时工作");

        Integer[] testArray = { 1, 2, 3, 4, 5 };

        ArrayTypesAnnotationModel model = new ArrayTypesAnnotationModel();
        model.setId(100);
        model.setArrayNoAnnotation(testArray);
        model.setArrayJdbcType(testArray);
        model.setArrayTypeHandler(testArray);
        model.setArrayNumberSpecial(testArray);
        model.setArrayFullAnnotated(testArray);

        // 插入
        int rows = lambdaTemplate.insert(ArrayTypesAnnotationModel.class)//
                .applyEntity(model).executeSumResult();

        assertEquals(1, rows);

        // 查询
        ArrayTypesAnnotationModel loaded = lambdaTemplate.query(ArrayTypesAnnotationModel.class)//
                .eq(ArrayTypesAnnotationModel::getId, 100).queryForObject();

        assertNotNull(loaded);

        // 验证所有字段都正确映射
        assertNotNull("arrayNoAnnotation should not be null", loaded.getArrayNoAnnotation());
        assertNotNull("arrayJdbcType should not be null", loaded.getArrayJdbcType());
        assertNotNull("arrayTypeHandler should not be null", loaded.getArrayTypeHandler());
        assertNotNull("arrayNumberSpecial should not be null", loaded.getArrayNumberSpecial());
        assertNotNull("arrayFullAnnotated should not be null", loaded.getArrayFullAnnotated());

        // 验证数组内容
        assertArrayEquals(testArray, loaded.getArrayNoAnnotation());
        assertArrayEquals(testArray, loaded.getArrayJdbcType());
        assertArrayEquals(testArray, loaded.getArrayTypeHandler());

        // Number[] 字段需要转换后比较
        Number[] numberSpecial = loaded.getArrayNumberSpecial();
        Number[] numberFull = loaded.getArrayFullAnnotated();
        for (int i = 0; i < testArray.length; i++) {
            assertEquals(testArray[i].intValue(), numberSpecial[i].intValue());
            assertEquals(testArray[i].intValue(), numberFull[i].intValue());
        }

        System.out.println("[SUCCESS] 综合测试通过：所有5种注解场景在同一 Model 中正常工作");
        System.out.println("  - 场景1 (无注解属性): ✓");
        System.out.println("  - 场景2 (jdbcType): ✓");
        System.out.println("  - 场景3 (typeHandler): ✓");
        System.out.println("  - 场景4 (specialJavaType): ✓");
        System.out.println("  - 场景5 (jdbcType + specialJavaType): ✓");
    }
}
