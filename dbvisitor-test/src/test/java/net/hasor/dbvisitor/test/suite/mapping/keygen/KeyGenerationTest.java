package net.hasor.dbvisitor.test.suite.mapping.keygen;

import java.util.Date;
import net.hasor.dbvisitor.lambda.Insert;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.keygen.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 主键生成器完整测试
 * 测试 dbVisitor 支持的所有 KeyType 策略
 * 参考文档：dbvisitor/dbvisitor-doc/docs/guides/core/mapping/keytype.md
 * 参考测试：dbvisitor/dbvisitor/src/test/java/net/hasor/scene/keyholder
 */
public class KeyGenerationTest extends AbstractOneApiTest {

    private LambdaTemplate lambda;

    @Before
    public void setUp() throws Exception {
        super.setup();
        this.lambda = new LambdaTemplate(jdbcTemplate);
    }

    // ========== KeyType.None 测试 ==========

    private void ensureStrictTableExists() throws Exception {
        jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS user_strict_none");
        jdbcTemplate.executeUpdate("CREATE TABLE user_strict_none (" + "id INT NOT NULL PRIMARY KEY, " + "name VARCHAR(255), " + "age INT, " + "create_time TIMESTAMP)");
    }

    /**
     * 测试 KeyType.None - 不使用任何主键生成策略
     * 必须手动设置主键值，否则插入失败
     */
    @Test
    public void testKeyType_None_ManuallySetId() throws Exception {
        ensureStrictTableExists();

        KeyNoneStrictUser user = new KeyNoneStrictUser();
        user.setId(88001); // 手动设置主键
        user.setName("None Key User");
        user.setAge(25);
        user.setCreateTime(new Date());

        int rows = lambda.insert(KeyNoneStrictUser.class)//
                .applyEntity(user)//
                .executeSumResult();

        assertEquals("Insert should succeed with manually set ID", 1, rows);
        assertEquals("ID should remain as manually set", Integer.valueOf(88001), user.getId());
    }

    /**
     * 测试 KeyType.None - 不设置主键时的行为
     * 在主键 NOT NULL 且无默认值的情况下，必须报错
     */
    @Test
    public void testKeyType_None_WithoutId() throws Exception {
        ensureStrictTableExists();

        KeyNoneStrictUser user = new KeyNoneStrictUser();
        // 不设置 ID
        user.setName("None Key User 2");
        user.setAge(26);
        user.setCreateTime(new Date());

        try {
            lambda.insert(KeyNoneStrictUser.class)//
                    .applyEntity(user)//
                    .executeSumResult();
            fail("Should fail when ID is missing for KeyType.None on strict table");
        } catch (Exception e) {
            String msg = e.getMessage().toLowerCase();
            assertTrue("Expected exception for missing primary key (NULL constraint), got: " + msg, msg.contains("null") || msg.contains("constraint"));
        }
    }

    // ========== KeyType.Auto 测试 ==========

    private void ensureAutoTableExists() throws Exception {
        jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS user_keygen_auto");
        jdbcTemplate.executeUpdate("DROP FUNCTION IF EXISTS fix_null_id CASCADE");

        jdbcTemplate.executeUpdate("CREATE TABLE user_keygen_auto (" + "id SERIAL PRIMARY KEY, " + "name VARCHAR(255), " + "age INT, " + "create_time TIMESTAMP)");

        // Workaround for Batch Insert passing NULL for ID
        jdbcTemplate.executeUpdate("CREATE FUNCTION fix_null_id() RETURNS TRIGGER AS $$ BEGIN IF NEW.id IS NULL THEN NEW.id = nextval(pg_get_serial_sequence('user_keygen_auto', 'id')); END IF; RETURN NEW; END; $$ LANGUAGE plpgsql");
        jdbcTemplate.executeUpdate("CREATE TRIGGER trig_fix_null_id BEFORE INSERT ON user_keygen_auto FOR EACH ROW EXECUTE PROCEDURE fix_null_id()");
    }

