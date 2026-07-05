package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.contract.material.handler.UpperCaseTypeHandler;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.annotation.JdbcTypeUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.SpecialJavaTypeUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.TypeHandlerUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class AnnHandlerTest extends AbstractNxnContractTest {
    private LambdaTemplate lambda;

    @Before
    public void createLambdaTemplate() throws SQLException {
        this.lambda = new LambdaTemplate(dataSource);
    }

    protected int baseId() {
        return 940000;
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_TYPE_HANDLER_METADATA)
    public void columnAnnCustomTypeHandlerMeta() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(TypeHandlerUser.class);

        TableMapping<?> mapping = registry.findByEntity(TypeHandlerUser.class);
        assertNotNull(mapping);

        ColumnMapping name = mapping.getPropertyByName("name");
        assertNotNull(name);
        assertTrue(name.getTypeHandler() instanceof UpperCaseTypeHandler);
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_TYPE_HANDLER_INSERT)
    public void columnAnnCustomTypeHandlerOnInsert() throws SQLException {
        TypeHandlerUser user = new TypeHandlerUser();
        user.setId(baseId() + 1);
        user.setName("lowercase_name");
        user.setAge(25);
        user.setEmail("type-handler@nxn.test");
        user.setCreateTime(new Date());

        this.lambda.insert(TypeHandlerUser.class).applyEntity(user).executeSumResult();

        UserInfo raw = this.lambda.query(UserInfo.class) //
                .eq(UserInfo::getId, baseId() + 1) //
                .queryForObject();
        assertNotNull(raw);
        assertEquals("LOWERCASE_NAME", raw.getName());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_DEFAULT_TYPE_HANDLER)
    public void columnAnnDefTypeHandlerWhenUnspecified() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(JdbcTypeUser.class);

        TableMapping<?> mapping = registry.findByEntity(JdbcTypeUser.class);
        ColumnMapping email = mapping.getPropertyByName("email");
        assertNotNull(email);
        assertFalse(email.getTypeHandler() instanceof UpperCaseTypeHandler);
        assertNotNull(email.getTypeHandler());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_JDBC_TYPE_METADATA)
    public void columnAnnJdbcTypeMeta() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(JdbcTypeUser.class);

        TableMapping<?> mapping = registry.findByEntity(JdbcTypeUser.class);
        assertNotNull(mapping);

        ColumnMapping name = mapping.getPropertyByName("name");
        ColumnMapping age = mapping.getPropertyByName("age");
        assertNotNull(name);
        assertNotNull(age);
        assertEquals(Integer.valueOf(Types.VARCHAR), name.getJdbcType());
        assertEquals(Integer.valueOf(Types.INTEGER), age.getJdbcType());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_JDBC_TYPE_ROUND_TRIP)
    public void columnAnnJdbcTypeDuringRoundTrip() throws SQLException {
        JdbcTypeUser user = new JdbcTypeUser();
        user.setId(baseId() + 11);
        user.setName("JdbcTypeUser");
        user.setAge(30);
        user.setEmail("jdbc-type@nxn.test");

        this.lambda.insert(JdbcTypeUser.class).applyEntity(user).executeSumResult();

        JdbcTypeUser loaded = this.lambda.query(JdbcTypeUser.class) //
                .eq(JdbcTypeUser::getId, baseId() + 11) //
                .queryForObject();
        assertNotNull(loaded);
        assertEquals("JdbcTypeUser", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_SPECIAL_JAVA_TYPE_METADATA)
    public void columnAnnSpecialJavaTypeMeta() {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(SpecialJavaTypeUser.class);

        TableMapping<?> mapping = registry.findByEntity(SpecialJavaTypeUser.class);
        assertNotNull(mapping);

        ColumnMapping name = mapping.getPropertyByName("name");
        assertNotNull(name);
        assertEquals(String.class, name.getJavaType());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_SPECIAL_JAVA_TYPE_ROUND_TRIP)
    public void columnAnnSpecialJavaTypeDuringRoundTrip() throws SQLException {
        SpecialJavaTypeUser user = new SpecialJavaTypeUser();
        user.setId(baseId() + 21);
        user.setName("SpecialType");
        user.setAge(28);

        this.lambda.insert(SpecialJavaTypeUser.class).applyEntity(user).executeSumResult();

        SpecialJavaTypeUser loaded = this.lambda.query(SpecialJavaTypeUser.class) //
                .eq(SpecialJavaTypeUser::getId, baseId() + 21) //
                .queryForObject();
        assertNotNull(loaded);
        assertEquals("SpecialType", loaded.getName().toString());
    }
}
