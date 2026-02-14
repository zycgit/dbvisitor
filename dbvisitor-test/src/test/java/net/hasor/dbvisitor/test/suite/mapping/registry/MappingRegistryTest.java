package net.hasor.dbvisitor.test.suite.mapping.registry;

import java.util.Collection;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import net.hasor.dbvisitor.test.model.registry.OrderEntity;
import net.hasor.dbvisitor.test.model.registry.PlainPojo;
import net.hasor.dbvisitor.test.model.registry.ProductEntity;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * MappingRegistry API 测试。
 * 验证实体注册、查找、元数据解析等核心注册表功能。
 */
public class MappingRegistryTest extends AbstractOneApiTest {

    // ============ 注册和查找 ============

    /**
     * loadEntityToSpace 默认注册到空命名空间 + 类全名。
     */
    @Test
    public void testLoadEntityToSpace_Default() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class);

        TableMapping<?> mapping = registry.findByEntity(UserInfo.class);

        assertNotNull("Should find mapping by entity", mapping);
        assertEquals("user_info", mapping.getTable());
        assertEquals(UserInfo.class, mapping.entityType());
    }

    /**
     * loadEntityToSpace 指定自定义命名空间和名称。
     */
    @Test
    public void testLoadEntityToSpace_CustomSpaceName() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class, "mySpace", "myName");

        TableMapping<?> mapping = registry.findBySpace("mySpace", "myName");

        assertNotNull("Should find mapping by custom space+name", mapping);
        assertEquals("user_info", mapping.getTable());
    }

    /**
     * loadEntityAsTable 覆盖表名。
     */
    @Test
    public void testLoadEntityAsTable_CustomTable() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityAsTable(UserInfo.class, "custom_user_table");

        TableMapping<?> mapping = registry.findByTable("custom_user_table");

        assertNotNull("Should find mapping by custom table name", mapping);
        assertEquals("custom_user_table", mapping.getTable());
        assertEquals(UserInfo.class, mapping.entityType());
    }

    /**
     * loadEntityAsTable 覆盖 catalog + schema + table 三级结构。
     */
    @Test
    public void testLoadEntityAsTable_FullQualified() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityAsTable(UserInfo.class, "myCat", "mySch", "myTable");

        TableMapping<?> mapping = registry.findByTable("myCat", "mySch", "myTable");

        assertNotNull(mapping);
        assertEquals("myCat", mapping.getCatalog());
        assertEquals("mySch", mapping.getSchema());
        assertEquals("myTable", mapping.getTable());
    }

    /**
     * findByTable 按表名查找。
     */
    @Test
    public void testFindByTable() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class);

        TableMapping<?> mapping = registry.findByTable("user_info");

        assertNotNull("Should find by table name", mapping);
        assertEquals(UserInfo.class, mapping.entityType());
    }

    /**
     * findBySpace 按命名空间 + 类查找。
     */
    @Test
    public void testFindBySpace() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class, "test");

        TableMapping<?> mapping = registry.findBySpace("test", UserInfo.class);

        assertNotNull("Should find by space + class", mapping);
    }

    /**
     * isEntity 静态方法判断类是否有 @Table 注解。
     */
    @Test
    public void testIsEntity_WithAnnotation() throws Exception {
        assertTrue("UserInfo has @Table → should be entity", //
                MappingRegistry.isEntity(UserInfo.class));
    }

    @Test
    public void testIsEntity_WithoutAnnotation() throws Exception {
        assertFalse("PlainPojo has no @Table → not entity",//
                MappingRegistry.isEntity(PlainPojo.class));
    }

    /**
     * 同一 class 重复 loadEntityToSpace 应抛 IllegalStateException。
     * saveDefToSpace 检测到 name 已注册时直接报错，不允许重复注册。
     */
    @Test
    public void testDuplicateLoad_ThrowsISE() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class);

        try {
            registry.loadEntityToSpace(UserInfo.class);
            fail("Duplicate loadEntityToSpace should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue("Expected 'already exists', got: " + e.getMessage(),//
                    e.getMessage().contains("already exists"));
        }

        // 首次注册的映射仍可正常使用
        TableMapping<?> mapping = registry.findByEntity(UserInfo.class);
        assertNotNull("First registration should still be available", mapping);
    }

    // ============ 元数据验证 ============

    /**
     * 验证 TableMapping 元数据：catalog/schema、autoMapping、useDelimited 等。
     */
    @Test
    public void testTableMappingMetadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(OrderEntity.class);

        TableMapping<?> mapping = registry.findByEntity(OrderEntity.class);

        assertNotNull(mapping);
        assertEquals("test_cat", mapping.getCatalog());
        assertEquals("test_sch", mapping.getSchema());
        assertEquals("test_orders", mapping.getTable());
        assertTrue("autoMapping default is true", mapping.isAutoProperty());
        assertFalse("useDelimited default is false", mapping.useDelimited());
        assertTrue("caseInsensitive default is true", mapping.isCaseInsensitive());
        assertFalse("mapUnderscoreToCamelCase default is false", mapping.isToCamelCase());
        assertTrue("KeyType.Auto → useGeneratedKey=true", mapping.useGeneratedKey());
    }

    /**
     * 验证 ColumnMapping 元数据：primary、insert、update、column name 等。
     */
    @Test
    public void testColumnMappingMetadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(ProductEntity.class);

        TableMapping<?> mapping = registry.findByEntity(ProductEntity.class);

        assertNotNull(mapping);
        assertTrue("autoMapping=false", !mapping.isAutoProperty());
        assertTrue("useDelimited=true", mapping.useDelimited());

        // 检查 id 列
        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull("id column should exist", idCol);
        assertTrue("id should be primary key", idCol.isPrimaryKey());

        // 检查 name 列 (insert=false)
        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull("name column should exist", nameCol);
        assertEquals("product_name", nameCol.getColumn());
        assertFalse("name insert should be false", nameCol.isInsert());
        assertTrue("name update should be true (default)", nameCol.isUpdate());

        // 检查 price 列 (update=false)
        ColumnMapping priceCol = mapping.getPropertyByName("price");
        assertNotNull("price column should exist", priceCol);
        assertEquals("price", priceCol.getColumn());
        assertTrue("price insert should be true (default)", priceCol.isInsert());
        assertFalse("price update should be false", priceCol.isUpdate());
    }

    /**
     * 验证 getProperties/getColumns 返回所有映射信息。
     */
    @Test
    public void testMappingCollections() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class);

        TableMapping<?> mapping = registry.findByEntity(UserInfo.class);

        Collection<ColumnMapping> properties = mapping.getProperties();
        assertFalse("Should have mapped properties", properties.isEmpty());

        Collection<String> columns = mapping.getColumns();
        assertTrue("Should contain 'id' column", columns.contains("id"));
        assertTrue("Should contain 'name' column", columns.contains("name"));
        assertTrue("Should contain 'create_time' column", columns.contains("create_time"));

        // 验证 primary key
        ColumnMapping pkCol = mapping.getPropertyByName("id");
        assertNotNull(pkCol);
        assertTrue("id should be primary key", pkCol.isPrimaryKey());
    }
}
