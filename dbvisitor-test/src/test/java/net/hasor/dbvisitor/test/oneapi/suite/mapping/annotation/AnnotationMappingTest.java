package net.hasor.dbvisitor.test.oneapi.suite.mapping.annotation;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import net.hasor.dbvisitor.test.oneapi.model.annotation.CatalogSchemaUser;
import net.hasor.dbvisitor.test.oneapi.model.annotation.ColumnMappedUser;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Annotation Mapping Test
 * 验证注解映射能力：@Table、@Column、@Ignore 等
 */
public class AnnotationMappingTest extends AbstractOneApiTest {
    /**
     * 测试 @Table 注解
     * 验证类映射到表名
     */
    @Test
    public void testTableAnnotation() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo user = createUser(28001, "TableAnno", 25);

        // @Table 注解应正确映射到 user_info 表
        int result = lambda.insert(UserInfo.class)//
                .applyEntity(user)//
                .executeSumResult();

        assertEquals(1, result);

        // 验证插入成功
        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 28001)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("TableAnno", loaded.getName());
    }

    /**
     * 测试 @Column 注解
     * 验证属性映射到列名
     */
    @Test
    public void testColumnAnnotation() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo user = createUser(28101, "ColAnno", 30);
        Date createTime = new Date();
        user.setCreateTime(createTime);

        lambda.insert(UserInfo.class)//
                .applyEntity(user)//
                .executeSumResult();

        // @Column("create_time") 应正确映射
        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 28101)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull("createTime should be mapped via @Column", loaded.getCreateTime());

        // 验证日期在合理范围内（允许秒级误差）
        long diff = Math.abs(loaded.getCreateTime()//
                .getTime() - createTime.getTime());
        assertTrue("Date should be preserved within 1 second", diff < 1000);
    }

    /**
     * 测试 autoMapping = true（默认）
     * 所有属性自动映射
     */
    @Test
    public void testAutoMappingTrue() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo user = createUser(28201, "AutoTrue", 35);
        lambda.insert(UserInfo.class)//
                .applyEntity(user)   //
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 28201)     //
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("AutoTrue", loaded.getName());
        assertEquals(Integer.valueOf(35), loaded.getAge());
        assertEquals("autotrue@mapping.com", loaded.getEmail());
    }

    /**
     * 测试 @Table 的 catalog 和 schema 属性
     * 通过元数据验证 @Table(catalog, schema) 注解已被正确解析
     */
    @Test
    public void testCatalogAndSchema() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(CatalogSchemaUser.class);

        TableMapping<?> mapping = registry.findByEntity(CatalogSchemaUser.class);
        assertNotNull(mapping);
        assertEquals("catalog should be parsed", "test_catalog", mapping.getCatalog());
        assertEquals("schema should be parsed", "test_schema", mapping.getSchema());
        assertEquals("table should be parsed", "user_info", mapping.getTable());

        // 对比: UserInfo 未设置 catalog/schema，应为空
        MappingRegistry registry2 = new MappingRegistry();
        registry2.loadEntityToSpace(UserInfo.class);
        TableMapping<?> defaultMapping = registry2.findByEntity(UserInfo.class);
        assertTrue("Default catalog should be empty", defaultMapping.getCatalog() == null || defaultMapping.getCatalog().isEmpty());
        assertTrue("Default schema should be empty", defaultMapping.getSchema() == null || defaultMapping.getSchema().isEmpty());
    }

    /**
     * 测试属性名到列名的默认映射
     * 无注解时，属性名即列名
     */
    @Test
    public void testDefaultPropertyToColumnMapping() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo user = new UserInfo();
        user.setId(28401);
        user.setName("DefaultMap"); // name -> name
        user.setAge(30);            // age -> age
        user.setEmail("default@test.com"); // email -> email

        lambda.insert(UserInfo.class)//
                .applyEntity(user)//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 28401)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("DefaultMap", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
    }

    /**
     * 测试 @Column 覆盖默认映射
     * 对比: 无 @Column 时属性名=列名; 有 @Column 时列名被覆盖
     */
    @Test
    public void testColumnOverrideDefault() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class);
        TableMapping<?> mapping = registry.findByEntity(UserInfo.class);
        assertNotNull(mapping);

        // 无 @Column: 属性名 name -> 列名 name（默认映射）
        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertEquals("Default: property name = column name", "name", nameCol.getColumn());

        // 有 @Column("create_time"): 属性名 createTime -> 列名 create_time（覆盖）
        ColumnMapping ctCol = mapping.getPropertyByName("createTime");
        assertNotNull(ctCol);
        assertEquals("Override: @Column overrides property name", "create_time", ctCol.getColumn());

        // 进一步对比: ColumnMappedUser 中 userName -> name
        MappingRegistry registry2 = new MappingRegistry();
        registry2.loadEntityToSpace(ColumnMappedUser.class);
        TableMapping<?> mapped = registry2.findByEntity(ColumnMappedUser.class);
        ColumnMapping userNameCol = mapped.getPropertyByName("userName");
        assertNotNull(userNameCol);
        assertEquals("Override: userName -> name", "name", userNameCol.getColumn());
    }

    /**
     * 测试多个字段都有 @Column 注解
     * 使用 ColumnMappedUser（所有字段都通过 @Column 显式指定列名），
     * 验证每个属性的列映射元数据正确，并通过实际数据库操作验证读写。
     */
    @Test
    public void testMultipleColumnAnnotations() throws SQLException {
        // 1. 元数据验证：所有 @Column 都正确解析
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(ColumnMappedUser.class);
        TableMapping<?> mapping = registry.findByEntity(ColumnMappedUser.class);

        assertNotNull(mapping);
        assertEquals("user_info", mapping.getTable());
        assertEquals(5, mapping.getProperties().size());

        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull("id should be mapped", idCol);
        assertTrue("id should be primary key", idCol.isPrimaryKey());

        ColumnMapping nameCol = mapping.getPropertyByName("userName");
        assertNotNull("userName should be mapped", nameCol);
        assertEquals("name", nameCol.getColumn());

        ColumnMapping mailCol = mapping.getPropertyByName("mailAddr");
        assertNotNull("mailAddr should be mapped", mailCol);
        assertEquals("email", mailCol.getColumn());

        ColumnMapping ctCol = mapping.getPropertyByName("createTime");
        assertNotNull("createTime should be mapped", ctCol);
        assertEquals("create_time", ctCol.getColumn());

        // 2. 实际数据库操作验证
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        ColumnMappedUser user = new ColumnMappedUser();
        user.setId(28601);
        user.setUserName("MultiCol");
        user.setAge(27);
        user.setMailAddr("multi@col.test");
        user.setCreateTime(new Date());
        lambda.insert(ColumnMappedUser.class).applyEntity(user).executeSumResult();

        ColumnMappedUser loaded = lambda.query(ColumnMappedUser.class).eq(ColumnMappedUser::getId, 28601).queryForObject();

        assertNotNull(loaded);
        assertEquals("MultiCol", loaded.getUserName());
        assertEquals(Integer.valueOf(27), loaded.getAge());
        assertEquals("multi@col.test", loaded.getMailAddr());
        assertNotNull(loaded.getCreateTime());
    }

    /**
     * 测试 NULL 值的映射
     * NULL 属性应正确映射到 NULL 列
     */
    @Test
    public void testNullValueMapping() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo user = new UserInfo();
        user.setId(28701);
        user.setName("NullMapping");
        user.setAge(null); // NULL age
        user.setEmail(null); // NULL email

        lambda.insert(UserInfo.class)//
                .applyEntity(user)//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 28701)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("NullMapping", loaded.getName());
        assertNull("Age should be NULL", loaded.getAge());
        assertNull("Email should be NULL", loaded.getEmail());
    }

    /**
     * 测试空字符串映射
     * 空字符串应与 NULL 区分
     */
    @Test
    public void testEmptyStringMapping() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo user = new UserInfo();
        user.setId(28801);
        user.setName(""); // 空字符串
        user.setAge(25);
        user.setEmail("empty@test.com");

        lambda.insert(UserInfo.class)//
                .applyEntity(user)//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 28801)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull("Name should not be null", loaded.getName());
        assertEquals("Empty string should be preserved", "", loaded.getName());
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@mapping.com");
        u.setCreateTime(new Date());
        return u;
    }
}
