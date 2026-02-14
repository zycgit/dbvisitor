package net.hasor.dbvisitor.test.suite.mapping.xml;

import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * XML 映射 SQL 模版属性测试。
 * 覆盖：selectTemplate、insertTemplate、setValueTemplate、
 * whereColTemplate、whereValueTemplate。
 * 对应 annotation 侧由 annotation/SqlTemplateTest 覆盖。
 */
public class XmlTemplateTest extends AbstractOneApiTest {

    private static final String XML_RESOURCE = "mapping/template_attrs.xml";

    /** selectTemplate 和 insertTemplate 属性正确解析 */
    @Test
    public void testSelectAndInsertTemplate() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.template", "SelectInsertTemplateEntity");
        assertNotNull(mapping);

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertEquals("UPPER(name)", nameCol.getSelectTemplate());
        assertEquals("LOWER(?)", nameCol.getInsertTemplate());
        // 其他模版未设置 → null
        assertNull(nameCol.getSetValueTemplate());
        assertNull(nameCol.getWhereColTemplate());
    }

    /** setValueTemplate 和 whereColTemplate 属性正确解析 */
    @Test
    public void testSetValueAndWhereColTemplate() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.template", "UpdateWhereTemplateEntity");
        assertNotNull(mapping);

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertEquals("TRIM(?)", nameCol.getSetValueTemplate());
        assertEquals("LOWER(name)", nameCol.getWhereColTemplate());
        // 其他模版未设置 → null
        assertNull(nameCol.getSelectTemplate());
        assertNull(nameCol.getInsertTemplate());
    }

    /** 所有 5 个模版属性同时设置在同一列上，全部正确解析 */
    @Test
    public void testAllTemplates_Combined() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.template", "AllTemplatesEntity");
        assertNotNull(mapping);

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertEquals("UPPER(name)", nameCol.getSelectTemplate());
        assertEquals("LOWER(?)", nameCol.getInsertTemplate());
        assertEquals("TRIM(?)", nameCol.getSetValueTemplate());
        assertEquals("LOWER(name)", nameCol.getWhereColTemplate());
        assertEquals("LOWER(?)", nameCol.getWhereValueTemplate());
    }

    /** 未设置任何模版的列，所有模版属性为 null */
    @Test
    public void testNoTemplate_DefaultNull() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.template", "SelectInsertTemplateEntity");
        assertNotNull(mapping);

        // id 列没有设置任何模版
        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull(idCol);
        assertNull("id selectTemplate should be null", idCol.getSelectTemplate());
        assertNull("id insertTemplate should be null", idCol.getInsertTemplate());
        assertNull("id setValueTemplate should be null", idCol.getSetValueTemplate());
        assertNull("id whereColTemplate should be null", idCol.getWhereColTemplate());
        assertNull("id whereValueTemplate should be null", idCol.getWhereValueTemplate());
        assertNull("id orderByColTemplate should be null", idCol.getOrderByColTemplate());
    }
}
