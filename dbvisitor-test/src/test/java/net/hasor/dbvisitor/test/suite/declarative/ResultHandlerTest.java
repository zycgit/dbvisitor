package net.hasor.dbvisitor.test.suite.declarative;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.dao.declarative.ResultHandlerMapper;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for @Query result handler attributes:
 * - resultSetExtractor: custom ResultSetExtractor processes entire ResultSet
 * - resultRowMapper: custom RowMapper maps each row individually
 * - resultRowCallback: custom RowCallbackHandler for void-return processing
 * - Combinations with other @Query attributes (fetchSize, resultSetType, timeout)
 * - Comparison with default (no custom handler) behavior
 */
public class ResultHandlerTest extends AbstractOneApiTest {

    private static final String              PATTERN = "HandlerTest%";
    private              ResultHandlerMapper mapper;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Configuration configuration = new Configuration();
        Session session = configuration.newSession(dataSource);
        mapper = session.createMapper(ResultHandlerMapper.class);

        // Prepare test data
        for (int i = 1; i <= 10; i++) {
            UserInfo user = new UserInfo();
            user.setId(53000 + i);
            user.setName("HandlerTest" + i);
            user.setAge(20 + i);
            user.setEmail("handler" + i + "@test.com");
            user.setCreateTime(new Date());
            mapper.insertUser(user);
        }
    }

    // ==================== Default behavior (baseline) ====================

    /** No custom handler — framework's default mapping */
    @Test
    public void testDefault_NoHandler() throws SQLException {
        List<UserInfo> users = mapper.selectDefault(PATTERN);

        assertNotNull(users);
        assertTrue(users.size() >= 10);
        assertEquals("HandlerTest1", users.get(0).getName());
    }

    // ==================== resultSetExtractor ====================

    /** Custom ResultSetExtractor processes entire ResultSet at once */
    @Test
    public void testExtractor_Basic() throws SQLException {
        List<UserInfo> users = mapper.selectWithExtractor(PATTERN);

        assertNotNull(users);
        assertTrue(users.size() >= 10);
        // CustomResultSetExtractor maps all fields manually
        for (UserInfo user : users) {
            assertNotNull(user.getId());
            assertNotNull(user.getName());
            assertTrue(user.getName().startsWith("HandlerTest"));
        }
    }

    /** ResultSetExtractor combined with fetchSize and resultSetType */
    @Test
    public void testExtractor_WithOptions() throws SQLException {
        List<UserInfo> users = mapper.selectWithExtractorAndOptions(PATTERN);

        assertNotNull(users);
        assertTrue(users.size() >= 10);
        for (UserInfo user : users) {
            assertNotNull(user.getId());
        }
    }

    /** ResultSetExtractor returns same count as default query */
    @Test
    public void testExtractor_ConsistentWithDefault() throws SQLException {
        List<UserInfo> defaultResult = mapper.selectDefault(PATTERN);
        List<UserInfo> extractorResult = mapper.selectWithExtractor(PATTERN);

        assertEquals(defaultResult.size(), extractorResult.size());
    }

    // ==================== resultRowMapper ====================

    /** Custom RowMapper adds [RowN] prefix to name field */
    @Test
    public void testRowMapper_Basic() throws SQLException {
        List<UserInfo> users = mapper.selectWithRowMapper(PATTERN);

        assertNotNull(users);
        assertTrue(users.size() >= 10);
        // CustomRowMapper adds "[RowN]" prefix to name
        for (int i = 0; i < users.size(); i++) {
            UserInfo user = users.get(i);
            assertNotNull(user.getName());
            assertTrue("Name should contain [Row" + i + "] prefix", user.getName().contains("[Row" + i + "]"));
        }
    }

    /** Custom RowMapper for single result — row number is 0 */
    @Test
    public void testRowMapper_SingleResult() throws SQLException {
        UserInfo user = mapper.selectSingleWithRowMapper(53001);

        assertNotNull(user);
        assertTrue(user.getName().contains("[Row0]"));
        assertTrue(user.getName().contains("HandlerTest1"));
    }

    /** RowMapper combined with timeout and fetchSize */
    @Test
    public void testRowMapper_WithOptions() throws SQLException {
        List<UserInfo> users = mapper.selectWithRowMapperAndOptions(PATTERN);

        assertNotNull(users);
        assertTrue(users.size() >= 10);
        // Verify RowMapper still adds prefix
        for (UserInfo user : users) {
            assertTrue(user.getName().contains("[Row"));
        }
    }

    /** RowMapper returns same count as default query */
    @Test
    public void testRowMapper_ConsistentCount() throws SQLException {
        List<UserInfo> defaultResult = mapper.selectDefault(PATTERN);
        List<UserInfo> mapperResult = mapper.selectWithRowMapper(PATTERN);

        assertEquals(defaultResult.size(), mapperResult.size());
    }

    // ==================== resultRowCallback ====================

    /** RowCallbackHandler — void return type, rows processed via callback */
    @Test
    public void testRowCallback_Basic() throws SQLException {
        // RowCallbackHandler returns void — no exception means success
        mapper.selectWithRowCallback(PATTERN);
    }

    /** RowCallbackHandler with timeout attribute */
    @Test
    public void testRowCallback_WithTimeout() throws SQLException {
        mapper.selectWithRowCallbackAndTimeout(PATTERN);
    }

    // ==================== Empty result handling ====================

    /** ResultSetExtractor with no matching rows — returns empty list */
    @Test
    public void testExtractor_EmptyResult() throws SQLException {
        List<UserInfo> users = mapper.selectWithExtractor("NoMatch%");

        assertNotNull(users);
        assertEquals(0, users.size());
    }

    /** RowMapper with no matching rows — returns empty list */
    @Test
    public void testRowMapper_EmptyResult() throws SQLException {
        List<UserInfo> users = mapper.selectWithRowMapper("NoMatch%");

        assertNotNull(users);
        assertEquals(0, users.size());
    }

    /** Single result RowMapper returns null for non-existent record */
    @Test
    public void testRowMapper_NullResult() throws SQLException {
        UserInfo user = mapper.selectSingleWithRowMapper(99999);
        assertNull(user);
    }

    /** RowCallbackHandler with no matching rows — completes without error */
    @Test
    public void testRowCallback_EmptyResult() throws SQLException {
        mapper.selectWithRowCallback("NoMatch%");
    }
}
