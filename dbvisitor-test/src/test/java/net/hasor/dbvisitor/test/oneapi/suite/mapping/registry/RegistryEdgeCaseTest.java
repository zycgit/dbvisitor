package net.hasor.dbvisitor.test.oneapi.suite.mapping.registry;

import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import net.hasor.dbvisitor.test.oneapi.model.registry.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * MappingRegistry 边界条件和错误路径测试。
 * 覆盖: 参数校验、重复注册冲突、多映射查找、findByTable 边界、loadResultMapToSpace 等。
 */
public class RegistryEdgeCaseTest extends AbstractOneApiTest {

    // ==================== 参数校验 ====================

    /** loadEntityToSpace(null) → NullPointerException (entityType.getName() called before null check) */
    @Test(expected = NullPointerException.class)
    public void testLoadEntityToSpace_NullType() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(null);
    }

    /** loadEntityToSpace(cls, space, "") → IllegalArgumentException */
    @Test(expected = IllegalArgumentException.class)
    public void testLoadEntityToSpace_EmptyName() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class, "space", "");
    }

    /** loadEntityAsTable(null, "t") → IllegalArgumentException */
    @Test(expected = IllegalArgumentException.class)
    public void testLoadEntityAsTable_NullType() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityAsTable(null, "some_table");
    }

    /** loadEntityAsTable(cls, "") → IllegalArgumentException */
    @Test(expected = IllegalArgumentException.class)
    public void testLoadEntityAsTable_EmptyTable() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityAsTable(UserInfo.class, "");
    }

    /** loadResultMapToSpace(null) → IllegalArgumentException */
    @Test(expected = IllegalArgumentException.class)
    public void testLoadResultMapToSpace_NullType() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadResultMapToSpace(null);
    }

    /** loadResultMapToSpace(cls, space, "") → IllegalArgumentException */
    @Test(expected = IllegalArgumentException.class)
    public void testLoadResultMapToSpace_EmptyName() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadResultMapToSpace(UserInfo.class, "space", "");
    }

    // ==================== 重复注册冲突 ====================

    /** 两个不同类注册到相同 space+name → IllegalStateException */
    @Test(expected = IllegalStateException.class)
    public void testDuplicateRegistration_SameSpaceName() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(EntityA.class, "", "conflict_name");
        registry.loadEntityToSpace(EntityB.class, "", "conflict_name"); // 冲突
    }

    // ==================== 多映射查找 ====================

    /** 同一张表两个不同映射，无 specifyName → IllegalStateException */
    @Test(expected = IllegalStateException.class)
    public void testFindByTable_MultipleDefinitions_NoSpecify() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(SameTableEntity1.class);
        registry.loadEntityToSpace(SameTableEntity2.class);

        // 两个实体都映射到 "same_table"，不指定 name 应抛异常
        registry.findByTable("same_table");
    }

    /** 同一张表两个不同映射，用 specifyName 精确查找 */
    @Test
    public void testFindByTable_MultipleDefinitions_WithSpecify() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(SameTableEntity1.class);
        registry.loadEntityToSpace(SameTableEntity2.class);

        TableMapping<?> m1 = registry.findByTable(null, null, "same_table", SameTableEntity1.class.getName());
        TableMapping<?> m2 = registry.findByTable(null, null, "same_table", SameTableEntity2.class.getName());

        assertNotNull(m1);
        assertNotNull(m2);
        assertEquals(SameTableEntity1.class, m1.entityType());
        assertEquals(SameTableEntity2.class, m2.entityType());
    }

    /** findByTable 不存在的表 → null */
    @Test
    public void testFindByTable_NotFound() {
        MappingRegistry registry = new MappingRegistry();
        assertNull(registry.findByTable("non_existent_table"));
    }

    /** findBySpace 不存在的命名空间 → null */
    @Test
    public void testFindBySpace_NotFound() {
        MappingRegistry registry = new MappingRegistry();
        assertNull(registry.findBySpace("missing_space", "missing_name"));
    }

    /** findByEntity 未注册 → null */
    @Test
    public void testFindByEntity_NotRegistered() {
        MappingRegistry registry = new MappingRegistry();
        assertNull(registry.findByEntity(UserInfo.class));
    }

    // ==================== loadResultMapToSpace ====================

    /** loadResultMapToSpace 从 @ResultMap 注解中读取 space + name */
    @Test
    public void testLoadResultMapToSpace_FromAnnotation() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadResultMapToSpace(AnnotatedResultMapEntity.class);

        TableMapping<?> mapping = registry.findBySpace("myMapSpace", "MyResultMap");
        assertNotNull("Should find resultMap by @ResultMap annotation", mapping);
        assertEquals(AnnotatedResultMapEntity.class, mapping.entityType());

        // resultMap 不应设置表名
        assertEquals("", mapping.getTable());
    }

    /** loadResultMapToSpace 覆盖 @ResultMap 注解中的 space+name */
    @Test
    public void testLoadResultMapToSpace_OverrideAnnotation() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadResultMapToSpace(AnnotatedResultMapEntity.class, "override_space", "override_name");

        TableMapping<?> mapping = registry.findBySpace("override_space", "override_name");
        assertNotNull("Should find with overridden space+name", mapping);
    }

    // ==================== loadEntityAsTable 三级结构 ====================

    /** loadEntityAsTable 覆盖 catalog/schema/table 并通过 findByTable 四参数查找 */
    @Test
    public void testLoadEntityAsTable_CatalogSchemaTable() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityAsTable(UserInfo.class, "cat1", "sch1", "tbl1");

        TableMapping<?> mapping = registry.findByTable("cat1", "sch1", "tbl1");
        assertNotNull(mapping);
        assertEquals("cat1", mapping.getCatalog());
        assertEquals("sch1", mapping.getSchema());
        assertEquals("tbl1", mapping.getTable());
    }

    // ==================== 构造方法和访问器 ====================

    /** MappingRegistry 构造参数及全局 Options */
    @Test
    public void testRegistryConstructorAndAccessors() {
        Options opts = Options.of();
        opts.setCatalog("g_cat");
        opts.setSchema("g_sch");

        MappingRegistry registry = new MappingRegistry(null, opts);

        assertNotNull(registry.getClassLoader());
        assertNotNull(registry.getTypeRegistry());
        assertEquals("g_cat", registry.getGlobalOptions().getCatalog());
        assertEquals("g_sch", registry.getGlobalOptions().getSchema());
    }

    /** 无注解 POJO 使用 Options 中的 catalog/schema */
    @Test
    public void testPlainPojo_InheritsOptionsDefaults() {
        Options opts = Options.of();
        opts.setCatalog("pojo_cat");
        opts.setSchema("pojo_sch");

        MappingRegistry registry = new MappingRegistry(null, opts);
        registry.loadEntityToSpace(PlainPojo.class);

        TableMapping<?> mapping = registry.findByEntity(PlainPojo.class);
        assertNotNull(mapping);
        assertEquals("pojo_cat", mapping.getCatalog());
        assertEquals("pojo_sch", mapping.getSchema());
    }

    /** DEFAULT 静态实例基本属性 */
    @Test
    public void testDefaultInstance() {
        assertNotNull("DEFAULT should exist", MappingRegistry.DEFAULT);
        assertNotNull("DEFAULT classLoader", MappingRegistry.DEFAULT.getClassLoader());
        assertNotNull("DEFAULT typeRegistry", MappingRegistry.DEFAULT.getTypeRegistry());
        assertNotNull("DEFAULT globalOptions", MappingRegistry.DEFAULT.getGlobalOptions());
    }

    /** 无注解 POJO + mapUnderscoreToCamelCase → 表名自动转换 */
    @Test
    public void testPlainPojo_TableNameAutoDerive() {
        Options opts = Options.of();
        opts.setMapUnderscoreToCamelCase(true);

        MappingRegistry registry = new MappingRegistry(null, opts);
        registry.loadEntityToSpace(PlainPojo.class);

        TableMapping<?> mapping = registry.findByEntity(PlainPojo.class);
        assertNotNull(mapping);
        // PlainPojo → plain_pojo (驼峰转下划线)
        assertEquals("plain_pojo", mapping.getTable());
    }
}
