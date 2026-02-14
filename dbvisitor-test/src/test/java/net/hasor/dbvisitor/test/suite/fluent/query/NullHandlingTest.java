package net.hasor.dbvisitor.test.suite.fluent.query;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * NULL Value Handling Test
 * 验证 NULL 值的查询、排序及处理逻辑
 */
public class NullHandlingTest extends AbstractOneApiTest {

    /**
     * 测试 IS NULL 查询
     */
    @Test
    public void testIsNull() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入包含 NULL 值的记录
        UserInfo u1 = new UserInfo();
        u1.setId(1001);
        u1.setName("UserWithEmail");
        u1.setAge(25);
        u1.setEmail("user@test.com");
        u1.setCreateTime(new Date());
        lambda.insert(UserInfo.class).applyEntity(u1).executeSumResult();

        UserInfo u2 = new UserInfo();
        u2.setId(1002);
        u2.setName("UserNoEmail");
        u2.setAge(30);
        u2.setEmail(null); // NULL email
        u2.setCreateTime(new Date());
        lambda.insert(UserInfo.class).applyEntity(u2).executeSumResult();

        UserInfo u3 = new UserInfo();
        u3.setId(1003);
        u3.setName("UserNoAge");
        u3.setAge(null); // NULL age
        u3.setEmail("noage@test.com");
        u3.setCreateTime(new Date());
        lambda.insert(UserInfo.class).applyEntity(u3).executeSumResult();

        // 查询 email IS NULL 的记录
        List<UserInfo> usersWithoutEmail = lambda.query(UserInfo.class)//
                .isNull(UserInfo::getEmail)//
                .queryForList();

        assertNotNull(usersWithoutEmail);
        assertEquals("Should find exactly 1 user with NULL email", 1, usersWithoutEmail.size());
        assertEquals("UserNoEmail", usersWithoutEmail.get(0).getName());

        // 查询 age IS NULL 的记录
        long countNoAge = lambda.query(UserInfo.class)//
                .isNull(UserInfo::getAge)//
                .queryForCount();

        assertEquals("Should find 1 user with NULL age", 1, countNoAge);
    }

    /**
     * 测试 IS NOT NULL 查询
     */
    @Test
    public void testIsNotNull() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入测试数据
        UserInfo u1 = new UserInfo();
        u1.setId(2001);
        u1.setName("User1");
        u1.setAge(25);
        u1.setEmail("user1@test.com");
        u1.setCreateTime(new Date());
        lambda.insert(UserInfo.class).applyEntity(u1).executeSumResult();

        UserInfo u2 = new UserInfo();
        u2.setId(2002);
        u2.setName("User2");
        u2.setAge(null); // NULL age
        u2.setEmail("user2@test.com");
        u2.setCreateTime(new Date());
        lambda.insert(UserInfo.class)//
                .applyEntity(u2)//
                .executeSumResult();

        // 查询 age IS NOT NULL 的记录
        List<UserInfo> usersWithAge = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "User%")//
                .isNotNull(UserInfo::getAge)//
                .queryForList();

        assertNotNull(usersWithAge);
        assertEquals("Should find 1 user with non-NULL age", 1, usersWithAge.size());
        assertEquals("User1", usersWithAge.get(0).getName());
    }

    /**
     * 测试 NULL 值在 UPDATE 中的处理
     */
    @Test
    public void testUpdateToNull() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入记录
        UserInfo u = new UserInfo();
        u.setId(3001);
        u.setName("UpdateTest");
        u.setAge(30);
        u.setEmail("update@test.com");
        u.setCreateTime(new Date());
        lambda.insert(UserInfo.class).applyEntity(u).executeSumResult();

        // 将 email 更新为 NULL
        int updated = lambda.update(UserInfo.class)//
                .eq(UserInfo::getId, 3001)//
                .updateTo(UserInfo::getEmail, null)//
                .doUpdate();

        assertEquals(1, updated);

        // 验证更新结果
        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 3001)//
                .queryForObject();

        assertNotNull(loaded);
        assertNull("Email should be NULL after update", loaded.getEmail());
        assertEquals("Name should remain unchanged", "UpdateTest", loaded.getName());
    }

    /**
     * 测试 NULL 值与条件组合
     */
    @Test
    public void testNullWithComplexConditions() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入测试数据
        for (int i = 1; i <= 5; i++) {
            UserInfo u = new UserInfo();
            u.setId(4000 + i);
            u.setName("Complex" + i);
            u.setAge(i % 2 == 0 ? 20 + i : null); // 偶数有 age，奇数 age 为 NULL
            u.setEmail("complex" + i + "@test.com");
            u.setCreateTime(new Date());
            lambda.insert(UserInfo.class).applyEntity(u).executeSumResult();
        }

        // 查询: age IS NULL AND name LIKE 'Complex%'
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Complex%")//
                .isNull(UserInfo::getAge)//
                .queryForCount();

        assertEquals("Should find 3 users with NULL age", 3, count);

        // 查询: age IS NOT NULL OR email IS NULL
        // 注意: or() 内仅包含自己的条件，like() 是外层 AND 条件
        long count2 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Complex%")//
                .or(q -> q.isNotNull(UserInfo::getAge)//
                        .isNull(UserInfo::getEmail))//
                .queryForCount();

        assertTrue("Should find users matching (age IS NOT NULL OR email IS NULL)", count2 >= 2);
    }

    /**
     * 测试 NULL 值在 DELETE 中的条件
     */
    @Test
    public void testDeleteWithNull() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入测试数据
        UserInfo u1 = new UserInfo();
        u1.setId(5001);
        u1.setName("DeleteTest1");
        u1.setAge(null);
        u1.setEmail("delete1@test.com");
        u1.setCreateTime(new Date());
        lambda.insert(UserInfo.class).applyEntity(u1).executeSumResult();

        UserInfo u2 = new UserInfo();
        u2.setId(5002);
        u2.setName("DeleteTest2");
        u2.setAge(25);
        u2.setEmail("delete2@test.com");
        u2.setCreateTime(new Date());
        lambda.insert(UserInfo.class)//
                .applyEntity(u2)//
                .executeSumResult();

        // 删除 age IS NULL 的记录
        int deleted = lambda.delete(UserInfo.class)//
                .like(UserInfo::getName, "DeleteTest%")//
                .isNull(UserInfo::getAge)//
                .doDelete();

        assertEquals(1, deleted);

        // 验证删除结果
        long remaining = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DeleteTest%")//
                .queryForCount();

        assertEquals("Should have 1 user remaining", 1, remaining);
    }
}
