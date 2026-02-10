package net.hasor.dbvisitor.test.oneapi.suite.mapping.xml;

import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * XML 映射写入策略（insert/update 属性）测试。
 * 覆盖：insert="false"、update="false"、双 false 只读、默认 true。
 * 对应 annotation 侧由 annotation/WritePolicyTest 覆盖。
 */
public class XmlWritePolicyTest extends AbstractOneApiTest {

    private static final String XML_RESOURCE = "oneapi/mapping/write_policy_test.xml";

    /** insert="false" 的列 isInsert() 为 false，isUpdate() 保持默认 true */
    @Test
    public void testInsertFalse_Metadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.writepolicy", "InsertFalseEntity");
        assertNotNull(mapping);

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertFalse("name insert should be false", nameCol.isInsert());
        assertTrue("name update should be true (default)", nameCol.isUpdate());

        // age 列未设置，默认均为 true
        ColumnMapping ageCol = mapping.getPropertyByName("age");
        assertNotNull(ageCol);
        assertTrue("age insert should be true (default)", ageCol.isInsert());
        assertTrue("age update should be true (default)", ageCol.isUpdate());
    }

    /** update="false" 的列 isUpdate() 为 false，isInsert() 保持默认 true */
    @Test
    public void testUpdateFalse_Metadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.writepolicy", "UpdateFalseEntity");
        assertNotNull(mapping);

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertTrue("name insert should be true (default)", nameCol.isInsert());
        assertFalse("name update should be false", nameCol.isUpdate());
    }

    /** insert="false" update="false" 的列两者均为 false（完全只读） */
    @Test
    public void testBothFalse_ReadOnly() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.writepolicy", "BothFalseEntity");
        assertNotNull(mapping);

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertFalse("name insert should be false", nameCol.isInsert());
        assertFalse("name update should be false", nameCol.isUpdate());

        // 其他列不受影响
        ColumnMapping ageCol = mapping.getPropertyByName("age");
        assertNotNull(ageCol);
        assertTrue("age insert should be true (default)", ageCol.isInsert());
        assertTrue("age update should be true (default)", ageCol.isUpdate());
    }

    /** 不设置 insert/update 属性时，默认均为 true */
    @Test
    public void testDefaultPolicy_BothTrue() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.writepolicy", "DefaultPolicyEntity");
        assertNotNull(mapping);

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertTrue("name insert should be true (default)", nameCol.isInsert());
        assertTrue("name update should be true (default)", nameCol.isUpdate());

        ColumnMapping ageCol = mapping.getPropertyByName("age");
        assertNotNull(ageCol);
        assertTrue("age insert should be true (default)", ageCol.isInsert());
        assertTrue("age update should be true (default)", ageCol.isUpdate());
    }
}
