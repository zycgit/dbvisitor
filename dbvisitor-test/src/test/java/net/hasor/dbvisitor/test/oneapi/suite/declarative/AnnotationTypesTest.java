package net.hasor.dbvisitor.test.oneapi.suite.declarative;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.dao.declarative.AnnotationTestMapper;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for mapper annotation types: @Insert, @Update, @Delete, @Query, @Execute.
 * Also covers: @SimpleMapper marker, multi-line SQL via value[], error handling.
 */
public class AnnotationTypesTest extends AbstractOneApiTest {

    private AnnotationTestMapper mapper;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Configuration configuration = new Configuration();
        Session session = configuration.newSession(dataSource);
        mapper = session.createMapper(AnnotationTestMapper.class);
    }

    // ==================== @Insert ====================

    /** @Insert with Bean parameter — all fields mapped from bean properties */
    @Test
    public void testInsert_BeanParam() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(48001);
        user.setName("InsertTest");
        user.setAge(25);
        user.setEmail("insert@test.com");
        user.setCreateTime(new Date());

        int result = mapper.insertUser(user);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(48001);
        assertNotNull(loaded);
        assertEquals("InsertTest", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
        assertEquals("insert@test.com", loaded.getEmail());
    }

    /** @Insert with multiple @Param annotated parameters */
    @Test
    public void testInsert_MultipleParams() throws SQLException {
        int result = mapper.insertUserWithParams(48101, "MultiParam", 28, "multi@test.com");
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(48101);
        assertNotNull(loaded);
        assertEquals("MultiParam", loaded.getName());
        assertEquals(Integer.valueOf(28), loaded.getAge());
        assertEquals("multi@test.com", loaded.getEmail());
    }

    /** @Insert with multi-line SQL via value[] array — lines concatenated */
    @Test
    public void testInsert_MultiLineSql() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(48102);
        user.setName("MultiLine");
        user.setAge(27);
        user.setEmail("multiline@test.com");
        user.setCreateTime(new Date());

        int result = mapper.insertUserMultiLine(user);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(48102);
        assertNotNull(loaded);
        assertEquals("MultiLine", loaded.getName());
    }

    /** @Insert with NULL parameter values — nullable columns accept null */
    @Test
    public void testInsert_NullParams() throws SQLException {
        int result = mapper.insertUserWithParams(48103, "NullParam", null, null);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(48103);
        assertNotNull(loaded);
        assertEquals("NullParam", loaded.getName());
        assertNull(loaded.getAge());
        assertNull(loaded.getEmail());
    }

    /** @Insert with special characters — prepared statement prevents SQL injection */
    @Test
    public void testInsert_SpecialChars() throws SQLException {
        int result = mapper.insertUserWithParams(48104, "O'Brien & Co.", 28, "special@test.com");
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(48104);
        assertEquals("O'Brien & Co.", loaded.getName());
    }

    /** @Insert with Unicode characters */
    @Test
    public void testInsert_Unicode() throws SQLException {
        int result = mapper.insertUserWithParams(48105, "测试用户 テスト", 25, "unicode@test.com");
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(48105);
        assertEquals("测试用户 テスト", loaded.getName());
    }

    // ==================== @Update ====================

    /** @Update single field — returns 1 affected row */
    @Test
    public void testUpdate_SingleField() throws SQLException {
        mapper.insertUserWithParams(48201, "UpdateTest", 30, "update@test.com");

        int result = mapper.updateUserAge(48201, 35);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(48201);
        assertEquals(Integer.valueOf(35), loaded.getAge());
    }

    /** @Update multiple fields at once */
    @Test
    public void testUpdate_MultipleFields() throws SQLException {
        mapper.insertUserWithParams(48202, "UpdateMulti", 25, "multi@test.com");

        int result = mapper.updateUserInfo(48202, "UpdatedName", 30);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(48202);
        assertEquals("UpdatedName", loaded.getName());
        assertEquals(Integer.valueOf(30), loaded.getAge());
    }

    /** @Update batch — returns total affected rows */
    @Test
    public void testUpdate_AffectedRows() throws SQLException {
        for (int i = 1; i <= 5; i++) {
            mapper.insertUserWithParams(48300 + i, "Affected" + i, 32, "affected" + i + "@test.com");
        }

        int affected = mapper.updateAgeByRange(32, 40);
        assertEquals(5, affected);
    }

    /** @Update returns 0 when no rows matched */
    @Test
    public void testUpdate_NoMatch() throws SQLException {
        int result = mapper.updateUserAge(99999, 50);
        assertEquals(0, result);
    }

    // ==================== @Delete ====================

    /** @Delete by primary key — data no longer retrievable */
    @Test
    public void testDelete_ById() throws SQLException {
        mapper.insertUserWithParams(48401, "DeleteTest", 28, "delete@test.com");

        int result = mapper.deleteById(48401);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(48401);
        assertNull(loaded);
    }

    /** @Delete by condition — bulk delete with affected rows count */
    @Test
    public void testDelete_ByCondition() throws SQLException {
        for (int i = 1; i <= 5; i++) {
            mapper.insertUserWithParams(48500 + i, "DeleteCond" + i, 25, "cond" + i + "@test.com");
        }

        int result = mapper.deleteByAge(25);
        assertEquals(5, result);

        List<UserInfo> remaining = mapper.selectByAge(25);
        assertEquals(0, remaining.size());
    }

    /** @Delete returns 0 when no rows matched */
    @Test
    public void testDelete_NoMatch() throws SQLException {
        int result = mapper.deleteById(99999);
        assertEquals(0, result);
    }

    // ==================== @Query ====================

    /** @Query returns single entity */
    @Test
    public void testQuery_SingleObject() throws SQLException {
        mapper.insertUserWithParams(48601, "QuerySingle", 30, "single@test.com");

        UserInfo user = mapper.selectById(48601);
        assertNotNull(user);
        assertEquals("QuerySingle", user.getName());
        assertEquals(Integer.valueOf(30), user.getAge());
    }

    /** @Query returns entity list */
    @Test
    public void testQuery_List() throws SQLException {
        for (int i = 1; i <= 5; i++) {
            mapper.insertUserWithParams(48700 + i, "QueryList" + i, 28, "list" + i + "@test.com");
        }

        List<UserInfo> users = mapper.selectByAge(28);
        assertEquals(5, users.size());
    }

    /** @Query returns scalar count via aggregation */
    @Test
    public void testQuery_Count() throws SQLException {
        for (int i = 1; i <= 3; i++) {
            mapper.insertUserWithParams(48800 + i, "CountTest" + i, 35, "count" + i + "@test.com");
        }

        int count = mapper.countByAge(35);
        assertEquals(3, count);
    }

    /** @Query with LIKE pattern matching */
    @Test
    public void testQuery_Like() throws SQLException {
        for (int i = 1; i <= 3; i++) {
            mapper.insertUserWithParams(48900 + i, "LikeTest" + i, 26, "like" + i + "@test.com");
        }

        List<UserInfo> users = mapper.selectByNameLike("LikeTest%");
        assertEquals(3, users.size());
        for (UserInfo u : users) {
            assertTrue(u.getName().startsWith("LikeTest"));
        }
    }

    /** @Query returns null for non-existent record */
    @Test
    public void testQuery_ReturnsNull() throws SQLException {
        UserInfo user = mapper.selectById(99999);
        assertNull(user);
    }

    /** @Query returns empty list when no rows match */
    @Test
    public void testQuery_ReturnsEmptyList() throws SQLException {
        List<UserInfo> users = mapper.selectByAge(999);
        assertNotNull(users);
        assertEquals(0, users.size());
    }

    /** @Query with IN clause — @{in} rule auto-expands Integer[] to (?, ?, ...) */
    @Test
    public void testQuery_InClause() throws SQLException {
        mapper.insertUserWithParams(48951, "InClause1", 41, "in1@test.com");
        mapper.insertUserWithParams(48952, "InClause2", 42, "in2@test.com");
        mapper.insertUserWithParams(48953, "InClause3", 43, "in3@test.com");
        mapper.insertUserWithParams(48954, "InClause4", 44, "in4@test.com");

        List<UserInfo> users = mapper.selectByAgeIn(new Integer[] { 41, 43, 44 });
        assertEquals(3, users.size());

        List<Integer> ages = new java.util.ArrayList<Integer>();
        for (UserInfo u : users) {
            ages.add(u.getAge());
        }
        assertTrue(ages.containsAll(Arrays.asList(41, 43, 44)));
    }

    // ==================== @Execute ====================

    /** @Execute DDL: create temp table, insert data, verify via query */
    @Test
    public void testExecute_CreateAndUseTable() throws SQLException {
        try {
            mapper.createTempTable();
            mapper.insertTempData(1, "TempData");

            String name = mapper.selectTempData(1);
            assertEquals("TempData", name);
        } finally {
            try {
                mapper.dropTempTable();
            } catch (Exception ignored) {
            }
        }
    }

    /** @Execute DDL: drop table — subsequent queries should fail */
    @Test
    public void testExecute_DropTable() throws SQLException {
        mapper.createTempTable();
        mapper.dropTempTable();

        try {
            mapper.selectTempData(1);
            fail("Table should have been dropped");
        } catch (Exception e) {
            // UndeclaredThrowableException wraps the real cause; getMessage() may be null
            assertNotNull(e);
        }
    }

    // ==================== Error handling ====================

    /** SQL syntax error (FORM instead of FROM) throws exception */
    @Test
    public void testError_SyntaxError() {
        try {
            mapper.selectWithSyntaxError();
            fail("Should throw exception for SQL syntax error");
        } catch (Exception e) {
            // UndeclaredThrowableException wraps PSQLException; getMessage() may be null
            assertNotNull(e);
        }
    }

    /** Non-existent table throws exception */
    @Test
    public void testError_TableNotExists() {
        try {
            mapper.selectFromNonExistentTable();
            fail("Should throw exception for non-existent table");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    /** Non-existent column throws exception */
    @Test
    public void testError_ColumnNotExists() {
        try {
            mapper.selectNonExistentColumn();
            fail("Should throw exception for non-existent column");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    /** Primary key conflict throws exception */
    @Test
    public void testError_PrimaryKeyConflict() throws SQLException {
        mapper.insertUserWithParams(49100, "PKConflict1", 25, "pk1@test.com");

        try {
            mapper.insertUserWithParams(49100, "PKConflict2", 26, "pk2@test.com");
            fail("Should throw exception for duplicate key");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }
}
