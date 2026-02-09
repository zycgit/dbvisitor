package net.hasor.dbvisitor.test.oneapi.suite.fluent;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Exception Handling Test
 * 验证框架级别的异常/边界行为（非数据库约束限制）
 */
public class ExceptionHandlingTest extends AbstractOneApiTest {

    /**
     * 测试查询无结果时的行为
     * queryForObject() 应返回 null
     */
    @Test
    public void testQueryForObjectNoResult() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 99999999)//
                .queryForObject();

        assertNull("Should return null when no result", result);
    }

    /**
     * 测试查询多条记录时使用 queryForObject()
     * 框架实现: RowMapperResultSetExtractor 使用 rowsExpected=1 限制提取行数,
     * 因此 queryForObject 在多条匹配时只返回第一条, 不会抛异常
     */
    @Test
    public void testQueryForObjectMultipleResults() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(22201, "Multi1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(22202, "Multi2", 26)).executeSumResult();

        UserInfo result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(22201, 22202))//
                .orderBy("id")//
                .queryForObject();

        // 框架只取第一条, 不报错
        assertNotNull("Should return first matching record", result);
        assertEquals("Should return first record by order", Integer.valueOf(22201), Integer.valueOf(result.getId()));
        assertEquals("Multi1", result.getName());
    }

    /**
     * 测试更新不存在的记录
     * 应返回 0（受影响行数）
     */
    @Test
    public void testUpdateNonExistentRecord() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        int updated = lambda.update(UserInfo.class)//
                .eq(UserInfo::getId, 99999999)//
                .updateTo(UserInfo::getAge, 100)//
                .doUpdate();

        assertEquals("Should return 0 for non-existent record", 0, updated);
    }

    /**
     * 测试删除不存在的记录
     * 应返回 0（受影响行数）
     */
    @Test
    public void testDeleteNonExistentRecord() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        int deleted = lambda.delete(UserInfo.class)//
                .eq(UserInfo::getId, 99999999)//
                .doDelete();

        assertEquals("Should return 0 for non-existent record", 0, deleted);
    }

    // ==================== 空条件保护（allowEmptyWhere）====================

    /** 空条件删除 - 未开启 allowEmptyWhere 应抛异常 */
    @Test
    public void testDeleteWithoutCondition_NotAllowed() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(22601, "Del1", 25)).executeSumResult();

        try {
            lambda.delete(UserInfo.class).doDelete();
            fail("Should throw for empty-where delete without allowEmptyWhere()");
        } catch (IllegalStateException e) {
            assertTrue("Should mention allowEmptyWhere",//
                    e.getMessage().contains("allowEmptyWhere"));
        }

        // 数据不受影响
        assertNotNull(lambda.query(UserInfo.class).eq(UserInfo::getId, 22601).queryForObject());
    }

    /** 空条件删除 - 开启 allowEmptyWhere 后应成功 */
    @Test
    public void testDeleteWithoutCondition_Allowed() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(22701, "Del2", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(22702, "Del3", 26)).executeSumResult();

        int deleted = lambda.delete(UserInfo.class)//
                .allowEmptyWhere()//
                .doDelete();

        assertTrue("Should delete at least 2 rows", deleted >= 2);
        assertEquals(0, lambda.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(22701, 22702))//
                .queryForCount());
    }

    /** 空条件更新 - 未开启 allowEmptyWhere 应抛异常 */
    @Test
    public void testUpdateWithoutCondition_NotAllowed() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(22801, "Upd1", 25)).executeSumResult();

        try {
            lambda.update(UserInfo.class)//
                    .updateTo(UserInfo::getAge, 99)//
                    .doUpdate();
            fail("Should throw for empty-where update without allowEmptyWhere()");
        } catch (IllegalStateException e) {
            assertTrue("Should mention allowEmptyWhere",//
                    e.getMessage().contains("allowEmptyWhere"));
        }

        // 数据不受影响
        assertEquals(Integer.valueOf(25), lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 22801).queryForObject().getAge());
    }

    /** 空条件更新 - 开启 allowEmptyWhere 后应成功 */
    @Test
    public void testUpdateWithoutCondition_Allowed() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(22901, "Upd2", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(22902, "Upd3", 30)).executeSumResult();

        int updated = lambda.update(UserInfo.class)//
                .allowEmptyWhere()//
                .updateTo(UserInfo::getAge, 99)//
                .doUpdate();

        assertTrue("Should update at least 2 rows", updated >= 2);

        assertEquals(Integer.valueOf(99), lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 22901).queryForObject().getAge());
        assertEquals(Integer.valueOf(99), lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 22902).queryForObject().getAge());
    }

    /**
     * 测试空 IN 列表，框架对空 IN 列表应抛出异常
     */
    @Test
    public void testEmptyInList() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(23001, "InEmpty", 25)).executeSumResult();

        try {
            lambda.query(UserInfo.class)//
                    .in(UserInfo::getId, java.util.Collections.emptyList())//
                    .queryForList();
            fail("Should throw exception for empty IN list");
        } catch (Exception e) {
            assertTrue("Exception message should mention empty",//
                    e.getMessage().toLowerCase().contains("empty"));
        }
    }

    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@exception.com");
        u.setCreateTime(new Date());
        return u;
    }
}
