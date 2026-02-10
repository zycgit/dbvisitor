package net.hasor.dbvisitor.test.oneapi.suite.mapping.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import net.hasor.cobble.io.IOUtils;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * XML 映射命名选项测试（mapUnderscoreToCamelCase、caseInsensitive、useDelimited）。
 * 覆盖：entity 级别设置、mapper 根级别继承、entity 覆盖根级别、默认值。
 * 对应 annotation 侧由 naming/ 目录测试套件覆盖。
 */
public class XmlNamingTest extends AbstractOneApiTest {

    private static final String XML_RESOURCE = "oneapi/mapping/naming_options.xml";

    /** entity 级别 mapUnderscoreToCamelCase="true" 应正确设置标志 */
    @Test
    public void testCamelCase_EntityLevel() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.naming", "CamelCaseEntity");

        assertNotNull(mapping);
        assertTrue("mapUnderscoreToCamelCase should be true", mapping.isToCamelCase());
    }

    /** entity 级别 caseInsensitive="true" 应正确设置标志 */
    @Test
    public void testCaseInsensitive_EntityLevel() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.naming", "CaseInsensitiveEntity");

        assertNotNull(mapping);
        assertTrue("caseInsensitive should be true", mapping.isCaseInsensitive());
    }

    /** entity 级别 useDelimited="true" 应正确设置标志 */
    @Test
    public void testUseDelimited_EntityLevel() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.naming", "DelimitedEntity");

        assertNotNull(mapping);
        assertTrue("useDelimited should be true", mapping.useDelimited());
    }

    /** entity 同时设置 camelCase=true, caseInsensitive=false, useDelimited=true */
    @Test
    public void testAllNamingOptions_Combined() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(XML_RESOURCE);

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.xml.naming", "AllNamingEntity");

        assertNotNull(mapping);
        assertTrue("mapUnderscoreToCamelCase should be true", mapping.isToCamelCase());
        assertFalse("caseInsensitive should be false", mapping.isCaseInsensitive());
        assertTrue("useDelimited should be true", mapping.useDelimited());
    }

    /** mapper 根级别 mapUnderscoreToCamelCase="true" 应被子 entity 继承 */
    @Test
    public void testCamelCase_RootLevelInherited() throws Exception {
        String xml = IOUtils.readToString(getClass().getResourceAsStream("/oneapi/mapping/naming_root_cc.xml"), "UTF-8");

        MappingRegistry registry = new MappingRegistry();
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            registry.loadMapping("naming-root-cc", stream);
        }

        TableMapping<?> mapping = registry.findBySpace("naming.rootcc", "InheritedEntity");
        assertNotNull(mapping);
        assertTrue("Entity should inherit root's mapUnderscoreToCamelCase=true", mapping.isToCamelCase());
    }

    /** entity 级别 mapUnderscoreToCamelCase="false" 覆盖 mapper 根级别 mapUnderscoreToCamelCase="true" */
    @Test
    public void testCamelCase_EntityOverridesRoot() throws Exception {
        String xml = IOUtils.readToString(getClass().getResourceAsStream("/oneapi/mapping/naming_override.xml"), "UTF-8");

        MappingRegistry registry = new MappingRegistry();
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            registry.loadMapping("naming-override", stream);
        }

        TableMapping<?> mapping = registry.findBySpace("naming.override", "OverrideEntity");
        assertNotNull(mapping);
        assertFalse("Entity camelCase=false should override root camelCase=true", mapping.isToCamelCase());
    }

    /** 默认情况 mapUnderscoreToCamelCase 为 false（根和 entity 均不设置） */
    @Test
    public void testCamelCase_DefaultFalse() throws Exception {
        String xml = IOUtils.readToString(getClass().getResourceAsStream("/oneapi/mapping/naming_default.xml"), "UTF-8");

        MappingRegistry registry = new MappingRegistry();
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            registry.loadMapping("naming-default", stream);
        }

        TableMapping<?> mapping = registry.findBySpace("naming.default", "DefaultEntity");
        assertNotNull(mapping);
        assertFalse("Default mapUnderscoreToCamelCase should be false", mapping.isToCamelCase());
    }
}
