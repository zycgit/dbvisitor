package net.hasor.dbvisitor.test.oneapi.suite.fluent;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.lambda.DuplicateKeyStrategy;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * DuplicateKeyStrategy 策略测试
 * 全面测试 Into / Ignore / Update 三种主键冲突策略的行为
 */
public class StrategyTest extends AbstractOneApiTest {

    // ==================== Into（标准 INSERT）====================

    /** Into 策略 - 无冲突正常插入 */
    @Test
    public void testInto_NoDuplicate() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        int result = lambda.insert(UserInfo.class)//
                .onDuplicateStrategy(DuplicateKeyStrategy.Into)//
                .applyEntity(createUser(30001, "IntoNormal", 25))//
                .executeSumResult();
        assertEquals(1, result);

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 30001)//
                .queryForObject();
        assertEquals("IntoNormal", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
    }

    /** Into 策略 - 主键冲突应抛出 SQLException */
    @Test
    public void testInto_DuplicateThrowsException() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(30101, "Original", 25))//
                .executeSumResult();

        try {
            lambda.insert(UserInfo.class)//
                    .onDuplicateStrategy(DuplicateKeyStrategy.Into)//
                    .applyEntity(createUser(30101, "Duplicate", 30))//
                    .executeSumResult();
            fail("Into strategy should throw on duplicate key");
        } catch (SQLException e) {
            String msg = e.getMessage().toLowerCase();
            assertTrue("Should be constraint/duplicate error: " + e.getMessage(),//
                    msg.contains("constraint") || msg.contains("duplicate")//
                            || msg.contains("unique") || msg.contains("primary"));
        }

        // 原始数据不受影响
        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 30101)//
                .queryForObject();
        assertEquals("Original", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
    }

    /** 默认策略（不设置 onDuplicateStrategy）等同于 Into，冲突应抛异常 */
    @Test
    public void testDefault_SameAsInto() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(30151, "DefaultOrig", 20))//
                .executeSumResult();

        try {
            lambda.insert(UserInfo.class)// 不设置策略
                    .applyEntity(createUser(30151, "DefaultDup", 30))//
                    .executeSumResult();
            fail("Default strategy should throw on duplicate key");
        } catch (SQLException e) {
            // expected
        }

        assertEquals("DefaultOrig", lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 30151)//
                .queryForObject()//
                .getName());
    }

    // ==================== Ignore ====================

    /** Ignore 策略 - 无冲突正常插入 */
    @Test
    public void testIgnore_NoDuplicate() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        int result = lambda.insert(UserInfo.class)//
                .onDuplicateStrategy(DuplicateKeyStrategy.Ignore)//
                .applyEntity(createUser(30201, "IgnoreNew", 25))//
                .executeSumResult();
        assertEquals(1, result);

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 30201)//
                .queryForObject();
        assertEquals("IgnoreNew", loaded.getName());
    }

    /** Ignore 策略 - 主键冲突时静默忽略，原记录不变 */
    @Test
    public void testIgnore_DuplicateSkipped() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(30301, "Original", 25))//
                .executeSumResult();

        // 冲突插入，不应抛异常
        int result = lambda.insert(UserInfo.class)//
                .onDuplicateStrategy(DuplicateKeyStrategy.Ignore)//
                .applyEntity(createUser(30301, "Ignored", 99))//
                .executeSumResult();

        // PG 返回 0（未插入），MySQL 可能返回 1
        assertTrue("Affected rows should be 0 or 1", result == 0 || result == 1);

        // 原始数据不变
        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 30301)//
                .queryForObject();
        assertEquals("Original", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
    }

    /** Ignore 策略 - 对同一记录重复插入多次，始终保持原始值 */
    @Test
    public void testIgnore_RepeatedDuplicate() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(30401, "First", 10))//
                .executeSumResult();

        for (int i = 0; i < 3; i++) {
            lambda.insert(UserInfo.class)//
                    .onDuplicateStrategy(DuplicateKeyStrategy.Ignore)//
                    .applyEntity(createUser(30401, "Attempt" + i, 20 + i))//
                    .executeSumResult();
        }

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 30401)//
                .queryForObject();
        assertEquals("First", loaded.getName());
        assertEquals(Integer.valueOf(10), loaded.getAge());
    }

    /** Ignore 策略 - 混合新记录和冲突记录，仅新记录被插入 */
    @Test
    public void testIgnore_MixedNewAndDuplicate() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 预插入 2 条
        lambda.insert(UserInfo.class).applyEntity(createUser(30501, "Existing1", 10)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(30503, "Existing3", 30)).executeSumResult();

        // 逐条 Ignore 插入 4 条（30501 冲突、30502 新、30503 冲突、30504 新）
        lambda.insert(UserInfo.class).onDuplicateStrategy(DuplicateKeyStrategy.Ignore)//
                .applyEntity(createUser(30501, "Dup1", 11)).executeSumResult();
        lambda.insert(UserInfo.class).onDuplicateStrategy(DuplicateKeyStrategy.Ignore)//
                .applyEntity(createUser(30502, "New2", 22)).executeSumResult();
        lambda.insert(UserInfo.class).onDuplicateStrategy(DuplicateKeyStrategy.Ignore)//
                .applyEntity(createUser(30503, "Dup3", 33)).executeSumResult();
        lambda.insert(UserInfo.class).onDuplicateStrategy(DuplicateKeyStrategy.Ignore)//
                .applyEntity(createUser(30504, "New4", 44)).executeSumResult();

        // 冲突记录保持原值
        assertEquals("Existing1", lambda.query(UserInfo.class).eq(UserInfo::getId, 30501).queryForObject().getName());
        assertEquals("Existing3", lambda.query(UserInfo.class).eq(UserInfo::getId, 30503).queryForObject().getName());
        // 新记录成功插入
        assertEquals("New2", lambda.query(UserInfo.class).eq(UserInfo::getId, 30502).queryForObject().getName());
        assertEquals("New4", lambda.query(UserInfo.class).eq(UserInfo::getId, 30504).queryForObject().getName());
    }

    // ==================== Update（Upsert）====================

    /** Update 策略 - 无冲突正常插入 */
    @Test
    public void testUpdate_NoDuplicate() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        int result = lambda.insert(UserInfo.class)//
                .onDuplicateStrategy(DuplicateKeyStrategy.Update)//
                .applyEntity(createUser(30601, "UpdateNew", 25))//
                .executeSumResult();
        assertTrue("Should insert at least 1 row", result >= 1);

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 30601)//
                .queryForObject();
        assertEquals("UpdateNew", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
    }

    /** Update 策略 - 主键冲突时更新所有字段 */
    @Test
    public void testUpdate_DuplicateUpdatesAllFields() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(30701, "OldName", 25))//
                .executeSumResult();

        UserInfo updated = createUser(30701, "NewName", 35);
        updated.setEmail("newname@strategy.com");

        lambda.insert(UserInfo.class)//
                .onDuplicateStrategy(DuplicateKeyStrategy.Update)//
                .applyEntity(updated)//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 30701)//
                .queryForObject();
        assertEquals("NewName", loaded.getName());
        assertEquals(Integer.valueOf(35), loaded.getAge());
        assertEquals("newname@strategy.com", loaded.getEmail());
    }

    /** Update 策略 - 多次 upsert 同一记录，最终值为最后一次 */
    @Test
    public void testUpdate_MultipleUpdates() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(30801, "V1", 10))//
                .executeSumResult();

        for (int i = 2; i <= 4; i++) {
            lambda.insert(UserInfo.class)//
                    .onDuplicateStrategy(DuplicateKeyStrategy.Update)//
                    .applyEntity(createUser(30801, "V" + i, 10 * i))//
                    .executeSumResult();
        }

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 30801)//
                .queryForObject();
        assertEquals("V4", loaded.getName());
        assertEquals(Integer.valueOf(40), loaded.getAge());
    }

    /** Update 策略 - 混合新记录和冲突记录 */
    @Test
    public void testUpdate_MixedNewAndDuplicate() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 预插入 2 条
        lambda.insert(UserInfo.class).applyEntity(createUser(30901, "Existing1", 10)).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(createUser(30903, "Existing3", 30)).executeSumResult();

        // Upsert 4 条：30901(冲突→更新)、30902(新增)、30903(冲突→更新)、30904(新增)
        lambda.insert(UserInfo.class).onDuplicateStrategy(DuplicateKeyStrategy.Update)//
                .applyEntity(createUser(30901, "Updated1", 11)).executeSumResult();
        lambda.insert(UserInfo.class).onDuplicateStrategy(DuplicateKeyStrategy.Update)//
                .applyEntity(createUser(30902, "New2", 22)).executeSumResult();
        lambda.insert(UserInfo.class).onDuplicateStrategy(DuplicateKeyStrategy.Update)//
                .applyEntity(createUser(30903, "Updated3", 33)).executeSumResult();
        lambda.insert(UserInfo.class).onDuplicateStrategy(DuplicateKeyStrategy.Update)//
                .applyEntity(createUser(30904, "New4", 44)).executeSumResult();

        // 冲突记录被更新
        assertEquals("Updated1", lambda.query(UserInfo.class).eq(UserInfo::getId, 30901).queryForObject().getName());
        assertEquals("Updated3", lambda.query(UserInfo.class).eq(UserInfo::getId, 30903).queryForObject().getName());
        // 新记录成功插入
        assertEquals("New2", lambda.query(UserInfo.class).eq(UserInfo::getId, 30902).queryForObject().getName());
        assertEquals("New4", lambda.query(UserInfo.class).eq(UserInfo::getId, 30904).queryForObject().getName());
    }

    /** Update 策略 - upsert 时部分字段为 null，验证 null 字段不参与更新 */
    @Test
    public void testUpdate_NullFieldPreservesOld() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(31001, "WithEmail", 25))//
                .executeSumResult();

        // upsert 时 email 为 null（不参与 INSERT 列，因此旧值保留）
        UserInfo partial = new UserInfo();
        partial.setId(31001);
        partial.setName("NoEmail");
        partial.setAge(30);
        partial.setEmail(null);
        partial.setCreateTime(new Date());

        lambda.insert(UserInfo.class)//
                .onDuplicateStrategy(DuplicateKeyStrategy.Update)//
                .applyEntity(partial)//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 31001)//
                .queryForObject();
        assertEquals("NoEmail", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
        // null 字段不参与 upsert，旧值保留
        assertEquals("withemail@strategy.com", loaded.getEmail());
    }

    // ==================== 跨策略场景 ====================

    /** 先 Ignore 保留旧值，再 Update 覆盖新值 */
    @Test
    public void testIgnoreThenUpdate() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        lambda.insert(UserInfo.class)//
                .applyEntity(createUser(31101, "Original", 10))//
                .executeSumResult();

        // Ignore → 不变
        lambda.insert(UserInfo.class)//
                .onDuplicateStrategy(DuplicateKeyStrategy.Ignore)//
                .applyEntity(createUser(31101, "Ignored", 20))//
                .executeSumResult();

        assertEquals("Original", lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 31101).queryForObject().getName());

        // Update → 覆盖
        lambda.insert(UserInfo.class)//
                .onDuplicateStrategy(DuplicateKeyStrategy.Update)//
                .applyEntity(createUser(31101, "Updated", 30))//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 31101)//
                .queryForObject();
        assertEquals("Updated", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
    }

    // ==================== Helper ====================

    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@strategy.com");
        u.setCreateTime(new Date());
        return u;
    }
}
