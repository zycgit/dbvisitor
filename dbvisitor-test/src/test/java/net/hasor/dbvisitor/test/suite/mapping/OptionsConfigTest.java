package net.hasor.dbvisitor.test.suite.mapping;

import java.util.Collection;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.AnnotatedEntity;
import net.hasor.dbvisitor.test.model.ExplicitAnnotatedEntity;
import net.hasor.dbvisitor.test.model.PlainEntity;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Options 全局配置测试。
 * 验证 Options 的构建器、拷贝、默认值、以及与注解的优先级关系。
 */
public class OptionsConfigTest extends AbstractOneApiTest {

    // ============ 默认值和构建器 ============

    /**
     * Options.of() 创建的实例所有值为 null（不设定则不影响解析）。
     */
    @Test
    public void testOptionsDefaults() throws Exception {
        Options options = Options.of();

        assertNull("catalog should be null by default", options.getCatalog());
        assertNull("schema should be null by default", options.getSchema());
        assertNull("autoMapping should be null by default", options.getAutoMapping());
        assertNull("mapUnderscoreToCamelCase should be null by default", options.getMapUnderscoreToCamelCase());
        assertNull("caseInsensitive should be null by default", options.getCaseInsensitive());
        assertNull("useDelimited should be null by default", options.getUseDelimited());
    }

    /**
     * Options 支持链式 Builder 模式设置值。
     */
    @Test
    public void testOptionsBuilderChain() throws Exception {
        Options options = Options.of()         //
                .catalog("myCatalog")          //
                .schema("mySchema")            //
                .autoMapping(false)            //
                .mapUnderscoreToCamelCase(true)//
                .caseInsensitive(false)        //
                .useDelimited(true);

        assertEquals("myCatalog", options.getCatalog());
        assertEquals("mySchema", options.getSchema());
        assertEquals(Boolean.FALSE, options.getAutoMapping());
        assertEquals(Boolean.TRUE, options.getMapUnderscoreToCamelCase());
        assertEquals(Boolean.FALSE, options.getCaseInsensitive());
        assertEquals(Boolean.TRUE, options.getUseDelimited());
    }

    /**
     * Options 拷贝构造函数。
     */
    @Test
    public void testOptionsCopyConstructor() throws Exception {
        Options original = Options.of().catalog("cat1").schema("sch1").autoMapping(true);

        Options copy = Options.of(original);

        assertEquals("cat1", copy.getCatalog());
        assertEquals("sch1", copy.getSchema());
        assertEquals(Boolean.TRUE, copy.getAutoMapping());

        // 修改拷贝不影响原始
        copy.setCatalog("cat2");
        assertEquals("cat1", original.getCatalog());
    }

    /**
     * 拷贝构造函数拷贝全部 8 个属性，包括 ignoreNonExistStatement 和 dialect。
     */
    @Test
    public void testCopyConstructor_allFields() {
        Options original = Options.of()        //
                .catalog("cat")                //
                .schema("sch")                 //
                .autoMapping(true)             //
                .mapUnderscoreToCamelCase(true)//
                .caseInsensitive(false)        //
                .useDelimited(true)            //
                .dialect(MySqlDialect.DEFAULT) //
                .ignoreNonExistStatement(true);

        Options copy = Options.of(original);

        assertEquals("cat", copy.getCatalog());
        assertEquals("sch", copy.getSchema());
        assertEquals(Boolean.TRUE, copy.getAutoMapping());
        assertEquals(Boolean.TRUE, copy.getMapUnderscoreToCamelCase());
        assertEquals(Boolean.FALSE, copy.getCaseInsensitive());
        assertEquals(Boolean.TRUE, copy.getUseDelimited());
        assertSame(MySqlDialect.DEFAULT, copy.getDialect());
        assertEquals(Boolean.TRUE, copy.getIgnoreNonExistStatement());

        // 修改拷贝不影响原始
        copy.setDialect(null);
        assertSame("Original should still have MySqlDialect", MySqlDialect.DEFAULT, original.getDialect());
    }

    /**
     * Options(null) 拷贝构造——null 输入时所有字段保持 null。
     */
    @Test
    public void testCopyConstructor_nullInput() {
        Options copy = Options.of(null);
        assertNull(copy.getCatalog());
        assertNull(copy.getSchema());
        assertNull(copy.getAutoMapping());
        assertNull(copy.getDialect());
        assertNull(copy.getIgnoreNonExistStatement());
    }

    // ============ Options 与 MappingRegistry 交互 ============

