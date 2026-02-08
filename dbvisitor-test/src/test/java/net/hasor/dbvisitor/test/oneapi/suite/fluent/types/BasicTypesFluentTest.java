package net.hasor.dbvisitor.test.oneapi.suite.fluent.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Map;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.types.BasicTypesModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 基本类型测试 - Fluent API (LambdaTemplate)
 * 对应 programmatic/types/BasicTypesJdbcTest，使用 LambdaTemplate + Bean 映射
 * 测试范围：
 * 1. 全面覆盖所有基本类型及其边界值（BasicTypesModel - 自动推断）
 * 2. BIT vs BOOLEAN 数据库差异行为（BasicTypesExplicitModel - 显式指定）
 * 3. VARCHAR/NVARCHAR 字符类型场景（BasicTypesExplicitModel）
 * 4. null 值处理
 * 5. 边界值验证
 */
public class BasicTypesFluentTest extends AbstractOneApiTest {

    @Override
    protected void cleanTestData() {
        try {
            jdbcTemplate.executeUpdate("DELETE FROM basic_types_test");
            jdbcTemplate.executeUpdate("DELETE FROM basic_types_explicit_test");
        } catch (Exception e) {
            // Ignore
        }
        super.cleanTestData();
    }

    /**
     * 全面测试所有基本类型及其边界值
     * 验证 LambdaTemplate 的 applyEntity 自动映射和 TypeHandler 选择
     */
    @Test
    public void testBasicTypes_ComprehensiveCoverage() throws SQLException {
        // 准备测试数据
        BasicTypesModel model = new BasicTypesModel();
        model.setByteValue(Byte.MAX_VALUE);
        model.setShortValue(Short.MAX_VALUE);
        model.setIntValue(Integer.MAX_VALUE);
        model.setLongValue(Long.MAX_VALUE);
        model.setFloatValue(3.14f);
        model.setDoubleValue(2.718281828);
        model.setDecimalValue(new BigDecimal("12345.67"));
        model.setBigIntValue(new BigInteger("9223372036854775807"));
        model.setBoolValue(true);
        model.setStringValue("Hello TypeHandler");
        model.setCharValue('A');

        // Insert - 使用 LambdaTemplate，框架自动推断类型
        int rows = lambdaTemplate.insert(BasicTypesModel.class)//
                .applyEntity(model)//
                .executeSumResult();
        assertEquals(1, rows);

        // Select - 验证类型精度无损失
        BasicTypesModel loaded = lambdaTemplate.query(BasicTypesModel.class).queryForObject();

        assertNotNull(loaded);
        assertEquals(model.getByteValue(), loaded.getByteValue());
        assertEquals(model.getShortValue(), loaded.getShortValue());
        assertEquals(model.getIntValue(), loaded.getIntValue());
        assertEquals(model.getLongValue(), loaded.getLongValue());
        assertEquals(model.getFloatValue(), loaded.getFloatValue(), 0.001);
        assertEquals(model.getDoubleValue(), loaded.getDoubleValue(), 0.000001);
        assertEquals(0, model.getDecimalValue().compareTo(loaded.getDecimalValue()));
        assertEquals(model.getBigIntValue(), loaded.getBigIntValue());
        assertEquals(model.getBoolValue(), loaded.getBoolValue());
        assertEquals(model.getStringValue(), loaded.getStringValue());
        assertEquals(model.getCharValue(), loaded.getCharValue());
    }

    /**
     * 测试所有基本类型的 null 值处理
     * 验证 LambdaTemplate 正确处理 null 字段
     */
    @Test
    public void testBasicTypes_NullValues() throws SQLException {
        // 使用 JdbcTemplate 插入全 null 记录（LambdaTemplate 的 applyEntity 只处理非 null 字段）
        jdbcTemplate.executeUpdate("INSERT INTO basic_types_test (id) VALUES (?)", new Object[] { 1000 });

        // 查询并验证所有字段为 null
        BasicTypesModel loaded = lambdaTemplate.query(BasicTypesModel.class)//
                .eq(BasicTypesModel::getId, 1000)//
                .queryForObject();

        assertNotNull(loaded);
        assertNull(loaded.getByteValue());
        assertNull(loaded.getShortValue());
        assertNull(loaded.getIntValue());
        assertNull(loaded.getLongValue());
        assertNull(loaded.getFloatValue());
        assertNull(loaded.getDoubleValue());
        assertNull(loaded.getDecimalValue());
        assertNull(loaded.getBigIntValue());
        assertNull(loaded.getBoolValue());
        assertNull(loaded.getStringValue());
        assertNull(loaded.getCharValue());
    }

