/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.unit.mapping;

import java.util.List;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.annotation.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class AnnotationMappingMetadataTest {
    @Test
    public void annotationMapping_shouldExposePrimaryKeyMetadata() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class);

        TableMapping<?> mapping = registry.findByEntity(UserInfo.class);
        assertNotNull(mapping);

        ColumnMapping idColumn = mapping.getPropertyByName("id");
        ColumnMapping nameColumn = mapping.getPropertyByName("name");
        ColumnMapping ageColumn = mapping.getPropertyByName("age");

        assertNotNull(idColumn);
        assertNotNull(nameColumn);
        assertNotNull(ageColumn);
        assertTrue(idColumn.isPrimaryKey());
        assertFalse(nameColumn.isPrimaryKey());
        assertFalse(ageColumn.isPrimaryKey());
    }

    @Test
    public void annotationMapping_shouldKeepPrimaryKeyMetadataPerEntity() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class, "", "userInfo");
        registry.loadEntityToSpace(DiffPkEntity.class, "", "diffPk");

        TableMapping<?> userMapping = registry.findBySpace("", "userInfo");
        TableMapping<?> diffMapping = registry.findBySpace("", "diffPk");

        assertTrue(userMapping.getPropertyByName("id").isPrimaryKey());
        assertTrue(diffMapping.getPropertyByName("code").isPrimaryKey());
        assertFalse(diffMapping.getPropertyByName("id").isPrimaryKey());
    }

    @Test
    public void annotationMapping_shouldUsePrimaryMarkerForDuplicateColumnMappings() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(PrimaryMarkerUser.class);

        TableMapping<?> mapping = registry.findByEntity(PrimaryMarkerUser.class);
        assertNotNull(mapping);

        List<ColumnMapping> nameColumns = mapping.getPropertyByColumn("name");
        assertEquals(2, nameColumns.size());

        ColumnMapping primaryColumn = mapping.getPrimaryPropertyByColumn("name");
        assertNotNull(primaryColumn);
        assertEquals("primaryName", primaryColumn.getProperty());
    }

    @Test
    public void resultMapAnnotation_shouldLoadSpaceIdValueAndCustomOverride() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadResultMapToSpace(UserResultMap.class);
        registry.loadResultMapToSpace(SimpleResultMap.class);
        registry.loadResultMapToSpace(SimpleResultMap.class, "customSpace", "customName");

        TableMapping<?> userResultMap = registry.findBySpace("mySpace", "myResultMap");
        assertNotNull(userResultMap);
        assertTrue(userResultMap.isAutoProperty());
        assertTrue(userResultMap.isCaseInsensitive());
        assertTrue(userResultMap.isToCamelCase());
        assertTrue(userResultMap.getTable() == null || userResultMap.getTable().isEmpty());

        assertNotNull(registry.findBySpace("", "simpleResult"));
        assertNotNull(registry.findBySpace("customSpace", "customName"));
    }

    @Test
    public void resultMapAnnotation_shouldNotMakeClassAnEntity() {
        assertFalse(MappingRegistry.isEntity(UserResultMap.class));
    }

    @Test
    public void annotationMapping_shouldExposeInsertAndUpdatePolicyMetadata() {
        MappingRegistry registry = new MappingRegistry();

        registry.loadEntityToSpace(UserInfo.class, "", "default");
        registry.loadEntityToSpace(InsertExcludedUser.class, "", "insertExcluded");
        registry.loadEntityToSpace(UpdateExcludedUser.class, "", "updateExcluded");
        registry.loadEntityToSpace(ReadOnlyEmailUser.class, "", "readOnly");

        ColumnMapping defaultEmail = registry.findBySpace("", "default").getPropertyByName("email");
        assertTrue(defaultEmail.isInsert());
        assertTrue(defaultEmail.isUpdate());

        ColumnMapping insertExcludedEmail = registry.findBySpace("", "insertExcluded").getPropertyByName("email");
        assertFalse(insertExcludedEmail.isInsert());
        assertTrue(insertExcludedEmail.isUpdate());

        ColumnMapping updateExcludedEmail = registry.findBySpace("", "updateExcluded").getPropertyByName("email");
        assertTrue(updateExcludedEmail.isInsert());
        assertFalse(updateExcludedEmail.isUpdate());

        ColumnMapping readOnlyEmail = registry.findBySpace("", "readOnly").getPropertyByName("email");
        assertFalse(readOnlyEmail.isInsert());
        assertFalse(readOnlyEmail.isUpdate());
    }
}
