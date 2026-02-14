package net.hasor.dbvisitor.test.suite.mapping.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import net.hasor.cobble.io.IOUtils;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * XML 映射 Options 继承与覆盖测试。
 * 覆盖：entity 覆盖 mapper 根级别选项、Global Options 的 catalog/schema 继承、
 * resultMap 的 autoMapping 行为、result 和 mapping 标签等价性。
 */
public class XmlOptionsInheritanceTest extends AbstractOneApiTest {

    /** entity 级别 caseInsensitive="false" 覆盖 mapper 根级别 caseInsensitive="true" */
    @Test
    public void testEntityOverridesRoot_CaseInsensitive() throws Exception {
        String xml = IOUtils.readToString(getClass().getResourceAsStream("/mapping/opt_ci.xml"), "UTF-8");

        MappingRegistry registry = new MappingRegistry();
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            registry.loadMapping("opt-ci", stream);
        }

        TableMapping<?> mapping = registry.findBySpace("opt.ci", "OverrideCI");
        assertNotNull(mapping);
        assertFalse("Entity CI=false should override root CI=true", mapping.isCaseInsensitive());
    }

    /** entity 级别 autoMapping="false" 覆盖 mapper 根级别 autoMapping="true" */
    @Test
    public void testEntityOverridesRoot_AutoMapping() throws Exception {
        String xml = IOUtils.readToString(getClass().getResourceAsStream("/mapping/opt_am.xml"), "UTF-8");

        MappingRegistry registry = new MappingRegistry();
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            registry.loadMapping("opt-am", stream);
        }

        TableMapping<?> mapping = registry.findBySpace("opt.am", "OverrideAM");
        assertNotNull(mapping);
        assertFalse("Entity AM=false should override root AM=true", mapping.isAutoProperty());
    }

    /** Global Options 的 catalog/schema 被 entity 继承（entity 不设置时使用全局值） */
    @Test
    public void testGlobalOptions_CatalogSchemaInherited() throws Exception {
        String xml = IOUtils.readToString(getClass().getResourceAsStream("/mapping/opt_global.xml"), "UTF-8");

        Options globalOpts = Options.of().catalog("global_cat").schema("global_sch");
        MappingRegistry registry = new MappingRegistry(null, null, globalOpts);
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            registry.loadMapping("opt-global", stream);
        }

        TableMapping<?> mapping = registry.findBySpace("opt.global", "GlobalEntity");
        assertNotNull(mapping);
        assertEquals("Should inherit global catalog", "global_cat", mapping.getCatalog());
        assertEquals("Should inherit global schema", "global_sch", mapping.getSchema());
    }

    /** entity 设置 catalog/schema 时覆盖 Global Options 的值 */
    @Test
    public void testGlobalOptions_EntityOverridesGlobal() throws Exception {
        String xml = IOUtils.readToString(getClass().getResourceAsStream("/mapping/opt_override.xml"), "UTF-8");

        Options globalOpts = Options.of().catalog("global_cat").schema("global_sch");
        MappingRegistry registry = new MappingRegistry(null, null, globalOpts);
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            registry.loadMapping("opt-override", stream);
        }

        TableMapping<?> mapping = registry.findBySpace("opt.override", "OverrideEntity");
        assertNotNull(mapping);
        assertEquals("Entity catalog should override global", "entity_cat", mapping.getCatalog());
        assertEquals("Entity schema should override global", "entity_sch", mapping.getSchema());
    }

    /** resultMap 的 autoMapping="false"，只包含显式声明的列 */
    @Test
    public void testResultMap_AutoMappingFalse() throws Exception {
        String xml = IOUtils.readToString(getClass().getResourceAsStream("/mapping/opt_rm.xml"), "UTF-8");

        MappingRegistry registry = new MappingRegistry();
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            registry.loadMapping("opt-rm", stream);
        }

        TableMapping<?> mapping = registry.findBySpace("opt.rm", "PartialResultMap");
        assertNotNull(mapping);
        assertFalse("resultMap autoMapping should be false", mapping.isAutoProperty());
        assertEquals("Should have exactly 2 declared columns", 2, mapping.getProperties().size());
        // resultMap 的 table 应为空
        assertEquals("resultMap table should be empty", "", mapping.getTable());
    }

    /** resultMap 的 caseInsensitive 属性正确继承 */
    @Test
    public void testResultMap_CaseInsensitive() throws Exception {
        String xml = IOUtils.readToString(getClass().getResourceAsStream("/mapping/opt_rmci.xml"), "UTF-8");

        MappingRegistry registry = new MappingRegistry();
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            registry.loadMapping("opt-rmci", stream);
        }

        TableMapping<?> mapping = registry.findBySpace("opt.rmci", "CIResultMap");
        assertNotNull(mapping);
        assertTrue("resultMap should inherit root caseInsensitive=true", mapping.isCaseInsensitive());
    }

    /** entity 不设置 autoMapping（默认 true）→ isAutoProperty() 为 true */
    @Test
    public void testAutoMapping_DefaultTrue() throws Exception {
        String xml = IOUtils.readToString(getClass().getResourceAsStream("/mapping/opt_defaultam.xml"), "UTF-8");

        MappingRegistry registry = new MappingRegistry();
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            registry.loadMapping("opt-defaultam", stream);
        }

        TableMapping<?> mapping = registry.findBySpace("opt.defaultam", "DefaultAM");
        assertNotNull(mapping);
        assertTrue("Default autoMapping should be true", mapping.isAutoProperty());
    }
}