    /**
     * 无 @Table 注解的实体使用 Options 作为全局默认配置。
     * Options.autoMapping(false) → 无注解实体的字段不自动映射。
     */
    @Test
    public void testOptions_AffectsPlainEntity() throws Exception {
        Options options = Options.of().autoMapping(false);
        MappingRegistry registry = new MappingRegistry(null, null, options);
        registry.loadEntityToSpace(PlainEntity.class);

        TableMapping<?> mapping = registry.findByEntity(PlainEntity.class);

        assertNotNull(mapping);
        assertFalse("Options autoMapping=false → isAutoProperty should be false", mapping.isAutoProperty());

        // autoMapping=false 且无 @Column → 无映射字段
        Collection<ColumnMapping> props = mapping.getProperties();
        assertTrue("No mapped properties (no @Column, autoMapping=false)", props.isEmpty());
    }

    // ============ ignoreNonExistStatement ============

    /**
     * ignoreNonExistStatement 属性的 get/set 测试。
     */
    @Test
    public void testIgnoreNonExistStatement_getSet() {
        Options options = Options.of();
        assertNull("default is null", options.getIgnoreNonExistStatement());

        options.setIgnoreNonExistStatement(true);
        assertEquals(Boolean.TRUE, options.getIgnoreNonExistStatement());

        options.setIgnoreNonExistStatement(false);
        assertEquals(Boolean.FALSE, options.getIgnoreNonExistStatement());
    }

    /**
     * ignoreNonExistStatement 链式 API。
     */
    @Test
    public void testIgnoreNonExistStatement_fluentChain() {
        Options options = Options.of().ignoreNonExistStatement(true);
        assertEquals(Boolean.TRUE, options.getIgnoreNonExistStatement());

        // 确认链式返回 this
        Options same = options.ignoreNonExistStatement(false);
        assertSame("Fluent method should return same instance", options, same);
        assertEquals(Boolean.FALSE, options.getIgnoreNonExistStatement());
    }

    // ============ dialect ============

    /**
     * dialect 属性的 get/set 测试。
     */
    @Test
    public void testDialect_getSet() {
        Options options = Options.of();
        assertNull("default dialect is null", options.getDialect());

        options.setDialect(MySqlDialect.DEFAULT);
        assertSame(MySqlDialect.DEFAULT, options.getDialect());
    }

    /**
     * dialect 链式 API。
     */
    @Test
    public void testDialect_fluentChain() {
        Options options = Options.of().dialect(MySqlDialect.DEFAULT);
        assertSame(MySqlDialect.DEFAULT, options.getDialect());

        Options same = options.dialect(null);
        assertSame(options, same);
        assertNull(options.getDialect());
    }

    // ============ toString ============

    /**
     * toString() 包含 autoMapping、mapUnderscoreToCamelCase、caseInsensitive、
     * useDelimited、ignoreNonExistStatement、dialect 类名。
     */
    @Test
    public void testToString_format() {
        Options options = Options.of()          //
                .autoMapping(true)              //
                .mapUnderscoreToCamelCase(false)//
                .caseInsensitive(true)          //
                .useDelimited(false)            //
                .ignoreNonExistStatement(true)  //
                .dialect(MySqlDialect.DEFAULT);

        String str = options.toString();

        assertTrue("toString should start with 'Options['", str.startsWith("Options["));
        assertTrue("toString should end with ']'", str.endsWith("]"));
        assertTrue("should contain autoMapping=true", str.contains("true"));
        assertTrue("should contain dialect class name", str.contains(MySqlDialect.class.getName()));
    }

    /**
     * toString() 所有字段为 null 时也正常输出（不 NPE）。
     */
    @Test
    public void testToString_allNull() {
        Options options = Options.of();
        String str = options.toString();

        assertNotNull(str);
        assertTrue("Should start with 'Options['", str.startsWith("Options["));
        assertTrue("Should contain 'null' for unset values", str.contains("null"));
    }

    // ============ New Tests: Options affecting Mapping behavior ============

    /**
     * caseInsensitive=true/false 传递到 TableMapping。
     */
    @Test
    public void testOptions_CaseInsensitive() throws Exception {
        // 1. Options caseInsensitive=true
        Options optsTrue = Options.of().caseInsensitive(true);
        MappingRegistry regTrue = new MappingRegistry(null, null, optsTrue);
        regTrue.loadEntityToSpace(PlainEntity.class);
        TableMapping<?> mapTrue = regTrue.findByEntity(PlainEntity.class);
        // TableMapping 默认值检查 - 应该跟随 Options
        assertTrue("TableMapping caseInsensitive should be true via Options", mapTrue.isCaseInsensitive());

        // 2. Options caseInsensitive=false
        Options optsFalse = Options.of().caseInsensitive(false);
        MappingRegistry regFalse = new MappingRegistry(null, null, optsFalse);
        regFalse.loadEntityToSpace(PlainEntity.class);
        TableMapping<?> mapFalse = regFalse.findByEntity(PlainEntity.class);
        assertFalse("TableMapping caseInsensitive should be false via Options", mapFalse.isCaseInsensitive());
    }

