package net.hasor.dbvisitor.test.suite.programmatic.types;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Map;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.types.BasicTypesModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 基本类型测试 - Programmatic API (JdbcTemplate)
 * 测试范围：
 * 1. 全面覆盖所有基本类型及其边界值
 * 2. BIT vs BOOLEAN 数据库差异行为
 * 3. 字符类型的特殊场景（Unicode、空字符串vs null）
 * 数据库初始化：通过 /sql/{dialect}/init.sql 脚本自动创建 basic_types_test 和 basic_types_explicit_test 表
 */
public class BasicTypesJdbcTest extends AbstractOneApiTest {

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
     * 验证框架自动 TypeHandler 选择和类型精度保持
     */
    @Test
    public void testBasicTypes_ComprehensiveCoverage() throws Exception {
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

        // Insert - 使用 JdbcTemplate，框架自动推断类型
        String insertSql = "INSERT INTO basic_types_test (byte_value, short_value, int_value, long_value, " + "float_value, double_value, decimal_value, big_int_value, bool_value, string_value, char_value) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int rows = jdbcTemplate.executeUpdate(insertSql, new Object[] { model.getByteValue(), model.getShortValue(), model.getIntValue(), model.getLongValue(), model.getFloatValue(), model.getDoubleValue(), model.getDecimalValue(), model.getBigIntValue(), model.getBoolValue(), model.getStringValue(), model.getCharValue() });

        assertEquals(1, rows);

        // Select - 验证类型精度无损失
        String selectSql = "SELECT * FROM basic_types_test ORDER BY id DESC LIMIT 1";
        BasicTypesModel loaded = jdbcTemplate.queryForObject(selectSql, BasicTypesModel.class);

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
     * 测试 BIT vs BOOLEAN - 验证行为差异
     * H2: BIT(1) ≈ BOOLEAN
     * MySQL: BIT ≈ TINYINT(1)
     * PG: BIT vs BOOLEAN 是不同类型（BIT 需要特殊的 bit 字符串）
     */
    @Test
    public void testBitVsBoolean_BehaviorDifference() throws SQLException {
        // 测试 true/false 值
        // 注意：PostgreSQL BIT 类型需要使用 CAST，因此在 SQL 中直接处理
        String insertSql = "INSERT INTO basic_types_explicit_test (id, bool_bit, bool_boolean) VALUES (?, CAST(? AS BIT), ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 2, 1, false });

        String selectSql = "SELECT bool_bit, bool_boolean FROM basic_types_explicit_test WHERE id = ?";
        Map<String, Object> result = jdbcTemplate.queryForMap(selectSql, new Object[] { 2 });
        // BIT(1) 读取时也返回 boolean
        assertTrue((Boolean) result.get("bool_bit"));
        assertFalse((Boolean) result.get("bool_boolean"));

        // 测试 null 值
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 3, null, null });

        Map<String, Object> result2 = jdbcTemplate.queryForMap(selectSql, new Object[] { 3 });
        assertNull(result2.get("bool_bit"));
        assertNull(result2.get("bool_boolean"));
    }

    /**
     * 测试 VARCHAR/NVARCHAR - 跨数据库字符类型兼容
     */
    @Test
    public void testVarcharNvarchar_CharacterTypes() throws SQLException {
        // 测试 ASCII 和 Unicode 字符
        String insertSql = "INSERT INTO basic_types_explicit_test (id, char_value, varchar_value, nvarchar_value) VALUES (?, ?, ?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 6, 'A', "Hello World!", "你好世界！🌍" });

        // 查询并验证
        String selectSql = "SELECT char_value, varchar_value, nvarchar_value FROM basic_types_explicit_test WHERE id = ?";
        Map<String, Object> result = jdbcTemplate.queryForMap(selectSql, new Object[] { 6 });
        String charValue = (String) result.get("char_value");
        assertNotNull(charValue);
        assertEquals('A', charValue.charAt(0));
        assertEquals("Hello World!", result.get("varchar_value"));
        assertEquals("你好世界！🌍", result.get("nvarchar_value"));

        // 测试空字符串 vs null
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 7, null, "", null });

        Map<String, Object> result2 = jdbcTemplate.queryForMap(selectSql, new Object[] { 7 });
        assertNull(result2.get("char_value"));
        assertEquals("", result2.get("varchar_value"));
        assertNull(result2.get("nvarchar_value"));
    }
}
