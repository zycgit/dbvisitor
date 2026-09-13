/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.mapping;

import org.junit.Test;

import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.annotation.TableAttrUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.DelimitedUser;
import net.hasor.dbvisitor.test.contract.material.model.tabledef.CamelCaseEntity;
import net.hasor.dbvisitor.test.contract.material.model.tabledef.CaseSensitiveUser;
import net.hasor.dbvisitor.test.contract.material.model.tabledef.CatalogSchemaUser;
import net.hasor.dbvisitor.test.contract.material.model.tabledef.OrderByTemplateUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class AnnotationTableMetadataCase extends AbstractNxnContractTest {
    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_TABLE_OPTIONS_METADATA)
    public void tableAnnotation_shouldExposeCatalogSchemaDelimitedCaseAndCamelOptions() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(CatalogSchemaUser.class);
        registry.loadEntityToSpace(DelimitedUser.class);
        registry.loadEntityToSpace(CaseSensitiveUser.class);
        registry.loadEntityToSpace(UserInfo.class, "", "userInfo");
        registry.loadEntityToSpace(CamelCaseEntity.class);

        TableMapping<?> catalog = registry.findByEntity(CatalogSchemaUser.class);
        assertEquals("my_catalog", catalog.getCatalog());
        assertEquals("my_schema", catalog.getSchema());
        assertEquals("user_info", catalog.getTable());

        assertTrue(registry.findByEntity(DelimitedUser.class).useDelimited());
        assertFalse(registry.findByEntity(CaseSensitiveUser.class).isCaseInsensitive());
        assertTrue(registry.findBySpace("", "userInfo").isCaseInsensitive());

        TableMapping<?> camel = registry.findByEntity(CamelCaseEntity.class);
        assertTrue(camel.isToCamelCase());
        assertEquals("create_time", camel.getPropertyByName("createTime").getColumn());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_TABLE_ATTRIBUTE_EQUIVALENCE)
    public void tableAnnotation_shouldTreatValueAndTableAttributesAsEquivalent() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(TableAttrUser.class, "", "tableAttr");
        registry.loadEntityToSpace(UserInfo.class, "", "tableValue");

        TableMapping<?> attrMapping = registry.findBySpace("", "tableAttr");
        TableMapping<?> valueMapping = registry.findBySpace("", "tableValue");

        assertEquals("user_info", attrMapping.getTable());
        assertEquals("user_info", valueMapping.getTable());
        assertEquals(valueMapping.getTable(), attrMapping.getTable());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_CATALOG_SCHEMA_METADATA)
    public void tableAnnotation_shouldExposeAnnotationCatalogAndSchemaMetadata() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(net.hasor.dbvisitor.test.contract.material.model.annotation.CatalogSchemaUser.class);
        registry.loadEntityToSpace(UserInfo.class, "", "defaultUser");

        TableMapping<?> mapping = registry.findByEntity(net.hasor.dbvisitor.test.contract.material.model.annotation.CatalogSchemaUser.class);
        assertNotNull(mapping);
        assertEquals("test_catalog", mapping.getCatalog());
        assertEquals("test_schema", mapping.getSchema());
        assertEquals("user_info", mapping.getTable());

        TableMapping<?> defaultMapping = registry.findBySpace("", "defaultUser");
        assertTrue(defaultMapping.getCatalog() == null || defaultMapping.getCatalog().isEmpty());
        assertTrue(defaultMapping.getSchema() == null || defaultMapping.getSchema().isEmpty());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_ORDER_BY_TEMPLATE)
    public void columnAnnotation_shouldExposeOrderByColumnTemplates() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(OrderByTemplateUser.class);

        TableMapping<?> mapping = registry.findByEntity(OrderByTemplateUser.class);
        ColumnMapping name = mapping.getPropertyByName("name");
        ColumnMapping age = mapping.getPropertyByName("age");
        ColumnMapping email = mapping.getPropertyByName("email");

        assertNotNull(name);
        assertNotNull(age);
        assertNotNull(email);
        assertEquals("LOWER(name)", name.getOrderByColTemplate());
        assertEquals("age * -1", age.getOrderByColTemplate());
        assertTrue(email.getOrderByColTemplate() == null || email.getOrderByColTemplate().isEmpty());
    }
}