    /**
     * useDelimited=true/false 传递到 TableMapping。
     */
    @Test
    public void testOptions_UseDelimited() throws Exception {
        // 1. Options useDelimited=true
        Options optsTrue = Options.of().useDelimited(true);
        MappingRegistry regTrue = new MappingRegistry(null, null, optsTrue);
        regTrue.loadEntityToSpace(PlainEntity.class);
        TableMapping<?> mapTrue = regTrue.findByEntity(PlainEntity.class);
        assertTrue("TableMapping useDelimited should be true via Options", mapTrue.useDelimited());

        // 2. Options useDelimited=false
        Options optsFalse = Options.of().useDelimited(false);
        MappingRegistry regFalse = new MappingRegistry(null, null, optsFalse);
        regFalse.loadEntityToSpace(PlainEntity.class);
        TableMapping<?> mapFalse = regFalse.findByEntity(PlainEntity.class);
        assertFalse("TableMapping useDelimited should be false via Options", mapFalse.useDelimited());
    }

    /**
     * Catalog 和 Schema 的全局设置。
     */
    @Test
    public void testOptions_GlobalCatalogSchema() throws Exception {
        Options options = Options.of().catalog("global_cat").schema("global_sch");
        MappingRegistry registry = new MappingRegistry(null, null, options);
        registry.loadEntityToSpace(PlainEntity.class);

        TableMapping<?> mapping = registry.findByEntity(PlainEntity.class);

        assertEquals("global_cat", mapping.getCatalog());
        assertEquals("global_sch", mapping.getSchema());
    }

    /**
     * AutoMapping=true (默认) vs false。
     */
    @Test
    public void testOptions_AutoMapping() throws Exception {
        // Options: autoMapping=null (Default behavior check) -> usually defaults to true in logic if not set?
        // Let's check when explicitly set to true
        Options optsTrue = Options.of().autoMapping(true);
        MappingRegistry regTrue = new MappingRegistry(null, null, optsTrue);
        regTrue.loadEntityToSpace(PlainEntity.class);
        assertTrue("isAutoProperty should be true", regTrue.findByEntity(PlainEntity.class).isAutoProperty());

        // Explicit false
        Options optsFalse = Options.of().autoMapping(false);
        MappingRegistry regFalse = new MappingRegistry(null, null, optsFalse);
        regFalse.loadEntityToSpace(PlainEntity.class);
        assertFalse("isAutoProperty should be false", regFalse.findByEntity(PlainEntity.class).isAutoProperty());
    }

    /**
     * @Table 注解属性未显式设置时，Options 配置优先（覆盖注解默认值）。
     * Options 设 autoMapping=false，AnnotatedEntity 未显式设置 autoMapping (即使默认true) → Application defaults (Options) wins over Annotation defaults.
     */
    @Test
    public void testOptions_OverridesAnnotationImplicitDefault() throws Exception {
        Options options = Options.of().autoMapping(false);
        MappingRegistry registry = new MappingRegistry(null, null, options);
        registry.loadEntityToSpace(AnnotatedEntity.class);

        TableMapping<?> mapping = registry.findByEntity(AnnotatedEntity.class);

        assertNotNull(mapping);
        assertFalse("Options autoMapping=false should override implicit @Table(autoMapping=true)", mapping.isAutoProperty());
    }

    /**
     * @Table 显式设置属性优先于 Options 配置。
     * Options 设 autoMapping=false，但类显式 @Table(autoMapping=true) → 显式注解优先。
     */
    @Test
    public void testOptions_ExplicitAnnotationOverridesOptions() throws Exception {
        Options options = Options.of().autoMapping(false);
        MappingRegistry registry = new MappingRegistry(null, null, options);
        registry.loadEntityToSpace(ExplicitAnnotatedEntity.class);

        TableMapping<?> mapping = registry.findByEntity(ExplicitAnnotatedEntity.class);

        assertNotNull(mapping);
        assertTrue("Explicit @Table(autoMapping=true) should override Options.autoMapping=false", mapping.isAutoProperty());
    }

    /**
     * Options.mapUnderscoreToCamelCase(true) 对无注解实体生效。
     * 类名 PlainEntity → 表名转为 plain_entity。
     */
    @Test
    public void testOptions_CamelCaseTableName() throws Exception {
        Options options = Options.of().mapUnderscoreToCamelCase(true);
        MappingRegistry registry = new MappingRegistry(null, null, options);
        registry.loadEntityToSpace(PlainEntity.class);

        TableMapping<?> mapping = registry.findByEntity(PlainEntity.class);

        assertNotNull(mapping);
        assertEquals("mapUnderscoreToCamelCase=true → PlainEntity → plain_entity", "plain_entity", mapping.getTable());

        // 字段名也应转换: userName → user_name
        ColumnMapping userNameCol = mapping.getPropertyByName("userName");
        if (userNameCol != null) {
            assertEquals("userName → user_name", "user_name", userNameCol.getColumn());
        }
    }
}
