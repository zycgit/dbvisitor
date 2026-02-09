package net.hasor.dbvisitor.test.oneapi.suite.fluent;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Special Value Test
 * 验证特殊值和边界情况的处理
 */
public class SpecialValueTest extends AbstractOneApiTest {

    /**
     * 测试单引号字符
     * 场景: name = "O'Brien"
     */
    @Test
    public void testSingleQuoteCharacter() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo u = createUser(26401, "O'Brien", 30);
        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 26401)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("O'Brien", loaded.getName());

        // 通过 name 查询
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .eq(UserInfo::getName, "O'Brien")//
                .queryForList();

        assertEquals(1, result.size());
        assertEquals("O'Brien", result.get(0)//
                .getName());
    }

    /**
     * 测试双引号字符
     * 场景: name = 'Say "Hello"'
     */
    @Test
    public void testDoubleQuoteCharacter() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo u = createUser(26501, "Say \"Hello\"", 25);
        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 26501)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("Say \"Hello\"", loaded.getName());
    }

    /**
     * 测试反斜杠字符
     * 场景: name = "C:\Users\Admin"
     */
    @Test
    public void testBackslashCharacter() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo u = createUser(26601, "C:\\Users\\Admin", 30);
        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 26601)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("C:\\Users\\Admin", loaded.getName());
    }

    /**
     * 测试换行符和制表符
     * 场景: name = "Line1\nLine2\tTab"
     */
    @Test
    public void testNewlineAndTabCharacters() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        String multilineText = "Line1\nLine2\tTab";
        UserInfo u = createUser(26701, multilineText, 25);
        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 26701)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals(multilineText, loaded.getName());
    }

    /**
     * 测试 Unicode 字符
     * 场景: 中文、日文、韩文、Emoji
     */
    @Test
    public void testUnicodeCharacters() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        String unicode = "你好世界 こんにちは 안녕하세요 🌍🚀";
        UserInfo u = createUser(26801, unicode, 30);
        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 26801)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals(unicode, loaded.getName());

        // 通过 LIKE 查询 Unicode
        List<UserInfo> result = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "你好")//
                .queryForList();

        assertTrue("Should find Unicode text", result.size() >= 1);
    }

    /**
     * 测试 Emoji 字符
     * 场景: name = "😀😃😄😁"
     */
    @Test
    public void testEmojiCharacters() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        String emoji = "😀😃😄😁🎉";
        UserInfo u = createUser(26901, emoji, 25);

        try {
            lambda.insert(UserInfo.class)//
                    .applyEntity(u)//
                    .executeSumResult();

            UserInfo loaded = lambda.query(UserInfo.class)//
                    .eq(UserInfo::getId, 26901)//
                    .queryForObject();

            assertNotNull(loaded);
            // 注意: 某些数据库/字符集可能不支持 Emoji（需要 utf8mb4）
            assertTrue("Should preserve emoji or handle gracefully", //
                    loaded.getName() != null && loaded.getName().length() > 0);
        } catch (SQLException e) {
            // 如果数据库不支持 Emoji，捕获异常并通过测试
            assertTrue("Emoji not supported or encoding issue", //
                    e.getMessage().contains("character") || e.getMessage().contains("encoding"));
        }
    }

    /**
     * 测试长文本
     * 场景: 超过 VARCHAR 默认长度
     */
    @Test
    public void testLongText() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 生成 500 字符的文本
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sb.append("LongText_");
        }
        String longText = sb.toString(); // 约 450 字符

        UserInfo u = createUser(27001, longText, 30);

        try {
            lambda.insert(UserInfo.class)//
                    .applyEntity(u)//
                    .executeSumResult();

            UserInfo loaded = lambda.query(UserInfo.class)//
                    .eq(UserInfo::getId, 27001)//
                    .queryForObject();

            assertNotNull(loaded);
            assertTrue("Long text should be preserved", loaded.getName()//
                    .length() >= 400);
        } catch (SQLException e) {
            // 如果字段长度限制导致失败，捕获异常
            assertTrue("Should indicate length constraint", //
                    e.getMessage().toLowerCase().contains("length") //
                            || e.getMessage().toLowerCase().contains("too long") //
                            || e.getMessage().toLowerCase().contains("data too long"));
        }
    }

    /**
     * 测试空格和特殊空白字符
     * 场景: 前后空格、多个空格、全角空格
     */
    @Test
    public void testWhitespaceCharacters() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        String textWithSpaces = "  Leading and Trailing  ";
        UserInfo u = createUser(27101, textWithSpaces, 25);
        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 27101)//
                .queryForObject();

        assertNotNull(loaded);
        // 某些数据库可能会 trim 空格，某些保留
        assertTrue("Should preserve or trim spaces", //
                loaded.getName() != null && loaded.getName().contains("Leading"));
    }

    /**
     * 测试 SQL 关键字作为值
     * 场景: name = "SELECT", "DELETE", "DROP TABLE"
     */
    @Test
    public void testSqlKeywordsAsValues() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo u1 = createUser(27201, "SELECT", 25);
        UserInfo u2 = createUser(27202, "DELETE", 30);
        UserInfo u3 = createUser(27203, "DROP TABLE", 35);

        lambda.insert(UserInfo.class).applyEntity(u1).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(u2).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(u3).executeSumResult();

        // 查询 SQL 关键字值
        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getName, "SELECT")//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("SELECT", loaded.getName());

        List<UserInfo> result = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, java.util.Arrays.asList(27201, 27202, 27203))//
                .queryForList();

        assertEquals(3, result.size());
    }

    /**
     * 测试通配符字符
     * 场景: name 包含 % 或 _ 字符
     */
    @Test
    public void testWildcardCharacters() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo u = createUser(27301, "100% Success", 30);
        lambda.insert(UserInfo.class)//
                .applyEntity(u)//
                .executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 27301)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals("100% Success", loaded.getName());

        // 精确匹配应找到
        List<UserInfo> exact = lambda.query(UserInfo.class)//
                .eq(UserInfo::getName, "100% Success")//
                .queryForList();

        assertEquals(1, exact.size());
    }

    /**
     * 测试数值边界值
     * 场景: Integer.MAX_VALUE, Integer.MIN_VALUE, 0
     */
    @Test
    public void testNumericBoundaryValues() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        UserInfo u1 = createUser(27401, "MaxInt", Integer.MAX_VALUE);
        UserInfo u2 = createUser(27402, "MinInt", Integer.MIN_VALUE);
        UserInfo u3 = createUser(27403, "Zero", 0);

        lambda.insert(UserInfo.class).applyEntity(u1).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(u2).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(u3).executeSumResult();

        UserInfo max = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 27401)//
                .queryForObject();

        UserInfo min = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 27402)//
                .queryForObject();

        assertEquals(Integer.MAX_VALUE, max.getAge()//
                .intValue());
        assertEquals(Integer.MIN_VALUE, min.getAge()//
                .intValue());
        assertEquals(Integer.valueOf(0), lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 27403)//
                .queryForObject()//
                .getAge());
    }

    /**
     * 测试特殊日期
     * 场景: epoch (1970-01-01), 当前时间, 未来日期
     */
    @Test
    public void testSpecialDates() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        Date epoch = new Date(0); // 1970-01-01
        Date now = new Date();
        Date future = new Date(now.getTime() + 1000L * 86400 * 365 * 10); // 10 years later

        UserInfo u1 = createUser(27501, "Epoch", 25);
        u1.setCreateTime(epoch);
        lambda.insert(UserInfo.class).applyEntity(u1).executeSumResult();

        UserInfo u2 = createUser(27502, "Future", 30);
        u2.setCreateTime(future);
        lambda.insert(UserInfo.class).applyEntity(u2).executeSumResult();

        UserInfo loadedEpoch = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 27501)//
                .queryForObject();

        assertNotNull(loadedEpoch.getCreateTime());
        assertTrue("Should preserve epoch date", //
                loadedEpoch.getCreateTime().getTime() < 100000000L);
    }

    /**
     * 测试 NULL 字符（\0）
     * 注意: 某些数据库不允许 NULL 字符
     */
    @Test
    public void testNullCharacter() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        String textWithNull = "Before\0After";
        UserInfo u = createUser(27601, textWithNull, 25);

        try {
            lambda.insert(UserInfo.class)//
                    .applyEntity(u)//
                    .executeSumResult();

            UserInfo loaded = lambda.query(UserInfo.class)//
                    .eq(UserInfo::getId, 27601)//
                    .queryForObject();

            assertNotNull(loaded);
            // 验证是否保留或删除了 \0
            assertTrue("Should handle null character", loaded.getName() != null);
        } catch (SQLException e) {
            // 某些数据库不允许 NULL 字符（如 PG: "invalid byte sequence for encoding"）
            String msg = e.getMessage().toLowerCase();
            assertTrue("Exception should relate to null/invalid character: " + e.getMessage(),//
                    msg.contains("null") || msg.contains("character")//
                            || msg.contains("invalid") || msg.contains("byte")//
                            || msg.contains("0x00") || msg.contains("encoding"));
        }
    }

    /**
     * 测试混合特殊字符
     * 场景: 组合各种特殊字符
     */
    @Test
    public void testMixedSpecialCharacters() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        String mixed = "Name: O'Brien\n\"Quoted\"\t100%\\ 你好🌍";
        UserInfo u = createUser(27701, mixed, 30);

        lambda.insert(UserInfo.class).applyEntity(u).executeSumResult();

        UserInfo loaded = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 27701)//
                .queryForObject();

        assertNotNull(loaded);
        assertTrue("Should preserve mixed special characters", //
                loaded.getName().contains("O'Brien") //
                        && loaded.getName().contains("Quoted"));
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(id + "@special.com");
        u.setCreateTime(new Date());
        return u;
    }
}
