/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.sql.Types;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.mapping.DdlAuto;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnDef;
import net.hasor.dbvisitor.mapping.def.ColumnDescription;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.IndexDescription;
import net.hasor.dbvisitor.mapping.def.TableDescription;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.contract.material.model.tabledef.BlankIndexNameEntity;
import net.hasor.dbvisitor.test.contract.material.model.tabledef.EmptyIndexColEntity;
import net.hasor.dbvisitor.test.contract.material.model.tabledef.FullDdlEntity;
import net.hasor.dbvisitor.test.contract.material.model.tabledef.HasTableAnno;
import net.hasor.dbvisitor.test.contract.material.model.tabledef.NoDdlEntity;
import net.hasor.dbvisitor.test.contract.material.model.tabledef.NoTableAnno;
import net.hasor.dbvisitor.test.contract.material.model.tabledef.ResultMapEntity;
import net.hasor.dbvisitor.test.contract.material.model.tabledef.SimpleDdlEntity;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@NxnContract
public abstract class MappingTableDefinitionContractTest extends AbstractNxnContractTest {
    @Test
    @Capability(CapabilityId.MAPPING_TABLEDEF_DDL_AUTO_METADATA)
    public void tableDefinition_shouldExposeDdlAutoMetadata() {
        TableDescription create = mapping(FullDdlEntity.class).getDescription();
        assertNotNull(create);
        assertEquals(DdlAuto.Create, create.getDdlAuto());

        TableDescription addColumn = mapping(SimpleDdlEntity.class).getDescription();
        assertNotNull(addColumn);
        assertEquals(DdlAuto.AddColumn, addColumn.getDdlAuto());
        assertNull(addColumn.getCharacterSet());
        assertNull(addColumn.getCollation());
        assertNull(addColumn.getComment());

        assertNull(mapping(NoDdlEntity.class).getDescription());
    }

    @Test
    @Capability(CapabilityId.MAPPING_TABLEDEF_TABLE_DESCRIPTION)
    public void tableDefinition_shouldExposeTableDescriptionAttributes() {
        TableDescription desc = mapping(FullDdlEntity.class).getDescription();
        assertNotNull(desc);
        assertEquals("utf8mb4", desc.getCharacterSet());
        assertEquals("utf8mb4_general_ci", desc.getCollation());
        assertEquals("Test table for DDL", desc.getComment());
        assertEquals("ENGINE=InnoDB", desc.getOther());
    }

    @Test
    @Capability(CapabilityId.MAPPING_TABLEDEF_COLUMN_DESCRIPTION)
    public void tableDefinition_shouldExposeColumnDescriptionAttributes() {
        TableMapping<?> mapping = mapping(FullDdlEntity.class);

        ColumnDescription id = description(mapping, "id");
        assertEquals("BIGINT", id.getSqlType());
        assertFalse(id.isNullable());
        assertEquals("Primary key", id.getComment());

        ColumnDescription name = description(mapping, "name");
        assertEquals("VARCHAR(100)", name.getSqlType());
        assertFalse(name.isNullable());
        assertEquals("'unknown'", name.getDefault());
        assertEquals("User name", name.getComment());

        ColumnDescription email = description(mapping, "email");
        assertEquals("utf8", email.getCharacterSet());
        assertEquals("utf8_bin", email.getCollation());
        assertEquals("UNIQUE", email.getOther());
        assertEquals("Email address", email.getComment());

        ColumnDescription balance = description(mapping, "balance");
        assertEquals("DECIMAL", balance.getSqlType());
        assertEquals("10", balance.getPrecision());
        assertEquals("2", balance.getScale());

        assertNull(mapping(NoDdlEntity.class).getPropertyByName("name").getDescription());
    }

