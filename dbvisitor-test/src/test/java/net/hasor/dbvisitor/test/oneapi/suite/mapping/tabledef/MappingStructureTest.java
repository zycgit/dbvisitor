package net.hasor.dbvisitor.test.oneapi.suite.mapping.tabledef;

import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.tabledef.*;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @IndexDescribe 注解校验、ColumnDef.toString、
 * MappingRegistry 静态方法和结构性边界测试。
 */
public class MappingStructureTest extends AbstractOneApiTest {

    // ============ @IndexDescribe 校验 ============

    /**
     * @IndexDescribe 的 name 为空白 → IllegalArgumentException。
     */
    @Test
    public void testAnnotation_indexBlankName_throwsIAE() {
        MappingRegistry registry = new MappingRegistry();
        try {
            registry.loadEntityToSpace(BlankIndexNameEntity.class);
            fail("Should throw IAE for blank index name");
        } catch (IllegalArgumentException e) {
            assertTrue("Expected 'missing index name', got: " + e.getMessage(),//
                    e.getMessage().contains("missing index name"));
        }
    }

    /**
     * @IndexDescribe 的 columns 中含空字符串 → IllegalArgumentException。
     */
    @Test
    public void testAnnotation_indexEmptyColumn_throwsIAE() {
        MappingRegistry registry = new MappingRegistry();
        try {
            registry.loadEntityToSpace(EmptyIndexColEntity.class);
            fail("Should throw IAE for empty column in @IndexDescribe");
        } catch (IllegalArgumentException e) {
            assertTrue("Expected 'columns has empty', got: " + e.getMessage(),//
                    e.getMessage().contains("columns has empty"));
        }
    }

    // ============ ColumnDef.toString ============

    /**
     * ColumnDef.toString() 包含全部关键字段。
     */
    @Test
    public void testColumnDefToString() throws Exception {
        // 直接构建 ColumnDef 来验证 toString 格式
        TypeHandlerRegistry typeRegistry = new TypeHandlerRegistry();
        net.hasor.dbvisitor.mapping.def.ColumnDef col = new net.hasor.dbvisitor.mapping.def.ColumnDef("user_name", "userName", java.sql.Types.VARCHAR, String.class, typeRegistry.getDefaultTypeHandler(), null);
        col.setPrimaryKey(true);
        col.setInsert(true);
        col.setUpdate(false);

        String str = col.toString();
        assertTrue("Should contain 'ColumnDef{'", str.startsWith("ColumnDef{"));
        assertTrue("Should contain columnName", str.contains("columnName='user_name'"));
        assertTrue("Should contain propertyName", str.contains("propertyName='userName'"));
        assertTrue("Should contain jdbcType", str.contains("jdbcType=" + java.sql.Types.VARCHAR));
        assertTrue("Should contain insert=true", str.contains("insert=true"));
        assertTrue("Should contain update=false", str.contains("update=false"));
        assertTrue("Should contain primary=true", str.contains("primary=true"));
    }

    // ============ MappingRegistry.isEntity 静态方法 ============

    /**
     * MappingRegistry.isEntity(class) 根据是否有 @Table 注解返回。
     */
    @Test
    public void testIsEntity_staticMethod() {
        assertTrue("Class with @Table should be entity", MappingRegistry.isEntity(HasTableAnno.class));
        assertFalse("Class without @Table should not be entity", MappingRegistry.isEntity(NoTableAnno.class));
    }

    // ============ findBySpace(class) 重载 ============

    /**
     * findBySpace(space, entityClass) 重载方法正确查找。
     */
    @Test
    public void testFindBySpace_withClass() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(HasTableAnno.class, "my_space");

        TableMapping<?> mapping = registry.findBySpace("my_space", HasTableAnno.class);
        assertNotNull("Should find by space+class", mapping);
        assertEquals("has_table_anno", mapping.getTable());
    }

    /**
     * findBySpace(space, class) 找不到时返回 null。
     */
    @Test
    public void testFindBySpace_notFound() {
        MappingRegistry registry = new MappingRegistry();
        assertNull(registry.findBySpace("nonexistent", HasTableAnno.class));
    }

    // ============ loadResultMapToSpace 三参数覆盖 ============

    /**
     * loadResultMapToSpace(class, space, name) 三参覆盖注解中的 space/name。
     */
    @Test
    public void testLoadResultMapToSpace_overrideAnnotation() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadResultMapToSpace(ResultMapEntity.class, "overrideSpace", "overrideName");

        TableMapping<?> mapping = registry.findBySpace("overrideSpace", "overrideName");
        assertNotNull("Should find by overridden space+name", mapping);
    }

    // ============ loadMapping 幂等性（RES:: cache key） ============

    /**
     * 同一资源路径 loadMapping 两次 → 第二次静默跳过（幂等）。
     */
    @Test
    public void testLoadMapping_idempotent() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        String resource = "/oneapi/mapping/basic_entity_mapper.xml";
        registry.loadMapping(resource);
        registry.loadMapping(resource); // 第二次应被跳过不报错

        TableMapping<?> mapping = registry.findBySpace("net.hasor.test.mapping", "FullEntity");
        assertNotNull(mapping);
    }

    // ============ resultMap 的 catalog/schema/table 被清空 ============

    /**
     * loadResultMapToSpace 后 resultMap 的 catalog/schema/table 被设为空字符串。
     */
    @Test
    public void testResultMap_emptyTableInfo() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadResultMapToSpace(ResultMapEntity.class, "sp", "rm");

        TableMapping<?> mapping = registry.findBySpace("sp", "rm");
        assertNotNull(mapping);
        assertEquals("resultMap catalog should be empty", "", mapping.getCatalog());
        assertEquals("resultMap schema should be empty", "", mapping.getSchema());
        assertEquals("resultMap table should be empty", "", mapping.getTable());
    }
}
