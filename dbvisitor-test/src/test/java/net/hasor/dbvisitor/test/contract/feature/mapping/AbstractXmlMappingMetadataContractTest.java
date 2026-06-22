package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import net.hasor.cobble.io.IOUtils;
import net.hasor.dbvisitor.dialect.provider.PostgreSqlDialect;
import net.hasor.dbvisitor.mapping.DdlAuto;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.IndexDescription;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.contract.material.handler.UpperCaseTypeHandler;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class AbstractXmlMappingMetadataContractTest extends AbstractNxnContractTest {
    @Test
    @Capability(CapabilityId.MAPPING_XML_WRITE_POLICY)
    public void xmlMapping_shouldExposeWritePolicyMetadata() throws Exception {
        MappingRegistry registry = load("mapping/write_policy_test.xml");

        ColumnMapping insertFalseName = mapping(registry, "net.hasor.test.xml.writepolicy", "InsertFalseEntity").getPropertyByName("name");
        assertFalse(insertFalseName.isInsert());
        assertTrue(insertFalseName.isUpdate());

        ColumnMapping updateFalseName = mapping(registry, "net.hasor.test.xml.writepolicy", "UpdateFalseEntity").getPropertyByName("name");
        assertTrue(updateFalseName.isInsert());
        assertFalse(updateFalseName.isUpdate());

        ColumnMapping readOnlyName = mapping(registry, "net.hasor.test.xml.writepolicy", "BothFalseEntity").getPropertyByName("name");
        assertFalse(readOnlyName.isInsert());
        assertFalse(readOnlyName.isUpdate());

        ColumnMapping defaultAge = mapping(registry, "net.hasor.test.xml.writepolicy", "DefaultPolicyEntity").getPropertyByName("age");
        assertTrue(defaultAge.isInsert());
        assertTrue(defaultAge.isUpdate());
    }

    @Test
    @Capability(CapabilityId.MAPPING_XML_TYPE_HANDLER)
    public void xmlMapping_shouldLoadCustomTypeHandlerClass() throws Exception {
        MappingRegistry registry = load("mapping/type_handler_test.xml");

        ColumnMapping name = mapping(registry, "net.hasor.test.xml.typehandler", "TypeHandlerEntity").getPropertyByName("name");

        assertNotNull(name.getTypeHandler());
        assertTrue(name.getTypeHandler() instanceof UpperCaseTypeHandler);
    }

    @Test
    @Capability(CapabilityId.MAPPING_XML_JDBC_TYPE)
    public void xmlMapping_shouldExposeExplicitJdbcTypes() throws Exception {
        MappingRegistry registry = load("mapping/type_handler_test.xml");
        TableMapping<?> mapping = mapping(registry, "net.hasor.test.xml.typehandler", "JdbcTypeEntity");

        assertEquals(Integer.valueOf(4), mapping.getPropertyByName("id").getJdbcType());
        assertEquals(Integer.valueOf(12), mapping.getPropertyByName("name").getJdbcType());
    }

    @Test
    @Capability(CapabilityId.MAPPING_XML_COLUMN_DESCRIPTION)
    public void xmlMapping_shouldExposeColumnDescriptionAttributes() throws Exception {
        MappingRegistry registry = load("mapping/type_handler_test.xml");

        ColumnMapping name = mapping(registry, "net.hasor.test.xml.typehandler", "DescCharsetEntity").getPropertyByName("name");

        assertNotNull(name.getDescription());
        assertEquals("utf8mb4", name.getDescription().getCharacterSet());
        assertEquals("utf8mb4_bin", name.getDescription().getCollation());
        assertEquals("COMMENT 'extra'", name.getDescription().getOther());
    }

    @Test
    @Capability(CapabilityId.MAPPING_XML_TEMPLATE_ATTRS)
    public void xmlMapping_shouldExposeSqlTemplateAttributes() throws Exception {
        MappingRegistry registry = load("mapping/template_attrs.xml");

        ColumnMapping selectInsert = mapping(registry, "net.hasor.test.xml.template", "SelectInsertTemplateEntity").getPropertyByName("name");
        assertEquals("UPPER(name)", selectInsert.getSelectTemplate());
        assertEquals("LOWER(?)", selectInsert.getInsertTemplate());
        assertNull(selectInsert.getSetValueTemplate());
        assertNull(selectInsert.getWhereColTemplate());

        ColumnMapping updateWhere = mapping(registry, "net.hasor.test.xml.template", "UpdateWhereTemplateEntity").getPropertyByName("name");
        assertEquals("TRIM(?)", updateWhere.getSetValueTemplate());
        assertEquals("LOWER(name)", updateWhere.getWhereColTemplate());
        assertNull(updateWhere.getSelectTemplate());

        ColumnMapping allTemplates = mapping(registry, "net.hasor.test.xml.template", "AllTemplatesEntity").getPropertyByName("name");
        assertEquals("UPPER(name)", allTemplates.getSelectTemplate());
        assertEquals("LOWER(?)", allTemplates.getInsertTemplate());
        assertEquals("TRIM(?)", allTemplates.getSetValueTemplate());
        assertEquals("LOWER(name)", allTemplates.getWhereColTemplate());
        assertEquals("LOWER(?)", allTemplates.getWhereValueTemplate());

        ColumnMapping id = mapping(registry, "net.hasor.test.xml.template", "SelectInsertTemplateEntity").getPropertyByName("id");
        assertNull(id.getSelectTemplate());
        assertNull(id.getInsertTemplate());
        assertNull(id.getSetValueTemplate());
        assertNull(id.getWhereColTemplate());
        assertNull(id.getWhereValueTemplate());
        assertNull(id.getOrderByColTemplate());
    }

    @Test
    @Capability(CapabilityId.MAPPING_XML_ENTITY_METADATA)
    public void xmlMapping_shouldExposeEntityMetadataFromXml() throws Exception {
        MappingRegistry registry = load("mapping/basic_entity_mapper.xml");
        TableMapping<?> full = mapping(registry, "net.hasor.test.mapping", "FullEntity");

        assertEquals("user_info", full.getTable());
        assertEquals("test_cat", full.getCatalog());
        assertEquals("test_sch", full.getSchema());
        assertEquals(UserInfo.class, full.entityType());
        assertNotNull(full.getDescription());
        assertEquals(DdlAuto.Create, full.getDescription().getDdlAuto());
        assertEquals("utf8mb4", full.getDescription().getCharacterSet());
        assertEquals("utf8mb4_general_ci", full.getDescription().getCollation());
        assertEquals("user table", full.getDescription().getComment());
        assertEquals("ENGINE=InnoDB", full.getDescription().getOther());

        assertTrue(full.getPropertyByName("id").isPrimaryKey());
        assertFalse(full.getPropertyByName("age").isInsert());
        assertTrue(full.getPropertyByName("age").isUpdate());
        assertTrue(full.getPropertyByName("email").isInsert());
        assertFalse(full.getPropertyByName("email").isUpdate());

        assertEquals(2, full.getIndexes().size());
        assertEquals("name", full.getIndex("idx_name").getColumns().get(0));
        assertFalse(full.getIndex("idx_name").isUnique());
        assertTrue(full.getIndex("idx_email_age").isUnique());
        assertEquals("composite index", full.getIndex("idx_email_age").getComment());

        TableMapping<?> desc = mapping(registry, "net.hasor.test.mapping", "DescEntity");
        assertEquals("BIGINT", desc.getPropertyByName("id").getDescription().getSqlType());
        assertFalse(desc.getPropertyByName("id").getDescription().isNullable());
        assertEquals("VARCHAR", desc.getPropertyByName("name").getDescription().getSqlType());
        assertEquals("128", desc.getPropertyByName("name").getDescription().getLength());
        assertEquals("'unknown'", desc.getPropertyByName("name").getDescription().getDefault());
        assertEquals("user name", desc.getPropertyByName("name").getDescription().getComment());
        assertEquals("10", desc.getPropertyByName("age").getDescription().getPrecision());
        assertEquals("0", desc.getPropertyByName("age").getDescription().getScale());

        TableMapping<?> partial = mapping(registry, "net.hasor.test.mapping", "PartialEntity");
        assertEquals(2, partial.getProperties().size());
        assertNotNull(partial.getPropertyByName("id"));
        assertNotNull(partial.getPropertyByName("name"));
        assertNull(partial.getPropertyByName("age"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_XML_RESULT_MAP_METADATA)
    public void xmlMapping_shouldExposeResultMapMetadata() throws Exception {
        MappingRegistry registry = load("mapping/basic_entity_mapper.xml");
        TableMapping<?> resultMap = mapping(registry, "net.hasor.test.mapping", "SimpleResultMap");

        assertEquals(UserInfo.class, resultMap.entityType());
        assertEquals("", resultMap.getTable());
        Collection<ColumnMapping> props = resultMap.getProperties();
        assertEquals(5, props.size());
        assertNotNull(resultMap.getPropertyByName("id"));
        assertNotNull(resultMap.getPropertyByName("name"));
        assertNotNull(resultMap.getPropertyByName("createTime"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_XML_LOAD_BOUNDARIES)
    public void xmlMapping_shouldExposeLoadBoundaries() throws Exception {
        String xml = readResource("/mapping/stream_test.xml");
        MappingRegistry streamRegistry = new MappingRegistry();
        try (InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            streamRegistry.loadMapping("test-stream-id", stream);
        }
        assertEquals("user_info", mapping(streamRegistry, "stream.test", "StreamEntity").getTable());

        MappingRegistry duplicate = load("mapping/basic_entity_mapper.xml");
        duplicate.loadMapping("mapping/basic_entity_mapper.xml");
        assertNotNull(duplicate.findBySpace("net.hasor.test.mapping", "FullEntity"));

        MappingRegistry blank = new MappingRegistry();
        blank.loadMapping("");
        blank.loadMapping("  ");
        try (InputStream stream = new ByteArrayInputStream("<mapper/>".getBytes("UTF-8"))) {
            blank.loadMapping("  ", stream);
        }
    }

    @Test
    @Capability(CapabilityId.MAPPING_XML_OPTIONS_INHERITANCE)
    public void xmlMapping_shouldApplyOptionsInheritanceAndOverrides() throws Exception {
        assertFalse(loadFromResource("opt-ci", "/mapping/opt_ci.xml").findBySpace("opt.ci", "OverrideCI").isCaseInsensitive());
        assertFalse(loadFromResource("opt-am", "/mapping/opt_am.xml").findBySpace("opt.am", "OverrideAM").isAutoProperty());

        Options globalOptions = Options.of().catalog("global_cat").schema("global_sch");
        MappingRegistry global = new MappingRegistry(null, null, globalOptions);
        loadFromResource(global, "opt-global", "/mapping/opt_global.xml");
        TableMapping<?> globalEntity = mapping(global, "opt.global", "GlobalEntity");
        assertEquals("global_cat", globalEntity.getCatalog());
        assertEquals("global_sch", globalEntity.getSchema());

        MappingRegistry override = new MappingRegistry(null, null, globalOptions);
        loadFromResource(override, "opt-override", "/mapping/opt_override.xml");
        TableMapping<?> overrideEntity = mapping(override, "opt.override", "OverrideEntity");
        assertEquals("entity_cat", overrideEntity.getCatalog());
        assertEquals("entity_sch", overrideEntity.getSchema());

        TableMapping<?> partialResultMap = mapping(loadFromResource("opt-rm", "/mapping/opt_rm.xml"), "opt.rm", "PartialResultMap");
        assertFalse(partialResultMap.isAutoProperty());
        assertEquals(2, partialResultMap.getProperties().size());
        assertEquals("", partialResultMap.getTable());

        assertTrue(mapping(loadFromResource("opt-rmci", "/mapping/opt_rmci.xml"), "opt.rmci", "CIResultMap").isCaseInsensitive());
        assertTrue(mapping(loadFromResource("opt-defaultam", "/mapping/opt_defaultam.xml"), "opt.defaultam", "DefaultAM").isAutoProperty());
        assertFalse(mapping(loadFromResource("opt-test", "/mapping/opt_root_level.xml"), "opt.test", "InheritedOpt").isCaseInsensitive());
        assertTrue(mapping(loadFromResource("opt-test", "/mapping/opt_root_level.xml"), "opt.test", "InheritedOpt").useDelimited());
    }

    @Test
    @Capability(CapabilityId.MAPPING_XML_NAMING_OPTIONS)
    public void xmlMapping_shouldExposeNamingOptions() throws Exception {
        MappingRegistry registry = load("mapping/naming_options.xml");
        assertTrue(mapping(registry, "net.hasor.test.xml.naming", "CamelCaseEntity").isToCamelCase());
        assertTrue(mapping(registry, "net.hasor.test.xml.naming", "CaseInsensitiveEntity").isCaseInsensitive());
        assertTrue(mapping(registry, "net.hasor.test.xml.naming", "DelimitedEntity").useDelimited());

        TableMapping<?> all = mapping(registry, "net.hasor.test.xml.naming", "AllNamingEntity");
        assertTrue(all.isToCamelCase());
        assertFalse(all.isCaseInsensitive());
        assertTrue(all.useDelimited());

        assertTrue(mapping(loadFromResource("naming-root-cc", "/mapping/naming_root_cc.xml"), "naming.rootcc", "InheritedEntity").isToCamelCase());
        assertFalse(mapping(loadFromResource("naming-override", "/mapping/naming_override.xml"), "naming.override", "OverrideEntity").isToCamelCase());
        assertFalse(mapping(loadFromResource("naming-default", "/mapping/naming_default.xml"), "naming.default", "DefaultEntity").isToCamelCase());
    }

    @Test
    @Capability(CapabilityId.MAPPING_XML_KEYGEN_METADATA)
    public void xmlMapping_shouldExposeKeyGenerationMetadata() throws Exception {
        MappingRegistry registry = load("mapping/keygen_types.xml");
        assertKeyHolder(mapping(registry, "net.hasor.test.xml.keygen", "AutoKeyEntity"), "id", "Auto@");
        assertNull(mapping(registry, "net.hasor.test.xml.keygen", "UUID32KeyEntity").getPropertyByName("id").getKeySeqHolder());
        assertKeyHolder(mapping(registry, "net.hasor.test.xml.keygen", "UUID32KeyEntity"), "name", "UUID32@");
        assertKeyHolder(mapping(registry, "net.hasor.test.xml.keygen", "UUID36KeyEntity"), "name", "UUID36@");
        assertNull(mapping(registry, "net.hasor.test.xml.keygen", "NoneKeyEntity").getPropertyByName("id").getKeySeqHolder());
        assertNull(mapping(registry, "net.hasor.test.xml.keygen", "NoneKeyEntity").getPropertyByName("name").getKeySeqHolder());
        assertKeyHolder(mapping(registry, "net.hasor.test.xml.keygen", "MappingKeyEntity"), "id", "Auto@");
        assertKeyHolder(mapping(registry, "net.hasor.test.xml.keygen", "MappingKeyEntity"), "name", "UUID36@");
        assertKeyHolder(mapping(registry, "net.hasor.test.xml.keygen", "MappingKeyEntity"), "email", "UUID32@");

        MappingRegistry sequence = new MappingRegistry(null, null, Options.of().dialect(new PostgreSqlDialect()));
        loadFromResource(sequence, "keygen-seq", "/mapping/keygen_sequence.xml");
        assertKeyHolder(mapping(sequence, "keygen.seq", "SeqEntity"), "id", "Sequence@");

        MappingRegistry custom = load("/mapping/custom_key_factory.xml");
        assertNotNull(mapping(custom, "net.hasor.test.xml.customkey", "CustomKeyEntity").getPropertyByName("id").getKeySeqHolder());
    }

    @Test
    @Capability(CapabilityId.MAPPING_XML_ERROR_PATHS)
    public void xmlMapping_shouldExposeErrorPaths() throws Exception {
        assertThrows(ClassCastException.class, "is not a subclass of", () -> load("/mapping/bad_javatype.xml"));
        assertThrows(IllegalStateException.class, "already exists", () -> load("/mapping/table_level_dup.xml"));
        assertThrows(UnsupportedOperationException.class, "Unsupported", () -> load("/mapping/unsupported_tag.xml"));
        assertThrows(IllegalArgumentException.class, "columns is empty", () -> load("/mapping/index_empty_cols.xml"));
        assertThrows(IOException.class, "nonExistentProperty", () -> load("/mapping/unknown_property.xml"));
        assertThrows(IOException.class, null, () -> loadFromResource("err-missing-type", "/mapping/err_missing_type.xml"));
        assertThrows(IOException.class, null, () -> loadFromResource("err-missing-table", "/mapping/err_missing_table.xml"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_XML_INDEX_METADATA)
    public void xmlMapping_shouldExposeIndexSubElementsAndClassAnnotationFallback() throws Exception {
        MappingRegistry registry = load("/mapping/index_column_sub.xml");
        TableMapping<?> mapping = mapping(registry, "net.hasor.test.xml.indexcolsub", "IndexColSubEntity");
        IndexDescription idx = mapping.getIndex("idx_cols_sub");
        assertNotNull(idx);
        assertEquals("idx_cols_sub", idx.getName());
        assertTrue(idx.isUnique());
        assertEquals("index via sub elements", idx.getComment());
        assertNull(idx.getOther());
        List<String> columns = idx.getColumns();
        assertEquals(2, columns.size());
        assertEquals("name", columns.get(0));
        assertEquals("email", columns.get(1));
        assertTrue(mapping.getProperties().size() >= 3);
        assertNotNull(mapping.getPropertyByName("id"));
        assertNotNull(mapping.getPropertyByName("name"));
        assertNotNull(mapping.getPropertyByName("email"));

        TableMapping<?> fallback = mapping(load("/mapping/no_mapping_entity.xml"), "net.hasor.test.xml.nomapping", "NoMappingEntity");
        assertEquals("user_info", fallback.getTable());
        assertTrue(fallback.getPropertyByName("id").isPrimaryKey());
        assertEquals("create_time", fallback.getPropertyByName("createTime").getColumn());
        assertNotNull(fallback.getPropertyByName("name"));
    }

    private MappingRegistry load(String resource) throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping(resource);
        return registry;
    }

    private TableMapping<?> mapping(MappingRegistry registry, String namespace, String id) {
        TableMapping<?> mapping = registry.findBySpace(namespace, id);
        assertNotNull(mapping);
        return mapping;
    }

    private String readResource(String resource) throws IOException {
        return IOUtils.readToString(getClass().getResourceAsStream(resource), "UTF-8");
    }

    private MappingRegistry loadFromResource(String id, String resource) throws Exception {
        MappingRegistry registry = new MappingRegistry();
        loadFromResource(registry, id, resource);
        return registry;
    }

    private void loadFromResource(MappingRegistry registry, String id, String resource) throws Exception {
        try (InputStream stream = new ByteArrayInputStream(readResource(resource).getBytes("UTF-8"))) {
            registry.loadMapping(id, stream);
        }
    }

    private void assertKeyHolder(TableMapping<?> mapping, String property, String prefix) {
        ColumnMapping column = mapping.getPropertyByName(property);
        assertNotNull(column);
        assertNotNull(column.getKeySeqHolder());
        assertTrue(column.getKeySeqHolder().toString().startsWith(prefix));
    }

    private void assertThrows(Class<? extends Throwable> expected, String messagePart, ThrowingRunnable runnable) {
        try {
            runnable.run();
            fail("Expected " + expected.getName());
        } catch (Throwable e) {
            if (!expected.isInstance(e)) {
                throw new AssertionError("Expected " + expected.getName() + " but got " + e.getClass().getName(), e);
            }
            if (messagePart != null && !messageContains(e, messagePart)) {
                throw new AssertionError("Expected message containing '" + messagePart + "', got: " + e.getMessage(), e);
            }
        }
    }

    private boolean messageContains(Throwable throwable, String messagePart) {
        for (Throwable cursor = throwable; cursor != null; cursor = cursor.getCause()) {
            if (cursor.getMessage() != null && cursor.getMessage().contains(messagePart)) {
                return true;
            }
        }
        return false;
    }

    private interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
