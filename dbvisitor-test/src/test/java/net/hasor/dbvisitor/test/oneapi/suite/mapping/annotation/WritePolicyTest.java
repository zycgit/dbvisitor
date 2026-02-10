package net.hasor.dbvisitor.test.oneapi.suite.mapping.annotation;
import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import net.hasor.dbvisitor.test.oneapi.model.annotation.InsertExcludedUser;
import net.hasor.dbvisitor.test.oneapi.model.annotation.ReadOnlyEmailUser;
import net.hasor.dbvisitor.test.oneapi.model.annotation.UpdateExcludedUser;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Write Policy Test
 * 验证写入策略：@Column 的 insert/update 属性
 */
public class WritePolicyTest extends AbstractOneApiTest {
    /**
     * 验证 @Column 的 insert 属性元数据。
     * 默认 insert=true，显式设置 insert=false 后应在元数据中体现。
     */
    @Test
    public void testInsertPolicyMetadata() {
        MappingRegistry registry = new MappingRegistry();

        // UserInfo: 所有字段默认 insert=true
        registry.loadEntityToSpace(UserInfo.class, "", "default");
        TableMapping<?> defaultMapping = registry.findBySpace("", "default");
        ColumnMapping emailDefault = defaultMapping.getPropertyByName("email");
        assertNotNull(emailDefault);
        assertTrue("Default insert policy should be true", emailDefault.isInsert());

        // InsertExcludedUser: email 设置了 insert=false
        registry.loadEntityToSpace(InsertExcludedUser.class, "", "excluded");
        TableMapping<?> excludedMapping = registry.findBySpace("", "excluded");
        ColumnMapping emailExcluded = excludedMapping.getPropertyByName("email");
        assertNotNull(emailExcluded);
        assertFalse("InsertExcludedUser.email should have insert=false", emailExcluded.isInsert());

        // 其他字段进行正常插入
        ColumnMapping nameExcluded = excludedMapping.getPropertyByName("name");
        assertTrue("name should still have insert=true", nameExcluded.isInsert());
    }

    /**
     * 测试 NULL 值更新
     * NULL 值应能更新到列
     */
    @Test
    public void testUpdateToNullValue() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 先插入有值的数据
        UserInfo user = createUser(31201, "UpdateNull", 30);
        lambda.insert(UserInfo.class)//
                .applyEntity(user)//
                .executeSumResult();

        // 更新为 NULL
        int updated = lambda.update(UserInfo.class)//
                .eq(UserInfo::getId, 31201)//
                .updateTo(UserInfo::getAge, null)//
                .updateTo(UserInfo::getEmail, null)//
                .doUpdate();

        assertEquals(1, updated);

