package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class LambdaSpecialTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 770000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SPECIAL_QUOTED_TEXT)
    public void lambdaSpecialValueSingleDoubleQuotesAndBackslashes() throws SQLException {
        insertUser(baseId() + 1, "O'Brien", 30, "quote1@test.com", new Date());
        insertUser(baseId() + 2, "Say \"Hello\"", 31, "quote2@test.com", new Date());
        insertUser(baseId() + 3, "C:\\Users\\Admin", 32, "quote3@test.com", new Date());

        assertEquals("O'Brien", loadName(baseId() + 1));
        assertEquals("Say \"Hello\"", loadName(baseId() + 2));
        assertEquals("C:\\Users\\Admin", loadName(baseId() + 3));

        List<UserInfo> users = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getName, "O'Brien")//
                .queryForList();
        assertEquals(1, users.size());
        assertEquals(Integer.valueOf(baseId() + 1), users.get(0).getId());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SPECIAL_CONTROL_WHITESPACE)
    public void lambdaSpecialValueNewlineTabAndWhitespace() throws SQLException {
        String multiline = "Line1\nLine2\tTab";
        String spaces = "  Leading and Trailing  ";
        insertUser(baseId() + 11, multiline, 25, "control1@test.com", new Date());
        insertUser(baseId() + 12, spaces, 26, "control2@test.com", new Date());

        assertEquals(multiline, loadName(baseId() + 11));
        String loadedSpaces = loadName(baseId() + 12);
        assertNotNull(loadedSpaces);
        assertTrue(loadedSpaces.contains("Leading and Trailing"));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SPECIAL_UNICODE_EMOJI)
    public void lambdaSpecialValueUnicodeAndEmojiText() throws SQLException {
        String unicode = "你好世界 こんにちは 안녕하세요";
        String emoji = "emoji 😀😃🎉";
        String mixed = "Name: O'Brien\n\"Quoted\"\t100%\\ 你好";
        insertUser(baseId() + 21, unicode, 30, "unicode1@test.com", new Date());
        insertUser(baseId() + 22, emoji, 31, "unicode2@test.com", new Date());
        insertUser(baseId() + 23, mixed, 32, "unicode3@test.com", new Date());

        assertEquals(unicode, loadName(baseId() + 21));
        assertEquals(emoji, loadName(baseId() + 22));
        assertEquals(mixed, loadName(baseId() + 23));
        assertEquals(1, lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 21)//
                .like(UserInfo::getName, "你好%")//
                .queryForCount());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SPECIAL_KEYWORD_WILDCARD)
    public void lambdaSpecialValueSqlKeywordsAndWildcardsAsValues() throws SQLException {
        insertUser(baseId() + 31, "SELECT", 25, "keyword1@test.com", new Date());
        insertUser(baseId() + 32, "DELETE", 26, "keyword2@test.com", new Date());
        insertUser(baseId() + 33, "DROP TABLE", 27, "keyword3@test.com", new Date());
        insertUser(baseId() + 34, "100% Success", 28, "wildcard1@test.com", new Date());
        insertUser(baseId() + 35, "under_score_value", 29, "wildcard2@test.com", new Date());

        assertEquals(Integer.valueOf(baseId() + 31), lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getName, "SELECT")//
                .queryForObject().getId());
        List<UserInfo> keywordUsers = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, Arrays.asList(baseId() + 31, baseId() + 32, baseId() + 33))//
                .orderBy("id")//
                .queryForList();
        assertEquals(3, keywordUsers.size());
        assertEquals(1, lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getName, "100% Success")//
                .queryForCount());
        assertEquals(1, lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getName, "under_score_value")//
                .queryForCount());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SPECIAL_NUMERIC_BOUNDARY)
    public void lambdaSpecialValueIntegerEdgeValues() throws SQLException {
        insertUser(baseId() + 41, "MaxInt", Integer.MAX_VALUE, "numeric1@test.com", new Date());
        insertUser(baseId() + 42, "MinInt", Integer.MIN_VALUE, "numeric2@test.com", new Date());
        insertUser(baseId() + 43, "Zero", 0, "numeric3@test.com", new Date());

        assertEquals(Integer.MAX_VALUE, loadAge(baseId() + 41).intValue());
        assertEquals(Integer.MIN_VALUE, loadAge(baseId() + 42).intValue());
        assertEquals(Integer.valueOf(0), loadAge(baseId() + 43));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SPECIAL_DATE_BOUNDARY)
    public void lambdaSpecialValueEpochAndFutureDates() throws SQLException {
        Date epoch = new Date(0);
        Date future = new Date(4102444800000L); // 2100-01-01T00:00:00Z
        insertUser(baseId() + 51, "Epoch", 25, "date1@test.com", epoch);
        insertUser(baseId() + 52, "Future", 26, "date2@test.com", future);

        UserInfo loadedEpoch = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 51)//
                .queryForObject();
        UserInfo loadedFuture = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 52)//
                .queryForObject();

        assertNotNull(loadedEpoch.getCreateTime());
        assertNotNull(loadedFuture.getCreateTime());
        assertTrue("epoch timestamp should stay near 1970", loadedEpoch.getCreateTime().getTime() < 100000000L);
        assertTrue("future timestamp should remain after 2099", loadedFuture.getCreateTime().getTime() > 4070908800000L);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SPECIAL_NULL_CHARACTER)
    public void lambdaSpecialValueEitherBindOrRejectNullCharClearly() throws SQLException {
        String value = "Before\0After";
        try {
            insertUser(baseId() + 61, value, 25, "null-char@test.com", new Date());
            String loaded = loadName(baseId() + 61);
            assertNotNull(loaded);
            assertTrue(loaded.contains("Before"));
        } catch (SQLException e) {
            String message = lowerMessage(e);
            assertTrue("null-character rejection should be explicit: " + e.getMessage(), //
                    message.contains("null") || message.contains("character") || message.contains("invalid") //
                            || message.contains("byte") || message.contains("0x00") || message.contains("encoding"));
        }
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SPECIAL_LENGTH_CONSTRAINT)
    public void lambdaSpecialValueLengthConstraintForStandardNameCol() throws SQLException {
        requiresNxnFeature(FeatureId.LENGTH_LIMIT_ENFORCED);

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            builder.append("LongText_");
        }
        String tooLong = builder.toString();
        assertTrue(tooLong.length() > 100);

        try {
            insertUser(baseId() + 71, tooLong, 30, "too-long@test.com", new Date());
            fail("standard user_info.name VARCHAR(100) should reject overlength values");
        } catch (SQLException e) {
            assertTrue("length rejection should be explicit: " + e.getMessage(), isLengthLimitMessage(e));
        }
    }

    private void insertUser(int id, String name, Integer age, String email, Date createTime) throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        user.setCreateTime(createTime);
        lambdaTemplate.insert(UserInfo.class)//
                .applyEntity(user)//
                .executeSumResult();
    }

    private String loadName(int id) throws SQLException {
        return lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .queryForObject().getName();
    }

    private Integer loadAge(int id) throws SQLException {
        return lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, id)//
                .queryForObject().getAge();
    }

    private String lowerMessage(SQLException e) {
        return e.getMessage() == null ? "" : e.getMessage().toLowerCase();
    }
}
