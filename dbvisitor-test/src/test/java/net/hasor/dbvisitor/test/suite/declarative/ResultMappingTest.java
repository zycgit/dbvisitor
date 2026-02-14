package net.hasor.dbvisitor.test.suite.declarative;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.dao.declarative.ResultMappingMapper;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for @Query return type mapping:
 * - Entity mapping (@Table/@Column annotated class)
 * - Map mapping (dynamic key-value)
 * - Scalar types (Integer, String, Long, Date)
 * - List variants (List&lt;Entity&gt;, List&lt;Map&gt;, List&lt;String&gt;, List&lt;Integer&gt;)
 * - Partial field mapping
 * - NULL value mapping
 * - Aggregate queries (MIN/MAX/AVG, GROUP BY, DISTINCT)
 * - Pagination with PageObject
 */
public class ResultMappingTest extends AbstractOneApiTest {

    private static final String              PATTERN = "ResultMap%";
    private              ResultMappingMapper mapper;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Configuration configuration = new Configuration();
        Session session = configuration.newSession(dataSource);
        mapper = session.createMapper(ResultMappingMapper.class);

        // Prepare test data
        for (int i = 1; i <= 10; i++) {
            UserInfo user = new UserInfo();
            user.setId(51300 + i);
            user.setName("ResultMap" + i);
            user.setAge(20 + i);
            user.setEmail("result" + i + "@test.com");
            user.setCreateTime(new Date());
            mapper.insertUser(user);
        }
    }

    // ==================== Entity mapping ====================

    /** @Query returns single entity — all @Column fields mapped */
    @Test
    public void testEntity_AllFields() throws SQLException {
        UserInfo user = mapper.selectUserById(51301);

        assertNotNull(user);
        assertEquals(Integer.valueOf(51301), user.getId());
        assertEquals("ResultMap1", user.getName());
        assertEquals(Integer.valueOf(21), user.getAge());
        assertEquals("result1@test.com", user.getEmail());
        assertNotNull(user.getCreateTime());
    }

    /** @Query returns entity with partial SELECT — unmapped fields are null */
    @Test
    public void testEntity_PartialFields() throws SQLException {
        UserInfo user = mapper.selectUserPartial(51305);

        assertNotNull(user);
        assertEquals(Integer.valueOf(51305), user.getId());
        assertEquals("ResultMap5", user.getName());
        assertNull(user.getAge());   // not in SELECT
        assertNull(user.getEmail()); // not in SELECT
    }

    /** @Query returns null for non-existent record */
    @Test
    public void testEntity_Null() throws SQLException {
        UserInfo user = mapper.selectUserById(99999);
        assertNull(user);
    }

    // ==================== Map mapping ====================

    /** @Query returns Map — column names as keys */
    @Test
    public void testMap_SingleRow() throws SQLException {
        Map<String, Object> userMap = mapper.selectUserAsMap(51302);

        assertNotNull(userMap);
        assertNotNull(userMap.get("id"));
        assertEquals("ResultMap2", userMap.get("name"));
    }

    /** @Query returns List<Map> */
    @Test
    public void testMap_List() throws SQLException {
        List<Map<String, Object>> users = mapper.selectUsersAsMapList();

        assertNotNull(users);
        assertTrue(users.size() >= 10);
        assertNotNull(users.get(0).get("id"));
        assertNotNull(users.get(0).get("name"));
    }

    // ==================== Scalar types ====================

    /** @Query returns Integer (single column) */
    @Test
    public void testScalar_Integer() throws SQLException {
        Integer age = mapper.selectAgeById(51303);

        assertNotNull(age);
        assertEquals(Integer.valueOf(23), age);
    }

    /** @Query returns String (single column) */
    @Test
    public void testScalar_String() throws SQLException {
        String name = mapper.selectNameById(51304);

        assertNotNull(name);
        assertEquals("ResultMap4", name);
    }

    /** @Query returns Long (COUNT aggregate) */
    @Test
    public void testScalar_Long() throws SQLException {
        Long count = mapper.selectCount();

        assertNotNull(count);
        assertTrue(count >= 10);
    }

    /** @Query returns Date (timestamp column) */
    @Test
    public void testScalar_Date() throws SQLException {
        Date createTime = mapper.selectCreateTimeById(51307);

        assertNotNull(createTime);
        assertTrue(createTime instanceof Date);
    }

    /** @Query returns null String for non-existent record */
    @Test
    public void testScalar_NullString() throws SQLException {
        String name = mapper.selectNameById(99999);
        assertNull(name);
    }

    // ==================== List variants ====================

    /** @Query returns List<Entity> with range filter */
    @Test
    public void testList_Entity() throws SQLException {
        List<UserInfo> users = mapper.selectUsersByAgeRange(21, 25);

        assertNotNull(users);
        assertTrue(users.size() >= 5);
        for (UserInfo user : users) {
            assertTrue(user.getAge() >= 21 && user.getAge() <= 25);
        }
    }

    /** @Query returns empty List<Entity> when no rows match */
    @Test
    public void testList_Empty() throws SQLException {
        List<UserInfo> users = mapper.selectUsersByAgeRange(999, 1000);

        assertNotNull(users);
        assertEquals(0, users.size());
    }

    /** @Query returns List<String> */
    @Test
    public void testList_String() throws SQLException {
        List<String> names = mapper.selectAllNames(PATTERN);

        assertNotNull(names);
        assertTrue(names.size() >= 10);
        assertTrue(names.contains("ResultMap1"));
    }

    /** @Query returns List<Integer> */
    @Test
    public void testList_Integer() throws SQLException {
        List<Integer> ids = mapper.selectIdRange(51301, 51310);

        assertNotNull(ids);
        assertTrue(ids.size() >= 10);
        assertTrue(ids.contains(51301));
    }

    // ==================== NULL value mapping ====================

    /** NULL column values mapped correctly to bean */
    @Test
    public void testNull_ColumnValues() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(51401);
        user.setName("NullMapping");
        user.setAge(null);
        user.setEmail(null);
        user.setCreateTime(new Date());
        mapper.insertUser(user);

        UserInfo loaded = mapper.selectUserById(51401);
        assertNotNull(loaded);
        assertEquals("NullMapping", loaded.getName());
        assertNull(loaded.getAge());
        assertNull(loaded.getEmail());
    }

    // ==================== Underscore to camelCase ====================

    /** create_time column mapped to createTime property via @Column */
    @Test
    public void testMapping_UnderscoreToCamelCase() throws SQLException {
        UserInfo user = mapper.selectUserById(51301);
        assertNotNull(user.getCreateTime());
    }

    // ==================== Aggregate queries ====================

    /** MAX aggregate returns single scalar */
    @Test
    public void testAggregate_Max() throws SQLException {
        Integer maxAge = mapper.selectMaxAge();

        assertNotNull(maxAge);
        assertTrue(maxAge >= 30);
    }

    /** Multiple aggregates (MIN, MAX, AVG) in one query → Map */
    @Test
    public void testAggregate_MultiColumn() throws SQLException {
        Map<String, Object> stats = mapper.selectAgeStats(PATTERN);

        assertNotNull(stats);
        assertNotNull(stats.get("minage"));
        assertNotNull(stats.get("maxage"));
        assertNotNull(stats.get("avgage"));
    }

    /** DISTINCT returns deduplicated list */
    @Test
    public void testAggregate_Distinct() throws SQLException {
        List<Integer> distinctAges = mapper.selectDistinctAges(PATTERN);

        assertNotNull(distinctAges);
        assertTrue(distinctAges.size() <= 10);
        // Verify no duplicates
        assertEquals(distinctAges.size(), new java.util.HashSet<Integer>(distinctAges).size());
    }

    /** GROUP BY returns list of aggregate maps */
    @Test
    public void testAggregate_GroupBy() throws SQLException {
        List<Map<String, Object>> groupResult = mapper.selectCountByAge(PATTERN);

        assertNotNull(groupResult);
        assertTrue(groupResult.size() > 0);

        Map<String, Object> first = groupResult.get(0);
        assertNotNull(first.get("age"));
        assertNotNull(first.get("cnt"));
    }

    // ==================== Pagination ====================

    /** PageObject controls page size and offset — LIMIT/OFFSET applied */
    @Test
    public void testPagination_FirstPage() throws SQLException {
        PageObject page = new PageObject(0, 5);

        List<UserInfo> users = mapper.selectUsersWithPagination(PATTERN, page);

        assertNotNull(users);
        assertEquals(5, users.size());
        // Note: totalCount is only calculated when return type is PageResult
    }

    /** PageObject second page — different data from first page */
    @Test
    public void testPagination_SecondPage() throws SQLException {
        PageObject page1 = new PageObject(0, 5);
        List<UserInfo> users1 = mapper.selectUsersWithPagination(PATTERN, page1);

        PageObject page2 = new PageObject(1, 5);
        List<UserInfo> users2 = mapper.selectUsersWithPagination(PATTERN, page2);

        assertNotNull(users1);
        assertNotNull(users2);
        assertEquals(5, users1.size());
        assertEquals(5, users2.size());
        // Two pages should contain different data
        assertNotEquals(users1.get(0).getId(), users2.get(0).getId());
    }
}
