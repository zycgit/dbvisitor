package net.hasor.dbvisitor.test.suite.fluent.query;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Dynamic Condition Test
 * 验证动态条件构建（条件激活/跳过）
 */
public class DynamicConditionTest extends AbstractOneApiTest {

    /**
     * 测试基础动态条件（test 参数控制）
     * SQL: SELECT * FROM user_info WHERE name LIKE ? (仅当 test=true 时)
     */
    @Test
    public void testBasicDynamicCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(20501, "Dynamic1", 25))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(20502, "Dynamic2", 30))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(20503, "Other", 35))//
                .executeSumResult();

        // 场景1: test=true，条件生效
        List<UserInfo> result1 = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(20501, 20502, 20503))//
                .like(true, UserInfo::getName, "Dynamic")//
                .queryForList();

        assertEquals("Condition should apply when test=true", 2, result1.size());

        // 场景2: test=false，条件跳过
        List<UserInfo> result2 = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(20501, 20502, 20503))//
                .like(false, UserInfo::getName, "Dynamic")//
                .queryForList();

        assertEquals("Condition should be skipped when test=false", 3, result2.size());
    }

    /**
     * 测试多个动态条件组合
     * SQL: WHERE (age > 20 if ageFilter) AND (name LIKE ? if nameFilter)
     */
    @Test
    public void testMultipleDynamicConditions() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(20601, "User1", 20))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(20602, "User2", 25))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(20603, "Admin", 30))//
                .executeSumResult();

        // 场景1: 两个条件都激活
        boolean ageFilter = true;
        boolean nameFilter = true;

        List<UserInfo> result1 = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(20601, 20602, 20603))//
                .gt(ageFilter, UserInfo::getAge, 20)//
                .like(nameFilter, UserInfo::getName, "User")//
                .queryForList();

        assertEquals("Both conditions active: age>20 AND name like User", 1, result1.size());
        assertEquals("User2", result1.get(0)//
                .getName());

        // 场景2: 只激活年龄过滤
        ageFilter = true;
        nameFilter = false;

        List<UserInfo> result2 = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(20601, 20602, 20603))//
                .gt(ageFilter, UserInfo::getAge, 20)//
                .like(nameFilter, UserInfo::getName, "User")//
                .queryForList();

        assertEquals("Only age filter active", 2, result2.size());

        // 场景3: 都不激活
        ageFilter = false;
        nameFilter = false;

        List<UserInfo> result3 = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(20601, 20602, 20603))//
                .gt(ageFilter, UserInfo::getAge, 20)//
                .like(nameFilter, UserInfo::getName, "User")//
                .queryForList();

        assertEquals("No filters active", 3, result3.size());
    }

    /**
     * 测试动态条件与 NULL 值
     * 场景: 当搜索值为 NULL 时，跳过该条件
     */
    @Test
    public void testDynamicConditionWithNullValue() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(20701, "Search1", 25))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(20702, "Search2", 30))//
                .executeSumResult();

        // 模拟搜索场景
        String nameFilter = null;
        Integer ageFilter = 25;

        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Search")//
                .eq(nameFilter != null, UserInfo::getName, nameFilter) // 跳过 NULL 条件
                .eq(ageFilter != null, UserInfo::getAge, ageFilter)    // 激活非 NULL 条件
                .queryForList();

        assertEquals("Should apply only non-NULL conditions", 1, result.size());
        assertEquals("Search1", result.get(0)//
                .getName());
    }

    /**
     * 测试动态条件与空字符串
     * 场景: 区分空字符串和 NULL
     */
    @Test
    public void testDynamicConditionWithEmptyString() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(20801, "Empty1", 25))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(20802, "Empty2", 30))//
                .executeSumResult();

        // 空字符串应跳过 LIKE 条件
        String emptyFilter = "";
        String validFilter = "Empty";

        List<UserInfo> result1 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Empty")//
                .like(emptyFilter != null && !emptyFilter.isEmpty(), UserInfo::getName, emptyFilter)//
                .queryForList();

        assertEquals("Empty string should skip condition", 2, result1.size());

        // 非空字符串应激活条件
        List<UserInfo> result2 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Empty")//
                .like(validFilter != null && !validFilter.isEmpty(), UserInfo::getName, validFilter)//
                .queryForList();

        assertEquals("Valid filter should apply", 2, result2.size());
    }

    /**
     * 测试动态分页条件
     * 场景: 仅当 enablePaging=true 时应用分页
     */
    @Test
    public void testDynamicPagingCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入 20 条数据
        for (int i = 1; i <= 20; i++) {
            lambda.insert(UserInfo.class)//
                    .applyEntity(createUser(20900 + i, "Page" + i, 25))//
                    .executeSumResult();
        }

        boolean enablePaging = true;
        int pageSize = 5;
        int pageNumber = 0;

        EntityQuery<UserInfo> query = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Page%")//
                .orderBy("id");

        // 动态应用分页
        if (enablePaging) {
            query.initPage(pageSize, pageNumber);
        }

        List<UserInfo> result = query.queryForList();

        if (enablePaging) {
            assertEquals("Should return paged results", 5, result.size());
        } else {
            assertEquals("Should return all results", 20, result.size());
        }
    }

    /**
     * 测试动态排序条件
     * 场景: 根据用户选择动态排序
     */
    @Test
    public void testDynamicSortCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(21001, "Sort1", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21002, "Sort2", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21003, "Sort3", 25)).executeSumResult();

        // 场景1: 按年龄升序
        String sortBy = "age";
        boolean ascending = true;

        EntityQuery<UserInfo> query1 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Sort%");

        if ("age".equals(sortBy)) {
            if (ascending) {
                query1.asc("age");
            } else {
                query1.desc("age");
            }
        }

        List<UserInfo> result1 = query1.queryForList();
        assertEquals(Integer.valueOf(20), result1.get(0).getAge());
        assertEquals(Integer.valueOf(30), result1.get(2).getAge());

        // 场景2: 按年龄降序
        ascending = false;

        EntityQuery<UserInfo> query2 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "Sort%");

        if ("age".equals(sortBy)) {
            if (ascending) {
                query2.asc("age");
            } else {
                query2.desc("age");
            }
        }

        List<UserInfo> result2 = query2.queryForList();
        assertEquals(Integer.valueOf(30), result2.get(0).getAge());
        assertEquals(Integer.valueOf(20), result2.get(2).getAge());
    }

    /**
     * 测试动态 IN 条件
     * 场景: 当集合为空时跳过 IN 条件
     */
    @Test
    public void testDynamicInCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(21101, "In1", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21102, "In2", 30)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21103, "In3", 35)).executeSumResult();

        // 场景1: 空集合，跳过 IN 条件
        List<Integer> ageList = java.util.Collections.emptyList();

        List<UserInfo> result1 = lambda.query(UserInfo.class)     //
                .like(UserInfo::getName, "In%")             //
                .in(!ageList.isEmpty(), UserInfo::getAge, ageList)//
                .queryForList();

        assertEquals("Empty IN list should be skipped", 3, result1.size());

        // 场景2: 非空集合，应用 IN 条件
        ageList = java.util.Arrays.asList(25, 30);

        List<UserInfo> result2 = lambda.query(UserInfo.class)     //
                .like(UserInfo::getName, "In%")             //
                .in(!ageList.isEmpty(), UserInfo::getAge, ageList)//
                .queryForList();

        assertEquals("Non-empty IN list should apply", 2, result2.size());
    }

    /**
     * 测试动态 UPDATE 条件
     * 场景: 根据条件选择性更新字段
     */
    @Test
    public void testDynamicUpdateCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class).applyEntity(createUser(21201, "Update1", 25))//
                .executeSumResult();

        // 动态更新参数
        boolean updateAge = true;
        boolean updateEmail = false;
        Integer newAge = 30;
        String newEmail = "new@email.com";

        int updated = lambda.update(UserInfo.class)//
                .eq(UserInfo::getId, 21201)//
                .updateTo(updateAge, UserInfo::getAge, newAge)//
                .updateTo(updateEmail, UserInfo::getEmail, newEmail)//
                .doUpdate();

        assertEquals(1, updated);

        // 验证: 只有 age 被更新
        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 21201)//
                .queryForObject();

        assertEquals(Integer.valueOf(30), loaded.getAge());
        assertEquals("update1@dynamic.com", loaded.getEmail()); // 未变
    }

    /**
     * 测试动态嵌套条件
     * 场景: 根据条件决定是否添加嵌套逻辑
     */
    @Test
    public void testDynamicNestedCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(21301, "Nested1", 20))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(21302, "Nested2", 25))//
                .executeSumResult();
        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(21303, "Other", 30))//
                .executeSumResult();

        // 场景1: 激活嵌套条件
        boolean enableAdvancedFilter = true;

        EntityQuery<UserInfo> query1 = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(21301, 21302, 21303));

        if (enableAdvancedFilter) {
            query1.nested(q -> q.like(UserInfo::getName, "Nested")//
                    .gt(UserInfo::getAge, 20));
        }

        List<UserInfo> result1 = query1.queryForList();
        assertEquals("Advanced filter active", 1, result1.size());
        assertEquals("Nested2", result1.get(0)//
                .getName());

        // 场景2: 跳过嵌套条件
        enableAdvancedFilter = false;

        EntityQuery<UserInfo> query2 = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(21301, 21302, 21303));

        if (enableAdvancedFilter) {
            query2.nested(q -> q.like(UserInfo::getName, "Nested")//
                    .gt(UserInfo::getAge, 20));
        }

        List<UserInfo> result2 = query2.queryForList();
        assertEquals("Advanced filter skipped", 3, result2.size());
    }

    // ==================== 补充：缺失的动态条件方法 ====================

    /**
     * 测试 notLike 动态版本 (boolean test)
     */
    @Test
    public void testDynamicNotLikeCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(UserInfo.class).applyEntity(createUser(21401, "DynNL-Alice", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21402, "DynNL-Bob", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21403, "DynNL-Charlie", 30)).executeSumResult();

        // test=true => notLike 生效, 排除 Bob
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynNL-%")//
                .notLike(true, UserInfo::getName, "Bob")//
                .queryForCount();
        assertEquals(2, count);

        // test=false => notLike 跳过
        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynNL-%")//
                .notLike(false, UserInfo::getName, "Bob")//
                .queryForCount();
        assertEquals(3, count);
    }

    /**
     * 测试 likeRight 动态版本 (boolean test)
     */
    @Test
    public void testDynamicLikeRightCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(UserInfo.class).applyEntity(createUser(21411, "DynLR-Alice", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21412, "DynLR-Bob", 25)).executeSumResult();

        // test=true => likeRight 生效
        long count = lambda.query(UserInfo.class)//
                .likeRight(true, UserInfo::getName, "DynLR-A")//
                .queryForCount();
        assertEquals(1, count);

        // test=false => likeRight 跳过
        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynLR-%")//
                .likeRight(false, UserInfo::getName, "DynLR-A")//
                .queryForCount();
        assertEquals(2, count);
    }

    /**
     * 测试 notLikeRight 动态版本 (boolean test)
     */
    @Test
    public void testDynamicNotLikeRightCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(UserInfo.class).applyEntity(createUser(21421, "DynNLR-Alice", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21422, "DynNLR-Bob", 25)).executeSumResult();

        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynNLR-%")//
                .notLikeRight(true, UserInfo::getName, "DynNLR-A")//
                .queryForCount();
        assertEquals(1, count); // Bob

        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynNLR-%")//
                .notLikeRight(false, UserInfo::getName, "DynNLR-A")//
                .queryForCount();
        assertEquals(2, count);
    }

    /**
     * 测试 likeLeft 动态版本 (boolean test)
     */
    @Test
    public void testDynamicLikeLeftCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(UserInfo.class).applyEntity(createUser(21431, "DynLL-Alice", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21432, "DynLL-Bob", 25)).executeSumResult();

        long count = lambda.query(UserInfo.class)//
                .likeLeft(true, UserInfo::getName, "Alice")//
                .queryForCount();
        assertEquals(1, count);

        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynLL-%")//
                .likeLeft(false, UserInfo::getName, "Alice")//
                .queryForCount();
        assertEquals(2, count);
    }

    /**
     * 测试 notLikeLeft 动态版本 (boolean test)
     */
    @Test
    public void testDynamicNotLikeLeftCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(UserInfo.class).applyEntity(createUser(21441, "DynNLL-Alice", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21442, "DynNLL-Bob", 25)).executeSumResult();

        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynNLL-%")//
                .notLikeLeft(true, UserInfo::getName, "Alice")//
                .queryForCount();
        assertEquals(1, count); // Bob

        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynNLL-%")//
                .notLikeLeft(false, UserInfo::getName, "Alice")//
                .queryForCount();
        assertEquals(2, count);
    }

    /**
     * 测试 notIn 动态版本 (boolean test)
     */
    @Test
    public void testDynamicNotInCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(UserInfo.class).applyEntity(createUser(21451, "DynNI1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21452, "DynNI2", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21453, "DynNI3", 30)).executeSumResult();

        // test=true => notIn 生效
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynNI%")//
                .notIn(true, UserInfo::getAge, java.util.Arrays.asList(20, 30))//
                .queryForCount();
        assertEquals(1, count); // 25

        // test=false => notIn 跳过
        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynNI%")//
                .notIn(false, UserInfo::getAge, java.util.Arrays.asList(20, 30))//
                .queryForCount();
        assertEquals(3, count);
    }

    /**
     * 测试 isNull 动态版本 (boolean test)
     */
    @Test
    public void testDynamicIsNullCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        UserInfo u1 = createUser(21461, "DynNull1", 20);
        u1.setEmail(null);
        lambda.insert(UserInfo.class).applyEntity(u1).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21462, "DynNull2", 25)).executeSumResult();

        // test=true => isNull 生效
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynNull%")//
                .isNull(true, UserInfo::getEmail)//
                .queryForCount();
        assertEquals(1, count);

        // test=false => isNull 跳过
        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynNull%")//
                .isNull(false, UserInfo::getEmail)//
                .queryForCount();
        assertEquals(2, count);
    }

    /**
     * 测试 isNotNull 动态版本 (boolean test)
     */
    @Test
    public void testDynamicIsNotNullCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        UserInfo u1 = createUser(21471, "DynNotNull1", 20);
        u1.setEmail(null);
        lambda.insert(UserInfo.class).applyEntity(u1).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21472, "DynNotNull2", 25)).executeSumResult();

        // test=true => isNotNull 生效
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynNotNull%")//
                .isNotNull(true, UserInfo::getEmail)//
                .queryForCount();
        assertEquals(1, count);

        // test=false => isNotNull 跳过
        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynNotNull%")//
                .isNotNull(false, UserInfo::getEmail)//
                .queryForCount();
        assertEquals(2, count);
    }

    /**
     * 测试 le 动态版本 (boolean test)
     */
    @Test
    public void testDynamicLeCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(UserInfo.class).applyEntity(createUser(21481, "DynLE1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21482, "DynLE2", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21483, "DynLE3", 30)).executeSumResult();

        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynLE%")//
                .le(true, UserInfo::getAge, 25)//
                .queryForCount();
        assertEquals(2, count); // 20, 25

        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynLE%")//
                .le(false, UserInfo::getAge, 25)//
                .queryForCount();
        assertEquals(3, count);
    }

    /**
     * 测试 lt 动态版本 (boolean test)
     */
    @Test
    public void testDynamicLtCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(UserInfo.class).applyEntity(createUser(21491, "DynLT1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21492, "DynLT2", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21493, "DynLT3", 30)).executeSumResult();

        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynLT%")//
                .lt(true, UserInfo::getAge, 25)//
                .queryForCount();
        assertEquals(1, count); // 20

        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynLT%")//
                .lt(false, UserInfo::getAge, 25)//
                .queryForCount();
        assertEquals(3, count);
    }

    /**
     * 测试 ne 动态版本 (boolean test)
     */
    @Test
    public void testDynamicNeCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(UserInfo.class).applyEntity(createUser(21501, "DynNE1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21502, "DynNE2", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21503, "DynNE3", 30)).executeSumResult();

        // test=true => ne 生效
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynNE%")//
                .ne(true, UserInfo::getAge, 25)//
                .queryForCount();
        assertEquals(2, count); // 20,30

        // test=false => ne 跳过
        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynNE%")//
                .ne(false, UserInfo::getAge, 25)//
                .queryForCount();
        assertEquals(3, count);
    }

    /**
     * 测试 ge 动态版本 (boolean test)
     */
    @Test
    public void testDynamicGeCondition() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        lambda.insert(UserInfo.class).applyEntity(createUser(21511, "DynGE1", 20)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21512, "DynGE2", 25)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(21513, "DynGE3", 30)).executeSumResult();

        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynGE%")//
                .ge(true, UserInfo::getAge, 25)//
                .queryForCount();
        assertEquals(2, count); // 25, 30

        count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "DynGE%")//
                .ge(false, UserInfo::getAge, 25)//
                .queryForCount();
        assertEquals(3, count);
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@dynamic.com");
        u.setCreateTime(new Date());
        return u;
    }
}