        // 验证 NULL 更新成功
        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 31201)//
                .queryForObject();

        assertNull("Age should be updated to NULL", loaded.getAge());
        assertNull("Email should be updated to NULL", loaded.getEmail());
    }

    /**
     * 测试部分字段插入
     * 只插入指定的字段
     */
    @Test
    public void testPartialFieldInsert() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo user = new UserInfo();
        user.setId(31301);
        user.setName("PartialInsert");
        user.setAge(28);
        // email 不设置

        lambda.insert(UserInfo.class)//
                .applyEntity(user)   //
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 31301)     //
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("PartialInsert", loaded.getName());
        assertEquals(Integer.valueOf(28), loaded.getAge());
    }

    /**
     * 测试部分字段更新
     * 只更新指定的字段
     */
    @Test
    public void testPartialFieldUpdate() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 先插入完整数据
        UserInfo user = createUser(31401, "PartialUpdate", 25);
        lambda.insert(UserInfo.class)//
                .applyEntity(user)   //
                .executeSumResult();

        // 只更新 name
        int updated = lambda.update(UserInfo.class)//
                .eq(UserInfo::getId, 31401)  //
                .updateTo(UserInfo::getName, "OnlyNameUpdated")//
                .doUpdate();

        assertEquals(1, updated);

        // 验证只有 name 被更新
        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 31401)     //
                .queryForObject();

        assertEquals("OnlyNameUpdated", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge()); // age 未变
        assertEquals("partialupdate@policy.com", loaded.getEmail()); // email 未变
    }

    /**
     * 验证 @Column 的 update 属性元数据。
     * 默认 update=true，显式设置 update=false 后应在元数据中体现。
     */
    @Test
    public void testUpdatePolicyMetadata() {
        MappingRegistry registry = new MappingRegistry();

        // UserInfo: 所有字段默认 update=true
        registry.loadEntityToSpace(UserInfo.class, "", "default");
        TableMapping<?> defaultMapping = registry.findBySpace("", "default");
        ColumnMapping emailDefault = defaultMapping.getPropertyByName("email");
        assertNotNull(emailDefault);
        assertTrue("Default update policy should be true", emailDefault.isUpdate());

        // UpdateExcludedUser: email 设置了 update=false
        registry.loadEntityToSpace(UpdateExcludedUser.class, "", "excluded");
        TableMapping<?> excludedMapping = registry.findBySpace("", "excluded");
        ColumnMapping emailExcluded = excludedMapping.getPropertyByName("email");
        assertNotNull(emailExcluded);
        assertFalse("UpdateExcludedUser.email should have update=false", emailExcluded.isUpdate());

        // ReadOnlyEmailUser: email insert=false, update=false
        registry.loadEntityToSpace(ReadOnlyEmailUser.class, "", "readonly");
        TableMapping<?> readonlyMapping = registry.findBySpace("", "readonly");
        ColumnMapping emailReadonly = readonlyMapping.getPropertyByName("email");
        assertFalse("ReadOnly email should have insert=false", emailReadonly.isInsert());
        assertFalse("ReadOnly email should have update=false", emailReadonly.isUpdate());
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@policy.com");
        u.setCreateTime(new Date());
        return u;
    }

    // ============ @Column(insert=false/update=false) 注解驱动测试 ============

    /**
     * @Column(insert=false): email 字段即使 Java 对象有值，INSERT 也不包含该列。
     * 数据库 email 应为 NULL。
     */
    @Test
    public void testColumnInsertFalse_ExcludedFromInsert() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        InsertExcludedUser user = new InsertExcludedUser();
        user.setId(31801);
        user.setName("InsertExcluded");
        user.setAge(25);
        user.setEmail("should_not_be_inserted@test.com");
        user.setCreateTime(new Date());
        lambda.insert(InsertExcludedUser.class).applyEntity(user).executeSumResult();

        // 通过原始 UserInfo 验证 email 为 NULL
        UserInfo raw = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 31801)  //
                .queryForObject();

        assertNotNull(raw);
        assertEquals("InsertExcluded", raw.getName());
        assertNull("email should be NULL due to @Column(insert=false)", raw.getEmail());
    }

    /**
     * @Column(update=false): email 字段即使在更新请求中设值，UPDATE 也不修改该列。
     * 原始 email 值应保持不变。
     */
    @Test
    public void testColumnUpdateFalse_ExcludedFromUpdate() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 先用原始 UserInfo 插入含 email 的数据
        UserInfo rawUser = createUser(31901, "UpdateExcluded", 30);
        lambda.insert(UserInfo.class).applyEntity(rawUser).executeSumResult();

        // 用 UpdateExcludedUser 尝试更新 name 和 email
        int updated = lambda.update(UpdateExcludedUser.class)//
                .eq(UpdateExcludedUser::getId, 31901)  //
                .updateTo(UpdateExcludedUser::getName, "NameChanged")//
                .doUpdate();

        assertEquals(1, updated);

        // 验证 name 被更新，但 email 保持原值（update=false）
        UserInfo afterUpdate = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 31901)          //
                .queryForObject();

        assertEquals("NameChanged", afterUpdate.getName());
        assertEquals("updateexcluded@policy.com", afterUpdate.getEmail());
    }

    /**
     * @Column(insert=false, update=false): email 字段完全只读，
     * INSERT 和 UPDATE 均不影响该列。
     */
    @Test
    public void testColumnBothFalse_ReadOnlyField() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 1. INSERT：email 不参与
        ReadOnlyEmailUser user = new ReadOnlyEmailUser();
        user.setId(32001);
        user.setName("ReadOnly");
        user.setAge(28);
        user.setEmail("wont_be_written@test.com");
        user.setCreateTime(new Date());
        lambda.insert(ReadOnlyEmailUser.class).applyEntity(user).executeSumResult();

        UserInfo afterInsert = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 32001)          //
                .queryForObject();
        assertNull("email should be NULL after INSERT (insert=false)", //
                afterInsert.getEmail());

        // 2. 直接用 SQL 设置 email
        jdbcTemplate.executeUpdate("UPDATE user_info SET email = ? WHERE id = ?",//
                new Object[] { "db_set_email@test.com", 32001 });

        // 3. UPDATE：email 不参与
        ReadOnlyEmailUser updateData = new ReadOnlyEmailUser();
        updateData.setName("ReadOnlyUpdated");
        updateData.setEmail("should_not_update@test.com"); // 尝试更新一个新值
        lambda.update(ReadOnlyEmailUser.class)            //
                .eq(ReadOnlyEmailUser::getId, 32001)//
                .updateToSample(updateData)               // 即使由 Entity 提供了值，也不应当被更新
                .doUpdate();

        UserInfo afterUpdate = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 32001)          //
                .queryForObject();

        assertEquals("ReadOnlyUpdated", afterUpdate.getName());
        assertEquals("db_set_email@test.com", afterUpdate.getEmail()); // email 未被覆盖

        // 4. SELECT：email 仍可正常读取
        ReadOnlyEmailUser loaded = lambda.query(ReadOnlyEmailUser.class)//
                .eq(ReadOnlyEmailUser::getId, 32001)              //
                .queryForObject();
        assertEquals("db_set_email@test.com", loaded.getEmail());
    }
}
