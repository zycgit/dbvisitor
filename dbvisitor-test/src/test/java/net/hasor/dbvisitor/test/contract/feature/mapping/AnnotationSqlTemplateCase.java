/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.mapping;

import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.annotation.*;
import net.hasor.dbvisitor.test.contract.material.model.tabledef.OrderByTemplateUser;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class AnnotationSqlTemplateCase extends AbstractNxnContractTest {
    private static final Set<String>    INITIALIZED_ENVS = new HashSet<>();
    private              LambdaTemplate lambdaTemplate;

    @Before
    public void createLambdaTemplate() throws SQLException {
        this.lambdaTemplate = new LambdaTemplate(dataSource);
        synchronized (INITIALIZED_ENVS) {
            if (INITIALIZED_ENVS.add(profile().env())) {
                dropTableIfExists("test_md5_user");
                jdbcTemplate.executeUpdate("CREATE TABLE test_md5_user (" + primaryKeyColumn("id", "varchar(50)") + ", name varchar(100), password varchar(100))");
                dropTableIfExists("test_template_user");
                jdbcTemplate.executeUpdate("CREATE TABLE test_template_user (" + primaryKeyColumn("id", "int") + ", name varchar(50), login_ip varchar(50), create_at " + profile().datetimeColumnType() + ", data_value varchar(200))");
                createSqlTemplateDefinitions();
            } else {
                deleteAllRows("test_md5_user");
                deleteAllRows("test_template_user");
            }
        }
    }

    protected void createSqlTemplateDefinitions() throws SQLException {
    }

    protected Class<? extends AbstractMd5User> md5UserType() {
        return Md5User.class;
    }

    // 能力归属：对象映射 / 字段类型与语句模板 / 语句模板。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_MD5, column = "mapping-keys/field-types-and-statement-templates/statement-templates")
    public void annotationSqlTemplate_shouldApplyMd5InsertTemplateWhenSupported() throws Exception {
        AbstractMd5User user = md5UserType().getDeclaredConstructor().newInstance();
        user.setId("nxn-md5-1");
        user.setName("user1");
        user.setPassword("123456");

        insertMd5User(md5UserType(), user);

        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT password FROM test_md5_user WHERE id = ?", new Object[] { "nxn-md5-1" });
        assertEquals("e10adc3949ba59abbe56e057f20f883e", value(row, "password"));
    }

    // 能力归属：对象映射 / 字段类型与语句模板 / 语句模板。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_INSERT_SET, column = "mapping-keys/field-types-and-statement-templates/statement-templates")
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

    // 能力归属：对象映射 / 字段类型与语句模板 / 语句模板。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_SELECT, column = "mapping-keys/field-types-and-statement-templates/statement-templates")
    public void annotationSqlTemplate_shouldApplySelectTemplate() throws SQLException {
        insertRawUser(910101, "hello", 25);

        SelectTemplateUser result = lambdaTemplate.query(SelectTemplateUser.class)//
                .eq(SelectTemplateUser::getId, 910101)//
                .queryForObject();

        assertNotNull(result);
        assertEquals("HELLO", result.getName());
    }

    // 能力归属：对象映射 / 字段类型与语句模板 / 排序列模板。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_ORDER_BY, column = "mapping-keys/field-types-and-statement-templates/statement-templates")
    public void annotationSqlTemplate_shouldApplyOrderByColumnTemplate() throws SQLException {
        insertRawUser(910108, "OrderMiddle", 20);
        insertRawUser(910109, "OrderSmall", 10);
        insertRawUser(910110, "OrderLarge", 30);

        BoundSql boundSql = lambdaTemplate.query(OrderByTemplateUser.class)//
                .ge(OrderByTemplateUser::getId, 910108)//
                .le(OrderByTemplateUser::getId, 910110)//
                .asc(OrderByTemplateUser::getAge)//
                .getBoundSql();
        assertTrue(boundSql.getSqlString(), boundSql.getSqlString().contains("age * -1"));

        List<OrderByTemplateUser> ascending = lambdaTemplate.query(OrderByTemplateUser.class)//
                .ge(OrderByTemplateUser::getId, 910108)//
                .le(OrderByTemplateUser::getId, 910110)//
                .asc(OrderByTemplateUser::getAge)//
                .queryForList();
        assertEquals(3, ascending.size());
        assertEquals(Integer.valueOf(910110), ascending.get(0).getId());
        assertEquals(Integer.valueOf(910108), ascending.get(1).getId());
        assertEquals(Integer.valueOf(910109), ascending.get(2).getId());

        List<OrderByTemplateUser> descending = lambdaTemplate.query(OrderByTemplateUser.class)//
                .ge(OrderByTemplateUser::getId, 910108)//
                .le(OrderByTemplateUser::getId, 910110)//
                .desc(OrderByTemplateUser::getAge)//
                .queryForList();
        assertEquals(3, descending.size());
        assertEquals(Integer.valueOf(910109), descending.get(0).getId());
        assertEquals(Integer.valueOf(910108), descending.get(1).getId());
        assertEquals(Integer.valueOf(910110), descending.get(2).getId());
    }

    // 能力归属：对象映射 / 字段类型与语句模板 / 语句模板。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_INSERT, column = "mapping-keys/field-types-and-statement-templates/statement-templates")
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

    // 能力归属：对象映射 / 字段类型与语句模板 / 语句模板。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_SET_VALUE, column = "mapping-keys/field-types-and-statement-templates/statement-templates")
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

    // 能力归属：对象映射 / 字段类型与语句模板 / 语句模板。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_WHERE_COLUMN, column = "mapping-keys/field-types-and-statement-templates/statement-templates")
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

    // 能力归属：对象映射 / 字段类型与语句模板 / 语句模板。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_WHERE_VALUE, column = "mapping-keys/field-types-and-statement-templates/statement-templates")
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

    // 能力归属：对象映射 / 字段类型与语句模板 / 语句模板。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_COMBINED_INSERT_SELECT, column = "mapping-keys/field-types-and-statement-templates/statement-templates")
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

    // 能力归属：对象映射 / 字段类型与语句模板 / 语句模板。
    @Test
    @Capability(value = CapabilityId.MAPPING_ANNOTATION_SQL_TEMPLATE_COMBINED_UPDATE, column = "mapping-keys/field-types-and-statement-templates/statement-templates")
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

    private <T extends AbstractMd5User> void insertMd5User(Class<T> userType, AbstractMd5User user) throws SQLException {
        lambdaTemplate.insert(userType)//
                .applyEntity(userType.cast(user))//
                .executeSumResult();
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
