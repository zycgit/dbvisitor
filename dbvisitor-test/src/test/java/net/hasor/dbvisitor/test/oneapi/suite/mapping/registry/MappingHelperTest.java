package net.hasor.dbvisitor.test.oneapi.suite.mapping.registry;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.mapping.MappingHelper;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * MappingHelper 工具方法测试。
 * 覆盖: typeName 反向映射、typeMappingOr 别名解析、NameInfo 等。
 */
public class MappingHelperTest extends AbstractOneApiTest {

    // ==================== typeName 反向映射 ====================

    /** 基本类型 → 别名 */
    @Test
    public void testTypeName_PrimitiveTypes() {
        assertEquals("int", MappingHelper.typeName(int.class));
        assertEquals("long", MappingHelper.typeName(long.class));
        // boolean → "bool" because putTypeMap("bool", boolean.class) overwrites "boolean" in reverse map
        assertEquals("bool", MappingHelper.typeName(boolean.class));
        assertEquals("double", MappingHelper.typeName(double.class));
        assertEquals("float", MappingHelper.typeName(float.class));
        assertEquals("byte", MappingHelper.typeName(byte.class));
        assertEquals("short", MappingHelper.typeName(short.class));
        assertEquals("char", MappingHelper.typeName(char.class));
    }

    /** 常用对象类型 → 别名 */
    @Test
    public void testTypeName_CommonTypes() {
        assertEquals("string", MappingHelper.typeName(String.class));
        assertEquals("decimal", MappingHelper.typeName(BigDecimal.class));
        assertEquals("bigint", MappingHelper.typeName(BigInteger.class));
        assertEquals("map", MappingHelper.typeName(Map.class));
        assertEquals("date", MappingHelper.typeName(java.util.Date.class));
        assertEquals("void", MappingHelper.typeName(void.class));
    }

    /** 未注册的类型 → 返回 class 全名 */
    @Test
    public void testTypeName_UnregisteredType() {
        assertEquals(ArrayList.class.getName(), MappingHelper.typeName(ArrayList.class));
    }

    /** null → null */
    @Test
    public void testTypeName_Null() {
        assertNull(MappingHelper.typeName(null));
    }

    // ==================== typeMappingOr 别名解析 ====================

    /** 已知别名(含大小写不敏感) → 对应 class */
    @Test
    public void testTypeMappingOr_KnownAliases() throws Exception {
        assertEquals(int.class, MappingHelper.typeMappingOr("int", s -> null));
        assertEquals(int.class, MappingHelper.typeMappingOr("INT", s -> null));
        assertEquals(int.class, MappingHelper.typeMappingOr("Int", s -> null));
        assertEquals(String.class, MappingHelper.typeMappingOr("string", s -> null));
        assertEquals(String.class, MappingHelper.typeMappingOr("STRING", s -> null));
        assertEquals(boolean.class, MappingHelper.typeMappingOr("bool", s -> null));
        assertEquals(byte[].class, MappingHelper.typeMappingOr("bytes", s -> null));
        assertEquals(Number.class, MappingHelper.typeMappingOr("number", s -> null));
    }

    /** 未知别名 → 调用 defaultType 函数 */
    @Test
    public void testTypeMappingOr_UnknownAlias() throws Exception {
        Class<?> result = MappingHelper.typeMappingOr("java.util.ArrayList", className -> Class.forName(className));
        assertEquals(ArrayList.class, result);
    }

    /** null → null */
    @Test
    public void testTypeMappingOr_Null() throws Exception {
        assertNull(MappingHelper.typeMappingOr(null, s -> String.class));
    }

    /** 时间类型别名 */
    @Test
    public void testTypeMappingOr_TimeAliases() throws Exception {
        assertEquals(java.sql.Date.class, MappingHelper.typeMappingOr("sqldate", s -> null));
        assertEquals(java.sql.Time.class, MappingHelper.typeMappingOr("sqltime", s -> null));
        assertEquals(java.sql.Timestamp.class, MappingHelper.typeMappingOr("sqltimestamp", s -> null));
        assertEquals(java.time.LocalDate.class, MappingHelper.typeMappingOr("localdate", s -> null));
        assertEquals(java.time.LocalTime.class, MappingHelper.typeMappingOr("localtime", s -> null));
        assertEquals(java.time.LocalDateTime.class, MappingHelper.typeMappingOr("localdatetime", s -> null));
        assertEquals(java.time.OffsetDateTime.class, MappingHelper.typeMappingOr("offsetdatetime", s -> null));
        assertEquals(java.time.OffsetTime.class, MappingHelper.typeMappingOr("offsettime", s -> null));
    }

    /** 特殊类型别名: url, uri, map 变体 */
    @Test
    public void testTypeMappingOr_SpecialAliases() throws Exception {
        assertEquals(URL.class, MappingHelper.typeMappingOr("url", s -> null));
        assertEquals(URI.class, MappingHelper.typeMappingOr("uri", s -> null));
        assertEquals(HashMap.class, MappingHelper.typeMappingOr("hashmap", s -> null));
        assertEquals(LinkedHashMap.class, MappingHelper.typeMappingOr("linkedmap", s -> null));
        assertEquals(LinkedCaseInsensitiveMap.class, MappingHelper.typeMappingOr("caseinsensitivemap", s -> null));
    }

    /** Year/YearMonth/Month/MonthDay 别名 */
    @Test
    public void testTypeMappingOr_CalendarAliases() throws Exception {
        assertEquals(java.time.Year.class, MappingHelper.typeMappingOr("year", s -> null));
        assertEquals(java.time.YearMonth.class, MappingHelper.typeMappingOr("yearmonth", s -> null));
        assertEquals(java.time.Month.class, MappingHelper.typeMappingOr("month", s -> null));
        assertEquals(java.time.MonthDay.class, MappingHelper.typeMappingOr("monthday", s -> null));
    }

    // ==================== caseInsensitive ====================

    /** Options null → true (默认大小写不敏感) */
    @Test
    public void testCaseInsensitive_Null() {
        assertTrue(MappingHelper.caseInsensitive(null));
    }

    // ==================== isEntity ====================

    /** isEntity 静态方法检测注解 */
    @Test
    public void testIsEntity() {
        assertTrue(MappingRegistry.isEntity(UserInfo.class));
        assertFalse(MappingRegistry.isEntity(String.class));
    }
}
