package net.hasor.dbvisitor.test.suite.mapping.keygen;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.keygen.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 主键生成器异常场景和边界条件测试
 * 测试各种错误情况和边界条件下的行为
 */
public class KeyGenerationEdgeCaseTest extends AbstractOneApiTest {

    private LambdaTemplate lambda;

    @Before
    public void setUp() throws Exception {
        super.setup();
        this.lambda = new LambdaTemplate(jdbcTemplate);
    }

    // ========== 主键冲突测试 ==========

    /**
     * 测试主键冲突 - 插入重复的主键值
     */
    @Test
    public void testKeyGeneration_DuplicateKey() throws Exception {
        jdbcTemplate.executeUpdate("DELETE FROM user_info WHERE id = 66666", new Object[] {});

        // 第一次插入
        KeyNoneUser user1 = new KeyNoneUser();
        user1.setId(66666);
        user1.setName("Duplicate User 1");
        user1.setAge(30);
        user1.setCreateTime(new Date());

        int result = lambda.insert(KeyNoneUser.class)//
                .applyEntity(user1)//
                .executeSumResult();
        assertEquals(1, result);

        // 第二次插入相同主键，应该失败
        KeyNoneUser user2 = new KeyNoneUser();
        user2.setId(66666); // 重复的主键
        user2.setName("Duplicate User 2");
        user2.setAge(31);
        user2.setCreateTime(new Date());

        try {
            lambda.insert(KeyNoneUser.class)//
                    .applyEntity(user2)//
                    .executeSumResult();
            fail("Expected exception due to duplicate primary key");
        } catch (Exception e) {
            String msg = e.getMessage().toLowerCase();
            assertTrue("Expected exception message to contain 'duplicate' or 'constraint' or 'unique', but got: " + e.getMessage(), //
                    msg.contains("duplicate") || msg.contains("constraint") || msg.contains("unique"));
        }
    }

    /**
     * 测试自增主键回绕 - 达到最大值后的行为
     */
    @Test
    public void testKeyGeneration_AutoIncrement_MaxValue() throws Exception {
        // 创建一个小范围的自增表来测试回绕
        try {
            jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS small_id_table");
            jdbcTemplate.executeUpdate("DROP SEQUENCE IF EXISTS small_id_seq");

            // 使用 Postgres 语法模拟溢出 (SMALLINT Max 32767)
            jdbcTemplate.executeUpdate("CREATE SEQUENCE small_id_seq MINVALUE 1 MAXVALUE 32767 START 32760");
            jdbcTemplate.executeUpdate("CREATE TABLE small_id_table (" + "id SMALLINT DEFAULT nextval('small_id_seq') PRIMARY KEY, " + "name VARCHAR(100)" + ")");

            // 插入接近 SMALLINT 最大值的数据
            // 32760 -> 32767 (8 inserts OK, 9th fails)
            for (int i = 0; i < 20; i++) {
                try {
                    jdbcTemplate.executeUpdate("INSERT INTO small_id_table (name) VALUES (?)", new Object[] { "User " + i });
                } catch (Exception e) {
                    // 预期在超过最大值时抛出异常
                    String msg = e.getMessage().toLowerCase();
                    assertTrue("Should throw overflow exception but got: " + e.getMessage(), //
                            msg.contains("overflow") || msg.contains("out of range") || msg.contains("limit") || msg.contains("reached maximum"));
                    return; // Test passed
                }
            }
            fail("Should have thrown exception due to sequence overflow");
        } finally {
            jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS small_id_table");
            jdbcTemplate.executeUpdate("DROP SEQUENCE IF EXISTS small_id_seq");
        }
    }

    /**
     * 测试 UUID 生成器在非 String 字段上的行为
     */
    @Test
    public void testKeyGeneration_UUID_WrongType() throws Exception {
        // 尝试在 Integer 字段上使用 UUID 生成器
        KeyWrongTypeUuidUser user = new KeyWrongTypeUuidUser();
        user.setName("Wrong Type User");
        user.setAge(35);
        user.setCreateTime(new Date());

        try {
            lambda.insert(KeyWrongTypeUuidUser.class)//
                    .applyEntity(user)//
                    .executeSumResult();

            // 如果生成器强行设置 UUID 到 Integer 字段，应该会转换失败或报错
            fail("Should fail when setting UUID to Integer field");
        } catch (Exception e) {
            // 预期发生类型转换错误或 Argument 错误
            assertNotNull(e);
        }
    }

