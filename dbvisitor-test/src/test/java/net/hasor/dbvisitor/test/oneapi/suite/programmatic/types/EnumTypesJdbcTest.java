package net.hasor.dbvisitor.test.oneapi.suite.programmatic.types;
import java.sql.SQLException;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.types.SimpleEnum;
import net.hasor.dbvisitor.test.oneapi.model.types.StatusEnumOfCode;
import net.hasor.dbvisitor.test.oneapi.model.types.StatusEnumOfValue;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 枚举类型测试 - Programmatic API (JdbcTemplate)
 * 测试范围：
 * 1. 普通枚举 - 直接使用枚举类型查询（通过 name() 自动映射）
 * 2. EnumOfCode 接口 - 自定义字符串代码映射
 * 3. EnumOfValue 接口 - 数值代码映射
 * 4. 枚举存储为 ordinal（整数序号）
 * 5. null 值处理
 * 6. 无效值处理
 * 数据库初始化：通过 /oneapi/sql/{dialect}/init.sql 脚本自动创建 enum_types_explicit_test 表
 * 重点：JdbcTemplate 通过 EnumTypeHandler 支持直接使用枚举类型作为查询结果类型
 * 枚举类型定义位于：net.hasor.dbvisitor.test.oneapi.model.types
 */
public class EnumTypesJdbcTest extends AbstractOneApiTest {

    @Override
    protected void cleanTestData() {
        try {
            jdbcTemplate.executeUpdate("DELETE FROM enum_types_explicit_test");
        } catch (Exception e) {
            // Ignore
        }
        super.cleanTestData();
    }

