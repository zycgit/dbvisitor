package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.annotation.CombinedTemplateUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.InsertTemplateUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.Md5User;
import net.hasor.dbvisitor.test.contract.material.model.annotation.SelectTemplateUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.TemplateUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.UpdateTemplateUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.WhereColTemplateUser;
import net.hasor.dbvisitor.test.contract.material.model.annotation.WhereValueTemplateUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.types.SqlArg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractAnnotationSqlTemplateContractTest extends AbstractNxnContractTest {
    private LambdaTemplate lambdaTemplate;

    @Before
    public void createLambdaTemplate() throws SQLException {
        this.lambdaTemplate = new LambdaTemplate(dataSource);
        dropTableIfExists("test_md5_user");
        jdbcTemplate.executeUpdate("CREATE TABLE test_md5_user (" + primaryKeyColumn("id", "varchar(50)") + ", name varchar(100), password varchar(100))");
        dropTableIfExists("test_template_user");
        jdbcTemplate.executeUpdate("CREATE TABLE test_template_user (" + primaryKeyColumn("id", "int") + ", name varchar(50), login_ip varchar(50), create_at " + profile().datetimeColumnType() + ", data_value varchar(200))");
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_MD5)
    public void annotationSqlTemplate_shouldApplyMd5InsertTemplateWhenSupported() throws SQLException {
        requiresNxnFeature(FeatureId.SQL_MD5_FUNCTION);
        Md5User user = new Md5User();
        user.setId("nxn-md5-1");
        user.setName("user1");
        user.setPassword("123456");

        lambdaTemplate.insert(Md5User.class)//
                .applyEntity(user)//
                .executeSumResult();

        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT password FROM test_md5_user WHERE id = ?", new Object[] { "nxn-md5-1" });
        assertEquals("e10adc3949ba59abbe56e057f20f883e", value(row, "password"));
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_INSERT_SET)
    public void annotationSqlTemplate_shouldApplyInsertAndSetValueTemplates() throws SQLException {
        TemplateUser user = new TemplateUser();
        user.setId(910001);
        user.setName("lower_case");
        user.setLoginIp("1.1.1.1");
        user.setCreateAt(new Date());
        user.setData("init");

        lambdaTemplate.insert(TemplateUser.class)//
                .applyEntity(user)//
                .executeSumResult();
        TemplateUser stored = lambdaTemplate.query(TemplateUser.class)//
                .eq(TemplateUser::getId, 910001)//
                .queryForObject();

        assertNotNull(stored);
        assertEquals("LOWER_CASE", stored.getName());
        assertEquals("PRE_1.1.1.1", stored.getLoginIp());
        assertNotNull(stored.getCreateAt());

        lambdaTemplate.update(TemplateUser.class)//
                .eq(TemplateUser::getId, 910001)//
                .updateTo(TemplateUser::getData, "test_val")//
                .doUpdate();
        TemplateUser updated = lambdaTemplate.query(TemplateUser.class)//
                .eq(TemplateUser::getId, 910001)//
                .queryForObject();

        assertEquals("test_val_updated", updated.getData());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_SELECT)
    public void annotationSqlTemplate_shouldApplySelectTemplate() throws SQLException {
        insertRawUser(910101, "hello", 25);

        SelectTemplateUser result = lambdaTemplate.query(SelectTemplateUser.class)//
                .eq(SelectTemplateUser::getId, 910101)//
                .queryForObject();

        assertNotNull(result);
        assertEquals("HELLO", result.getName());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_INSERT)
    public void annotationSqlTemplate_shouldApplyInsertTemplate() throws SQLException {
        deleteRawUser(910102);
        InsertTemplateUser user = new InsertTemplateUser();
        user.setId(910102);
        user.setName("HELLO WORLD");
        user.setAge(30);
        user.setCreateTime(new Date());

        lambdaTemplate.insert(InsertTemplateUser.class)//
                .applyEntity(user)//
                .executeSumResult();

        UserInfo raw = readRawUser(910102);
        assertNotNull(raw);
        assertEquals("hello world", raw.getName());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_SET_VALUE)
    public void annotationSqlTemplate_shouldApplySetValueTemplate() throws SQLException {
        insertRawUser(910103, "initial", 25);

        int updated = lambdaTemplate.update(UpdateTemplateUser.class)//
                .eq(UpdateTemplateUser::getId, 910103)//
                .updateTo(UpdateTemplateUser::getName, "hello world")//
                .doUpdate();

        assertEquals(1, updated);
        UserInfo raw = readRawUser(910103);
        assertNotNull(raw);
        assertEquals("HELLO WORLD", raw.getName());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_WHERE_COLUMN)
    public void annotationSqlTemplate_shouldApplyWhereColumnTemplate() throws SQLException {
        insertRawUser(910104, "Hello", 25);

        BoundSql boundSql = lambdaTemplate.query(WhereColTemplateUser.class)//
                .eq(WhereColTemplateUser::getName, "hello")//
                .getBoundSql();
        assertTrue(boundSql.getSqlString().toUpperCase().contains("LOWER("));
        assertEquals("hello", argValue(boundSql.getArgs()[0]));

        WhereColTemplateUser result = lambdaTemplate.query(WhereColTemplateUser.class)//
                .eq(WhereColTemplateUser::getName, "hello")//
                .queryForObject();

        assertNotNull(result);
        assertEquals(Integer.valueOf(910104), result.getId());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_WHERE_VALUE)
    public void annotationSqlTemplate_shouldApplyWhereValueTemplate() throws SQLException {
        insertRawUser(910105, "HELLO", 25);

        BoundSql boundSql = lambdaTemplate.query(WhereValueTemplateUser.class)//
                .eq(WhereValueTemplateUser::getName, "hello")//
                .getBoundSql();
        assertTrue(boundSql.getSqlString().toUpperCase().contains("UPPER(?"));
        assertEquals("hello", argValue(boundSql.getArgs()[0]));

        WhereValueTemplateUser result = lambdaTemplate.query(WhereValueTemplateUser.class)//
                .eq(WhereValueTemplateUser::getName, "hello")//
                .queryForObject();

        assertNotNull(result);
        assertEquals(Integer.valueOf(910105), result.getId());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_COMBINED_INSERT_SELECT)
    public void annotationSqlTemplate_shouldApplyCombinedInsertAndSelectTemplates() throws SQLException {
        deleteRawUser(910106);
        CombinedTemplateUser user = new CombinedTemplateUser();
        user.setId(910106);
        user.setName("Hello World");
        user.setAge(28);
        user.setCreateTime(new Date());

        BoundSql insertSql = lambdaTemplate.insert(CombinedTemplateUser.class)//
                .applyEntity(user)//
                .getBoundSql();
        assertTrue(insertSql.getSqlString().toUpperCase().contains("LOWER(?"));

        lambdaTemplate.insert(CombinedTemplateUser.class)//
                .applyEntity(user)//
                .executeSumResult();
        UserInfo raw = readRawUser(910106);
        assertNotNull(raw);
        assertEquals("hello world", raw.getName());

        BoundSql selectSql = lambdaTemplate.query(CombinedTemplateUser.class)//
                .eq(CombinedTemplateUser::getId, 910106)//
                .getBoundSql();
        assertTrue(selectSql.getSqlString().toUpperCase().contains("UPPER("));

        CombinedTemplateUser result = lambdaTemplate.query(CombinedTemplateUser.class)//
                .eq(CombinedTemplateUser::getId, 910106)//
                .queryForObject();
        assertNotNull(result);
        assertEquals("HELLO WORLD", result.getName());
    }

    @Test
    @Capability(CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_COMBINED_UPDATE)
    public void annotationSqlTemplate_shouldApplyCombinedUpdateTemplate() throws SQLException {
        insertRawUser(910107, "initial", 25);

        BoundSql updateSql = lambdaTemplate.update(CombinedTemplateUser.class)//
                .eq(CombinedTemplateUser::getId, 910107)//
                .updateTo(CombinedTemplateUser::getName, "hello world")//
                .getBoundSql();
        assertTrue(updateSql.getSqlString().toUpperCase().contains("UPPER(?"));
        assertTrue(containsArgValue(updateSql.getArgs(), "hello world"));

        int updated = lambdaTemplate.update(CombinedTemplateUser.class)//
                .eq(CombinedTemplateUser::getId, 910107)//
                .updateTo(CombinedTemplateUser::getName, "hello world")//
                .doUpdate();

        assertEquals(1, updated);
        UserInfo raw = readRawUser(910107);
        assertNotNull(raw);
        assertEquals("HELLO WORLD", raw.getName());
    }

    private void deleteRawUser(int id) throws SQLException {
        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id = ?", new Object[] { id });
    }

    private UserInfo insertRawUser(int id, String name, int age) throws SQLException {
        deleteRawUser(id);
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setCreateTime(new Date());
        lambdaTemplate.insert(UserInfo.class)//
                .applyEntity(user)//
                .executeSumResult();
        return user;
    }

    private UserInfo readRawUser(int id) throws SQLException {
        return lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .queryForObject();
    }

    private boolean containsArgValue(Object[] args, Object expected) {
        for (Object arg : args) {
            if (expected.equals(argValue(arg))) {
                return true;
            }
        }
        return false;
    }

    private Object argValue(Object arg) {
        if (arg instanceof SqlArg) {
            return ((SqlArg) arg).getValue();
        }
        return arg;
    }

    private Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