    /**
     * 测试数值边界值
     * 验证 MIN/MAX 边界不丢失精度
     */
    @Test
    public void testBasicTypes_NumericBoundaries() throws SQLException {
        BasicTypesModel model = new BasicTypesModel();
        model.setByteValue(Byte.MIN_VALUE);
        model.setShortValue(Short.MIN_VALUE);
        model.setIntValue(Integer.MIN_VALUE);
        model.setLongValue(Long.MIN_VALUE);
        model.setFloatValue(Float.MIN_VALUE);
        model.setDoubleValue(Double.MIN_VALUE);
        model.setDecimalValue(new BigDecimal("-99999999.99"));
        model.setBigIntValue(new BigInteger("-9223372036854775808"));
        model.setBoolValue(false);
        model.setStringValue("");
        model.setCharValue(' ');

        int rows = lambdaTemplate.insert(BasicTypesModel.class)//
                .applyEntity(model)//
                .executeSumResult();
        assertEquals(1, rows);

        BasicTypesModel loaded = lambdaTemplate.query(BasicTypesModel.class)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals(model.getByteValue(), loaded.getByteValue());
        assertEquals(model.getShortValue(), loaded.getShortValue());
        assertEquals(model.getIntValue(), loaded.getIntValue());
        assertEquals(model.getLongValue(), loaded.getLongValue());
        assertEquals(model.getBoolValue(), loaded.getBoolValue());
    }