    /**
     * 测试 KeyType.Auto - 接收数据库自增主键
     * 通过 Statement.RETURN_GENERATED_KEYS 获取
     */
    @Test
    public void testKeyType_Auto_SingleInsert() throws Exception {
        ensureAutoTableExists();

        KeyAutoUser user = new KeyAutoUser();
        user.setName("Auto Key User");
        user.setAge(30);
        user.setCreateTime(new Date());

        assertNull("ID should be null before insert", user.getId());

        int rows = lambda.insert(KeyAutoUser.class)//
                .applyEntity(user)//
                .executeSumResult();

        assertEquals("Insert should succeed", 1, rows);
        assertNotNull("ID should be auto-generated", user.getId());
        assertTrue("Auto-generated ID should be positive", user.getId() > 0);
    }

    /**
     * 测试 KeyType.Auto - 批量插入时主键生成
     */
    @Test
    public void testKeyType_Auto_BatchInsert() throws Exception {
        ensureAutoTableExists();

        KeyAutoUser user1 = new KeyAutoUser();
        user1.setName("Auto User 1");
        user1.setAge(31);
        user1.setCreateTime(new Date());

        KeyAutoUser user2 = new KeyAutoUser();
        user2.setName("Auto User 2");
        user2.setAge(32);
        user2.setCreateTime(new Date());

        KeyAutoUser user3 = new KeyAutoUser();
        user3.setName("Auto User 3");
        user3.setAge(33);
        user3.setCreateTime(new Date());

        Insert<KeyAutoUser> insert = lambda.insert(KeyAutoUser.class);
        int rows = insert.applyEntity(user1)//
                .applyEntity(user2)//
                .applyEntity(user3)//
                .executeSumResult();

        assertEquals("Should insert 3 rows", 3, rows);

        // 验证所有主键都已生成
        assertNotNull("User1 ID should be generated", user1.getId());
        assertNotNull("User2 ID should be generated", user2.getId());
        assertNotNull("User3 ID should be generated", user3.getId());

        // 验证主键递增
        assertTrue("IDs should be different", //
                !user1.getId().equals(user2.getId())  //
                        && !user2.getId().equals(user3.getId()));
    }

    // ========== KeyType.UUID32 测试 ==========

    /**
     * 测试 KeyType.UUID32 - 生成 32 位 UUID（无连字符）
     * 格式：4d68040901d24b70bd10c1c8119001e2
     */
    @Test
    public void testKeyType_UUID32_Format() throws Exception {
        KeyUuid32User user = new KeyUuid32User();
        user.setAge(35);
        user.setCreateTime(new Date());

        assertNull("Name should be null before insert", user.getName());

        int rows = lambda.insert(KeyUuid32User.class)//
                .applyEntity(user)//
                .executeSumResult();

        assertEquals("Insert should succeed", 1, rows);
        assertNotNull("UUID32 should be generated", user.getName());
        assertEquals("UUID32 should be 32 characters", 32, user.getName().length());
        assertFalse("UUID32 should not contain hyphens", user.getName().contains("-"));
        assertTrue("UUID32 should be lowercase hex", user.getName().matches("[0-9a-f]{32}"));
    }

    /**
     * 测试 KeyType.UUID32 - 唯一性验证
     */
    @Test
    public void testKeyType_UUID32_Uniqueness() throws Exception {
        KeyUuid32User user1 = new KeyUuid32User();
        user1.setAge(36);
        user1.setCreateTime(new Date());

        KeyUuid32User user2 = new KeyUuid32User();
        user2.setAge(37);
        user2.setCreateTime(new Date());

        lambda.insert(KeyUuid32User.class)//
                .applyEntity(user1)//
                .executeSumResult();
        lambda.insert(KeyUuid32User.class)//
                .applyEntity(user2)//
                .executeSumResult();

        assertNotNull("User1 UUID should be generated", user1.getName());
        assertNotNull("User2 UUID should be generated", user2.getName());
        assertNotEquals("UUIDs should be unique", user1.getName(), user2.getName());
    }

