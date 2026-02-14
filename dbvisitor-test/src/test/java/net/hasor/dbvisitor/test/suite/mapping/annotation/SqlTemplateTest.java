package net.hasor.dbvisitor.test.suite.mapping.annotation;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import net.hasor.dbvisitor.test.model.annotation.*;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * SQL 语句模板功能测试。
 * 验证 @Column 的 selectTemplate/insertTemplate/setValueTemplate/
 * whereColTemplate/whereValueTemplate 五种模板的行为。
 * 参考文档: mapping/template.md
 */
public class SqlTemplateTest extends AbstractOneApiTest {

    // ============ Helper ============

    private UserInfo insertRawUser(LambdaTemplate lambda, int id, String name, int age) throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setCreateTime(new Date());
        lambda.insert(UserInfo.class).applyEntity(user).executeSumResult();
        return user;
    }

    private UserInfo readRawUser(LambdaTemplate lambda, int id) throws SQLException {
        return lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .queryForObject();
    }

    private Object getArgValue(Object arg) {
        if (arg instanceof SqlArg) {
            return ((SqlArg) arg).getValue();
        }
        return arg;
    }

    // ============ Tests ============

    /**
     * selectTemplate = "UPPER(name)" 使 SELECT 结果中 name 被转为大写。
     * 数据存储为小写，通过 selectTemplate 在查询时自动转大写返回。
     */
    @Test
    public void testSelectTemplate_UpperCase() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertRawUser(lambda, 40001, "hello", 25);

        // 使用 selectTemplate 实体查询 → SELECT UPPER(name) AS name
        SelectTemplateUser result = lambda.query(SelectTemplateUser.class)//
                .eq(SelectTemplateUser::getId, 40001)//
                .queryForObject();

        assertNotNull(result);
        assertEquals("HELLO", result.getName());
    }

    /**
     * insertTemplate = "LOWER(?)" 使 INSERT 时 name 被转为小写存储。
     */
    @Test
    public void testInsertTemplate_LowerCase() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        InsertTemplateUser user = new InsertTemplateUser();
        user.setId(40101);
        user.setName("HELLO WORLD");
        user.setAge(30);
        user.setCreateTime(new Date());
        lambda.insert(InsertTemplateUser.class).applyEntity(user).executeSumResult();

        // 用原始 UserInfo 读取，验证存储值为小写
        UserInfo raw = readRawUser(lambda, 40101);
        assertNotNull(raw);
        assertEquals("hello world", raw.getName());
    }

    /**
     * setValueTemplate = "UPPER(?)" 使 UPDATE SET 时 name 被转为大写存储。
     */
    @Test
    public void testSetValueTemplate_UpperCase() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertRawUser(lambda, 40201, "initial", 25);

        // 使用 setValueTemplate 实体更新 → SET name = UPPER(?)
        int updated = lambda.update(UpdateTemplateUser.class)//
                .eq(UpdateTemplateUser::getId, 40201)//
                .updateTo(UpdateTemplateUser::getName, "hello world")//
                .doUpdate();

        assertEquals(1, updated);

        UserInfo raw = readRawUser(lambda, 40201);
        assertNotNull(raw);
        assertEquals("HELLO WORLD", raw.getName());
    }

    /**
     * whereColTemplate = "LOWER(name)" 使 WHERE 条件用 LOWER(name) 实现大小写无关匹配。
     * 数据库中存储 "Hello"，查询 "hello" 应能匹配上。
     */
    @Test
    public void testWhereColTemplate_CaseInsensitiveMatch() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertRawUser(lambda, 40301, "Hello", 25);

        // Verify SQL: WHERE LOWER(name) = ?
        BoundSql boundSql = lambda.query(WhereColTemplateUser.class)//
                .eq(WhereColTemplateUser::getName, "hello")//
                .getBoundSql();
        assertTrue("SQL should contain LOWER function", boundSql.getSqlString().toUpperCase().contains("LOWER("));
        assertEquals("Parameter should keep case", "hello", getArgValue(boundSql.getArgs()[0]));

        // WHERE LOWER(name) = ? 传入 "hello"
        WhereColTemplateUser result = lambda.query(WhereColTemplateUser.class)//
                .eq(WhereColTemplateUser::getName, "hello")//
                .queryForObject();

        assertNotNull("Should find record via LOWER(name)", result);
        assertEquals(Integer.valueOf(40301), result.getId());
    }

    /**
     * whereValueTemplate = "UPPER(?)" 使 WHERE 参数被转为大写后比较。
     * 数据库中存储 "HELLO"，查询 "hello" → UPPER("hello")="HELLO" 匹配。
     */
    @Test
    public void testWhereValueTemplate_UpperCaseParam() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertRawUser(lambda, 40401, "HELLO", 25);

        // Verify SQL: WHERE name = UPPER(?)
        BoundSql boundSql = lambda.query(WhereValueTemplateUser.class)//
                .eq(WhereValueTemplateUser::getName, "hello")//
                .getBoundSql();
        assertTrue("SQL should contain UPPER function for param", boundSql.getSqlString().toUpperCase().contains("UPPER(?"));
        assertEquals("Parameter should keep case", "hello", getArgValue(boundSql.getArgs()[0]));

        // WHERE name = UPPER(?)，传入 "hello" → UPPER("hello") = "HELLO" → 匹配
        WhereValueTemplateUser result = lambda.query(WhereValueTemplateUser.class)//
                .eq(WhereValueTemplateUser::getName, "hello")//
                .queryForObject();

        assertNotNull("Should find record via UPPER(?)", result);
        assertEquals(Integer.valueOf(40401), result.getId());
    }

    /**
     * 组合模板: insertTemplate(LOWER) + selectTemplate(UPPER) 联合验证。
     * 插入 "Hello World" → insertTemplate 存储 "hello world" → selectTemplate 读取 "HELLO WORLD"
     */
    @Test
    public void testCombinedTemplate_InsertAndSelect() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        CombinedTemplateUser user = new CombinedTemplateUser();
        user.setId(40601);
        user.setName("Hello World");
        user.setAge(28);
        user.setCreateTime(new Date());

        // Verify Insert SQL
        BoundSql insertSql = lambda.insert(CombinedTemplateUser.class).applyEntity(user).getBoundSql();
        // Insert template -> LOWER(?)
        assertTrue("Insert SQL should use LOWER(?)", insertSql.getSqlString().toUpperCase().contains("LOWER(?"));

        lambda.insert(CombinedTemplateUser.class).applyEntity(user).executeSumResult();

        // 通过原始 UserInfo 验证存储值（insertTemplate LOWER）
        UserInfo raw = readRawUser(lambda, 40601);
        assertEquals("hello world", raw.getName());

        // Verify Select SQL
        BoundSql selectSql = lambda.query(CombinedTemplateUser.class)//
                .eq(CombinedTemplateUser::getId, 40601)//
                .getBoundSql();
        // Select template -> UPPER(name)
        assertTrue("Select SQL should use UPPER(name)", selectSql.getSqlString().toUpperCase().contains("UPPER("));

        // 通过 CombinedTemplateUser 读取（selectTemplate UPPER）
        CombinedTemplateUser result = lambda.query(CombinedTemplateUser.class)//
                .eq(CombinedTemplateUser::getId, 40601)//
                .queryForObject();
        assertEquals("HELLO WORLD", result.getName());
    }

    /**
     * 组合模板: setValueTemplate(UPPER) + 原始读取验证。
     * 先插入原始数据，再用 setValueTemplate 更新。
     */
    @Test
    public void testCombinedTemplate_UpdateAndVerify() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertRawUser(lambda, 40701, "initial", 25);

        // Verify Update SQL: SET name = UPPER(?)
        BoundSql updateSql = lambda.update(CombinedTemplateUser.class)//
                .eq(CombinedTemplateUser::getId, 40701)//
                .updateTo(CombinedTemplateUser::getName, "hello world")//
                .getBoundSql();
        assertTrue("Update SQL should use UPPER(?)", updateSql.getSqlString().toUpperCase().contains("UPPER(?"));
        // Check params, ensure "hello world" is passed (wrapper applies function in SQL, not in Java side if using template)
        boolean paramFound = false;
        for (Object arg : updateSql.getArgs()) {
            if ("hello world".equals(getArgValue(arg))) {
                paramFound = true;
                break;
            }
        }
        assertTrue("Update params should contain 'hello world'", paramFound);

        // setValueTemplate UPPER → 存储 "HELLO WORLD"
        int updated = lambda.update(CombinedTemplateUser.class)//
                .eq(CombinedTemplateUser::getId, 40701)//
                .updateTo(CombinedTemplateUser::getName, "hello world")//
                .doUpdate();

        assertEquals(1, updated);

        UserInfo raw = readRawUser(lambda, 40701);
        assertEquals("HELLO WORLD", raw.getName());
    }
}
