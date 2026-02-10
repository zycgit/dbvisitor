package net.hasor.dbvisitor.test.oneapi.suite.mapping.annotation;

import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.annotation.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 实体继承映射测试。
 * Annotations.ofClassHierarchy 会遍历整个继承链，父类字段/方法的注解会被合并到子类中。
 * 因此父类的 @Column 注解能被子类正确继承。
 */
public class ExtendsMappingTest extends AbstractOneApiTest {

    // ==================== 测试方法 ====================

    /**
     * 父类字段通过 BeanUtils.getALLFieldToList 被收集，
     * Annotations.ofClassHierarchy 合并父类注解，@Column 可被正确继承。
     */
    @Test
    public void testInheritedFields_AutoMapped() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserEntity.class);

        TableMapping<?> mapping = registry.findByEntity(UserEntity.class);

        assertNotNull(mapping);

        // 父类字段: @Column(primary=true) 现在能被继承
        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull("id from parent should be mapped", idCol);
        assertEquals("id", idCol.getColumn());
        assertTrue("parent @Column(primary) inherited via ofClassHierarchy", idCol.isPrimaryKey());

        ColumnMapping ctCol = mapping.getPropertyByName("createTime");
        assertNotNull("createTime from parent should be mapped", ctCol);
        // 父类 @Column("create_time") 现在能被继承 → 列名="create_time"
        assertEquals("create_time", ctCol.getColumn());

        // 子类字段: 正常 @Column 映射
        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull("name from child should be mapped", nameCol);
        assertEquals("name", nameCol.getColumn());

        ColumnMapping ageCol = mapping.getPropertyByName("age");
        assertNotNull("age from child should be mapped", ageCol);
    }

    /** 子类 + 父类字段总数 (autoMapping 收集所有) */
    @Test
    public void testInheritedFieldCount() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserEntity.class);

        TableMapping<?> mapping = registry.findByEntity(UserEntity.class);

        // UserEntity: name, age; BaseEntity: id, createTime → 共 4
        assertEquals(4, mapping.getProperties().size());
    }

    /** 多层继承 (3 层) 也能收集所有字段 */
    @Test
    public void testMultiLevelInheritance() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(ExtendedEntity.class);

        TableMapping<?> mapping = registry.findByEntity(ExtendedEntity.class);

        assertNotNull(mapping);
        // ExtendedEntity: email; NamedBaseEntity: name; BaseEntity: id, createTime → 共 4
        assertEquals(4, mapping.getProperties().size());
        assertNotNull(mapping.getPropertyByName("id"));
        assertNotNull(mapping.getPropertyByName("createTime"));
        // NamedBaseEntity.name 的 @Column("name") 现在能被继承
        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertEquals("name", nameCol.getColumn());
        assertNotNull(mapping.getPropertyByName("email"));
    }

    /** autoMapping=false 时，有 @Column 的字段才会被映射（含继承的父类注解） */
    @Test
    public void testAutoMappingFalse_IncludesParentColumns() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(ManualChild.class);

        TableMapping<?> mapping = registry.findByEntity(ManualChild.class);

        assertNotNull(mapping);
        // autoMapping=false: ManualChild 的 @Column("name") + 父类继承的 @Column(id, createTime)
        assertEquals(3, mapping.getProperties().size());
        assertNotNull("name should be mapped", mapping.getPropertyByName("name"));
        assertNotNull("id from parent mapped via inherited @Column", mapping.getPropertyByName("id"));
        assertNotNull("createTime from parent mapped via inherited @Column", mapping.getPropertyByName("createTime"));
    }

    /** 无注解父类的字段仍被 autoMapping 收集 */
    @Test
    public void testPlainBaseAutoMapped() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(ChildWithPlainBase.class);

        TableMapping<?> mapping = registry.findByEntity(ChildWithPlainBase.class);

        assertNotNull(mapping);
        // autoMapping=true(默认): 子类 name(@Column) + 父类 id, code(auto) → 3
        assertEquals(3, mapping.getProperties().size());
        assertNotNull(mapping.getPropertyByName("name"));
        assertNotNull(mapping.getPropertyByName("id"));
        assertNotNull(mapping.getPropertyByName("code"));
    }

    /** getPropertyByColumn 对自动映射的继承字段的查找 */
    @Test
    public void testPropertyByColumn_AutoMappedInherited() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserEntity.class);

        TableMapping<?> mapping = registry.findByEntity(UserEntity.class);

        // 通过列名查找（父类 @Column 注解已继承，列名按注解配置）
        assertNotNull("Should find by column 'id'", mapping.getPropertyByColumn("id"));
        assertNotNull("Should find by column 'create_time'", mapping.getPropertyByColumn("create_time"));
        assertNotNull("Should find by column 'name'", mapping.getPropertyByColumn("name"));
    }

    /** getColumns() 返回所有列名 (含自动映射的继承列) */
    @Test
    public void testGetColumns_Inherited() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserEntity.class);

        TableMapping<?> mapping = registry.findByEntity(UserEntity.class);

        assertTrue(mapping.getColumns().contains("id"));
        assertTrue(mapping.getColumns().contains("create_time")); // 父类 @Column("create_time") 已继承
        assertTrue(mapping.getColumns().contains("name"));
        assertTrue(mapping.getColumns().contains("age"));
    }

    /** 继承结构中的 entityType 应为子类 */
    @Test
    public void testEntityType_IsChildClass() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserEntity.class);

        TableMapping<?> mapping = registry.findByEntity(UserEntity.class);
        assertEquals(UserEntity.class, mapping.entityType());
    }

    /**
     * 多层级继承：中间层 @Table(autoMapping=false)，叶子层 @Table(autoMapping=true)。
     * 验证只有叶子类的 autoMapping 生效（父类的 @Table 类级注解不会被 mergeMembers 合并）。
     * 结果：所有层级的字段（含未标注 @Column 的 age 和 email）都应被自动映射。
     * 继承链: BaseEntity(@Column: id, createTime) → ManualMiddleLayer(@Table autoMapping=false, @Column: name, 无注解: age)
     * → AutoTrueOverManualLeaf(@Table autoMapping=true, 无注解: email)
     */
    @Test
    public void testMultiLevel_LeafAutoTrue_OverridesParentFalse() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(AutoTrueOverManualLeaf.class);

        TableMapping<?> mapping = registry.findByEntity(AutoTrueOverManualLeaf.class);
        assertNotNull(mapping);

        // autoMapping=true（叶子层生效）→ 全部 5 个字段都应被映射
        assertEquals(5, mapping.getProperties().size());

        // Level 1 (BaseEntity): @Column 字段
        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull("id from grandparent @Column should be mapped", idCol);
        assertTrue(idCol.isPrimaryKey());
        assertNotNull("createTime from grandparent @Column should be mapped", mapping.getPropertyByName("createTime"));

        // Level 2 (ManualMiddleLayer): @Column 字段 + 无注解字段
        assertNotNull("name from parent @Column should be mapped", mapping.getPropertyByName("name"));
        assertNotNull("age from parent (no @Column) should be auto-mapped because leaf autoMapping=true", mapping.getPropertyByName("age"));

        // Level 3 (AutoTrueOverManualLeaf): 无注解字段
        assertNotNull("email from leaf (no @Column) should be auto-mapped", mapping.getPropertyByName("email"));
    }

    /**
     * 多层级继承：中间层 @Table(autoMapping=true)，叶子层 @Table(autoMapping=false)。
     * 验证只有叶子类的 autoMapping 生效。
     * 结果：只有标注 @Column 的字段被映射，中间层和叶子层无注解的字段不会被映射。
     * 继承链: BaseEntity(@Column: id, createTime) → AutoMiddleLayer(@Table autoMapping=true, @Column: name, 无注解: age)
     * → ManualOverAutoLeaf(@Table autoMapping=false, 无注解: email)
     */
    @Test
    public void testMultiLevel_LeafAutoFalse_OverridesParentTrue() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(ManualOverAutoLeaf.class);

        TableMapping<?> mapping = registry.findByEntity(ManualOverAutoLeaf.class);
        assertNotNull(mapping);

        // autoMapping=false（叶子层生效）→ 只有 @Column 标注的 3 个字段被映射
        assertEquals(3, mapping.getProperties().size());

        // Level 1 (BaseEntity): @Column 字段 → 被继承，映射
        assertNotNull("id from grandparent @Column should be mapped", mapping.getPropertyByName("id"));
        assertNotNull("createTime from grandparent @Column should be mapped", mapping.getPropertyByName("createTime"));

        // Level 2 (AutoMiddleLayer): @Column 字段 → 被继承，映射
        assertNotNull("name from parent @Column should be mapped", mapping.getPropertyByName("name"));

        // Level 2 无注解字段: autoMapping=false 生效 → 不映射
        assertNull("age from parent (no @Column) should NOT be mapped because leaf autoMapping=false", mapping.getPropertyByName("age"));

        // Level 3 无注解字段: 同上 → 不映射
        assertNull("email from leaf (no @Column) should NOT be mapped", mapping.getPropertyByName("email"));
    }
}
