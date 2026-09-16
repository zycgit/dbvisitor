/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.unit.mapping;

import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.contract.material.model.annotation.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class AnnotationInheritanceTest {
    @Test
    public void annotationInheritance_shouldMergeParentFieldColumnAnnotations() {
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
    public void annotationInheritance_shouldCountInheritedAndChildFields() {
        assertEquals(4, mapping(UserEntity.class).getProperties().size());
    }

    @Test
    public void annotationInheritance_shouldCollectMultiLevelHierarchyFields() {
        TableMapping<?> mapping = mapping(ExtendedEntity.class);

        assertEquals(4, mapping.getProperties().size());
        assertNotNull(mapping.getPropertyByName("id"));
        assertNotNull(mapping.getPropertyByName("createTime"));
        assertNotNull(mapping.getPropertyByName("email"));
        assertEquals("name", mapping.getPropertyByName("name").getColumn());
    }

    @Test
    public void annotationInheritance_shouldKeepParentColumnAnnotationsWhenAutoMappingFalse() {
        TableMapping<?> mapping = mapping(ManualChild.class);

        assertEquals(3, mapping.getProperties().size());
        assertNotNull(mapping.getPropertyByName("name"));
        assertNotNull(mapping.getPropertyByName("id"));
        assertNotNull(mapping.getPropertyByName("createTime"));
    }

    @Test
    public void annotationInheritance_shouldAutoMapPlainBaseFields() {
        TableMapping<?> mapping = mapping(ChildWithPlainBase.class);

        assertEquals(3, mapping.getProperties().size());
        assertNotNull(mapping.getPropertyByName("name"));
        assertNotNull(mapping.getPropertyByName("id"));
        assertNotNull(mapping.getPropertyByName("code"));
    }

    @Test
    public void annotationInheritance_shouldFindInheritedPropertiesByColumn() {
        TableMapping<?> mapping = mapping(UserEntity.class);

        assertNotNull(mapping.getPropertyByColumn("id"));
        assertNotNull(mapping.getPropertyByColumn("create_time"));
        assertNotNull(mapping.getPropertyByColumn("name"));
    }

    @Test
    public void annotationInheritance_shouldExposeAllInheritedColumns() {
        TableMapping<?> mapping = mapping(UserEntity.class);

        assertTrue(mapping.getColumns().contains("id"));
        assertTrue(mapping.getColumns().contains("create_time"));
        assertTrue(mapping.getColumns().contains("name"));
        assertTrue(mapping.getColumns().contains("age"));
    }

    @Test
    public void annotationInheritance_shouldKeepLeafEntityType() {
        assertEquals(UserEntity.class, mapping(UserEntity.class).entityType());
    }

    @Test
    public void annotationInheritance_shouldUseLeafAutoMappingTrueOverParentFalse() {
        TableMapping<?> mapping = mapping(AutoTrueOverManualLeaf.class);

        assertEquals(5, mapping.getProperties().size());
        assertTrue(mapping.getPropertyByName("id").isPrimaryKey());
        assertNotNull(mapping.getPropertyByName("createTime"));
        assertNotNull(mapping.getPropertyByName("name"));
        assertNotNull(mapping.getPropertyByName("age"));
        assertNotNull(mapping.getPropertyByName("email"));
    }

    @Test
    public void annotationInheritance_shouldUseLeafAutoMappingFalseOverParentTrue() {
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
