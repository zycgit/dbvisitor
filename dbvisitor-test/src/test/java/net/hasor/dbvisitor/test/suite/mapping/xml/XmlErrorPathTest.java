package net.hasor.dbvisitor.test.suite.mapping.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * XML 映射错误路径 &amp; 边界测试。
 * 覆盖自定义 keyType 工厂类、javaType 不兼容、
 * 表级重复注册 ISE、未知 property 错误。
 */
public class XmlErrorPathTest extends AbstractOneApiTest {

    private static final String XML_BASE = "/mapping/";

    // ============ 自定义 GeneratedKeyHandlerFactory 类名 ============

    /**
     * XML keyType 指定全限定类名 Uuid32KeySeqHolderFactory → 成功加载并生效。
     */
    @Test
    public void testXml_customKeyTypeFactory() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_BASE + "custom_key_factory.xml");

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.customkey", "CustomKeyEntity");
        assertNotNull("Should load entity with custom keyType factory", mapping);

        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull(idCol);
        // 自定义工厂应产生 GeneratedKeyHandler
        assertNotNull("Custom factory should set keySeqHolder", idCol.getKeySeqHolder());
    }

    // ============ javaType 不兼容 ============

    /**
     * XML 声明 javaType="java.lang.String" 但属性实际类型为 Integer（id 字段）。
     * String.isAssignableFrom(Integer) == false → ClassCastException。
     */
    @Test
    public void testXml_incompatibleJavaType_throwsCCE() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        try {
            registry.loadMapping(XML_BASE + "bad_javatype.xml");
            fail("Should throw ClassCastException for incompatible javaType");
        } catch (ClassCastException e) {
            assertTrue(e.getMessage().contains("is not a subclass of"));
        }
    }

    // ============ 表级重复注册 (mapForLevel) ============

    /**
     * 同一 XML 文件中两个 entity 使用相同 id（映射到同一 table+name 组合）。
     * 第一个注册到 mapForSpace 和 mapForLevel，第二个触发 mapForSpace 的 ISE（先命中）。
     */
    @Test
    public void testXml_tableLevelDuplicate_throwsISE() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        try {
            registry.loadMapping(XML_BASE + "table_level_dup.xml");
            fail("Should throw IllegalStateException for duplicate entity id");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("already exists"));
        }
    }

    // ============ 未知 property ============

    /**
     * XML mapping 引用不存在的 property → NoSuchFieldException。
     */
    @Test
    public void testXml_unknownProperty_throwsException() {
        MappingRegistry registry = new MappingRegistry();
        try {
            registry.loadMapping(XML_BASE + "unknown_property.xml");
            fail("Should throw IOException wrapping NoSuchFieldException");
        } catch (IOException e) {
            assertTrue(e.getCause() instanceof NoSuchFieldException);
            assertTrue(e.getCause().getMessage().contains("nonExistentProperty"));
        }
    }

    // ============ loadMapping 空白/null 流 ============

    /**
     * loadMapping(blank string) 大静默返回（不抛异常）。
     */
    @Test
    public void testLoadMapping_blankResource_silent() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        // 空字符串应安静返回
        registry.loadMapping("   ");
        // 无异常即通过
    }

    /**
     * loadMapping(streamId, stream) 当 streamId 为空白时安静返回。
     */
    @Test
    public void testLoadMapping_blankStreamId_silent() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        InputStream dummyStream = new ByteArrayInputStream("<mapper/>".getBytes());
        registry.loadMapping("  ", dummyStream);
        // 无异常即通过
    }

    // ============ getGlobalOptions ============

    /**
     * MappingRegistry 构造后 getGlobalOptions() 返回初始 Options。
     */
    @Test
    public void testGetGlobalOptions() {
        Options opts = Options.of().catalog("mycat").schema("mysch");
        MappingRegistry registry = new MappingRegistry(null, null, opts);

        Options global = registry.getGlobalOptions();
        assertNotNull(global);
        assertEquals("mycat", global.getCatalog());
        assertEquals("mysch", global.getSchema());
    }

    // ============ findByTable 系列 ============

    /**
     * findByTable(catalog, schema, table) 三参数版本正确查找。
     */
    @Test
    public void testFindByTable_catalogSchemaTable() throws Exception {
        Options opts = Options.of().catalog("cat1").schema("sch1");
        MappingRegistry registry = new MappingRegistry(null, null, opts);

        registry.loadMapping(XML_BASE + "basic_entity_mapper.xml");

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.mapping", "FullEntity");
        assertNotNull(mapping);

        // FullEntity 的 catalog/schema 来自 XML （test_cat / test_sch）
        TableMapping<?> byTable = registry.findByTable("test_cat", "test_sch", "user_info");
        assertNotNull("Should find by catalog+schema+table", byTable);
        assertEquals("user_info", byTable.getTable());
    }

    /**
     * findByTable(nonexistent) 返回 null。
     */
    @Test
    public void testFindByTable_nonExistent_returnsNull() {
        MappingRegistry registry = new MappingRegistry();
        assertNull(registry.findByTable("no_such_table"));
    }
}