    @Test
    @Capability(CapabilityId.MAPPING_TABLEDEF_INDEX_DESCRIPTION)
    public void tableDefinition_shouldExposeIndexDescriptions() {
        TableMapping<?> mapping = mapping(FullDdlEntity.class);
        List<IndexDescription> indexes = mapping.getIndexes();
        assertNotNull(indexes);
        assertEquals(2, indexes.size());

        IndexDescription name = mapping.getIndex("idx_name");
        assertNotNull(name);
        assertEquals("idx_name", name.getName());
        assertFalse(name.isUnique());
        assertEquals(1, name.getColumns().size());
        assertEquals("name", name.getColumns().get(0));
        assertEquals("Name index", name.getComment());

        IndexDescription composite = mapping.getIndex("idx_name_age");
        assertNotNull(composite);
        assertTrue(composite.isUnique());
        assertEquals(2, composite.getColumns().size());
        assertEquals("name", composite.getColumns().get(0));
        assertEquals("age", composite.getColumns().get(1));
        assertEquals("Composite unique index", composite.getComment());
        assertEquals("USING BTREE", composite.getOther());

        List<IndexDescription> noIndexes = mapping(NoDdlEntity.class).getIndexes();
        assertTrue(noIndexes == null || noIndexes.isEmpty());
        assertNull(mapping.getIndex("non_existent_index"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_TABLEDEF_INDEX_VALIDATION)
    public void tableDefinition_shouldRejectInvalidIndexDescriptions() {
        assertThrows(IllegalArgumentException.class, "missing index name", () -> new MappingRegistry().loadEntityToSpace(BlankIndexNameEntity.class));
        assertThrows(IllegalArgumentException.class, "columns has empty", () -> new MappingRegistry().loadEntityToSpace(EmptyIndexColEntity.class));
    }

    @Test
    @Capability(CapabilityId.MAPPING_TABLEDEF_REGISTRY_STRUCTURE)
    public void tableDefinition_shouldExposeRegistryStructureBoundaries() throws Exception {
        TypeHandlerRegistry typeRegistry = new TypeHandlerRegistry();
        ColumnDef column = new ColumnDef("user_name", "userName", Types.VARCHAR, String.class, typeRegistry.getDefaultTypeHandler(), null);
        column.setPrimaryKey(true);
        column.setInsert(true);
        column.setUpdate(false);

        String columnString = column.toString();
        assertTrue(columnString.startsWith("ColumnDef{"));
        assertTrue(columnString.contains("columnName='user_name'"));
        assertTrue(columnString.contains("propertyName='userName'"));
        assertTrue(columnString.contains("jdbcType=" + Types.VARCHAR));
        assertTrue(columnString.contains("insert=true"));
        assertTrue(columnString.contains("update=false"));
        assertTrue(columnString.contains("primary=true"));

        assertTrue(MappingRegistry.isEntity(HasTableAnno.class));
        assertFalse(MappingRegistry.isEntity(NoTableAnno.class));

        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(HasTableAnno.class, "my_space");
        assertEquals("has_table_anno", registry.findBySpace("my_space", HasTableAnno.class).getTable());
        assertNull(registry.findBySpace("nonexistent", HasTableAnno.class));

        registry.loadResultMapToSpace(ResultMapEntity.class, "overrideSpace", "overrideName");
        assertNotNull(registry.findBySpace("overrideSpace", "overrideName"));

        MappingRegistry xmlRegistry = new MappingRegistry();
        xmlRegistry.loadMapping("/mapping/basic_entity_mapper.xml");
        xmlRegistry.loadMapping("/mapping/basic_entity_mapper.xml");
        assertNotNull(xmlRegistry.findBySpace("net.hasor.test.mapping", "FullEntity"));

        MappingRegistry resultMapRegistry = new MappingRegistry();
        resultMapRegistry.loadResultMapToSpace(ResultMapEntity.class, "sp", "rm");
        TableMapping<?> resultMap = resultMapRegistry.findBySpace("sp", "rm");
        assertNotNull(resultMap);
        assertEquals("", resultMap.getCatalog());
        assertEquals("", resultMap.getSchema());
        assertEquals("", resultMap.getTable());
    }

    private TableMapping<?> mapping(Class<?> entityType) {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(entityType);
        TableMapping<?> mapping = registry.findByEntity(entityType);
        assertNotNull(mapping);
        return mapping;
    }

    private ColumnDescription description(TableMapping<?> mapping, String property) {
        ColumnMapping column = mapping.getPropertyByName(property);
        assertNotNull(column);
        assertNotNull(column.getDescription());
        return column.getDescription();
    }

    private void assertThrows(Class<? extends Throwable> expected, String messagePart, ThrowingRunnable runnable) {
        try {
            runnable.run();
            fail("Expected " + expected.getName());
        } catch (Throwable e) {
            if (!expected.isInstance(e)) {
                throw new AssertionError("Expected " + expected.getName() + " but got " + e.getClass().getName(), e);
            }
            assertTrue("Expected message containing '" + messagePart + "', got: " + e.getMessage(), e.getMessage().contains(messagePart));
        }
    }

    private interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
