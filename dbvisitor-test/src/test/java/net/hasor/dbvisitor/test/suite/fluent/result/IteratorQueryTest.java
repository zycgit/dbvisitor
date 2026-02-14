package net.hasor.dbvisitor.test.suite.fluent.result;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Iterator Query Test
 * 验证迭代器查询功能（大数据集分批迭代）
 */
public class IteratorQueryTest extends AbstractOneApiTest {

    /**
     * 测试基础迭代器查询 (iteratorForLimit)
     * 验证分批获取指定数量的记录
     */
    @Test
    public void testBasicIteratorForLimit() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入 100 条数据
        for (int i = 1; i <= 100; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(13000 + i, "Iter" + i, 20 + (i % 10)))//
                    .executeSumResult();
        }

        // 使用迭代器获取前 50 条（每批 10 条）
        Iterator<UserInfo> iterator = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Iter%")//
                .orderBy("id")//
                .iteratorForLimit(50, 10);

        int count = 0;
        List<Integer> ids = new ArrayList<>();

        while (iterator.hasNext()) {
            UserInfo user = iterator.next();
            assertNotNull(user);
            ids.add(user.getId());
            count++;
        }

        assertEquals("Should iterate exactly 50 records", 50, count);
        assertTrue("IDs should be sequential", ids.get(0) < ids.get(49));
    }

    /**
     * 测试迭代所有记录 (limit = -1)
     */
    @Test
    public void testIteratorForAll() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入 30 条数据
        for (int i = 1; i <= 30; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(13100 + i, "All" + i, 25))//
                    .executeSumResult();
        }

        // 迭代所有记录（limit = -1 表示全部）
        Iterator<UserInfo> iterator = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "All%")//
                .orderBy("id")//
                .iteratorForLimit(-1, 10); // 每批 10 条

        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }

        assertEquals("Should iterate all 30 records", 30, count);
    }

    /**
     * 测试小批次迭代 (batchSize = 5)
     */
    @Test
    public void testSmallBatchIterator() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入 20 条数据
        for (int i = 1; i <= 20; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(13200 + i, "Small" + i, 30))//
                    .executeSumResult();
        }

        // 小批次迭代（每批 5 条）
        Iterator<UserInfo> iterator = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Small%")//
                .orderBy("id")//
                .iteratorByBatch(5);

        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }

        assertEquals("Should iterate all 20 records", 20, count);
    }

    /**
     * 测试迭代器中途中断
     * 验证资源是否正确释放
     */
    @Test
    public void testIteratorEarlyBreak() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入 50 条数据
        for (int i = 1; i <= 50; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(13300 + i, "Break" + i, 25))//
                    .executeSumResult();
        }

        // 迭代到第 10 条就中断
        Iterator<UserInfo> iterator = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Break%")//
                .orderBy("id")//
                .iteratorForLimit(-1, 10);

        int count = 0;
        while (iterator.hasNext() && count < 10) {
            iterator.next();
            count++;
        }

        assertEquals("Should stop at 10", 10, count);

        // 验证迭代器仍然可用（hasNext 应返回 true）
        assertTrue("Iterator should still have more elements", iterator.hasNext());
    }

    /**
     * 测试迭代器与条件过滤组合
     */
    @Test
    public void testIteratorWithConditions() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入数据（部分匹配条件）
        for (int i = 1; i <= 50; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(13400 + i, "Cond" + i, 20 + (i % 5)))//
                    .executeSumResult();
        }

        // 迭代器 + WHERE 条件: age > 22
        Iterator<UserInfo> iterator = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Cond%")//
                .gt(UserInfo::getAge, 22)//
                .orderBy("id")//
                .iteratorForLimit(-1, 10);

        int count = 0;
        while (iterator.hasNext()) {
            UserInfo user = iterator.next();
            assertTrue("Age should be > 22", user.getAge() > 22);
            count++;
        }

        assertTrue("Should find multiple matching records", count > 10);
    }

    /**
     * 测试迭代器与转换函数 (Transform)
     */
    @Test
    public void testIteratorWithTransform() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入数据
        for (int i = 1; i <= 20; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(13500 + i, "Trans" + i, 30))//
                    .executeSumResult();
        }

        // 迭代器 + 转换（只获取 name）
        Iterator<String> iterator = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Trans%")//
                .orderBy("id")//
                .iteratorForLimit(-1, 10, user -> user.getName()); // Transform to String

        int count = 0;
        while (iterator.hasNext()) {
            String name = iterator.next();
            assertNotNull(name);
            assertTrue("Name should start with Trans", name.startsWith("Trans"));
            count++;
        }

        assertEquals("Should transform all 20 records", 20, count);
    }

    /**
     * 测试空结果迭代器
     */
    @Test
    public void testEmptyIterator() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 查询不存在的数据
        Iterator<UserInfo> iterator = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "NonExistent%")//
                .iteratorForLimit(-1, 10);

        assertFalse("Empty iterator should have no elements", iterator.hasNext());

        // 验证调用 next() 时的行为
        try {
            iterator.next();
            fail("Should throw exception when calling next() on empty iterator");
        } catch (Exception e) {
            // 预期抛出 NoSuchElementException 或类似异常
            assertTrue("Should throw NoSuchElement or similar exception", e.getClass()//
                    .getName()//
                    .contains("NoSuchElement") || e.getMessage()//
                    .toLowerCase()//
                    .contains("no") || e.getMessage()//
                    .toLowerCase()//
                    .contains("element"));
        }
    }

    /**
     * 测试大批次迭代（性能验证）
     */
    @Test
    public void testLargeBatchIterator() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入 500 条数据
        for (int i = 1; i <= 500; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(14000 + i, "Large" + i, 25))//
                    .executeSumResult();
        }

        // 大批次迭代（每批 100 条）
        long startTime = System.currentTimeMillis();

        Iterator<UserInfo> iterator = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Large%")//
                .orderBy("id")//
                .iteratorByBatch(100);

        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }

        long duration = System.currentTimeMillis() - startTime;

        assertEquals("Should iterate all 500 records", 500, count);
        assertTrue("Should complete in reasonable time", duration < 10000); // < 10 seconds
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@iterator.com");
        u.setCreateTime(new Date());
        return u;
    }
}
