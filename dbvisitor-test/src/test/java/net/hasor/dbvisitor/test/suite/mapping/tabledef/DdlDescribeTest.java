package net.hasor.dbvisitor.test.suite.mapping.tabledef;

import java.util.List;
import net.hasor.dbvisitor.mapping.DdlAuto;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.*;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.tabledef.FullDdlEntity;
import net.hasor.dbvisitor.test.model.tabledef.NoDdlEntity;
import net.hasor.dbvisitor.test.model.tabledef.SimpleDdlEntity;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * DDL 描述注解元数据测试。
 * 验证 @TableDescribe、@ColumnDescribe、@IndexDescribe 的注解解析和元数据注册。
 * 注意：这些不测试实际 DDL 执行，仅验证元数据正确性。
 */
public class DdlDescribeTest extends AbstractOneApiTest {

    // ============ @Table ddlAuto Tests ============

    /**
     * @Table(ddlAuto=DdlAuto.Create) 元数据验证。
     */
    @Test
    public void testDdlAuto_CreateMetadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(FullDdlEntity.class);

        TableMapping<?> mapping = registry.findByEntity(FullDdlEntity.class);
        assertNotNull(mapping);

        TableDescription desc = mapping.getDescription();
        assertNotNull("TableDescription should exist when @TableDescribe is present", desc);
        assertEquals(DdlAuto.Create, desc.getDdlAuto());
    }

    /**
     * @Table(ddlAuto=DdlAuto.AddColumn) 元数据验证。
     */
    @Test
    public void testDdlAuto_AddColumnMetadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(SimpleDdlEntity.class);

        TableMapping<?> mapping = registry.findByEntity(SimpleDdlEntity.class);
        assertNotNull(mapping);

        // 虽无 @TableDescribe，但 ddlAuto=AddColumn 不为 None，TableDescription 应存在
        TableDescription desc = mapping.getDescription();
        assertNotNull("ddlAuto=AddColumn should produce non-null description", desc);
        assertEquals(DdlAuto.AddColumn, desc.getDdlAuto());

        // 没有 @TableDescribe，其他字段应为 null
        assertNull("characterSet should be null without @TableDescribe", desc.getCharacterSet());
        assertNull("collation should be null without @TableDescribe", desc.getCollation());
        assertNull("comment should be null without @TableDescribe", desc.getComment());
    }

    /**
     * 默认 ddlAuto=None 时无 DDL 描述。
     */
    @Test
    public void testDdlAuto_NoneMetadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(NoDdlEntity.class);

        TableMapping<?> mapping = registry.findByEntity(NoDdlEntity.class);
        assertNotNull(mapping);

        TableDescription desc = mapping.getDescription();
        assertNull("Default ddlAuto=None should have no description", desc);
    }

    // ============ @TableDescribe Tests ============

    /**
     * @TableDescribe 的所有属性应正确映射到元数据。
     */
    @Test
    public void testTableDescribe_AllAttributes() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(FullDdlEntity.class);

        TableMapping<?> mapping = registry.findByEntity(FullDdlEntity.class);
        TableDescription desc = mapping.getDescription();
        assertNotNull(desc);

        assertEquals("utf8mb4", desc.getCharacterSet());
        assertEquals("utf8mb4_general_ci", desc.getCollation());
        assertEquals("Test table for DDL", desc.getComment());
        assertEquals("ENGINE=InnoDB", desc.getOther());
    }

    // ============ @ColumnDescribe Tests ============

    /**
     * @ColumnDescribe 完整属性验证（sqlType, nullable, defaultValue, comment）。
     */
    @Test
    public void testColumnDescribe_CompleteAttributes() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(FullDdlEntity.class);

        TableMapping<?> mapping = registry.findByEntity(FullDdlEntity.class);

        // id 列
        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull(idCol);
        ColumnDescription idDesc = idCol.getDescription();
        assertNotNull("id should have @ColumnDescribe", idDesc);
        assertEquals("BIGINT", idDesc.getSqlType());
        assertFalse("Primary key should be non-nullable", idDesc.isNullable());
        assertEquals("Primary key", idDesc.getComment());

        // name 列
        ColumnMapping nameCol = mapping.getPropertyByName("name");
        ColumnDescription nameDesc = nameCol.getDescription();
        assertNotNull(nameDesc);
        assertEquals("VARCHAR(100)", nameDesc.getSqlType());
        assertFalse("name nullable=false", nameDesc.isNullable());
        assertEquals("'unknown'", nameDesc.getDefault());
        assertEquals("User name", nameDesc.getComment());
    }

    /**
     * @ColumnDescribe 的 characterSet、collation、other 属性。
     */
    @Test
    public void testColumnDescribe_CharsetAndOther() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(FullDdlEntity.class);

        TableMapping<?> mapping = registry.findByEntity(FullDdlEntity.class);

        ColumnMapping emailCol = mapping.getPropertyByName("email");
        ColumnDescription emailDesc = emailCol.getDescription();
        assertNotNull(emailDesc);
        assertEquals("utf8", emailDesc.getCharacterSet());
        assertEquals("utf8_bin", emailDesc.getCollation());
        assertEquals("UNIQUE", emailDesc.getOther());
        assertEquals("Email address", emailDesc.getComment());
    }

    /**
     * @ColumnDescribe 的 precision 和 scale 属性。
     */
    @Test
    public void testColumnDescribe_PrecisionAndScale() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(FullDdlEntity.class);

        TableMapping<?> mapping = registry.findByEntity(FullDdlEntity.class);

        ColumnMapping balanceCol = mapping.getPropertyByName("balance");
        ColumnDescription balanceDesc = balanceCol.getDescription();
        assertNotNull(balanceDesc);
        assertEquals("DECIMAL", balanceDesc.getSqlType());
        assertEquals("10", balanceDesc.getPrecision());
        assertEquals("2", balanceDesc.getScale());
    }

    /**
     * 无 @ColumnDescribe 的列，getDescription() 应为 null。
     */
    @Test
    public void testColumnDescribe_MissingAnnotation() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(NoDdlEntity.class);

        TableMapping<?> mapping = registry.findByEntity(NoDdlEntity.class);
        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertNull("Column without @ColumnDescribe should have null description", nameCol.getDescription());
    }

    // ============ @IndexDescribe Tests ============

    /**
     * 多个 @IndexDescribe（@Repeatable）应正确注册。
     */
    @Test
    public void testIndexDescribe_MultipleIndexes() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(FullDdlEntity.class);

        TableMapping<?> mapping = registry.findByEntity(FullDdlEntity.class);
        List<IndexDescription> indexes = mapping.getIndexes();

        assertNotNull(indexes);
        assertEquals("Should have 2 indexes", 2, indexes.size());
    }

    /**
     * @IndexDescribe 单列索引属性验证。
     */
    @Test
    public void testIndexDescribe_SingleColumnIndex() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(FullDdlEntity.class);

        TableMapping<?> mapping = registry.findByEntity(FullDdlEntity.class);
        IndexDescription nameIdx = mapping.getIndex("idx_name");

        assertNotNull("Should find index by name", nameIdx);
        assertEquals("idx_name", nameIdx.getName());
        assertFalse("Should not be unique", nameIdx.isUnique());
        assertEquals(1, nameIdx.getColumns().size());
        assertEquals("name", nameIdx.getColumns().get(0));
        assertEquals("Name index", nameIdx.getComment());
    }

    /**
     * @IndexDescribe 复合唯一索引属性验证。
     */
    @Test
    public void testIndexDescribe_CompositeUniqueIndex() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(FullDdlEntity.class);

        TableMapping<?> mapping = registry.findByEntity(FullDdlEntity.class);
        IndexDescription compositeIdx = mapping.getIndex("idx_name_age");

        assertNotNull("Should find composite index", compositeIdx);
        assertTrue("Should be unique", compositeIdx.isUnique());
        assertEquals(2, compositeIdx.getColumns().size());
        assertEquals("name", compositeIdx.getColumns().get(0));
        assertEquals("age", compositeIdx.getColumns().get(1));
        assertEquals("Composite unique index", compositeIdx.getComment());
        assertEquals("USING BTREE", compositeIdx.getOther());
    }

    /**
     * 无 @IndexDescribe 的实体 getIndexes() 应为空列表。
     */
    @Test
    public void testIndexDescribe_NoIndexes() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(NoDdlEntity.class);

        TableMapping<?> mapping = registry.findByEntity(NoDdlEntity.class);
        List<IndexDescription> indexes = mapping.getIndexes();

        assertTrue("No indexes should exist", indexes == null || indexes.isEmpty());
    }

    /**
     * getIndex() 查找不存在的索引名应返回 null。
     */
    @Test
    public void testIndexDescribe_NotFoundReturnsNull() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(FullDdlEntity.class);

        TableMapping<?> mapping = registry.findByEntity(FullDdlEntity.class);
        IndexDescription notFound = mapping.getIndex("non_existent_index");

        assertNull("Non-existent index should return null", notFound);
    }
}