    /**
     * 测试普通枚举 - 直接使用枚举类型查询（通过 name() 自动映射）
     */
    @Test
    public void testSimpleEnum_ByName() throws SQLException {
        // 插入枚举的 name() 值
        String insertSql = "INSERT INTO enum_types_explicit_test (id, status_string) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 1, SimpleEnum.ACTIVE.name() });

        // 直接使用枚举类型查询 - EnumTypeHandler 自动处理
        String selectSql = "SELECT status_string FROM enum_types_explicit_test WHERE id = ?";
        SimpleEnum loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 1 }, SimpleEnum.class);

        assertNotNull(loaded);
        assertEquals(SimpleEnum.ACTIVE, loaded);

        // 测试所有枚举值
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 2, SimpleEnum.INACTIVE.name() });
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 3, SimpleEnum.DELETED.name() });

        SimpleEnum loaded2 = jdbcTemplate.queryForObject(selectSql, new Object[] { 2 }, SimpleEnum.class);
        assertEquals(SimpleEnum.INACTIVE, loaded2);

        SimpleEnum loaded3 = jdbcTemplate.queryForObject(selectSql, new Object[] { 3 }, SimpleEnum.class);
        assertEquals(SimpleEnum.DELETED, loaded3);
    }

    /**
     * 测试 EnumOfCode 接口 - 自定义字符串代码映射
     */
    @Test
    public void testEnumOfCode_CustomStringMapping() throws SQLException {
        // 插入自定义字符串代码（"active", "inactive", "deleted"）
        String insertSql = "INSERT INTO enum_types_explicit_test (id, status_string) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 4, "active" });
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 5, "inactive" });
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 6, "deleted" });

        // 直接使用枚举类型查询 - EnumTypeHandler 通过 EnumOfCode 接口自动映射
        String selectSql = "SELECT status_string FROM enum_types_explicit_test WHERE id = ?";

        StatusEnumOfCode loaded1 = jdbcTemplate.queryForObject(selectSql, new Object[] { 4 }, StatusEnumOfCode.class);
        assertEquals(StatusEnumOfCode.ACTIVE, loaded1);
        assertEquals("active", loaded1.codeName());

        StatusEnumOfCode loaded2 = jdbcTemplate.queryForObject(selectSql, new Object[] { 5 }, StatusEnumOfCode.class);
        assertEquals(StatusEnumOfCode.INACTIVE, loaded2);
        assertEquals("inactive", loaded2.codeName());

        StatusEnumOfCode loaded3 = jdbcTemplate.queryForObject(selectSql, new Object[] { 6 }, StatusEnumOfCode.class);
        assertEquals(StatusEnumOfCode.DELETED, loaded3);
        assertEquals("deleted", loaded3.codeName());
    }

    /**
     * 测试 EnumOfValue 接口 - 数值代码映射
     */
    @Test
    public void testEnumOfValue_IntegerMapping() throws SQLException {
        // 插入数值代码（1, 0, -1）
        String insertSql = "INSERT INTO enum_types_explicit_test (id, status_code) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 7, 1 });
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 8, 0 });
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 9, -1 });

        // 直接使用枚举类型查询 - EnumTypeHandler 通过 EnumOfValue 接口自动映射
        String selectSql = "SELECT status_code FROM enum_types_explicit_test WHERE id = ?";

        StatusEnumOfValue loaded1 = jdbcTemplate.queryForObject(selectSql, new Object[] { 7 }, StatusEnumOfValue.class);
        assertEquals(StatusEnumOfValue.ACTIVE, loaded1);
        assertEquals(1, loaded1.codeValue());

        StatusEnumOfValue loaded2 = jdbcTemplate.queryForObject(selectSql, new Object[] { 8 }, StatusEnumOfValue.class);
        assertEquals(StatusEnumOfValue.INACTIVE, loaded2);
        assertEquals(0, loaded2.codeValue());

        StatusEnumOfValue loaded3 = jdbcTemplate.queryForObject(selectSql, new Object[] { 9 }, StatusEnumOfValue.class);
        assertEquals(StatusEnumOfValue.DELETED, loaded3);
        assertEquals(-1, loaded3.codeValue());
    }

    /**
     * 测试枚举存储为 ordinal - 整数序号
     * 注意：需要使用 EnumOfValue 接口来正确处理整数映射
     */
    @Test
    public void testEnumAsOrdinal_IntegerIndex() throws SQLException {
        // StatusEnumOfValue: ACTIVE=1, INACTIVE=0, DELETED=-1
        String insertSql = "INSERT INTO enum_types_explicit_test (id, status_ordinal) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 10, 1 }); // ACTIVE
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 11, 0 }); // INACTIVE
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 12, -1 }); // DELETED

        // 直接使用枚举类型查询 - EnumTypeHandler 通过 EnumOfValue 接口自动映射
        String selectSql = "SELECT status_ordinal FROM enum_types_explicit_test WHERE id = ?";

        StatusEnumOfValue loaded1 = jdbcTemplate.queryForObject(selectSql, new Object[] { 10 }, StatusEnumOfValue.class);
        assertEquals(StatusEnumOfValue.ACTIVE, loaded1);
        assertEquals(1, loaded1.codeValue());

        StatusEnumOfValue loaded2 = jdbcTemplate.queryForObject(selectSql, new Object[] { 11 }, StatusEnumOfValue.class);
        assertEquals(StatusEnumOfValue.INACTIVE, loaded2);
        assertEquals(0, loaded2.codeValue());

        StatusEnumOfValue loaded3 = jdbcTemplate.queryForObject(selectSql, new Object[] { 12 }, StatusEnumOfValue.class);
        assertEquals(StatusEnumOfValue.DELETED, loaded3);
        assertEquals(-1, loaded3.codeValue());
    }

    /**
     * 测试枚举 null 值处理
     */
    @Test
    public void testEnumNullValues_AllTypes() throws SQLException {
        String insertSql = "INSERT INTO enum_types_explicit_test (id, status_string, status_ordinal, status_code) VALUES (?, ?, ?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 13, null, null, null });

        // 测试普通枚举 null (字符串列)
        SimpleEnum simpleEnum = jdbcTemplate.queryForObject("SELECT status_string FROM enum_types_explicit_test WHERE id = ?", new Object[] { 13 }, SimpleEnum.class);
        assertNull(simpleEnum);

        // 测试 EnumOfCode null (字符串列)
        StatusEnumOfCode enumOfCode = jdbcTemplate.queryForObject("SELECT status_string FROM enum_types_explicit_test WHERE id = ?", new Object[] { 13 }, StatusEnumOfCode.class);
        assertNull(enumOfCode);

        // 测试 EnumOfValue null (整数列)
        StatusEnumOfValue enumOfValue = jdbcTemplate.queryForObject("SELECT status_code FROM enum_types_explicit_test WHERE id = ?", new Object[] { 13 }, StatusEnumOfValue.class);
        assertNull(enumOfValue);

        // 测试 EnumOfValue null (ordinal 列)
        StatusEnumOfValue enumOfValueOrdinal = jdbcTemplate.queryForObject("SELECT status_ordinal FROM enum_types_explicit_test WHERE id = ?", new Object[] { 13 }, StatusEnumOfValue.class);
        assertNull(enumOfValueOrdinal);
    }

    /**
     * 测试枚举无效值处理
     */
    @Test
    public void testEnumInvalidValues_Handling() throws SQLException {
        // 插入无效的枚举名称
        jdbcTemplate.executeUpdate("INSERT INTO enum_types_explicit_test (id, status_string) VALUES (?, ?)", new Object[] { 14, "UNKNOWN" });

        try {
            // 尝试查询无效的枚举值 - 应该抛出异常
            jdbcTemplate.queryForObject("SELECT status_string FROM enum_types_explicit_test WHERE id = ?", new Object[] { 14 }, SimpleEnum.class);
            fail("Should throw exception for invalid enum value");
        } catch (Exception e) {
            // Expected - 无效的枚举值会导致异常
            assertTrue(e.getMessage().contains("UNKNOWN") || e.getCause().getMessage().contains("UNKNOWN"));
        }

        // 插入无效的 EnumOfValue 代码
        jdbcTemplate.executeUpdate("INSERT INTO enum_types_explicit_test (id, status_code) VALUES (?, ?)", new Object[] { 15, 999 });

        try {
            // 尝试查询无效的数值代码 - EnumOfValue.valueOfCode 返回 null 会导致问题
            StatusEnumOfValue result = jdbcTemplate.queryForObject("SELECT status_code FROM enum_types_explicit_test WHERE id = ?", new Object[] { 15 }, StatusEnumOfValue.class);
            // 如果没有抛出异常，结果应该为 null
            assertNull(result);
        } catch (Exception e) {
            // 也可能抛出异常，这也是合理的
        }
    }
}