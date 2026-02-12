package net.hasor.dbvisitor.test.oneapi.suite.mapping.tabledef;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import net.hasor.dbvisitor.test.oneapi.model.naming.DelimitedUser;
import net.hasor.dbvisitor.test.oneapi.model.tabledef.CamelCaseEntity;
import net.hasor.dbvisitor.test.oneapi.model.tabledef.CaseSensitiveUser;
import net.hasor.dbvisitor.test.oneapi.model.tabledef.CatalogSchemaUser;
import net.hasor.dbvisitor.test.oneapi.model.tabledef.ManualMappingUser;
import net.hasor.dbvisitor.test.oneapi.model.tabledef.OrderByTemplateUser;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @Table 注解高级属性和 @Column orderByColTemplate 测试。
 * 覆盖：catalog/schema、useDelimited 元数据、autoMapping=false、
 * orderByColTemplate、caseInsensitive=false 元数据。
 */
public class TableAdvancedTest extends AbstractOneApiTest {

    // ============ catalog/schema Tests ============

    /**
     * @Table 的 catalog 和 schema 属性元数据验证。
     */
    @Test
    public void testCatalogSchema_Metadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(CatalogSchemaUser.class);

        TableMapping<?> mapping = registry.findByEntity(CatalogSchemaUser.class);
        assertNotNull(mapping);
        assertEquals("my_catalog", mapping.getCatalog());
        assertEquals("my_schema", mapping.getSchema());
        assertEquals("user_info", mapping.getTable());
    }

    /**
     * 不指定 catalog/schema 时默认为空。
     */
    @Test
    public void testCatalogSchema_DefaultEmpty() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class);

        TableMapping<?> mapping = registry.findByEntity(UserInfo.class);
        assertNotNull(mapping);
        assertTrue("Default catalog should be empty",//
                mapping.getCatalog() == null || mapping.getCatalog().isEmpty());
        assertTrue("Default schema should be empty",//
                mapping.getSchema() == null || mapping.getSchema().isEmpty());
    }

    // ============ useDelimited Tests ============

    /**
     * useDelimited=true 的元数据验证。
     */
    @Test
    public void testUseDelimited_TrueMetadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(DelimitedUser.class);

        TableMapping<?> mapping = registry.findByEntity(DelimitedUser.class);
        assertNotNull(mapping);
        assertTrue("useDelimited should be true", mapping.useDelimited());
    }

    /**
     * useDelimited=false（默认）的元数据验证。
     */
    @Test
    public void testUseDelimited_DefaultFalse() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class);

        TableMapping<?> mapping = registry.findByEntity(UserInfo.class);
        assertNotNull(mapping);
        assertFalse("Default useDelimited should be false", mapping.useDelimited());
    }

    /**
     * useDelimited=true 时实际 DB 操作仍正常（PostgreSQL 使用双引号包裹标识符）。
     */
    @Test
    public void testUseDelimited_ActualDbOperation() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        DelimitedUser user = new DelimitedUser();
        user.setId(45001);
        user.setName("Delimited");
        user.setAge(30);
        user.setCreateTime(new Date());
        lambda.insert(DelimitedUser.class).applyEntity(user).executeSumResult();

        DelimitedUser loaded = lambda.query(DelimitedUser.class)//
                .eq(DelimitedUser::getId, 45001)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("Delimited", loaded.getName());
    }

    // ============ autoMapping=false Tests ============

    /**
     * autoMapping=false 时，只有 @Column 标注的字段参与映射。
     */
    @Test
    public void testAutoMappingFalse_Metadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(ManualMappingUser.class);

        TableMapping<?> mapping = registry.findByEntity(ManualMappingUser.class);
        assertNotNull(mapping);
        assertFalse("autoMapping should be false", mapping.isAutoProperty());

        // 只有 id, name, createTime 有 @Column 注解
        Collection<ColumnMapping> props = mapping.getProperties();
        assertEquals("Only @Column annotated fields should be mapped", 3, props.size());

        assertNotNull("id should be mapped", mapping.getPropertyByName("id"));
        assertNotNull("name should be mapped", mapping.getPropertyByName("name"));
        assertNotNull("createTime should be mapped", mapping.getPropertyByName("createTime"));
        assertNull("age should NOT be mapped", mapping.getPropertyByName("age"));
        assertNull("email should NOT be mapped", mapping.getPropertyByName("email"));
    }

    /**
     * autoMapping=false 时实际数据库操作——未映射的字段不参与 INSERT/SELECT。
     */
    @Test
    public void testAutoMappingFalse_DbOperation() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        ManualMappingUser user = new ManualMappingUser();
        user.setId(45101);
        user.setName("ManualMap");
        user.setAge(25); // 不会被 INSERT
        user.setEmail("notmapped@test.com"); // 不会被 INSERT
        user.setCreateTime(new Date());
        lambda.insert(ManualMappingUser.class).applyEntity(user).executeSumResult();

        // 用原始 UserInfo 验证未映射字段
        UserInfo raw = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 45101)//
                .queryForObject();
        assertNotNull(raw);
        assertEquals("ManualMap", raw.getName());
        assertNull("age should be NULL (not mapped for insert)", raw.getAge());
        assertNull("email should be NULL (not mapped for insert)", raw.getEmail());

        // 用 ManualMappingUser 查询，未映射字段应为 null
        ManualMappingUser loaded = lambda.query(ManualMappingUser.class)//
                .eq(ManualMappingUser::getId, 45101)//
                .queryForObject();
        assertNotNull(loaded);
        assertEquals("ManualMap", loaded.getName());
        assertNull("Unmapped age should be null", loaded.getAge());
        assertNull("Unmapped email should be null", loaded.getEmail());
    }

    // ============ caseInsensitive Tests ============

    /**
     * caseInsensitive=false 的元数据验证。
     */
    @Test
    public void testCaseInsensitive_FalseMeta() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(CaseSensitiveUser.class);

        TableMapping<?> mapping = registry.findByEntity(CaseSensitiveUser.class);
        assertNotNull(mapping);
        assertFalse("caseInsensitive should be false", mapping.isCaseInsensitive());
    }

    /**
     * caseInsensitive=true（默认）的元数据验证。
     */
    @Test
    public void testCaseInsensitive_DefaultTrue() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class);

        TableMapping<?> mapping = registry.findByEntity(UserInfo.class);
        assertNotNull(mapping);
        assertTrue("Default caseInsensitive should be true", mapping.isCaseInsensitive());
    }

    // ============ orderByColTemplate Tests ============

    /**
     * orderByColTemplate 元数据验证。
     */
    @Test
    public void testOrderByColTemplate_Metadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(OrderByTemplateUser.class);

        TableMapping<?> mapping = registry.findByEntity(OrderByTemplateUser.class);
        assertNotNull(mapping);

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertEquals("LOWER(name)", nameCol.getOrderByColTemplate());

        ColumnMapping ageCol = mapping.getPropertyByName("age");
        assertNotNull(ageCol);
        assertEquals("age * -1", ageCol.getOrderByColTemplate());
    }

    /**
     * 未设置 orderByColTemplate 的列，应为空字符串或 null。
     */
    @Test
    public void testOrderByColTemplate_Default() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(OrderByTemplateUser.class);

        TableMapping<?> mapping = registry.findByEntity(OrderByTemplateUser.class);

        ColumnMapping emailCol = mapping.getPropertyByName("email");
        assertNotNull(emailCol);
        assertTrue("Default orderByColTemplate should be empty",//
                emailCol.getOrderByColTemplate() == null || emailCol.getOrderByColTemplate().isEmpty());
    }

    // ============ mapUnderscoreToCamelCase Tests ============

    /**
     * mapUnderscoreToCamelCase=true 的元数据验证。
     */
    @Test
    public void testMapUnderscoreToCamelCase_Metadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(CamelCaseEntity.class);

        TableMapping<?> mapping = registry.findByEntity(CamelCaseEntity.class);
        assertNotNull(mapping);
        assertTrue("mapUnderscoreToCamelCase should be true", mapping.isToCamelCase());

        // createTime 属性应自动映射到 create_time 列
        ColumnMapping ctCol = mapping.getPropertyByName("createTime");
        assertNotNull("createTime should be mapped", ctCol);
        assertEquals("create_time", ctCol.getColumn());
    }

    /**
     * mapUnderscoreToCamelCase=true 时的实际数据库读写验证。
     */
    @Test
    public void testMapUnderscoreToCamelCase_DbOperation() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        CamelCaseEntity user = new CamelCaseEntity();
        user.setId(45201);
        user.setName("CamelCase");
        user.setAge(29);
        user.setCreateTime(new Date());
        lambda.insert(CamelCaseEntity.class).applyEntity(user).executeSumResult();

        CamelCaseEntity loaded = lambda.query(CamelCaseEntity.class)//
                .eq(CamelCaseEntity::getId, 45201)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("CamelCase", loaded.getName());
        assertNotNull("createTime should have value", loaded.getCreateTime());
    }
}
