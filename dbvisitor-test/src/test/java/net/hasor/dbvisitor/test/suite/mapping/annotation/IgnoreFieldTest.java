package net.hasor.dbvisitor.test.suite.mapping.annotation;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import net.hasor.dbvisitor.test.model.annotation.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @Ignore 注解功能测试。
 * 验证 @Ignore 在 INSERT/SELECT/UPDATE 中正确排除字段，
 * 以及与 @Column、autoMapping=false 的交互行为。
 */
public class IgnoreFieldTest extends AbstractOneApiTest {

    // ============ Helper ============

    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@ignore.test");
        u.setCreateTime(new Date());
        return u;
    }

    // ============ Tests ============

    /**
     * @Ignore 字段在 INSERT 时被排除。
     * 即使 Java 对象设置了 email，INSERT 语句也不包含该列 → 数据库存储 NULL。
     */
    @Test
    public void testIgnoreAnnotation_InsertExclusion() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        IgnoredEmailUser user = new IgnoredEmailUser();
        user.setId(33401);
        user.setName("IgnoreInsert");
        user.setAge(25);
        user.setEmail("should_be_ignored@test.com");
        user.setCreateTime(new Date());
        lambda.insert(IgnoredEmailUser.class).applyEntity(user).executeSumResult();

        // 通过原始 UserInfo 验证 email 为 NULL（INSERT 时被排除）
        UserInfo raw = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 33401)//
                .queryForObject();

        assertNotNull(raw);
        assertEquals("IgnoreInsert", raw.getName());
        assertNull("Email should be NULL because @Ignore excludes it from INSERT", raw.getEmail());
    }

    /**
     * @Ignore 字段在 SELECT 时被排除：
     * 1. 结果中 @Ignore 字段不被填充；
     * 2. 通过 eqBySample 传入包含 email 的样本对象查询时，
     * 框架主动忽略 @Ignore 的 email，不将其作为 WHERE 条件。
     */
    @Test
    public void testIgnoreAnnotation_SelectExclusion() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 用原始 UserInfo 插入两条数据
        UserInfo rawUser1 = createUser(33501, "IgnoreSelect", 25);
        lambda.insert(UserInfo.class).applyEntity(rawUser1).executeSumResult();

        UserInfo rawUser2 = createUser(33502, "IgnoreSelect2", 30);
        rawUser2.setEmail("other@ignore.test");
        lambda.insert(UserInfo.class).applyEntity(rawUser2).executeSumResult();

        // 1. SELECT 结果中 @Ignore 字段不被填充
        IgnoredEmailUser result = lambda.query(IgnoredEmailUser.class)//
                .eq(IgnoredEmailUser::getId, 33501)//
                .queryForObject();

        assertNotNull(result);
        assertEquals("IgnoreSelect", result.getName());
        assertEquals(Integer.valueOf(25), result.getAge());
        assertNull("Email should be null because @Ignore excludes it from SELECT columns", result.getEmail());

        // 2. 通过样本对象查询：email 有值但 @Ignore 使其不参与 WHERE 条件
        IgnoredEmailUser sample = new IgnoredEmailUser();
        sample.setName("IgnoreSelect");
        sample.setEmail("wrong_email@nowhere.com"); // 故意设错误的 email

        IgnoredEmailUser bySample = lambda.query(IgnoredEmailUser.class)//
                .eqBySample(sample)//
                .queryForObject();

        // 如果 email 参与了 WHERE，则查不到（email 值不匹配）；
        // @Ignore 生效 → email 被忽略 → 仅用 name 查询 → 能查到
        assertNotNull("Should find record: @Ignore email not used as WHERE condition", bySample);
        assertEquals(Integer.valueOf(33501), bySample.getId());
    }

    /**
     * @Ignore 字段在 UPDATE 时被框架主动排除。
     * 通过 updateRow 传入包含 email 新值的完整实体对象，
     * 框架在处理时应主动跳过 @Ignore 的 email 字段，数据库中 email 保持原值。
     */
    @Test
    public void testIgnoreAnnotation_UpdateExclusion() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 先用原始 UserInfo 插入含 email 的数据
        UserInfo rawUser = createUser(33601, "IgnoreUpdate", 25);
        lambda.insert(UserInfo.class).applyEntity(rawUser).executeSumResult();

        // 构造包含 email 新值的 @Ignore 实体，通过 updateRow 整行更新
        IgnoredEmailUser updateEntity = new IgnoredEmailUser();
        updateEntity.setId(33601);
        updateEntity.setName("UpdatedName");
        updateEntity.setAge(99);
        updateEntity.setEmail("new_email@should_not_apply.com"); // @Ignore 字段设了新值
        updateEntity.setCreateTime(new Date());

        int updated = lambda.update(IgnoredEmailUser.class)//
                .eq(IgnoredEmailUser::getId, 33601)//
                .updateRow(updateEntity)//
                .doUpdate();
        assertEquals(1, updated);

        // 验证 name/age 被更新，但 email 保持原值（框架主动忽略了 @Ignore 字段）
        UserInfo afterUpdate = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 33601)//
                .queryForObject();

        assertEquals("UpdatedName", afterUpdate.getName());
        assertEquals(Integer.valueOf(99), afterUpdate.getAge());
        assertEquals("ignoreupdate@ignore.test", afterUpdate.getEmail()); // email 未被更新
    }

    /**
     * 多个 @Ignore 字段同时排除。
     * age 和 email 都标注 @Ignore，均不参与 INSERT。
     */
    @Test
    public void testIgnoreAnnotation_MultipleFields() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        MultipleIgnoreUser user = new MultipleIgnoreUser();
        user.setId(33701);
        user.setName("MultiIgnore");
        user.setAge(30);
        user.setEmail("multi@ignore.test");
        user.setCreateTime(new Date());
        lambda.insert(MultipleIgnoreUser.class).applyEntity(user).executeSumResult();

        // 验证 age 和 email 均为 NULL
        UserInfo raw = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 33701)//
                .queryForObject();

        assertNotNull(raw);
        assertEquals("MultiIgnore", raw.getName());
        assertNull("Age should be NULL due to @Ignore", raw.getAge());
        assertNull("Email should be NULL due to @Ignore", raw.getEmail());
    }

    /**
     * @Ignore + @Column 同时存在时，@Ignore 优先。
     * 即使标注了 @Column("email")，email 仍不参与映射。
     */
    @Test
    public void testIgnoreWithColumnAnnotation() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        IgnoreWithColumnUser user = new IgnoreWithColumnUser();
        user.setId(33801);
        user.setName("IgnoreColumn");
        user.setAge(26);
        user.setEmail("should_ignored@column.test");
        user.setCreateTime(new Date());
        lambda.insert(IgnoreWithColumnUser.class).applyEntity(user).executeSumResult();

        // @Ignore 优先于 @Column，email 不参与 INSERT
        UserInfo raw = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 33801)//
                .queryForObject();

        assertNotNull(raw);
        assertNull("@Ignore should take priority over @Column", raw.getEmail());
    }

    /**
     * autoMapping=false 时，只有标注 @Column 的字段参与映射。
     * age 和 email 无 @Column 注解，不参与 INSERT。
     */
    @Test
    public void testAutoMappingFalse_OnlyAnnotatedFields() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        ExplicitMappingUser user = new ExplicitMappingUser();
        user.setId(33901);
        user.setName("ExplicitMap");
        user.setAge(28);
        user.setEmail("explicit@mapping.test");
        user.setCreateTime(new Date());
        lambda.insert(ExplicitMappingUser.class).applyEntity(user).executeSumResult();

        // 验证: name (有 @Column) 被插入，age/email (无 @Column) 为 NULL
        UserInfo raw = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 33901)//
                .queryForObject();

        assertNotNull(raw);
        assertEquals("ExplicitMap", raw.getName());
        assertNull("Age should be NULL (no @Column, autoMapping=false)", raw.getAge());
        assertNull("Email should be NULL (no @Column, autoMapping=false)", raw.getEmail());
    }

    /**
     * @Ignore 标注在 getter 方法上，效果与标注在字段上相同。
     */
    @Test
    public void testIgnoreOnGetterMethod() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        IgnoreOnMethodUser user = new IgnoreOnMethodUser();
        user.setId(34001);
        user.setName("IgnoreOnMethod");
        user.setAge(27);
        user.setEmail("method_ignore@test.com");
        user.setCreateTime(new Date());
        lambda.insert(IgnoreOnMethodUser.class).applyEntity(user).executeSumResult();

        // @Ignore 在 getter 上，email 不参与 INSERT
        UserInfo raw = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 34001)//
                .queryForObject();

        assertNotNull(raw);
        assertNull("Email should be NULL (@Ignore on getter method)", raw.getEmail());
    }

    /**
     * @Ignore 完整生命周期: INSERT、SELECT、UPDATE 均排除。
     */
    @Test
    public void testIgnoreAnnotation_FullLifecycle() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 1. INSERT 时 email 被排除
        IgnoredEmailUser user = new IgnoredEmailUser();
        user.setId(34101);
        user.setName("Lifecycle");
        user.setAge(25);
        user.setEmail("lifecycle@test.com");
        user.setCreateTime(new Date());
        lambda.insert(IgnoredEmailUser.class).applyEntity(user).executeSumResult();

        // 1a. 反查验证 INSERT 确实排除了 email
        UserInfo afterInsert = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 34101)//
                .queryForObject();
        assertNotNull(afterInsert);
        assertEquals("Lifecycle", afterInsert.getName());
        assertNull("Email should be NULL after INSERT (@Ignore excluded it)", afterInsert.getEmail());

        // 2. 用原始 SQL 设置 email 值
        jdbcTemplate.executeUpdate("UPDATE user_info SET email = ? WHERE id = ?", new Object[] { "db_email@test.com", 34101 });

        // 3. SELECT 时 email 不被填充（@Ignore 从映射中完全移除）
        IgnoredEmailUser loaded = lambda.query(IgnoredEmailUser.class)//
                .eq(IgnoredEmailUser::getId, 34101)//
                .queryForObject();
        assertNotNull(loaded);
        assertEquals("Lifecycle", loaded.getName());
        assertNull("Email should not be populated on SELECT (@Ignore)", loaded.getEmail());

        // 4. SELECT WHERE 条件中 @Ignore 字段被忽略（用样本对象过滤）
        IgnoredEmailUser sample = new IgnoredEmailUser();
        sample.setName("Lifecycle");
        sample.setEmail("wrong_email@test.com"); // 故意设错 email
        IgnoredEmailUser byWhere = lambda.query(IgnoredEmailUser.class)//
                .eqBySample(sample)//
                .queryForObject();
        assertNotNull("@Ignore email should be excluded from WHERE, record still found", byWhere);
        assertEquals("Lifecycle", byWhere.getName());

        // 5. UPDATE 时 email 不被修改（通过实体整体更新）
        loaded.setName("Updated");
        loaded.setAge(99);
        loaded.setEmail("new_email@test.com"); // 设置新 email，但框架应忽略
        lambda.update(IgnoredEmailUser.class)//
                .eq(IgnoredEmailUser::getId, 34101)//
                .updateRow(loaded)//
                .doUpdate();

        UserInfo verify = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 34101)//
                .queryForObject();
        assertEquals("Updated", verify.getName());
        assertEquals(Integer.valueOf(99), verify.getAge());
        assertEquals("db_email@test.com", verify.getEmail()); // email 未被 UPDATE 覆盖
    }
}
