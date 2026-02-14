package net.hasor.dbvisitor.test.suite.declarative;

import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.dao.declarative.ParameterBindingMapper;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for parameter binding in annotation-based mappers:
 * - Positional parameters (?)
 * - Named parameters with @Param (#{name})
 * - Bean property binding (single bean auto-expanded)
 * - Map parameter binding
 * - Array / List parameters for IN clauses
 * - Mixed parameters (@Param + Bean)
 * - Parameter reuse (same param referenced twice)
 * - Type conversion (String → Integer column)
 */
public class ParameterBindingTest extends AbstractOneApiTest {

    private ParameterBindingMapper mapper;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Configuration configuration = new Configuration();
        Session session = configuration.newSession(dataSource);
        mapper = session.createMapper(ParameterBindingMapper.class);
    }

    // ==================== Positional parameters (?) ====================

    /** Positional parameter binding with ? placeholders */
    @Test
    public void testPositional_Insert() throws SQLException {
        int result = mapper.insertByPosition(49601, "PositionalParam", 28);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(49601);
        assertNotNull(loaded);
        assertEquals("PositionalParam", loaded.getName());
        assertEquals(Integer.valueOf(28), loaded.getAge());
    }

    /** Positional parameters respect declaration order */
    @Test
    public void testPositional_UpdateOrder() throws SQLException {
        mapper.insertByPosition(49602, "OrderTest", 25);

        // updateByPosition(age, id) — age first, then id
        int updated = mapper.updateByPosition(28, 49602);
        assertEquals(1, updated);

        UserInfo loaded = mapper.selectById(49602);
        assertEquals(Integer.valueOf(28), loaded.getAge());
    }

    // ==================== @Param named parameters ====================

    /** @Param annotation binds parameters by name via #{name} syntax */
    @Test
    public void testParam_NamedBinding() throws SQLException {
        int result = mapper.insertWithParam(49701, "ParamAnno", 25, "param@test.com");
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(49701);
        assertNotNull(loaded);
        assertEquals("ParamAnno", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
        assertEquals("param@test.com", loaded.getEmail());
    }

    /** @Param with NULL values — correctly binds null to nullable columns */
    @Test
    public void testParam_NullValues() throws SQLException {
        int result = mapper.insertWithParam(49702, "NullParam", null, null);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(49702);
        assertNotNull(loaded);
        assertNull(loaded.getAge());
        assertNull(loaded.getEmail());
    }

    /** @Param with empty string — stored as empty string, not null */
    @Test
    public void testParam_EmptyString() throws SQLException {
        int result = mapper.insertWithParam(49703, "", 28, "");
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(49703);
        assertEquals("", loaded.getName());
        assertEquals("", loaded.getEmail());
    }

    /** @Param with special characters — prepared statement escapes properly */
    @Test
    public void testParam_SpecialChars() throws SQLException {
        String specialName = "Test'Quote\"Double\\Slash";
        int result = mapper.insertWithParam(49704, specialName, 25, "special@test.com");
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(49704);
        assertEquals(specialName, loaded.getName());
    }

    // ==================== Bean property binding ====================

    /** Single bean parameter — properties auto-expanded via #{propertyName} */
    @Test
    public void testBean_PropertyBinding() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(49801);
        user.setName("BeanBinding");
        user.setAge(32);
        user.setEmail("bean@test.com");
        user.setCreateTime(new Date());

        int result = mapper.insertBean(user);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(49801);
        assertEquals("BeanBinding", loaded.getName());
        assertEquals(Integer.valueOf(32), loaded.getAge());
    }

    // ==================== Map parameter ====================

    /** Map parameter — keys matched to #{key} placeholders */
    @Test
    public void testMap_ParameterBinding() throws SQLException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 49901);
        params.put("name", "MapParam");
        params.put("age", 27);
        params.put("email", "map@test.com");

        int result = mapper.insertByMap(params);
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(49901);
        assertEquals("MapParam", loaded.getName());
    }

    // ==================== Mixed parameters ======================================

    /** @Param("bean") + @Param("scalar") — access nested bean.property */
    @Test
    public void testMixed_ParamAndBean() throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(50201);
        user.setName("MixedParam");
        user.setAge(30);

        int result = mapper.insertMixed(user, "mixed@test.com");
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(50201);
        assertEquals("MixedParam", loaded.getName());
        assertEquals("mixed@test.com", loaded.getEmail());
    }

    // ==================== Parameter reuse ====================

    /** Same parameter referenced twice — #{name} used for both name and email */
    @Test
    public void testReuse_SameParamTwice() throws SQLException {
        int result = mapper.insertWithReuse(50301, "ReuseParam");
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(50301);
        assertEquals("ReuseParam", loaded.getName());
        assertEquals("ReuseParam", loaded.getEmail()); // email = #{name}
    }

    // ==================== Many parameters ====================

    /** 7 @Param parameters — all correctly bound (extra params ignored in SQL) */
    @Test
    public void testMany_SevenParams() throws SQLException {
        int result = mapper.insertWithManyParams(50401, "ManyParams", 30, "many@test.com", new Date(), "extra1", "extra2");
        assertEquals(1, result);

        UserInfo loaded = mapper.selectById(50401);
        assertEquals("ManyParams", loaded.getName());
    }

    // ==================== Range query with @Param ====================

    /** Two @Param parameters for BETWEEN clause */
    @Test
    public void testParam_RangeQuery() throws SQLException {
        mapper.insertWithParam(50601, "Range1", 20, "r1@test.com");
        mapper.insertWithParam(50602, "Range2", 25, "r2@test.com");
        mapper.insertWithParam(50603, "Range3", 30, "r3@test.com");

        List<UserInfo> users = mapper.selectByAgeRange(20, 30);
        assertEquals(3, users.size());
        for (UserInfo u : users) {
            assertTrue(u.getAge() >= 20 && u.getAge() <= 30);
        }
    }

    // ==================== Array / List IN clause via @{in} rule ====================

    /** Integer[] parameter — @{in} rule expands array to (?, ?, ...) placeholders */
    @Test
    public void testArray_InClause() throws SQLException {
        mapper.insertWithParam(50701, "ArrayIn1", 61, "ain1@test.com");
        mapper.insertWithParam(50702, "ArrayIn2", 62, "ain2@test.com");
        mapper.insertWithParam(50703, "ArrayIn3", 63, "ain3@test.com");
        mapper.insertWithParam(50704, "ArrayIn4", 64, "ain4@test.com");

        List<UserInfo> users = mapper.selectByAgesArray(new Integer[] { 61, 63, 64 });
        assertEquals(3, users.size());

        List<Integer> ages = new ArrayList<Integer>();
        for (UserInfo u : users) {
            ages.add(u.getAge());
        }
        assertTrue(ages.containsAll(Arrays.asList(61, 63, 64)));
    }

    /** List<Integer> parameter — @{in} rule expands list to (?, ?, ...) placeholders */
    @Test
    public void testList_InClause() throws SQLException {
        mapper.insertWithParam(50801, "ListIn1", 71, "lin1@test.com");
        mapper.insertWithParam(50802, "ListIn2", 72, "lin2@test.com");
        mapper.insertWithParam(50803, "ListIn3", 73, "lin3@test.com");

        List<Integer> ageList = new ArrayList<Integer>();
        ageList.add(71);
        ageList.add(73);

        List<UserInfo> users = mapper.selectByAgesList(ageList);
        assertEquals(2, users.size());

        List<Integer> ages = new ArrayList<Integer>();
        for (UserInfo u : users) {
            ages.add(u.getAge());
        }
        assertTrue(ages.containsAll(Arrays.asList(71, 73)));
    }
}
