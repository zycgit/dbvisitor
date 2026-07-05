package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.mapping.DdlAuto;
import net.hasor.dbvisitor.mapping.KeyType;
import net.hasor.dbvisitor.mapping.MappingHelper;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.contract.material.model.AnnotatedEntity;
import net.hasor.dbvisitor.test.contract.material.model.ExplicitAnnotatedEntity;
import net.hasor.dbvisitor.test.contract.material.model.PlainEntity;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.registry.AnnotatedResultMapEntity;
import net.hasor.dbvisitor.test.contract.material.model.registry.CacheEntityA;
import net.hasor.dbvisitor.test.contract.material.model.registry.CatalogSchemaEntity;
import net.hasor.dbvisitor.test.contract.material.model.registry.EntityA;
import net.hasor.dbvisitor.test.contract.material.model.registry.EntityB;
import net.hasor.dbvisitor.test.contract.material.model.registry.OrderEntity;
import net.hasor.dbvisitor.test.contract.material.model.registry.PlainPojo;
import net.hasor.dbvisitor.test.contract.material.model.registry.PlainUserNamePojo;
import net.hasor.dbvisitor.test.contract.material.model.registry.ProductEntity;
import net.hasor.dbvisitor.test.contract.material.model.registry.SameTableEntity1;
import net.hasor.dbvisitor.test.contract.material.model.registry.SameTableEntity2;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class MappingRegistryContractTest extends AbstractNxnContractTest {
    @Test
    @Capability(CapabilityId.MAPPING_REGISTRY_OPTIONS_OBJECT)
    public void options_shouldExposeDefaultsBuilderCopyFluentDialectAndToString() {
        Options defaults = Options.of();
        assertNull(defaults.getCatalog());
        assertNull(defaults.getSchema());
        assertNull(defaults.getAutoMapping());
        assertNull(defaults.getMapUnderscoreToCamelCase());
        assertNull(defaults.getCaseInsensitive());
        assertNull(defaults.getUseDelimited());
        assertNull(defaults.getIgnoreNonExistStatement());
        assertNull(defaults.getDialect());

        Options built = Options.of()//
                .catalog("cat")//
                .schema("sch")//
                .autoMapping(true)//
                .mapUnderscoreToCamelCase(true)//
                .caseInsensitive(false)//
                .useDelimited(true)//
                .dialect(MySqlDialect.DEFAULT)//
                .ignoreNonExistStatement(true);
        Options copy = Options.of(built);
        assertEquals("cat", copy.getCatalog());
        assertEquals("sch", copy.getSchema());
        assertEquals(Boolean.TRUE, copy.getAutoMapping());
        assertEquals(Boolean.TRUE, copy.getMapUnderscoreToCamelCase());
        assertEquals(Boolean.FALSE, copy.getCaseInsensitive());
        assertEquals(Boolean.TRUE, copy.getUseDelimited());
        assertSame(MySqlDialect.DEFAULT, copy.getDialect());
        assertEquals(Boolean.TRUE, copy.getIgnoreNonExistStatement());

        copy.setCatalog("changed");
        copy.setDialect(null);
        assertEquals("cat", built.getCatalog());
        assertSame(MySqlDialect.DEFAULT, built.getDialect());

        Options nullCopy = Options.of(null);
        assertNull(nullCopy.getCatalog());
        assertNull(nullCopy.getDialect());
        assertSame(nullCopy, nullCopy.ignoreNonExistStatement(false));
        assertSame(nullCopy, nullCopy.dialect(MySqlDialect.DEFAULT));
        assertSame(MySqlDialect.DEFAULT, nullCopy.getDialect());

        String stringValue = built.toString();
        assertTrue(stringValue.startsWith("Options["));
        assertTrue(stringValue.endsWith("]"));
        assertTrue(stringValue.contains(MySqlDialect.class.getName()));
        assertTrue(Options.of().toString().contains("null"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_REGISTRY_OPTIONS_MAPPING)
    public void options_shouldDrivePlainEntityMappingAndAnnotationPrecedence() {
        Options disabledAuto = Options.of().autoMapping(false);
        MappingRegistry disabledRegistry = new MappingRegistry(null, null, disabledAuto);
        disabledRegistry.loadEntityToSpace(PlainEntity.class);
        TableMapping<?> disabledPlain = disabledRegistry.findByEntity(PlainEntity.class);
        assertNotNull(disabledPlain);
        assertFalse(disabledPlain.isAutoProperty());
        assertTrue(disabledPlain.getProperties().isEmpty());

        Options enabled = Options.of()//
                .catalog("global_cat")//
                .schema("global_sch")//
                .autoMapping(true)//
                .mapUnderscoreToCamelCase(true)//
                .caseInsensitive(false)//
                .useDelimited(true);
        MappingRegistry enabledRegistry = new MappingRegistry(null, null, enabled);
        enabledRegistry.loadEntityToSpace(PlainEntity.class);
        TableMapping<?> enabledPlain = enabledRegistry.findByEntity(PlainEntity.class);
        assertEquals("global_cat", enabledPlain.getCatalog());
        assertEquals("global_sch", enabledPlain.getSchema());
        assertTrue(enabledPlain.isAutoProperty());
        assertTrue(enabledPlain.isToCamelCase());
        assertFalse(enabledPlain.isCaseInsensitive());
        assertTrue(enabledPlain.useDelimited());
        assertEquals("plain_entity", enabledPlain.getTable());
        assertEquals("user_name", enabledPlain.getPropertyByName("userName").getColumn());

        MappingRegistry implicitRegistry = new MappingRegistry(null, null, Options.of().autoMapping(false));
        implicitRegistry.loadEntityToSpace(AnnotatedEntity.class);
        assertFalse(implicitRegistry.findByEntity(AnnotatedEntity.class).isAutoProperty());

        MappingRegistry explicitRegistry = new MappingRegistry(null, null, Options.of().autoMapping(false));
        explicitRegistry.loadEntityToSpace(ExplicitAnnotatedEntity.class);
        assertTrue(explicitRegistry.findByEntity(ExplicitAnnotatedEntity.class).isAutoProperty());
    }

    @Test
    @Capability(CapabilityId.MAPPING_REGISTRY_HELPER_ALIASES)
    public void mappingHelper_shouldResolveTypeAliasesEnumsAndEntityDetection() throws Exception {
        assertEquals("int", MappingHelper.typeName(int.class));
        assertEquals("long", MappingHelper.typeName(long.class));
        assertEquals("bool", MappingHelper.typeName(boolean.class));
        assertEquals("string", MappingHelper.typeName(String.class));
        assertEquals("decimal", MappingHelper.typeName(BigDecimal.class));
        assertEquals("bigint", MappingHelper.typeName(BigInteger.class));
        assertEquals("map", MappingHelper.typeName(Map.class));
        assertEquals("date", MappingHelper.typeName(java.util.Date.class));
        assertEquals("void", MappingHelper.typeName(void.class));
        assertEquals(ArrayList.class.getName(), MappingHelper.typeName(ArrayList.class));
        assertNull(MappingHelper.typeName(null));

        assertEquals(int.class, MappingHelper.typeMappingOr("INT", s -> null));
        assertEquals(String.class, MappingHelper.typeMappingOr("string", s -> null));
        assertEquals(boolean.class, MappingHelper.typeMappingOr("bool", s -> null));
        assertEquals(byte[].class, MappingHelper.typeMappingOr("bytes", s -> null));
        assertEquals(Number.class, MappingHelper.typeMappingOr("number", s -> null));
        assertEquals(ArrayList.class, MappingHelper.typeMappingOr("java.util.ArrayList", Class::forName));
        assertNull(MappingHelper.typeMappingOr(null, s -> String.class));
        assertEquals(java.sql.Date.class, MappingHelper.typeMappingOr("sqldate", s -> null));
        assertEquals(java.sql.Time.class, MappingHelper.typeMappingOr("sqltime", s -> null));
        assertEquals(java.sql.Timestamp.class, MappingHelper.typeMappingOr("sqltimestamp", s -> null));
        assertEquals(java.time.LocalDate.class, MappingHelper.typeMappingOr("localdate", s -> null));
        assertEquals(java.time.LocalTime.class, MappingHelper.typeMappingOr("localtime", s -> null));
        assertEquals(java.time.LocalDateTime.class, MappingHelper.typeMappingOr("localdatetime", s -> null));
        assertEquals(java.time.OffsetDateTime.class, MappingHelper.typeMappingOr("offsetdatetime", s -> null));
        assertEquals(java.time.OffsetTime.class, MappingHelper.typeMappingOr("offsettime", s -> null));
        assertEquals(URL.class, MappingHelper.typeMappingOr("url", s -> null));
        assertEquals(URI.class, MappingHelper.typeMappingOr("uri", s -> null));
        assertEquals(HashMap.class, MappingHelper.typeMappingOr("hashmap", s -> null));
        assertEquals(LinkedHashMap.class, MappingHelper.typeMappingOr("linkedmap", s -> null));
        assertEquals(LinkedCaseInsensitiveMap.class, MappingHelper.typeMappingOr("caseinsensitivemap", s -> null));
        assertEquals(java.time.Year.class, MappingHelper.typeMappingOr("year", s -> null));
        assertEquals(java.time.YearMonth.class, MappingHelper.typeMappingOr("yearmonth", s -> null));
        assertEquals(java.time.Month.class, MappingHelper.typeMappingOr("month", s -> null));
        assertEquals(java.time.MonthDay.class, MappingHelper.typeMappingOr("monthday", s -> null));
        assertTrue(MappingHelper.caseInsensitive(null));

        assertEquals(DdlAuto.None, DdlAuto.valueOfCode("None"));
        assertEquals(DdlAuto.Create, DdlAuto.valueOfCode("Create"));
        assertEquals(DdlAuto.Create, DdlAuto.valueOfCode("create"));
        assertEquals(DdlAuto.Create, DdlAuto.valueOfCode("CREATE"));
        assertEquals(DdlAuto.AddColumn, DdlAuto.valueOfCode("AddColumn"));
        assertEquals(DdlAuto.AddColumn, DdlAuto.valueOfCode("addcolumn"));
        assertEquals(DdlAuto.AddColumn, DdlAuto.valueOfCode("ADDCOLUMN"));
        assertEquals(DdlAuto.AddColumn, DdlAuto.valueOfCode("add"));
        assertEquals(DdlAuto.Update, DdlAuto.valueOfCode("Update"));
        assertEquals(DdlAuto.Update, DdlAuto.valueOfCode("update"));
        assertEquals(DdlAuto.CreateDrop, DdlAuto.valueOfCode("CreateDrop"));
        assertEquals(DdlAuto.CreateDrop, DdlAuto.valueOfCode("createdrop"));
        assertEquals(DdlAuto.CreateDrop, DdlAuto.valueOfCode("create-drop"));
        assertEquals(DdlAuto.None, DdlAuto.valueOfCode(null));
        assertEquals(DdlAuto.None, DdlAuto.valueOfCode(""));
        assertEquals(DdlAuto.None, DdlAuto.valueOfCode("   "));
        assertEquals(DdlAuto.None, DdlAuto.valueOfCode("invalid"));
        assertEquals(DdlAuto.None, DdlAuto.valueOfCode("xyz"));
        assertEquals(KeyType.None, KeyType.valueOfCode("None"));
        assertEquals(KeyType.Auto, KeyType.valueOfCode("Auto"));
        assertEquals(KeyType.Auto, KeyType.valueOfCode("AUTO"));
        assertEquals(KeyType.Auto, KeyType.valueOfCode("auto"));
        assertEquals(KeyType.UUID32, KeyType.valueOfCode("UUID32"));
        assertEquals(KeyType.UUID32, KeyType.valueOfCode("uuid32"));
        assertEquals(KeyType.UUID36, KeyType.valueOfCode("UUID36"));
        assertEquals(KeyType.UUID36, KeyType.valueOfCode("uuid36"));
        assertEquals(KeyType.Sequence, KeyType.valueOfCode("Sequence"));
        assertEquals(KeyType.Sequence, KeyType.valueOfCode("sequence"));
        assertEquals(KeyType.Holder, KeyType.valueOfCode("Holder"));
        assertEquals(KeyType.Holder, KeyType.valueOfCode("holder"));
        assertNull(KeyType.valueOfCode(null));
        assertNull(KeyType.valueOfCode("invalid"));

        assertTrue(MappingRegistry.isEntity(UserInfo.class));
        assertFalse(MappingRegistry.isEntity(String.class));
        assertFalse(MappingRegistry.isEntity(PlainPojo.class));
    }

    @Test
    @Capability(CapabilityId.MAPPING_REGISTRY_LOAD_AND_FIND)
    public void mappingRegistry_shouldLoadFindAndOverrideEntityMappings() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class);
        assertEquals("user_info", registry.findByEntity(UserInfo.class).getTable());
        assertEquals(UserInfo.class, registry.findByTable("user_info").entityType());

        MappingRegistry customSpace = new MappingRegistry();
        customSpace.loadEntityToSpace(UserInfo.class, "mySpace", "myName");
        assertEquals("user_info", customSpace.findBySpace("mySpace", "myName").getTable());

        MappingRegistry classNameSpace = new MappingRegistry();
        classNameSpace.loadEntityToSpace(UserInfo.class, "classSpace");
        assertNotNull(classNameSpace.findBySpace("classSpace", UserInfo.class));

        MappingRegistry customTable = new MappingRegistry();
        customTable.loadEntityAsTable(UserInfo.class, "custom_user_table");
        assertEquals("custom_user_table", customTable.findByTable("custom_user_table").getTable());
        customTable.loadEntityAsTable(CacheEntityA.class, "oCat", "oSch", "override_table");
        assertEquals("oCat", customTable.findByTable("oCat", "oSch", "override_table").getCatalog());
    }

    @Test
    @Capability(CapabilityId.MAPPING_REGISTRY_TABLE_COLUMN_METADATA)
    public void mappingRegistry_shouldExposeTableColumnAndCollectionMetadata() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(OrderEntity.class);
        registry.loadEntityToSpace(ProductEntity.class);
        registry.loadEntityToSpace(UserInfo.class);
        registry.loadEntityToSpace(CatalogSchemaEntity.class);

        TableMapping<?> order = registry.findByEntity(OrderEntity.class);
        assertEquals("test_cat", order.getCatalog());
        assertEquals("test_sch", order.getSchema());
        assertEquals("test_orders", order.getTable());
        assertTrue(order.isAutoProperty());
        assertFalse(order.useDelimited());
        assertTrue(order.isCaseInsensitive());
        assertFalse(order.isToCamelCase());
        assertTrue(order.useGeneratedKey());

        TableMapping<?> product = registry.findByEntity(ProductEntity.class);
        assertFalse(product.isAutoProperty());
        assertTrue(product.useDelimited());
        ColumnMapping id = product.getPropertyByName("id");
        ColumnMapping name = product.getPropertyByName("name");
        ColumnMapping price = product.getPropertyByName("price");
        assertTrue(id.isPrimaryKey());
        assertEquals("product_name", name.getColumn());
        assertFalse(name.isInsert());
        assertTrue(name.isUpdate());
        assertTrue(price.isInsert());
        assertFalse(price.isUpdate());

        TableMapping<?> user = registry.findByEntity(UserInfo.class);
        Collection<ColumnMapping> properties = user.getProperties();
        Collection<String> columns = user.getColumns();
        assertFalse(properties.isEmpty());
        assertTrue(columns.contains("id"));
        assertTrue(columns.contains("name"));
        assertTrue(columns.contains("create_time"));

        TableMapping<?> catalog = registry.findByEntity(CatalogSchemaEntity.class);
        assertEquals("mycat", catalog.getCatalog());
        assertEquals("mysch", catalog.getSchema());
        assertNotNull(registry.findByTable("mycat", "mysch", "entity_with_catalog"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_REGISTRY_INVALID_AND_DUPLICATE)
    public void mappingRegistry_shouldRejectInvalidArgumentsAndDuplicateDefinitions() {
        MappingRegistry registry = new MappingRegistry();
        assertThrows(NullPointerException.class, () -> registry.loadEntityToSpace(null));
        assertThrows(IllegalArgumentException.class, () -> registry.loadEntityToSpace(UserInfo.class, "space", ""));
        assertThrows(IllegalArgumentException.class, () -> registry.loadEntityAsTable(null, "some_table"));
        assertThrows(IllegalArgumentException.class, () -> registry.loadEntityAsTable(UserInfo.class, ""));
        assertThrows(IllegalArgumentException.class, () -> registry.loadResultMapToSpace(null));
        assertThrows(IllegalArgumentException.class, () -> registry.loadResultMapToSpace(UserInfo.class, "space", ""));

        MappingRegistry duplicatedEntity = new MappingRegistry();
        duplicatedEntity.loadEntityToSpace(UserInfo.class);
        assertThrows(IllegalStateException.class, () -> duplicatedEntity.loadEntityToSpace(UserInfo.class));
        assertNotNull(duplicatedEntity.findByEntity(UserInfo.class));

        MappingRegistry duplicatedName = new MappingRegistry();
        duplicatedName.loadEntityToSpace(EntityA.class, "", "conflict_name");
        assertThrows(IllegalStateException.class, () -> duplicatedName.loadEntityToSpace(EntityB.class, "", "conflict_name"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_REGISTRY_MULTIPLE_AND_MISSING_LOOKUP)
    public void mappingRegistry_shouldHandleMultipleDefinitionsAndMissingLookups() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(SameTableEntity1.class);
        registry.loadEntityToSpace(SameTableEntity2.class);
        assertThrows(IllegalStateException.class, () -> registry.findByTable("same_table"));
        assertEquals(SameTableEntity1.class, registry.findByTable(null, null, "same_table", SameTableEntity1.class.getName()).entityType());
        assertEquals(SameTableEntity2.class, registry.findByTable(null, null, "same_table", SameTableEntity2.class.getName()).entityType());

        MappingRegistry sameEntityNames = new MappingRegistry();
        sameEntityNames.loadEntityToSpace(CacheEntityA.class, "", "nameA");
        sameEntityNames.loadEntityToSpace(CacheEntityA.class, "", "nameB");
        assertThrows(IllegalStateException.class, () -> sameEntityNames.findByTable("cache_entity_a"));
        assertNotNull(sameEntityNames.findByTable(null, null, "cache_entity_a", "nameA"));

        MappingRegistry empty = new MappingRegistry();
        assertNull(empty.findByTable("non_existent_table"));
        assertNull(empty.findBySpace("missing_space", "missing_name"));
        assertNull(empty.findByEntity(UserInfo.class));
    }

    @Test
    @Capability(CapabilityId.MAPPING_REGISTRY_RESULT_MAP)
    public void mappingRegistry_shouldLoadResultMapsFromAnnotationsAndOverrides() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadResultMapToSpace(AnnotatedResultMapEntity.class);
        TableMapping<?> annotation = registry.findBySpace("myMapSpace", "MyResultMap");
        assertNotNull(annotation);
        assertEquals(AnnotatedResultMapEntity.class, annotation.entityType());
        assertEquals("", annotation.getTable());

        registry.loadResultMapToSpace(AnnotatedResultMapEntity.class, "override_space", "override_name");
        assertNotNull(registry.findBySpace("override_space", "override_name"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_REGISTRY_CONSTRUCTOR_CACHE_SPACE)
    public void mappingRegistry_shouldExposeConstructorDefaultsCacheAndSpaceIsolation() {
        Options options = Options.of().catalog("g_cat").schema("g_sch");
        MappingRegistry registry = new MappingRegistry(null, options);
        assertNotNull(registry.getClassLoader());
        assertNotNull(registry.getTypeRegistry());
        assertEquals("g_cat", registry.getGlobalOptions().getCatalog());
        assertEquals("g_sch", registry.getGlobalOptions().getSchema());
        assertNotNull(MappingRegistry.DEFAULT);
        assertNotNull(MappingRegistry.DEFAULT.getClassLoader());
        assertNotNull(MappingRegistry.DEFAULT.getTypeRegistry());
        assertNotNull(MappingRegistry.DEFAULT.getGlobalOptions());

        MappingRegistry cacheRegistry = new MappingRegistry();
        TableMapping<?> first = cacheRegistry.loadEntityToSpace(CacheEntityA.class, "sp1", "name1");
        TableMapping<?> second = cacheRegistry.loadEntityToSpace(CacheEntityA.class, "sp2", "name2");
        assertEquals(first.getProperties().size(), second.getProperties().size());
        assertEquals(first.getTable(), second.getTable());

        MappingRegistry plainRegistry = new MappingRegistry(null, Options.of().catalog("pojo_cat").schema("pojo_sch").mapUnderscoreToCamelCase(true));
        plainRegistry.loadEntityToSpace(PlainPojo.class);
        plainRegistry.loadEntityToSpace(PlainUserNamePojo.class, "user", "plainUserName");
        assertEquals("pojo_cat", plainRegistry.findByEntity(PlainPojo.class).getCatalog());
        assertEquals("pojo_sch", plainRegistry.findByEntity(PlainPojo.class).getSchema());
        assertEquals("plain_pojo", plainRegistry.findByEntity(PlainPojo.class).getTable());

        MappingRegistry spaceRegistry = new MappingRegistry();
        spaceRegistry.loadEntityToSpace(CacheEntityA.class, "spaceA", "myEntity");
        assertNotNull(spaceRegistry.findBySpace("spaceA", "myEntity"));
        assertNull(spaceRegistry.findBySpace("spaceB", "myEntity"));
        assertNull(spaceRegistry.findBySpace("", "myEntity"));
    }

    private void assertThrows(Class<? extends Throwable> expected, ThrowingRunnable runnable) {
        try {
            runnable.run();
            fail("Expected " + expected.getName());
        } catch (Throwable e) {
            if (!expected.isInstance(e)) {
                throw new AssertionError("Expected " + expected.getName() + " but got " + e.getClass().getName(), e);
            }
        }
    }

    private interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
