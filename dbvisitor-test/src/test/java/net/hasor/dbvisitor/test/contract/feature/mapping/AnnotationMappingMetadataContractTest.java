/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.annotation.DiffPkEntity;
import net.hasor.dbvisitor.test.contract.material.model.annotation.InsertExcludedUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.PrimaryMarkerUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.ReadOnlyEmailUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.SimpleResultMap;
import net.hasor.dbvisitor.test.contract.material.model.annotation.UpdateExcludedUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.UserResultMap;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class AnnotationMappingMetadataContractTest extends AbstractNxnContractTest {
    @Override
    @Before
    public final void setup() {
        // These registry checks do not open a connection or measure datasource I/O support.
        OneApiDataSourceManager.assumeCurrentDataSource(profile().env());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_PRIMARY_KEY_METADATA)
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
    @Capability(CapabilityId.MAPPING_ANNOTATION_PRIMARY_KEY_MULTIPLE_ENTITIES)
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
    @Capability(CapabilityId.MAPPING_ANNOTATION_PRIMARY_MARKER)
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
    @Capability(CapabilityId.MAPPING_ANNOTATION_RESULT_MAP_METADATA)
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
    @Capability(CapabilityId.MAPPING_ANNOTATION_RESULT_MAP_NON_ENTITY)
    public void resultMapAnnotation_shouldNotMakeClassAnEntity() {
        assertFalse(MappingRegistry.isEntity(UserResultMap.class));
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_WRITE_POLICY_METADATA)
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