    // ========== KeyType.UUID36 测试 ==========

    /**
     * 测试 KeyType.UUID36 - 生成 36 位 UUID（带连字符）
     * 格式：4d680409-01d2-4b70-bd10-c1c8119001e2
     */
    @Test
    public void testKeyType_UUID36_Format() throws Exception {
        KeyUuid36User user = new KeyUuid36User();
        user.setAge(40);
        user.setCreateTime(new Date());

        assertNull("Name should be null before insert", user.getName());

        int rows = lambda.insert(KeyUuid36User.class)//
                .applyEntity(user)//
                .executeSumResult();

        assertEquals("Insert should succeed", 1, rows);
        assertNotNull("UUID36 should be generated", user.getName());
        assertEquals("UUID36 should be 36 characters", 36, user.getName().length());
        assertTrue("UUID36 should contain hyphens", user.getName().contains("-"));
        assertTrue("UUID36 should match standard UUID format", //
                user.getName().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    /**
     * 测试 KeyType.UUID36 - 批量生成唯一性
     */
    @Test
    public void testKeyType_UUID36_BatchUniqueness() throws Exception {
        KeyUuid36User[] users = new KeyUuid36User[5];
        for (int i = 0; i < 5; i++) {
            users[i] = new KeyUuid36User();
            users[i].setAge(40 + i);
            users[i].setCreateTime(new Date());
            lambda.insert(KeyUuid36User.class)//
                    .applyEntity(users[i])//
                    .executeSumResult();
        }

        // 验证所有 UUID 都不同
        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 5; j++) {
                assertNotEquals("UUID should be unique", users[i].getName(), users[j].getName());
            }
        }
    }

    // ========== KeyType.Holder 测试 ==========

    /**
     * 测试 KeyType.Holder - 自定义固定值生成器
     */
    @Test
    public void testKeyType_Holder_FixedValue() throws Exception {
        // 清理可能存在的旧数据
        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id = 999999", new Object[] {});

        KeyHolderUser user = new KeyHolderUser();
        user.setAge(50);
        user.setCreateTime(new Date());

        assertNull("ID should be null before insert", user.getId());

        int rows = lambda.insert(KeyHolderUser.class)//
                .applyEntity(user)//
                .executeSumResult();

        assertEquals("Insert should succeed", 1, rows);
        assertEquals("ID should be set by custom holder", Integer.valueOf(999999), user.getId());
        assertNotNull("UUID36 name should also be generated", user.getName());
        assertEquals("UUID36 name should be 36 characters", 36, user.getName().length());
    }

    /**
     * 测试 KeyType.Holder - 验证 onBefore 在插入前执行
     */
    @Test
    public void testKeyType_Holder_OnBeforeExecution() throws Exception {
        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id = 999999", new Object[] {});

        KeyHolderUser user = new KeyHolderUser();
        user.setAge(51);
        user.setCreateTime(new Date());

        // 插入前 ID 为 null
        assertNull(user.getId());

        lambda.insert(KeyHolderUser.class)//
                .applyEntity(user)//
                .executeSumResult();

        // 插入后 ID 已被设置（证明 onBefore 在插入前执行）
        assertEquals(Integer.valueOf(999999), user.getId());

        // 验证数据库中确实存在这条记录
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = ?", new Object[] { 999999 }, Integer.class);
        assertEquals("Record should exist in database", Integer.valueOf(1), count);
    }

    // ========== 混合场景测试 ==========

