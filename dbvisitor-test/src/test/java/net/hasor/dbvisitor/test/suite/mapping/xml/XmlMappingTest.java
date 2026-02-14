package net.hasor.dbvisitor.test.suite.mapping.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import net.hasor.cobble.io.IOUtils;
import net.hasor.dbvisitor.mapping.DdlAuto;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * XML 映射配置测试 (loadMapping 系列方法 + XmlTableMappingResolve)。
 * 覆盖: entity/resultMap 解析、XML 属性继承、索引、列描述符、keyType 等。
 */
public class XmlMappingTest extends AbstractOneApiTest {

    private static final String XML_RESOURCE = "mapping/basic_entity_mapper.xml";

    // ==================== loadMapping 基础 ====================

    /** 从 classpath 加载 XML mapper，验证 entity 元数据解析正确 */
    @Test
    public void testLoadMapping_EntityFromResource() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.mapping", "FullEntity");

        assertNotNull("Should find FullEntity by space+id", mapping);
        assertEquals("user_info", mapping.getTable());
        assertEquals("test_cat", mapping.getCatalog());
        assertEquals("test_sch", mapping.getSchema());
        assertEquals(UserInfo.class, mapping.entityType());
    }

    /** entity 的 TableDescribe 属性 (ddlAuto, character-set, collation, comment, other) */
    @Test
    public void testLoadMapping_EntityTableDescription() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.mapping", "FullEntity");

        assertNotNull(mapping.getDescription());
        assertEquals(DdlAuto.Create, mapping.getDescription().getDdlAuto());
        assertEquals("utf8mb4", mapping.getDescription().getCharacterSet());
        assertEquals("utf8mb4_general_ci", mapping.getDescription().getCollation());
        assertEquals("user table", mapping.getDescription().getComment());
        assertEquals("ENGINE=InnoDB", mapping.getDescription().getOther());
    }

    /** entity 列映射属性 (primary、insert、update) */
    @Test
    public void testLoadMapping_EntityColumnAttributes() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.mapping", "FullEntity");

        // id -> primary key
        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull(idCol);
        assertTrue("id should be primary", idCol.isPrimaryKey());

        // age -> insert=false
        ColumnMapping ageCol = mapping.getPropertyByName("age");
        assertNotNull(ageCol);
        assertFalse("age insert should be false", ageCol.isInsert());
        assertTrue("age update should be true (default)", ageCol.isUpdate());

        // email -> update=false
        ColumnMapping emailCol = mapping.getPropertyByName("email");
        assertNotNull(emailCol);
        assertTrue("email insert should be true (default)", emailCol.isInsert());
        assertFalse("email update should be false", emailCol.isUpdate());
    }

    /** XML 中的 <index> 标签解析 */
    @Test
    public void testLoadMapping_EntityIndexes() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.mapping", "FullEntity");

        assertEquals(2, mapping.getIndexes().size());

        assertNotNull(mapping.getIndex("idx_name"));
        assertEquals(1, mapping.getIndex("idx_name").getColumns().size());
        assertEquals("name", mapping.getIndex("idx_name").getColumns().get(0));
        assertFalse(mapping.getIndex("idx_name").isUnique());

        assertNotNull(mapping.getIndex("idx_email_age"));
        assertEquals(2, mapping.getIndex("idx_email_age").getColumns().size());
        assertTrue(mapping.getIndex("idx_email_age").isUnique());
        assertEquals("composite index", mapping.getIndex("idx_email_age").getComment());
    }

    /** resultMap 解析 */
    @Test
    public void testLoadMapping_ResultMap() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.mapping", "SimpleResultMap");

        assertNotNull("Should find SimpleResultMap", mapping);
        assertEquals(UserInfo.class, mapping.entityType());

        // resultMap 不是 entity，table 应为空
        assertEquals("", mapping.getTable());

        // 验证列映射
        Collection<ColumnMapping> props = mapping.getProperties();
        assertEquals(5, props.size());
        assertNotNull(mapping.getPropertyByName("id"));
        assertNotNull(mapping.getPropertyByName("name"));
        assertNotNull(mapping.getPropertyByName("createTime"));
    }

    /** autoMapping=false 时只有 XML 中声明的列 */
    @Test
    public void testLoadMapping_PartialEntity() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.mapping", "PartialEntity");

        assertNotNull(mapping);
        assertEquals(2, mapping.getProperties().size());
        assertNotNull("id should be mapped", mapping.getPropertyByName("id"));
        assertNotNull("name should be mapped", mapping.getPropertyByName("name"));
        assertNull("age should NOT be mapped", mapping.getPropertyByName("age"));
    }

    /** XML column description 属性 (sqlType, length, nullable, default, comment, precision, scale) */
    @Test
    public void testLoadMapping_ColumnDescription() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.mapping", "DescEntity");

        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull(idCol.getDescription());
        assertEquals("BIGINT", idCol.getDescription().getSqlType());
        assertFalse("id nullable should be false (primary)", idCol.getDescription().isNullable());

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol.getDescription());
        assertEquals("VARCHAR", nameCol.getDescription().getSqlType());
        assertEquals("128", nameCol.getDescription().getLength());
        assertEquals("'unknown'", nameCol.getDescription().getDefault());
        assertEquals("user name", nameCol.getDescription().getComment());

        ColumnMapping ageCol = mapping.getPropertyByName("age");
        assertNotNull(ageCol.getDescription());
        assertEquals("10", ageCol.getDescription().getPrecision());
        assertEquals("0", ageCol.getDescription().getScale());
    }

    // ==================== loadMapping 从流加载 ====================

    /** loadMapping(streamId, InputStream) 从输入流加载 */
    @Test
    public void testLoadMapping_FromInputStream() throws Exception {
        String xml = IOUtils.readToString(getClass().getResourceAsStream("/mapping/stream_test.xml"), "UTF-8");

        MappingRegistry registry = new MappingRegistry();
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            registry.loadMapping("test-stream-id", stream);
        }

        TableMapping<?> mapping = registry.findBySpace("stream.test", "StreamEntity");
        assertNotNull("Should load entity from stream", mapping);
        assertEquals("user_info", mapping.getTable());
    }

    // ==================== loadMapping 幂等和边界 ====================

    /** 同一 documentId 重复加载应幂等（tryLoaded 缓存） */
    @Test
    public void testLoadMapping_IdempotentLoad() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);
        registry.loadMapping(XML_RESOURCE); // 第二次应静默跳过

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.mapping", "FullEntity");
        assertNotNull("Should still work after double load", mapping);
    }

    /** 空白 resource 字符串不应抛异常 */
    @Test
    public void testLoadMapping_BlankResource() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(""); // should be silently ignored
        registry.loadMapping("  "); // should be silently ignored
    }

    /** type 为空时应抛 IOException */
    @Test(expected = IOException.class)
    public void testLoadMapping_EntityMissingType() throws Exception {
        String xml = IOUtils.readToString(getClass().getResourceAsStream("/mapping/err_missing_type.xml"), "UTF-8");

        MappingRegistry registry = new MappingRegistry();
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            registry.loadMapping("err-missing-type", stream);
        }
    }

    /** mapper 根元素上的 Options 属性应继承到子 entity */
    @Test
    public void testLoadMapping_RootLevelOptions() throws Exception {
        String xml = IOUtils.readToString(getClass().getResourceAsStream("/mapping/opt_root_level.xml"), "UTF-8");

        MappingRegistry registry = new MappingRegistry();
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            registry.loadMapping("opt-test", stream);
        }

        TableMapping<?> mapping = registry.findBySpace("opt.test", "InheritedOpt");
        assertNotNull(mapping);
        assertFalse("caseInsensitive from root should be false", mapping.isCaseInsensitive());
        assertTrue("useDelimited from root should be true", mapping.useDelimited());
    }

    /** entity 缺少 table 属性应抛 IOException */
    @Test(expected = IOException.class)
    public void testLoadMapping_EntityMissingTable() throws Exception {
        String xml = IOUtils.readToString(getClass().getResourceAsStream("/mapping/err_missing_table.xml"), "UTF-8");

        MappingRegistry registry = new MappingRegistry();
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            registry.loadMapping("err-missing-table", stream);
        }
    }

    /** 不存在的 property 应抛异常 */
    @Test(expected = Exception.class)
    public void testLoadMapping_UnknownProperty() throws Exception {
        String xml = IOUtils.readToString(getClass().getResourceAsStream("/mapping/err_bad_prop.xml"), "UTF-8");

        MappingRegistry registry = new MappingRegistry();
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            registry.loadMapping("err-bad-prop", stream);
        }
    }
}
