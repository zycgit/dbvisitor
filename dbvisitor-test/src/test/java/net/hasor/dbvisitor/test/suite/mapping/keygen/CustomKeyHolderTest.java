package net.hasor.dbvisitor.test.suite.mapping.keygen;

import java.util.Date;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.keygen.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 自定义 KeyHolder 详细测试
 * 测试各种自定义主键生成器的实现方式
 */
public class CustomKeyHolderTest extends AbstractOneApiTest {

    private LambdaTemplate lambda;

    @Before
    public void setUp() throws Exception {
        super.setup();
        this.lambda = new LambdaTemplate(jdbcTemplate);
    }

    // ========== onBefore 生成器测试 ==========

    /**
     * 测试 onBefore 生成器 - 插入前生成主键
     * 固定值生成器
     */
    @Test
    public void testCustomKeyHolder_OnBefore_FixedValue() throws Exception {
        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id = 999999", new Object[] {});

        KeyHolderUser user = new KeyHolderUser();
        user.setAge(30);
        user.setCreateTime(new Date());

        // 验证插入前主键为空
        assertNull(user.getId());

        lambda.insert(KeyHolderUser.class)//
                .applyEntity(user)//
                .executeSumResult();

        // 验证主键已设置
        assertEquals("Should use fixed value from custom holder",//
                Integer.valueOf(999999), user.getId());
    }

    /**
     * 测试 onBefore 生成器 - 时间戳生成器
     */
    @Test
    public void testCustomKeyHolder_OnBefore_Timestamp() throws Exception {
        KeyHolderTimestampUser user = new KeyHolderTimestampUser();
        user.setName("Timestamp User");
        user.setAge(35);
        user.setCreateTime(new Date());

        assertNull(user.getId());

        lambda.insert(KeyHolderTimestampUser.class)//
                .applyEntity(user)//
                .executeSumResult();

        assertNotNull("ID should be generated from timestamp", user.getId());
        assertTrue("ID should be positive", user.getId() > 0);
        assertTrue("ID should be less than 1000000", user.getId() < 1000000);
    }

    /**
     * 测试 onBefore 生成器 - 计数器生成器
     */
    @Test
    public void testCustomKeyHolder_OnBefore_Counter() throws Exception {
        KeyHolderCounterUser user1 = new KeyHolderCounterUser();
        user1.setName("Counter User 1");
        user1.setAge(40);
        user1.setCreateTime(new Date());

        KeyHolderCounterUser user2 = new KeyHolderCounterUser();
        user2.setName("Counter User 2");
        user2.setAge(41);
        user2.setCreateTime(new Date());

        lambda.insert(KeyHolderCounterUser.class).applyEntity(user1).executeSumResult();
        lambda.insert(KeyHolderCounterUser.class).applyEntity(user2).executeSumResult();

        assertNotNull(user1.getId());
        assertNotNull(user2.getId());
        assertEquals("Counter should increment", 1, user2.getId() - user1.getId());
        assertTrue("IDs should be greater than 800000", user1.getId() > 800000);
    }

    // ========== onAfter 生成器测试 ==========

    private void ensureAfterTableExists() throws Exception {
        jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS user_keygen_after");
        jdbcTemplate.executeUpdate("DROP FUNCTION IF EXISTS fix_null_id_after CASCADE");

        jdbcTemplate.executeUpdate("CREATE TABLE user_keygen_after (" + "id SERIAL PRIMARY KEY, " + "name VARCHAR(255), " + "age INT, " + "create_time TIMESTAMP)");

        // Add proper handling for null insertions if necessary (like in Auto test)
        // But for KeyType.Holder, if the holder is "After", it implies we are not supplying ID?
        // Or does DbVisitor supply NULL?
        // If DbVisitor supplies NULL, Postgres SERIAL might choke if not DEFAULT.
        // But SERIAL creates a DEFAULT nextval().
        // If we insert "id=NULL", Postgres might error or insert NULL (if allowed? PK is not null).
        // Postgres: INSERT INTO t (id) VALUES (NULL) -> fails not-null constraint, UNLESS column has default?
        // No, explicit NULL overrides default.
        // So we might need the trigger hack here too IF inserted value is explicitly NULL.

        jdbcTemplate.executeUpdate("CREATE FUNCTION fix_null_id_after() RETURNS TRIGGER AS $$ BEGIN IF NEW.id IS NULL THEN NEW.id = nextval(pg_get_serial_sequence('user_keygen_after', 'id')); END IF; RETURN NEW; END; $$ LANGUAGE plpgsql");
        jdbcTemplate.executeUpdate("CREATE TRIGGER trig_fix_null_id_after BEFORE INSERT ON user_keygen_after FOR EACH ROW EXECUTE PROCEDURE fix_null_id_after()");
    }