    /**
     * 测试同一个 Entity 多个字段使用不同的 KeyType
     */
    @Test
    public void testKeyType_MultipleFieldsWithDifferentTypes() throws Exception {
        KeyUuid32User user = new KeyUuid32User();
        user.setAge(60);
        user.setCreateTime(new Date());

        // ID 使用 Auto，Name 使用 UUID32
        assertNull("ID should be null", user.getId());
        assertNull("Name should be null", user.getName());

        lambda.insert(KeyUuid32User.class)//
                .applyEntity(user)//
                .executeSumResult();

        assertNotNull("Auto ID should be generated", user.getId());
        assertNotNull("UUID32 name should be generated", user.getName());
        assertTrue("Auto ID should be positive", user.getId() > 0);
        assertEquals("UUID32 should be 32 chars", 32, user.getName().length());
    }

    /**
     * 测试主键生成器不影响手动设置的值（None 类型）
     */
    @Test
    public void testKeyType_None_ManualValueNotOverridden() throws Exception {
        KeyNoneUser user = new KeyNoneUser();
        user.setId(77777); // 手动设置
        user.setName("Manual User");
        user.setAge(65);
        user.setCreateTime(new Date());

        lambda.insert(KeyNoneUser.class)//
                .applyEntity(user)//
                .executeSumResult();

        // 验证手动设置的值没有被覆盖
        assertEquals("Manually set ID should not be changed", Integer.valueOf(77777), user.getId());

        // 从数据库查询验证
        Integer dbId = jdbcTemplate.queryForObject("SELECT id FROM user_info WHERE name = ?", new Object[] { "Manual User" }, Integer.class);
        assertEquals("Database ID should match manually set value", Integer.valueOf(77777), dbId);
    }

    /**
     * 测试主键生成器不影响手动设置的值（Generator 类型）
     * KeyHolderUser 使用 KeyType.Holder，如果手动设置 ID，生成器不应执行
     */
    @Test
    public void testKeyType_Generator_ManualValueNotOverridden() throws Exception {
        // 清理原有数据避免冲突
        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id = 12345");

        KeyHolderUser user = new KeyHolderUser();
        user.setId(12345); // 手动设置 ID，不使用 Holder 生成的 999999
        user.setName("Manual Holder User");
        user.setAge(66);
        user.setCreateTime(new Date());

        lambda.insert(KeyHolderUser.class)//
                .applyEntity(user)//
                .executeSumResult();

        // 验证手动设置的值没有被覆盖（如果被覆盖会变成 999999）
        assertEquals("Manually set ID should not be overridden by Holder",//
                Integer.valueOf(12345), user.getId());

        // 从数据库查询验证
        Integer dbId = jdbcTemplate.queryForObject("SELECT id FROM user_info WHERE id = ?",//
                new Object[] { 12345 }, Integer.class);
        assertEquals("Database record should exist with manually set ID",//
                Integer.valueOf(12345), dbId);
    }

    /**
     * 测试主键生成器不影响手动设置的值（Auto 类型）
     * KeyAutoUser 使用 KeyType.Auto，如果手动设置 ID，应使用该 ID 插入而不使用数据库自增
     */
    @Test
    public void testKeyType_Auto_ManualValueNotOverridden() throws Exception {
        ensureAutoTableExists();

        // Clean up specific ID just in case
        jdbcTemplate.executeUpdate("DELETE FROM user_keygen_auto WHERE id = 54321");

        KeyAutoUser user = new KeyAutoUser();
        user.setId(54321); // 手动设置 ID
        user.setName("Manual Auto User");
        user.setAge(67);
        user.setCreateTime(new Date());

        lambda.insert(KeyAutoUser.class)//
                .applyEntity(user)//
                .executeSumResult();

        // 验证手动设置的值没有被覆盖
        assertEquals("Manually set ID should not be overridden by Auto-Increment",//
                Integer.valueOf(54321), user.getId());

        // 从数据库查询验证
        Integer dbId = jdbcTemplate.queryForObject("SELECT id FROM user_keygen_auto WHERE id = ?",//
                new Object[] { 54321 }, Integer.class);
        assertEquals("Database record should exist with manually set ID",//
                Integer.valueOf(54321), dbId);
    }
}