    // ========== 生成器运行时异常测试 ==========

    /**
     * 测试自定义生成器在运行时抛出 checked exception (SQLException)
     */
    @Test
    public void testKeyGeneration_CustomHolder_ThrowSQLException() throws Exception {
        KeyHolderSqlExceptionUser user = new KeyHolderSqlExceptionUser();
        user.setName("SQL Ex User");
        user.setAge(40);
        user.setCreateTime(new Date());

        try {
            // 应该捕获到 SQLException 并包装抛出
            lambda.insert(KeyHolderSqlExceptionUser.class)//
                    .applyEntity(user)//
                    .executeSumResult();
            fail("Expected exception due to SQLException in KeyHolder");
        } catch (Exception e) {
            // 必须是 RuntimeException 包装的 SQLException 或直接是 SQLException（取决于实现）
            // 这里验证根本原因
            Throwable cause = e;
            boolean foundSqlException = false;
            while (cause != null) {
                if (cause instanceof SQLException && cause.getMessage().contains("Intentional SQLException")) {
                    foundSqlException = true;
                    break;
                }
                cause = cause.getCause();
            }
            assertTrue("Expected exception cause to be SQLException with message 'Intentional SQLException', but got: " + e, foundSqlException);
        }
    }

    /**
     * 测试自定义生成器访问数据库连接
     */
    @Test
    public void testKeyGeneration_CustomHolder_UseConnection() throws Exception {
        KeyHolderConnectionUser user = new KeyHolderConnectionUser();
        user.setName("Connection Aware User");
        user.setAge(60);
        user.setCreateTime(new Date());

        lambda.insert(KeyHolderConnectionUser.class)//
                .applyEntity(user)//
                .executeSumResult();

        assertNotNull("ID should be generated from MAX(id) + 1", user.getId());
        assertTrue("ID should be positive", user.getId() > 0);
    }

    // ========== 复杂类型主键测试 ==========

    /**
     * 测试 Long 类型主键的 Auto 生成
     */
    @Test
    public void testKeyGeneration_LongType() throws Exception {
        jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS user_keygen_auto_long");
        jdbcTemplate.executeUpdate("CREATE TABLE user_keygen_auto_long (" + "id BIGSERIAL PRIMARY KEY, " + "name VARCHAR(255), " + "age INT, " + "create_time TIMESTAMP)");

        KeyAutoLongUser user = new KeyAutoLongUser();
        user.setName("Long ID User");
        user.setAge(65);
        user.setCreateTime(new Date());

        assertNull(user.getId());

        lambda.insert(KeyAutoLongUser.class)//
                .applyEntity(user)//
                .executeSumResult();

        assertNotNull("Long ID should be generated", user.getId());
        assertTrue("Long ID should be positive", user.getId() > 0);
    }

    /**
     * 测试 String 类型主键的多种生成策略
     */
    @Test
    public void testKeyGeneration_StringType_MultipleStrategies() throws Exception {
        jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS user_uuid");
        jdbcTemplate.executeUpdate("CREATE TABLE user_uuid (" + "id VARCHAR(36) PRIMARY KEY, " + "name VARCHAR(100), " + "age INT, " + "create_time TIMESTAMP)");

        KeyUuidStringUser user = new KeyUuidStringUser();
        // ID 自动生成 (UUID36)
        user.setName("String ID User");
        user.setAge(70);
        user.setCreateTime(new Date());

        assertNull(user.getId());

        try {
            lambda.insert(KeyUuidStringUser.class)//
                    .applyEntity(user)//
                    .executeSumResult();

            assertNotNull("String ID should be generated", user.getId());
            assertEquals("UUID36 length", 36, user.getId().length());
        } finally {
            jdbcTemplate.executeUpdate("DROP TABLE IF EXISTS user_uuid");
        }
    }
}
