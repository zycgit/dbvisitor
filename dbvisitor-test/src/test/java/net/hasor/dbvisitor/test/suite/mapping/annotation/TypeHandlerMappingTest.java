package net.hasor.dbvisitor.test.suite.mapping.annotation;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.handler.UpperCaseTypeHandler;
import net.hasor.dbvisitor.test.model.UserInfo;
import net.hasor.dbvisitor.test.model.annotation.JdbcTypeUser;
import net.hasor.dbvisitor.test.model.annotation.SpecialJavaTypeUser;
import net.hasor.dbvisitor.test.model.annotation.TypeHandlerUser;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @Column 的 typeHandler、jdbcType、specialJavaType 属性测试。
 * 验证注解级别的类型处理器、JDBC 类型指定和特殊 Java 类型映射。
 */
public class TypeHandlerMappingTest extends AbstractOneApiTest {

    // ============ Tests ============

    /**
     * 自定义 typeHandler 的元数据验证——映射解析后 typeHandler 应为指定类型。
     */
    @Test
    public void testCustomTypeHandler_Metadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(TypeHandlerUser.class);

        TableMapping<?> mapping = registry.findByEntity(TypeHandlerUser.class);
        assertNotNull(mapping);

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertTrue("TypeHandler should be UpperCaseTypeHandler",//
                nameCol.getTypeHandler() instanceof UpperCaseTypeHandler);
    }

    /**
     * 自定义 typeHandler 实际数据库操作——INSERT 时自动转大写。
     */
    @Test
    public void testCustomTypeHandler_InsertUpperCase() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        TypeHandlerUser user = new TypeHandlerUser();
        user.setId(44001);
        user.setName("lowercase_name");
        user.setAge(25);
        user.setEmail("th@test.com");
        user.setCreateTime(new Date());
        lambda.insert(TypeHandlerUser.class).applyEntity(user).executeSumResult();

        // 通过原始 UserInfo 读取，验证数据库中的值已被转为大写
        UserInfo raw = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 44001)//
                .queryForObject();

        assertNotNull(raw);
        assertEquals("LOWERCASE_NAME", raw.getName());
    }

    /**
     * jdbcType 的元数据验证。
     */
    @Test
    public void testJdbcType_Metadata() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(JdbcTypeUser.class);

        TableMapping<?> mapping = registry.findByEntity(JdbcTypeUser.class);
        assertNotNull(mapping);

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertEquals("jdbcType should be VARCHAR",//
                Integer.valueOf(Types.VARCHAR), nameCol.getJdbcType());

        ColumnMapping ageCol = mapping.getPropertyByName("age");
        assertNotNull(ageCol);
        assertEquals("jdbcType should be INTEGER",//
                Integer.valueOf(Types.INTEGER), ageCol.getJdbcType());
    }

    /**
     * 指定 jdbcType 后的实际数据库操作。
     */
    @Test
    public void testJdbcType_InsertAndQuery() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        JdbcTypeUser user = new JdbcTypeUser();
        user.setId(44101);
        user.setName("JdbcTypeUser");
        user.setAge(30);
        user.setEmail("jdbc@test.com");
        lambda.insert(JdbcTypeUser.class).applyEntity(user).executeSumResult();

        JdbcTypeUser loaded = lambda.query(JdbcTypeUser.class)//
                .eq(JdbcTypeUser::getId, 44101)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("JdbcTypeUser", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
    }

    /**
     * specialJavaType 的元数据验证——字段声明为 CharSequence，实际类型应为 String。
     */
    @Test
    public void testSpecialJavaType_Metadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(SpecialJavaTypeUser.class);

        TableMapping<?> mapping = registry.findByEntity(SpecialJavaTypeUser.class);
        assertNotNull(mapping);

        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertEquals("Java type should be String (specialJavaType)",//
                String.class, nameCol.getJavaType());
    }

    /**
     * specialJavaType 的实际数据库操作——使用 CharSequence 字段正常读写。
     */
    @Test
    public void testSpecialJavaType_InsertAndQuery() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        SpecialJavaTypeUser user = new SpecialJavaTypeUser();
        user.setId(44201);
        user.setName("SpecialType");
        user.setAge(28);
        lambda.insert(SpecialJavaTypeUser.class).applyEntity(user).executeSumResult();

        SpecialJavaTypeUser loaded = lambda.query(SpecialJavaTypeUser.class)//
                .eq(SpecialJavaTypeUser::getId, 44201)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("SpecialType", loaded.getName().toString());
    }

    /**
     * 不指定 typeHandler 时，应使用默认的 TypeHandler 处理。
     */
    @Test
    public void testDefaultTypeHandler() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(JdbcTypeUser.class);

        TableMapping<?> mapping = registry.findByEntity(JdbcTypeUser.class);
        ColumnMapping emailCol = mapping.getPropertyByName("email");
        assertNotNull(emailCol);

        // 未指定 typeHandler 时，不应是 UpperCaseTypeHandler
        assertFalse("Default should not be UpperCaseTypeHandler",//
                emailCol.getTypeHandler() instanceof UpperCaseTypeHandler);
        assertNotNull("Default typeHandler should not be null",  //
                emailCol.getTypeHandler());
    }
}
