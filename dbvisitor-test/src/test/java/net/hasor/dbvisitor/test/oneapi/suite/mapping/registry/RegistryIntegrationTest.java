package net.hasor.dbvisitor.test.oneapi.suite.mapping.registry;

import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.registry.CacheEntityA;
import net.hasor.dbvisitor.test.oneapi.model.registry.CatalogSchemaEntity;
import net.hasor.dbvisitor.test.oneapi.model.registry.PlainUserNamePojo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * ClassTableMappingResolve / WeakHashMap 缓存、
 * POJO 无注解实体的 catalog/schema 传递、
 * findByTable 多重映射 ISE、
 * isEntity 与 loadEntityAsTable 组合测试。
 */
public class RegistryIntegrationTest extends AbstractOneApiTest {

    // ============ WeakHashMap 缓存行为 ============

    /**
     * 同一实体类多次 loadEntityToSpace（不同 space/name）共享 ClassTableMappingResolve 缓存。
     * 两次调用应返回相同的映射定义（class-level 解析结果被缓存）。
     */
    @Test
    public void testClassResolveCache_sameEntityMultipleSpaces() throws Exception {
        MappingRegistry registry = new MappingRegistry();

        TableMapping<?> m1 = registry.loadEntityToSpace(CacheEntityA.class, "sp1", "name1");
        TableMapping<?> m2 = registry.loadEntityToSpace(CacheEntityA.class, "sp2", "name2");

        // 两方应有相同的列映射（来自同一缓存的 TableDef）
        assertEquals("Same entity → same number of properties", m1.getProperties().size(), m2.getProperties().size());
        assertEquals("Same entity → same table", m1.getTable(), m2.getTable());
    }

    // ============ Options 的 catalog/schema 传递给无注解 POJO ============

    /**
     * Options.catalog/schema 传递到 POJO（无 @Table）实体的 loadEntityAsTable。
     */
    @Test
    public void testOptions_catalogSchemaPassedToPlainPojo() throws Exception {
        Options opts = Options.of().catalog("globalCat").schema("globalSch");
        MappingRegistry registry = new MappingRegistry(null, null, opts);

        // loadEntityAsTable 会覆盖 table，但 catalog/schema 来自 Options
        // 不过 loadEntityAsTable 只设了 table，检查一下
        registry.loadEntityToSpace(PlainUserNamePojo.class);
        TableMapping<?> mapping = registry.findByEntity(PlainUserNamePojo.class);

        assertNotNull(mapping);
        // POJO 无 @Table → catalog/schema 来自 Options
        assertEquals("globalCat", mapping.getCatalog());
        assertEquals("globalSch", mapping.getSchema());
    }

    // ============ findByTable 多重映射 ============

    /**
     * 同一 table 注册两个不同 name 后，findByTable(table) 不带 specifyName → ISE。
     */
    @Test
    public void testFindByTable_multipleNames_throwsISE() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(CacheEntityA.class, "", "nameA");
        registry.loadEntityToSpace(CacheEntityA.class, "", "nameB");

        try {
            registry.findByTable("cache_entity_a");
            fail("Should throw ISE for multiple definitions without specifyName");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("multiple definitions"));
        }
    }

    /**
     * 同一 table 注册两个不同 name 后，findByTable + specifyName → 正确返回。
     */
    @Test
    public void testFindByTable_multipleNames_withSpecify() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(CacheEntityA.class, "", "nameA");
        registry.loadEntityToSpace(CacheEntityA.class, "", "nameB");

        TableMapping<?> m = registry.findByTable(null, null, "cache_entity_a", "nameA");
        assertNotNull("Should find with specifyName", m);
    }

    // ============ loadEntityAsTable 覆盖表信息 ============

    /**
     * loadEntityAsTable(class, catalog, schema, table) 四参版本覆盖所有表结构信息。
     */
    @Test
    public void testLoadEntityAsTable_fourParam_overrideAll() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        TableMapping<?> mapping = registry.loadEntityAsTable(CacheEntityA.class, "oCat", "oSch", "override_table");

        assertNotNull(mapping);
        assertEquals("oCat", mapping.getCatalog());
        assertEquals("oSch", mapping.getSchema());
        assertEquals("override_table", mapping.getTable());
    }

    /**
     * loadEntityAsTable(class, table) 双参版本 → only table is overridden。
     */
    @Test
    public void testLoadEntityAsTable_twoParam() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        TableMapping<?> mapping = registry.loadEntityAsTable(CacheEntityA.class, "new_table");

        assertNotNull(mapping);
        assertEquals("new_table", mapping.getTable());
    }

    // ============ @Table 上的 catalog/schema 从注解读取 ============

    /**
     * @Table(catalog, schema) 在注解上指定 → 映射中体现。
     */
    @Test
    public void testAnnotation_catalogSchema_fromTable() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(CatalogSchemaEntity.class);
        TableMapping<?> mapping = registry.findByEntity(CatalogSchemaEntity.class);

        assertNotNull(mapping);
        assertEquals("mycat", mapping.getCatalog());
        assertEquals("mysch", mapping.getSchema());
    }

    /**
     * findByTable 使用注解中的 catalog/schema/table 查找。
     */
    @Test
    public void testFindByTable_withAnnotationCatalogSchema() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(CatalogSchemaEntity.class);

        TableMapping<?> mapping = registry.findByTable("mycat", "mysch", "entity_with_catalog");
        assertNotNull("Should find by annotation catalog+schema+table", mapping);
    }

    // ============ loadEntityToSpace 不同 space 查找隔离 ============

    /**
     * 不同 space 之间的映射互相隔离。
     */
    @Test
    public void testSpaceIsolation() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(CacheEntityA.class, "spaceA", "myEntity");

        assertNotNull(registry.findBySpace("spaceA", "myEntity"));
        assertNull("Different space should not find", registry.findBySpace("spaceB", "myEntity"));
        assertNull("Default space should not find", registry.findBySpace("", "myEntity"));
    }

    // ============ loadEntityToSpace null/blank 校验 ============

    /**
     * loadEntityToSpace(null) → 异常（NullPointerException 或 IllegalArgumentException）。
     */
    @Test(expected = Exception.class)
    public void testLoadEntityToSpace_nullType_throwsException() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(null);
    }

    /**
     * loadEntityToSpace(class, space, blankName) → IllegalArgumentException。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLoadEntityToSpace_blankName_throwsIAE() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(CacheEntityA.class, "", "");
    }

    // ============ loadEntityAsTable null 校验 ============

    /**
     * loadEntityAsTable(null, table) → IAE。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLoadEntityAsTable_nullType_throwsIAE() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityAsTable(null, "some_table");
    }

    /**
     * loadEntityAsTable(class, blankTable) → IAE。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLoadEntityAsTable_blankTable_throwsIAE() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityAsTable(CacheEntityA.class, "");
    }

    // ============ loadResultMapToSpace null 校验 ============

    /**
     * loadResultMapToSpace(null) → IAE。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLoadResultMapToSpace_nullType_throwsIAE() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadResultMapToSpace(null);
    }

    /**
     * loadResultMapToSpace(class, space, blankName) → IAE。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLoadResultMapToSpace_blankName_throwsIAE() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadResultMapToSpace(CacheEntityA.class, "sp", "");
    }
}
