package net.hasor.dbvisitor.test.suite.fluent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Batch Update and Delete Test
 * 验证批量更新和批量删除操作
 */
public class BatchUpdateDeleteTest extends AbstractOneApiTest {

    /**
     * 测试批量更新（基于条件）
     * SQL: UPDATE user_info SET age = 25 WHERE age >= 20 AND age <= 30
     */
    @Test
    public void testBatchUpdateByCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入测试数据
        for (int i = 1; i <= 20; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(16000 + i, "Batch" + i, 15 + i))//
                    .executeSumResult();
        }

        // 批量更新: age >= 20 AND age <= 30 的记录，age 加 5
        int updated = lambda.update(UserInfo.class)//
                .like(UserInfo::getName, "Batch%")//
                .ge(UserInfo::getAge, 20)//
                .le(UserInfo::getAge, 30)//
                .updateTo(UserInfo::getAge, 25) // 统一设置为 25
                .doUpdate();

        assertTrue("Should update multiple records", updated >= 5);

        // 验证更新结果
        long count25 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Batch%")//
                .eq(UserInfo::getAge, 25)//
                .queryForCount();

        assertTrue("Should have multiple records with age=25", count25 >= 5);
    }

    /**
     * 测试批量更新多个字段
     * SQL: UPDATE user_info SET age = ?, email = ? WHERE id IN (...)
     */
    @Test
    public void testBatchUpdateMultipleFields() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入数据
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(16101, "Multi1", 20))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(16102, "Multi2", 25))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(16103, "Multi3", 30))//
                .executeSumResult();

        // 批量更新多字段
        int updated = lambda.update(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(16101, 16102, 16103))//
                .updateTo(UserInfo::getAge, 99)//
                .updateTo(UserInfo::getEmail, "batch@updated.com")//
                .doUpdate();

        assertEquals(3, updated);

        // 验证更新
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(16101, 16102, 16103))//
                .queryForList();

        for (UserInfo user : result) {
            assertEquals(Integer.valueOf(99), user.getAge());
            assertEquals("batch@updated.com", user.getEmail());
        }
    }

    /**
     * 测试批量更新（基于实体列表）
     * 注意: LambdaTemplate 可能不直接支持批量实体更新，这里验证通过循环更新
     */
    @Test
    public void testBatchUpdateByEntityList() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入数据
        List<UserInfo> users = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            UserInfo u = createUser(16200 + i, "Entity" + i, 20 + i);
            lambda.insert(UserInfo.class)//
                    .applyEntity(u)//
                    .executeSumResult();
            users.add(u);
        }

        // 修改实体属性
        for (UserInfo user : users) {
            user.setAge(user.getAge() + 10);
            user.setEmail("entity.updated@test.com");
        }

        // 批量更新（通过循环）
        int totalUpdated = 0;
        for (UserInfo user : users) {
            int updated = lambda.update(UserInfo.class)//
                    .eq(UserInfo::getId, user.getId())//
                    .updateTo(UserInfo::getAge, user.getAge())//
                    .updateTo(UserInfo::getEmail, user.getEmail())//
                    .doUpdate();
            totalUpdated += updated;
        }

        assertEquals(5, totalUpdated);

        // 验证
        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 16201)//
                .queryForObject();

        assertEquals(Integer.valueOf(31), loaded.getAge()); // 21 + 10
        assertEquals("entity.updated@test.com", loaded.getEmail());
    }

    /**
     * 测试批量删除（基于条件）
     * SQL: DELETE FROM user_info WHERE age < 18
     */
    @Test
    public void testBatchDeleteByCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入数据（部分 age < 18）
        for (int i = 1; i <= 10; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(16300 + i, "Delete" + i, 15 + i))//
                    .executeSumResult();
        }

        // 批量删除 age < 18 的记录
        int deleted = lambda.delete(UserInfo.class)//
                .like(UserInfo::getName, "Delete%")//
                .lt(UserInfo::getAge, 18)//
                .doDelete();

        assertTrue("Should delete multiple records", deleted >= 2);

        // 验证删除结果
        long remaining = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Delete%")//
                .queryForCount();

        assertTrue("Should have remaining records", remaining >= 8);
    }

    /**
     * 测试批量删除（基于 IN 条件）
     * SQL: DELETE FROM user_info WHERE id IN (?,?,?)
     */
    @Test
    public void testBatchDeleteByIdList() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入数据
        for (int i = 1; i <= 10; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(16400 + i, "DelIn" + i, 25))//
                    .executeSumResult();
        }

        // 批量删除指定 ID
        List<Integer> idsToDelete = java.util.Arrays.asList(16401, 16403, 16405, 16407);
        int deleted = lambda.delete(UserInfo.class)//
                .in(UserInfo::getId, idsToDelete)//
                .doDelete();

        assertEquals(4, deleted);

        // 验证剩余记录
        long remaining = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DelIn%")//
                .queryForCount();

        assertEquals(6, remaining);
    }

    /**
     * 测试批量删除全部（无条件）
     * 注意: 通常需要特殊标志允许无条件删除，这里测试带条件的"批量删除全部匹配"
     */
    @Test
    public void testBatchDeleteAllMatching() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入测试数据
        for (int i = 1; i <= 20; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(16500 + i, "DelAll" + i, 30))//
                    .executeSumResult();
        }

        // 删除所有匹配 name LIKE 'DelAll%' 的记录
        int deleted = lambda.delete(UserInfo.class)//
                .like(UserInfo::getName, "DelAll%")//
                .doDelete();

        assertEquals(20, deleted);

        // 验证全部删除
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DelAll%")//
                .queryForCount();

        assertEquals(0, count);
    }

    /**
     * 测试批量删除与分页的组合
     * 场景: 分批删除大量数据（避免一次性删除过多导致锁表）
     */
    @Test
    public void testBatchDeleteInChunks() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入 50 条数据
        for (int i = 1; i <= 50; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(16600 + i, "Chunk" + i, 25))//
                    .executeSumResult();
        }

        // 分批删除（每次删除 10 条）
        int totalDeleted = 0;
        while (true) {
            // 查询前 10 条的 ID
            List<UserInfo> batch = lambda.query(UserInfo.class)//
                    .like(UserInfo::getName, "Chunk%")//
                    .orderBy("id")//
                    .initPage(10, 0)//
                    .queryForList();

            if (batch.isEmpty()) {
                break;
            }

            // 提取 ID 并删除
            List<Integer> ids = new ArrayList<>();
            for (UserInfo user : batch) {
                ids.add(user.getId());
            }

            int deleted = lambda.delete(UserInfo.class)//
                    .in(UserInfo::getId, ids)//
                    .doDelete();

            totalDeleted += deleted;

            // 安全检查：避免无限循环
            if (totalDeleted >= 50) {
                break;
            }
        }

        assertEquals(50, totalDeleted);

        // 验证全部删除
        long remaining = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Chunk%")//
                .queryForCount();

        assertEquals(0, remaining);
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@batch.com");
        u.setCreateTime(new Date());
        return u;
    }
}