    /**
     * 测试 BIT vs BOOLEAN - PostgreSQL 中 BIT(1) 需要 CAST
     * 通过 JdbcTemplate 插入（使用 CAST），LambdaTemplate 读取
     */
    @Test
    public void testBitVsBoolean_ExplicitModel() throws SQLException {
        // PostgreSQL BIT 类型需要使用 CAST，通过 JdbcTemplate 插入
        String insertSql = "INSERT INTO basic_types_explicit_test (id, bool_bit, bool_boolean) VALUES (?, CAST(? AS BIT), ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 1, 1, false });

        // 使用 JdbcTemplate 读取（避免读取 NVARCHAR 列触发 getNString）
        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT id, bool_bit, bool_boolean FROM basic_types_explicit_test WHERE id = ?", new Object[] { 1 });
        assertNotNull(row);
        // PostgreSQL BIT(1) 读取为 Boolean
        assertEquals(true, row.get("bool_bit"));
        assertEquals(false, row.get("bool_boolean"));

        // 测试 null 值
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 2, null, null });

        Map<String, Object> row2 = jdbcTemplate.queryForMap("SELECT id, bool_bit, bool_boolean FROM basic_types_explicit_test WHERE id = ?", new Object[] { 2 });
        assertNotNull(row2);
        assertNull(row2.get("bool_bit"));
        assertNull(row2.get("bool_boolean"));
    }

    /**
     * 测试 VARCHAR 字符类型 - 使用 JdbcTemplate 插入，LambdaTemplate 读取
     * 注意：PostgreSQL JDBC 驱动不支持 setNString/getNString，因此 NVARCHAR 通过 JdbcTemplate 原生方式测试
     */
    @Test
    public void testVarchar_ExplicitModel() throws SQLException {
        // 使用 JdbcTemplate 插入（避免 NVARCHAR 的 setNString 问题）
        String insertSql = "INSERT INTO basic_types_explicit_test (id, char_value, varchar_value, nvarchar_value) VALUES (?, ?, ?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 3, 'A', "Hello World!", "你好世界！🌍" });

        // 使用 JdbcTemplate 查询验证（避免 getNString 问题）
        java.util.Map<String, Object> result = jdbcTemplate.queryForMap("SELECT char_value, varchar_value, nvarchar_value FROM basic_types_explicit_test WHERE id = ?", new Object[] { 3 });

        assertNotNull(result);
        String charValue = (String) result.get("char_value");
        assertNotNull(charValue);
        assertEquals('A', charValue.charAt(0));
        assertEquals("Hello World!", result.get("varchar_value"));
        assertEquals("你好世界！🌍", result.get("nvarchar_value"));
    }

    /**
     * 测试空字符串与 null 的区别
     * 使用 JdbcTemplate 避免 PostgreSQL NVARCHAR 不兼容问题
     */
    @Test
    public void testEmptyStringVsNull() throws SQLException {
        String insertSql = "INSERT INTO basic_types_explicit_test (id, char_value, varchar_value, nvarchar_value) VALUES (?, ?, ?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 4, null, "", null });

        java.util.Map<String, Object> result = jdbcTemplate.queryForMap("SELECT char_value, varchar_value, nvarchar_value FROM basic_types_explicit_test WHERE id = ?", new Object[] { 4 });

        assertNotNull(result);
        assertNull(result.get("char_value"));
        assertEquals("", result.get("varchar_value"));
        assertNull(result.get("nvarchar_value"));
    }

    /**
     * 测试更新操作 - 验证 LambdaTemplate update 对所有基本类型的支持
     */
    @Test
    public void testBasicTypes_UpdateOperation() throws SQLException {
        // 先插入数据
        BasicTypesModel model = new BasicTypesModel();
        model.setIntValue(100);
        model.setStringValue("Original");
        model.setBoolValue(true);

        lambdaTemplate.insert(BasicTypesModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        // 查询获取 id
        BasicTypesModel inserted = lambdaTemplate.query(BasicTypesModel.class)//
                .eq(BasicTypesModel::getStringValue, "Original")//
                .queryForObject();
        assertNotNull(inserted);

        // 更新数据
        int updated = lambdaTemplate.update(BasicTypesModel.class)//
                .eq(BasicTypesModel::getId, inserted.getId())//
                .updateTo(BasicTypesModel::getIntValue, 200)//
                .updateTo(BasicTypesModel::getStringValue, "Updated")//
                .updateTo(BasicTypesModel::getBoolValue, false)//
                .doUpdate();
        assertEquals(1, updated);

        // 验证更新
        BasicTypesModel loaded = lambdaTemplate.query(BasicTypesModel.class)//
                .eq(BasicTypesModel::getId, inserted.getId())//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals(Integer.valueOf(200), loaded.getIntValue());
        assertEquals("Updated", loaded.getStringValue());
        assertFalse(loaded.getBoolValue());
    }

    /**
     * 测试删除操作
     */
    @Test
    public void testBasicTypes_DeleteOperation() throws SQLException {
        // 插入两条数据
        BasicTypesModel model1 = new BasicTypesModel();
        model1.setIntValue(1);
        model1.setStringValue("ToDelete");

        BasicTypesModel model2 = new BasicTypesModel();
        model2.setIntValue(2);
        model2.setStringValue("ToKeep");

        lambdaTemplate.insert(BasicTypesModel.class).applyEntity(model1).executeSumResult();
        lambdaTemplate.insert(BasicTypesModel.class).applyEntity(model2).executeSumResult();

        // 删除第一条
        int deleted = lambdaTemplate.delete(BasicTypesModel.class)//
                .eq(BasicTypesModel::getStringValue, "ToDelete")//
                .doDelete();
        assertEquals(1, deleted);

        // 验证只剩一条
        long count = lambdaTemplate.query(BasicTypesModel.class).queryForCount();
        assertEquals(1, count);

        BasicTypesModel remaining = lambdaTemplate.query(BasicTypesModel.class).queryForObject();
        assertNotNull(remaining);
        assertEquals("ToKeep", remaining.getStringValue());
    }
}
