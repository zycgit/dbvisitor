package net.hasor.dbvisitor.test.suite.mapping.xml;

import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.handler.UpperCaseTypeHandler;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * XML 映射类型处理器和列描述补充属性测试。
 * 覆盖：typeHandler 自定义类、jdbcType 显式指定、
 * 列描述的 character-set/collation/other 补充属性。
 * 对应 annotation 侧由 annotation/TypeHandlerMappingTest 覆盖。
 */
public class XmlTypeHandlerTest extends AbstractOneApiTest {

    private static final String XML_RESOURCE = "mapping/type_handler_test.xml";

    /** typeHandler 属性指定自定义 TypeHandler 类名，正确加载 */
    @Test
    public void testTypeHandler_CustomClass() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.typehandler", "TypeHandlerEntity");
        assertNotNull(mapping);

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertNotNull("name typeHandler should not be null", nameCol.getTypeHandler());
        assertTrue("name typeHandler should be UpperCaseTypeHandler",//
                nameCol.getTypeHandler() instanceof UpperCaseTypeHandler);
    }

    /** jdbcType 属性显式指定 JDBC 类型号 */
    @Test
    public void testJdbcType_Explicit() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.typehandler", "JdbcTypeEntity");
        assertNotNull(mapping);

        // id → jdbcType=4 (INTEGER)
        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull(idCol);
        assertNotNull("id jdbcType should not be null", idCol.getJdbcType());
        assertEquals("id jdbcType should be 4 (INTEGER)", Integer.valueOf(4), idCol.getJdbcType());

        // name → jdbcType=12 (VARCHAR)
        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertNotNull("name jdbcType should not be null", nameCol.getJdbcType());
        assertEquals("name jdbcType should be 12 (VARCHAR)", Integer.valueOf(12), nameCol.getJdbcType());
    }

    /** 列描述的 character-set、collation、other 补充属性正确解析 */
    @Test
    public void testColumnDescription_CharsetCollationOther() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.typehandler", "DescCharsetEntity");
        assertNotNull(mapping);

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertNotNull("name description should not be null", nameCol.getDescription());
        assertEquals("utf8mb4", nameCol.getDescription().getCharacterSet());
        assertEquals("utf8mb4_bin", nameCol.getDescription().getCollation());
        assertEquals("COMMENT 'extra'", nameCol.getDescription().getOther());
    }
}
