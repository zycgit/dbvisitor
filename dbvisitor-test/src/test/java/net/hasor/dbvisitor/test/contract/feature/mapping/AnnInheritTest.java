package net.hasor.dbvisitor.test.contract.feature.mapping;

import org.junit.Test;

import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.contract.material.model.annotation.AutoTrueOverManualLeaf;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ChildWithPlainBase;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ExtendedEntity;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ManualChild;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ManualOverAutoLeaf;
import net.hasor.dbvisitor.test.contract.material.model.annotation.UserEntity;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class AnnInheritTest extends AbstractNxnContractTest {
    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_INHERITANCE_PARENT_COLUMNS)
    public void annotationInheritParentFieldColAnns() {
        TableMapping<?> mapping = mapping(UserEntity.class);

        ColumnMapping id = mapping.getPropertyByName("id");
        assertNotNull(id);
        assertEquals("id", id.getColumn());
        assertTrue(id.isPrimaryKey());

        ColumnMapping createTime = mapping.getPropertyByName("createTime");
        assertNotNull(createTime);
        assertEquals("create_time", createTime.getColumn());

        ColumnMapping name = mapping.getPropertyByName("name");
        assertNotNull(name);
        assertEquals("name", name.getColumn());
        assertNotNull(mapping.getPropertyByName("age"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_INHERITANCE_FIELD_COUNT)
    public void annotationInheritInheritedAndChildFields() {
        assertEquals(4, mapping(UserEntity.class).getProperties().size());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_INHERITANCE_MULTI_LEVEL)
    public void annotationInheritMultiLevelHierarchyFields() {
        TableMapping<?> mapping = mapping(ExtendedEntity.class);

        assertEquals(4, mapping.getProperties().size());
        assertNotNull(mapping.getPropertyByName("id"));
        assertNotNull(mapping.getPropertyByName("createTime"));
        assertNotNull(mapping.getPropertyByName("email"));
        assertEquals("name", mapping.getPropertyByName("name").getColumn());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_INHERITANCE_AUTO_FALSE_PARENT_COLUMNS)
    public void annotationInheritParentColAnnsWhenAutoMapFalse() {
        TableMapping<?> mapping = mapping(ManualChild.class);

        assertEquals(3, mapping.getProperties().size());
        assertNotNull(mapping.getPropertyByName("name"));
        assertNotNull(mapping.getPropertyByName("id"));
        assertNotNull(mapping.getPropertyByName("createTime"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_INHERITANCE_PLAIN_BASE)
    public void annotationInheritMapPlainBaseFields() {
        TableMapping<?> mapping = mapping(ChildWithPlainBase.class);

        assertEquals(3, mapping.getProperties().size());
        assertNotNull(mapping.getPropertyByName("name"));
        assertNotNull(mapping.getPropertyByName("id"));
        assertNotNull(mapping.getPropertyByName("code"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_INHERITANCE_COLUMN_LOOKUP)
    public void annotationInheritInheritedPropertiesByCol() {
        TableMapping<?> mapping = mapping(UserEntity.class);

        assertNotNull(mapping.getPropertyByColumn("id"));
        assertNotNull(mapping.getPropertyByColumn("create_time"));
        assertNotNull(mapping.getPropertyByColumn("name"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_INHERITANCE_COLUMNS_SET)
    public void annotationInheritAllInheritedCols() {
        TableMapping<?> mapping = mapping(UserEntity.class);

        assertTrue(mapping.getColumns().contains("id"));
        assertTrue(mapping.getColumns().contains("create_time"));
        assertTrue(mapping.getColumns().contains("name"));
        assertTrue(mapping.getColumns().contains("age"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_INHERITANCE_ENTITY_TYPE)
    public void annotationInheritLeafEntityType() {
        assertEquals(UserEntity.class, mapping(UserEntity.class).entityType());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_INHERITANCE_LEAF_AUTO_TRUE)
    public void annotationInheritLeafAutoMapTrueOverParentFalse() {
        TableMapping<?> mapping = mapping(AutoTrueOverManualLeaf.class);

        assertEquals(5, mapping.getProperties().size());
        assertTrue(mapping.getPropertyByName("id").isPrimaryKey());
        assertNotNull(mapping.getPropertyByName("createTime"));
        assertNotNull(mapping.getPropertyByName("name"));
        assertNotNull(mapping.getPropertyByName("age"));
        assertNotNull(mapping.getPropertyByName("email"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_INHERITANCE_LEAF_AUTO_FALSE)
    public void annotationInheritLeafAutoMapFalseOverParentTrue() {
        TableMapping<?> mapping = mapping(ManualOverAutoLeaf.class);

        assertEquals(3, mapping.getProperties().size());
        assertNotNull(mapping.getPropertyByName("id"));
        assertNotNull(mapping.getPropertyByName("createTime"));
        assertNotNull(mapping.getPropertyByName("name"));
        assertNull(mapping.getPropertyByName("age"));
        assertNull(mapping.getPropertyByName("email"));
    }

    private TableMapping<?> mapping(Class<?> entityType) {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(entityType);
        TableMapping<?> mapping = registry.findByEntity(entityType);
        assertNotNull(mapping);
        return mapping;
    }
}
