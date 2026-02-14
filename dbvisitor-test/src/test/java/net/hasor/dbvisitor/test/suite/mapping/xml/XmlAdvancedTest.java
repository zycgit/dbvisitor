package net.hasor.dbvisitor.test.suite.mapping.xml;

import java.util.Collection;
import java.util.List;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.IndexDescription;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * XML 映射高级场景测试。
 * 覆盖不支持的标签、index &lt;column&gt; 子元素、无映射回退到类注解、
 * index 空列校验等。
 */
public class XmlAdvancedTest extends AbstractOneApiTest {

    private static final String XML_BASE = "/mapping/";

    // ============== 不支持的标签 ==============

    /**
     * XML entity 中出现不支持的子标签 → UnsupportedOperationException。
     * unsupported_tag.xml 包含 &lt;badtag&gt; 元素。
     */
    @Test
    public void testXml_unsupportedTag_throwsException() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        try {
            registry.loadMapping(XML_BASE + "unsupported_tag.xml");
            fail("Should throw UnsupportedOperationException for unsupported tag");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("Unsupported"));
        }
    }

    // ============== index 用 <column> 子元素方式 ==============

    /**
     * XML index 使用 &lt;column&gt; 子元素声明索引列（替代 columns 属性）。
     */
    @Test
    public void testXml_indexWithColumnSubElements() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_BASE + "index_column_sub.xml");

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.indexcolsub", "IndexColSubEntity");
        assertNotNull(mapping);

        IndexDescription idx = mapping.getIndex("idx_cols_sub");
        assertNotNull("Should find index 'idx_cols_sub'", idx);
        assertTrue("Should be unique", idx.isUnique());
        assertEquals("index via sub elements", idx.getComment());

        List<String> cols = idx.getColumns();
        assertEquals("Should have 2 columns", 2, cols.size());
        assertEquals("name", cols.get(0));
        assertEquals("email", cols.get(1));
    }

    // ============== index 空列校验 ==============

    /**
     * XML index 既无 columns 属性也无 &lt;column&gt; 子元素 → IllegalArgumentException。
     */
    @Test
    public void testXml_indexEmptyColumns_throwsIAE() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        try {
            registry.loadMapping(XML_BASE + "index_empty_cols.xml");
            fail("Should throw IllegalArgumentException for empty index columns");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("columns is empty"));
        }
    }

    // ============== 无映射回退到类注解 ==============

    /**
     * XML entity 无 &lt;id&gt;/&lt;result&gt;/&lt;mapping&gt;/&lt;index&gt; 子元素时，
     * 回退到 ClassTableMappingResolve 使用类注解解析。
     * UserInfo 类有 @Column(primary=true) 在 id 上、@Column("create_time") 在 createTime 上。
     */
    @Test
    public void testXml_noMappingFallbackToClassAnnotations() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_BASE + "no_mapping_entity.xml");

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.nomapping", "NoMappingEntity");
        assertNotNull("Should load entity via no-mapping fallback", mapping);
        assertEquals("user_info", mapping.getTable());

        // UserInfo 有 @Column(primary=true) 在 id 上
        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull("Should have 'id' from class annotation", idCol);
        assertTrue("id should be primary key from @Column(primary=true)", idCol.isPrimaryKey());

        // UserInfo 有 @Column("create_time") 在 createTime 上
        ColumnMapping ctCol = mapping.getPropertyByName("createTime");
        assertNotNull("Should have 'createTime' from class annotation", ctCol);
        assertEquals("create_time", ctCol.getColumn());

        // 自动映射的字段也应存在
        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull("Auto-mapped 'name' should exist", nameCol);
    }

    // ============== XML Options 继承到 index ==============

    /**
     * XML entity 中 index 属性集完整：name, columns, unique, comment, other。
     * 验证 index_column_sub.xml 中 index 的 other 属性。
     */
    @Test
    public void testXml_indexAttributes_complete() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_BASE + "index_column_sub.xml");

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.indexcolsub", "IndexColSubEntity");
        assertNotNull(mapping);

        IndexDescription idx = mapping.getIndex("idx_cols_sub");
        assertNotNull(idx);
        assertEquals("idx_cols_sub", idx.getName());
        assertTrue(idx.isUnique());
        assertEquals("index via sub elements", idx.getComment());
        // other 属性未设置 → null
        assertNull("other not set → null", idx.getOther());
    }

    // ============== entity 中 mapping 列数验证 ==============

    /**
     * XML entity 显式映射部分列（此处仅 id + name + email）。
     * 验证属性数量与 id/result 标签一致。
     */
    @Test
    public void testXml_explicitMappingColumnCount() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_BASE + "index_column_sub.xml");

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.indexcolsub", "IndexColSubEntity");
        assertNotNull(mapping);

        // index_column_sub.xml 定义了 <id> id + <mapping> name + <mapping> email
        // autoMapping 默认 true → 剩余字段也会自动映射
        Collection<ColumnMapping> props = mapping.getProperties();
        assertTrue("Should have at least 3 explicitly mapped properties", props.size() >= 3);

        // 显式映射的列
        assertNotNull(mapping.getPropertyByName("id"));
        assertNotNull(mapping.getPropertyByName("name"));
        assertNotNull(mapping.getPropertyByName("email"));
    }
}
