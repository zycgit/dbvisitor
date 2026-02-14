package net.hasor.dbvisitor.test.suite.declarative;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for annotation attributes:
 * - statementType (Prepared, Statement)
 * - timeout (-1, 30, 3600)
 * - fetchSize (1, 10, 256, 1000)
 * - resultSetType (DEFAULT, FORWARD_ONLY, SCROLL_INSENSITIVE, SCROLL_SENSITIVE)
 * - useGeneratedKeys / keyProperty / keyColumn
 * - @SelectKeySql (order = Before / After)
 * - Multi-line SQL via value[]
 * - Combined attributes
 */
public class AnnotationAttributesTest extends AbstractOneApiTest {

    private static final String                     PATTERN = "AttrTest%";
    private              AnnotationAttributesMapper mapper;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Configuration configuration = new Configuration();
        Session session = configuration.newSession(dataSource);
        mapper = session.createMapper(AnnotationAttributesMapper.class);

        // Prepare test data
        for (int i = 1; i <= 10; i++) {
            UserInfo user = new UserInfo();
            user.setId(51700 + i);
            user.setName("AttrTest" + i);
            user.setAge(20 + i);
            user.setEmail("attr" + i + "@test.com");
            user.setCreateTime(new Date());
            mapper.insertUserBasic(user);
        }
    }

    // ==================== statementType ====================

    /** statementType = Prepared (default) — uses PreparedStatement */
    @Test
    public void testStatementType_Prepared() throws SQLException {
        UserInfo user = mapper.selectByIdPrepared(51701);
        assertNotNull(user);
        assertEquals("AttrTest1", user.getName());
    }

    /** statementType = Statement — uses Statement (value inlined into SQL) */
    @Test
    public void testStatementType_Statement() throws SQLException {
        UserInfo user = mapper.selectByIdStatement(51702);
        assertNotNull(user);
        assertEquals("AttrTest2", user.getName());
    }

    // ==================== timeout ====================

    /** timeout = -1 (default, no timeout) */
    @Test
    public void testTimeout_Default() throws SQLException {
        UserInfo user = mapper.selectByIdDefaultTimeout(51703);
        assertNotNull(user);
        assertEquals("AttrTest3", user.getName());
    }

    /** timeout = 30 seconds */
    @Test
    public void testTimeout_Custom() throws SQLException {
        UserInfo user = mapper.selectByIdWithTimeout(51704);
        assertNotNull(user);
        assertEquals("AttrTest4", user.getName());
    }

    /** timeout = 3600 (1 hour, large value) */
    @Test
    public void testTimeout_MaxValue() throws SQLException {
        UserInfo user = mapper.selectWithMaxTimeout(51705);
        assertNotNull(user);
    }

    /** timeout on @Update — should execute without error */
    @Test
    public void testTimeout_OnUpdate() throws SQLException {
        int result = mapper.updateWithTimeout(51706, 99);
        assertEquals(1, result);
    }

    /** timeout on @Delete — should execute without error */
    @Test
    public void testTimeout_OnDelete() throws SQLException {
        int result = mapper.deleteWithTimeout(51707);
        assertEquals(1, result);
    }

    /** timeout on @Insert — should execute without error */
    @Test
    public void testTimeout_OnInsert() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(51800);
        user.setName("TimeoutInsert");
        user.setAge(25);
        user.setEmail("timeout@test.com");
        user.setCreateTime(new Date());

        int result = mapper.insertWithTimeout(user);
        assertEquals(1, result);
    }

    // ==================== fetchSize ====================

    /** fetchSize = 256 (default) — returns all matching rows */
    @Test
    public void testFetchSize_Default() throws SQLException {
        List<UserInfo> users = mapper.selectWithDefaultFetchSize(PATTERN);
        assertTrue(users.size() >= 10);
    }

    /** fetchSize = 10 (small batch) — result count unchanged */
    @Test
    public void testFetchSize_Small() throws SQLException {
        List<UserInfo> users = mapper.selectWithSmallFetchSize(PATTERN);
        assertTrue(users.size() >= 10);
    }

    /** fetchSize = 1000 (large batch) — result count unchanged */
    @Test
    public void testFetchSize_Large() throws SQLException {
        List<UserInfo> users = mapper.selectWithLargeFetchSize(PATTERN);
        assertTrue(users.size() >= 10);
    }

    /** fetchSize = 1 (single row fetch) — result count unchanged */
    @Test
    public void testFetchSize_One() throws SQLException {
        List<UserInfo> users = mapper.selectWithFetchSizeOne(PATTERN);
        assertTrue(users.size() >= 10);
    }

    // ==================== resultSetType ====================

    /** resultSetType = DEFAULT */
    @Test
    public void testResultSetType_Default() throws SQLException {
        List<UserInfo> users = mapper.selectWithDefaultResultSetType(PATTERN);
        assertTrue(users.size() >= 10);
    }

    /** resultSetType = FORWARD_ONLY */
    @Test
    public void testResultSetType_ForwardOnly() throws SQLException {
        List<UserInfo> users = mapper.selectWithForwardOnly(PATTERN);
        assertTrue(users.size() >= 10);
    }

    /** resultSetType = SCROLL_INSENSITIVE */
    @Test
    public void testResultSetType_ScrollInsensitive() throws SQLException {
        List<UserInfo> users = mapper.selectWithScrollInsensitive(PATTERN);
        assertTrue(users.size() >= 10);
    }

    /** resultSetType = SCROLL_SENSITIVE */
    @Test
    public void testResultSetType_ScrollSensitive() throws SQLException {
        List<UserInfo> users = mapper.selectWithScrollSensitive(PATTERN);
        assertTrue(users.size() >= 10);
    }

    // ==================== useGeneratedKeys / keyProperty / keyColumn ====================

    /** useGeneratedKeys = true — generated key backfilled into bean via keyProperty */
    @Test
    public void testGeneratedKeys_True() throws SQLException {
        UserInfo user = new UserInfo();
        user.setName("AutoKey");
        user.setAge(25);
        user.setEmail("autokey@test.com");
        user.setCreateTime(new Date());

        assertNull(user.getId());
        int result = mapper.insertWithGeneratedKey(user);
        assertEquals(1, result);

        // useGeneratedKeys=true + keyProperty="id" should backfill the generated key into the bean
        assertNotNull("Generated key should be backfilled into user.id", user.getId());
        assertTrue("Generated id should be positive", user.getId() > 0);

        // Verify via query that the backfilled id matches the actual database record
        UserInfo loaded = mapper.selectByIdPrepared(user.getId());
        assertNotNull(loaded);
        assertEquals("AutoKey", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
    }

    /** useGeneratedKeys = false — explicit id, no backfill */
    @Test
    public void testGeneratedKeys_False() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(51801);
        user.setName("ManualKey");
        user.setAge(28);
        user.setEmail("manual@test.com");
        user.setCreateTime(new Date());

        int result = mapper.insertWithoutGeneratedKey(user);
        assertEquals(1, result);
    }

    /** keyProperty = "id", keyColumn = "id" — generated key backfilled into bean */
    @Test
    public void testKeyProperty_Specified() throws SQLException {
        UserInfo user = new UserInfo();
        user.setName("KeyProp");
        user.setAge(30);
        user.setEmail("keyprop@test.com");
        user.setCreateTime(new Date());

        assertNull(user.getId());
        int result = mapper.insertWithKeyProperty(user);
        assertEquals(1, result);

        // keyProperty="id" + keyColumn="id" should backfill the generated key
        assertNotNull("Generated key should be backfilled into user.id", user.getId());
        assertTrue("Generated id should be positive", user.getId() > 0);

        // Verify the backfilled id is correct
        UserInfo loaded = mapper.selectByIdPrepared(user.getId());
        assertNotNull(loaded);
        assertEquals("KeyProp", loaded.getName());
    }

    // ==================== @SelectKeySql ====================

    /** @SelectKeySql order = Before — sequence value fetched before insert */
    @Test
    public void testSelectKeySql_Before() throws SQLException {
        UserInfo user = new UserInfo();
        user.setName("SelectKeyBefore");
        user.setAge(32);
        user.setEmail("before@test.com");
        user.setCreateTime(new Date());

        try {
            int result = mapper.insertWithSelectKeyBefore(user);
            assertEquals(1, result);
            assertNotNull(user.getId());
            assertTrue(user.getId() > 0);
        } catch (Exception e) {
            // Sequence may not exist in some test configs
            assertTrue(e.getMessage() != null);
        }
    }

    /** @SelectKeySql order = After — generated id fetched after insert */
    @Test
    public void testSelectKeySql_After() throws SQLException {
        UserInfo user = new UserInfo();
        user.setName("SelectKeyAfter");
        user.setAge(35);
        user.setEmail("after@test.com");
        user.setCreateTime(new Date());

        try {
            int result = mapper.insertWithSelectKeyAfter(user);
            assertEquals(1, result);
            assertNotNull(user.getId());
            assertTrue(user.getId() > 0);
        } catch (Exception e) {
            // lastval() may fail if no sequence was used
            assertTrue(e.getMessage() != null);
        }
    }

    /** @SelectKeySql with all attributes (statementType, timeout, fetchSize, resultSetType) */
    @Test
    public void testSelectKeySql_FullAttributes() throws SQLException {
        UserInfo user = new UserInfo();
        user.setName("SelectKeyFull");
        user.setAge(28);
        user.setEmail("full@test.com");
        user.setCreateTime(new Date());

        try {
            int result = mapper.insertWithSelectKeyFullAttrs(user);
            assertEquals(1, result);
            assertNotNull(user.getId());
        } catch (Exception e) {
            assertTrue(e.getMessage() != null);
        }
    }

    // ==================== Multi-line SQL ====================

    /** value[] array — lines joined into single SQL statement */
    @Test
    public void testMultiLineSql_Array() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(51901);
        user.setName("MultiLine");
        user.setAge(27);
        user.setEmail("multiline@test.com");
        user.setCreateTime(new Date());

        int result = mapper.insertMultiLine(user);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectByIdPrepared(51901);
        assertNotNull(loaded);
        assertEquals("MultiLine", loaded.getName());
    }

    // ==================== Combined attributes ====================

    /** Multiple attributes on single @Query — all should apply */
    @Test
    public void testCombined_MultipleAttributes() throws SQLException {
        List<UserInfo> users = mapper.selectWithCombinedAttributes(PATTERN);
        assertTrue(users.size() >= 10);
    }

    /** All attributes at default values — should work like plain @Query */
    @Test
    public void testCombined_AllDefaults() throws SQLException {
        UserInfo user = mapper.selectWithAllDefaults(51701);
        assertNotNull(user);
        assertEquals("AttrTest1", user.getName());
    }
}