    /**
     * 测试 onAfter 生成器 - 从数据库返回值中获取主键
     */
    @Test
    public void testCustomKeyHolder_OnAfter_FromDatabase() throws Exception {
        ensureAfterTableExists();

        KeyHolderAfterUser user = new KeyHolderAfterUser();
        user.setName("After Key User");
        user.setAge(45);
        user.setCreateTime(new Date());

        assertNull(user.getId());

        lambda.insert(KeyHolderAfterUser.class).applyEntity(user).executeSumResult();

        // onAfter 应该从数据库自增中获取到 ID
        assertNotNull("ID should be retrieved from database", user.getId());
        assertTrue("ID should be positive", user.getId() > 0);
    }

    // ========== 混合 onBefore 和 onAfter 测试 ==========

    /**
     * 测试同时实现 onBefore 和 onAfter
     * 验证只有一个会被调用（通常是 onBefore 优先）
     */
    @Test
    public void testCustomKeyHolder_BothBeforeAndAfter() throws Exception {
        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id IN (888888, 777777)", new Object[] {});

        KeyHolderBothUser user = new KeyHolderBothUser();
        user.setName("Both Key User");
        user.setAge(50);
        user.setCreateTime(new Date());

        lambda.insert(KeyHolderBothUser.class).applyEntity(user).executeSumResult();

        // onBefore sets 888888, onAfter sets 777777. Since onAfter puts value back to bean, we expect 777777.
        assertEquals("Should use onAfter value (overwritten)", Integer.valueOf(777777), user.getId());
    }

    // ========== 访问上下文信息测试 ==========

    /**
     * 测试 GeneratedKeyHandlerContext 提供的上下文信息
     */
    @Test
    public void testCustomKeyHolder_AccessContext() throws Exception {
        KeyHolderContextUser user = new KeyHolderContextUser();
        user.setName("Context Key User");
        user.setAge(55);
        user.setCreateTime(new Date());

        lambda.insert(KeyHolderContextUser.class).applyEntity(user).executeSumResult();

        assertNotNull("ID should be generated", user.getId());
        assertTrue("ID should be in expected range", //
                user.getId() >= 700000 && user.getId() < 800000);
    }

    // ========== 错误处理测试 ==========

    /**
     * 测试生成器抛出异常时的行为
     */
    @Test(expected = RuntimeException.class)
    public void testCustomKeyHolder_ExceptionHandling() throws Exception {
        KeyHolderFailingUser user = new KeyHolderFailingUser();
        user.setName("Failing Key User");
        user.setAge(60);
        user.setCreateTime(new Date());

        // 应该抛出 RuntimeException
        lambda.insert(KeyHolderFailingUser.class).applyEntity(user).executeSumResult();
    }

    /**
     * 测试生成器返回 null 时的行为
     */
    @Test
    public void testCustomKeyHolder_ReturnNull() {
        KeyHolderNullUser user = new KeyHolderNullUser();
        user.setName("Null Key User");
        user.setAge(65);
        user.setCreateTime(new Date());

        try {
            lambda.insert(KeyHolderNullUser.class).applyEntity(user).executeSumResult();
            fail("Should throw exception when inserting NULL into Primary Key");
        } catch (Exception e) {
            String msg = e.getMessage().toLowerCase();
            boolean isNullError = msg.contains("null") || msg.contains("constraint");
            assertTrue("Expected database error about NULL or Constraint, but got: " + msg, isNullError);
        }
    }
}
