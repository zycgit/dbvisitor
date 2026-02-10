package net.hasor.dbvisitor.test.oneapi.suite.mapping;

import net.hasor.dbvisitor.mapping.DdlAuto;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * 枚举解析测试。
 * 验证 DdlAuto.valueOfCode() 和 KeyType.valueOfCode() 的字符串→枚举转换逻辑。
 */
public class EnumParseTest extends AbstractOneApiTest {

    // ============ DdlAuto.valueOfCode ============

    /** 标准枚举名称（不区分大小写） */
    @Test
    public void testDdlAutoValueOfCode_ExactName() {
        assertEquals(DdlAuto.None, DdlAuto.valueOfCode("None"));
        assertEquals(DdlAuto.Create, DdlAuto.valueOfCode("Create"));
        assertEquals(DdlAuto.AddColumn, DdlAuto.valueOfCode("AddColumn"));
        assertEquals(DdlAuto.Update, DdlAuto.valueOfCode("Update"));
        assertEquals(DdlAuto.CreateDrop, DdlAuto.valueOfCode("CreateDrop"));
    }

    /** 不区分大小写 */
    @Test
    public void testDdlAutoValueOfCode_CaseInsensitive() {
        assertEquals(DdlAuto.Create, DdlAuto.valueOfCode("create"));
        assertEquals(DdlAuto.Create, DdlAuto.valueOfCode("CREATE"));
        assertEquals(DdlAuto.AddColumn, DdlAuto.valueOfCode("addcolumn"));
        assertEquals(DdlAuto.AddColumn, DdlAuto.valueOfCode("ADDCOLUMN"));
        assertEquals(DdlAuto.Update, DdlAuto.valueOfCode("update"));
        assertEquals(DdlAuto.CreateDrop, DdlAuto.valueOfCode("createdrop"));
    }

    /** 别名支持 */
    @Test
    public void testDdlAutoValueOfCode_Aliases() {
        assertEquals(DdlAuto.AddColumn, DdlAuto.valueOfCode("add"));
        assertEquals(DdlAuto.CreateDrop, DdlAuto.valueOfCode("create-drop"));
    }

    /** null/空字符串返回 None */
    @Test
    public void testDdlAutoValueOfCode_NullAndEmpty() {
        assertEquals(DdlAuto.None, DdlAuto.valueOfCode(null));
        assertEquals(DdlAuto.None, DdlAuto.valueOfCode(""));
        assertEquals(DdlAuto.None, DdlAuto.valueOfCode("   "));
    }

    /** 无匹配返回 None */
    @Test
    public void testDdlAutoValueOfCode_InvalidReturnsNone() {
        assertEquals(DdlAuto.None, DdlAuto.valueOfCode("invalid"));
        assertEquals(DdlAuto.None, DdlAuto.valueOfCode("xyz"));
    }

    // ============ KeyType.valueOfCode ============

    /** 标准枚举名称 */
    @Test
    public void testKeyTypeValueOfCode_ExactName() {
        assertEquals(KeyType.None, KeyType.valueOfCode("None"));
        assertEquals(KeyType.Auto, KeyType.valueOfCode("Auto"));
        assertEquals(KeyType.UUID32, KeyType.valueOfCode("UUID32"));
        assertEquals(KeyType.UUID36, KeyType.valueOfCode("UUID36"));
        assertEquals(KeyType.Sequence, KeyType.valueOfCode("Sequence"));
        assertEquals(KeyType.Holder, KeyType.valueOfCode("Holder"));
    }

    /** 不区分大小写 */
    @Test
    public void testKeyTypeValueOfCode_CaseInsensitive() {
        assertEquals(KeyType.Auto, KeyType.valueOfCode("auto"));
        assertEquals(KeyType.Auto, KeyType.valueOfCode("AUTO"));
        assertEquals(KeyType.UUID32, KeyType.valueOfCode("uuid32"));
        assertEquals(KeyType.UUID36, KeyType.valueOfCode("uuid36"));
        assertEquals(KeyType.Sequence, KeyType.valueOfCode("sequence"));
        assertEquals(KeyType.Holder, KeyType.valueOfCode("holder"));
    }

    /** null 或无匹配返回 null */
    @Test
    public void testKeyTypeValueOfCode_InvalidReturnsNull() {
        assertNull("Invalid code should return null", KeyType.valueOfCode("invalid"));
        assertNull("Null code should return null", KeyType.valueOfCode(null));
    }
}
