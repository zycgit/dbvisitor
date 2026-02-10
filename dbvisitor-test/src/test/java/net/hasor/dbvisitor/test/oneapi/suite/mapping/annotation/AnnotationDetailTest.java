package net.hasor.dbvisitor.test.oneapi.suite.mapping.annotation;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import net.hasor.dbvisitor.test.oneapi.model.annotation.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 注解细节与高级映射测试。
 * 覆盖：@Column 标注在方法上、name vs value 等价性、@Table table vs value、
 * @Primary 注解、@ResultMap 注解、Map 实体支持。
 */
public class AnnotationDetailTest extends AbstractOneApiTest {

    // ============ Helper ============

    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@detail.test");
        u.setCreateTime(new Date());
        return u;
    }

    // ============ Tests ============

    /**
     * @Column 标注在字段上（属性名与列名不同），
     * 通过实际数据库操作验证 INSERT/SELECT 正确映射。
     */
    @Test
    public void testColumnFieldMapping() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        ColumnMappedUser user = new ColumnMappedUser();
        user.setId(42001);
        user.setUserName("FieldAnno");
        user.setAge(25);
        user.setMailAddr("field@detail.test");
        user.setCreateTime(new Date());
        lambda.insert(ColumnMappedUser.class).applyEntity(user).executeSumResult();

        ColumnMappedUser loaded = lambda.query(ColumnMappedUser.class)//
                .eq(ColumnMappedUser::getId, 42001)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("FieldAnno", loaded.getUserName());
        assertEquals("field@detail.test", loaded.getMailAddr());
    }

    /**
     * @Column 字段映射的元数据验证。
     */
    @Test
    public void testColumnFieldMapping_Metadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(ColumnMappedUser.class);

        TableMapping<?> mapping = registry.findByEntity(ColumnMappedUser.class);
        assertNotNull(mapping);

        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull("id should be mapped", idCol);
        assertTrue("id should be primary key", idCol.isPrimaryKey());

        ColumnMapping nameCol = mapping.getPropertyByName("userName");
        assertNotNull("userName property should be mapped", nameCol);
        assertEquals("name", nameCol.getColumn());

        ColumnMapping mailCol = mapping.getPropertyByName("mailAddr");
        assertNotNull("mailAddr property should be mapped", mailCol);
        assertEquals("email", mailCol.getColumn());

        ColumnMapping createTimeCol = mapping.getPropertyByName("createTime");
        assertNotNull("createTime should be mapped", createTimeCol);
        assertEquals("create_time", createTimeCol.getColumn());
    }

    /**
     * @Column(name="xxx") 通过 name 属性指定列名。
     */
    @Test
    public void testColumnNameAttribute() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        ColumnNameUser user = new ColumnNameUser();
        user.setId(42101);
        user.setUserName("NameAttr");
        user.setAge(26);
        user.setEmail("name@detail.test");
        user.setCreateTime(new Date());
        lambda.insert(ColumnNameUser.class).applyEntity(user).executeSumResult();

        // 验证数据库中 name 列的值
        UserInfo raw = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 42101)//
                .queryForObject();

        assertNotNull(raw);
        assertEquals("NameAttr", raw.getName());
    }

    /**
     * @Column(value="xxx") 通过 value 属性指定列名，效果等同于 @Column(name="xxx")。
     */
    @Test
    public void testColumnValueAttribute() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        ColumnValueUser user = new ColumnValueUser();
        user.setId(42201);
        user.setUserName("ValueAttr");
        user.setAge(27);
        user.setEmail("value@detail.test");
        user.setCreateTime(new Date());
        lambda.insert(ColumnValueUser.class).applyEntity(user).executeSumResult();

        UserInfo raw = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 42201)//
                .queryForObject();

        assertNotNull(raw);
        assertEquals("ValueAttr", raw.getName());
    }

    /**
     * @Column(name=...) 和 @Column(value=...) 的元数据应相同。
     */
    @Test
    public void testColumnNameValueEquivalence_Metadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(ColumnNameUser.class, "", "nameUser");
        registry.loadEntityToSpace(ColumnValueUser.class, "", "valueUser");

        TableMapping<?> nameMapping = registry.findBySpace("", "nameUser");
        TableMapping<?> valueMapping = registry.findBySpace("", "valueUser");

        ColumnMapping nameCol = nameMapping.getPropertyByName("userName");
        ColumnMapping valueCol = valueMapping.getPropertyByName("userName");

        assertNotNull(nameCol);
        assertNotNull(valueCol);
        assertEquals("Both should map to 'name' column", nameCol.getColumn(), valueCol.getColumn());
        assertEquals("name", nameCol.getColumn());
    }

    /**
     * @Table(table="user_info") 等价于 @Table("user_info")。
     */
    @Test
    public void testTableAttributeEquivalence() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(TableAttrUser.class, "", "tableAttr");
        registry.loadEntityToSpace(UserInfo.class, "", "tableValue");

        TableMapping<?> attrMapping = registry.findBySpace("", "tableAttr");
        TableMapping<?> valueMapping = registry.findBySpace("", "tableValue");

        assertEquals("user_info", attrMapping.getTable());
        assertEquals("user_info", valueMapping.getTable());
        assertEquals("Both should resolve to same table name", attrMapping.getTable(), valueMapping.getTable());
    }

    /**
     * @Primary 注解标记主属性——当同一列映射到多个属性时使用。
     * getPrimaryPropertyByColumn 应返回带 @Primary 的属性。
     */
    @Test
    public void testPrimaryAnnotation_Metadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(PrimaryMarkerUser.class);

        TableMapping<?> mapping = registry.findByEntity(PrimaryMarkerUser.class);
        assertNotNull(mapping);

        // 同一列 "name" 映射到 primaryName 和 aliasName
        List<ColumnMapping> nameColumns = mapping.getPropertyByColumn("name");
        assertEquals("name column should map to 2 properties", 2, nameColumns.size());

        // getPrimaryPropertyByColumn 应返回带 @Primary 的 primaryName
        ColumnMapping primaryCol = mapping.getPrimaryPropertyByColumn("name");
        assertNotNull("Should find primary mapping for 'name' column", primaryCol);
        assertEquals("primaryName", primaryCol.getProperty());
    }

    /**
     * @ResultMap 注解通过 loadResultMapToSpace 加载，验证 space/id 元数据。
     */
    @Test
    public void testResultMapAnnotation_SpaceAndId() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadResultMapToSpace(UserResultMap.class);

        TableMapping<?> mapping = registry.findBySpace("mySpace", "myResultMap");

        assertNotNull("Should find ResultMap by space+id", mapping);
        assertTrue("ResultMap autoMapping should be true", mapping.isAutoProperty());
        assertTrue("ResultMap caseInsensitive should be true", mapping.isCaseInsensitive());
        assertTrue("ResultMap mapUnderscoreToCamelCase should be true", mapping.isToCamelCase());

        // ResultMap 不关联表
        assertTrue("ResultMap table should be empty", mapping.getTable() == null || mapping.getTable().isEmpty());
    }

    /**
     * @ResultMap(value="xxx") 使用 value 代替 id。
     */
    @Test
    public void testResultMapAnnotation_ValueAttribute() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadResultMapToSpace(SimpleResultMap.class);

        // @ResultMap(value="simpleResult") 的 space 默认为 ""
        TableMapping<?> mapping = registry.findBySpace("", "simpleResult");

        assertNotNull("Should find ResultMap by value", mapping);
    }

    /**
     * loadResultMapToSpace 自定义 space+name 覆盖注解。
     */
    @Test
    public void testResultMapAnnotation_CustomOverride() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadResultMapToSpace(SimpleResultMap.class, "customSpace", "customName");

        TableMapping<?> mapping = registry.findBySpace("customSpace", "customName");
        assertNotNull("Should find ResultMap by custom space+name", mapping);
    }

    /**
     * Map<String,Object> 作为实体类型（isMapEntity=true）。
     */
    @Test
    public void testMapEntitySupport() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 先插入一条数据
        UserInfo rawUser = createUser(42601, "MapEntity", 30);
        lambda.insert(UserInfo.class).applyEntity(rawUser).executeSumResult();

        // 使用 Map 方式查询
        Map<String, Object> result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 42601)//
                .queryForMap();

        assertNotNull(result);
        assertEquals("MapEntity", result.get("name"));
    }

    /**
     * 不带 @Table 注解的普通 POJO 不被 isEntity() 识别为实体。
     */
    @Test
    public void testNonEntityClass() throws Exception {
        assertFalse("UserResultMap has @ResultMap not @Table → not entity", MappingRegistry.isEntity(UserResultMap.class));
    }
}
