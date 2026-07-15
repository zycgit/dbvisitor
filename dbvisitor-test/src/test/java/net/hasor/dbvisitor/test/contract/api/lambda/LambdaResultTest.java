package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.extractor.FilterResultSetExtractor;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class LambdaResultTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 750000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_MAP_AND_SCALAR)
    public void lambdaResultMapListsMapsCountsAndScalars() throws SQLException {
        insertUsers("LRMap", new int[] { 21, 22, 23, 24, 25 }, baseId() + 10);

        List<Map<String, Object>> maps = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "LRMap%")//
                .between("age", 22, 24)//
                .orderBy("age")//
                .queryForMapList();
        assertEquals(3, maps.size());
        assertEquals(22, ((Number) getVal(maps.get(0), "age")).intValue());

        Map<String, Object> one = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 11)//
                .queryForMap();
        assertEquals(baseId() + 11, ((Number) getVal(one, "id")).intValue());
        assertEquals("LRMap2", getVal(one, "name"));

        long count = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "LRMap%")//
                .ge(UserInfo::getAge, 23)//
                .queryForCount();
        assertEquals(3, count);

        Integer maxAge = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "LRMap%")//
                .applySelect("MAX(age)")//
                .queryForObject(Integer.class);
        assertEquals(Integer.valueOf(25), maxAge);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_ROW_MAPPER_CUSTOM)
    public void lambdaResultCustomRowMapperForSingleObject() throws SQLException {
        insertByJdbc(baseId() + 31, "LRMapper", 26, "lr-mapper@test.com");

        RowMapper<UserInfo> mapper = (rs, rowNum) -> {
            UserInfo user = new UserInfo();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name").toUpperCase());
            user.setAge(rs.getInt("age") * 2);
            user.setEmail(rs.getString("email"));
            return user;
        };

        UserInfo result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 31)//
                .queryForObject(mapper);

        assertNotNull(result);
        assertEquals(Integer.valueOf(baseId() + 31), result.getId());
        assertEquals("LRMAPPER", result.getName());
        assertEquals(Integer.valueOf(52), result.getAge());
        assertEquals("lr-mapper@test.com", result.getEmail());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_ROW_MAPPER_LIST)
    public void lambdaResultRowMapperForListsAndPartialObjects() throws SQLException {
        insertUsers("LRList", new int[] { 20, 25, 30 }, baseId() + 40);

        RowMapper<String> nameMapper = (rs, rowNum) -> rs.getString("name");
        List<String> names = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "LRList%")//
                .orderBy("id")//
                .queryForList(nameMapper);
        assertEquals(3, names.size());
        assertEquals("LRList1", names.get(0));
        assertEquals("LRList3", names.get(2));

        RowMapper<UserInfo> partialMapper = (rs, rowNum) -> {
            UserInfo user = new UserInfo();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            return user;
        };
        UserInfo partial = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 41)//
                .applySelect("id, name")//
                .queryForObject(partialMapper);
        assertEquals(Integer.valueOf(baseId() + 41), partial.getId());
        assertEquals("LRList2", partial.getName());
        assertNull(partial.getAge());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_TYPE_CONVERSION)
    public void lambdaResultScalarObjectsAndLists() throws SQLException {
        insertUsers("LRConvert", new int[] { 10, 20, 30 }, baseId() + 60);

        Integer ageInt = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 60)//
                .applySelect("age")//
                .queryForObject(Integer.class);
        Long ageLong = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 60)//
                .applySelect("age")//
                .queryForObject(Long.class);
        String ageString = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 60)//
                .applySelect("age")//
                .queryForObject(String.class);
        List<Integer> ages = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "LRConvert%")//
                .applySelect("age")//
                .orderBy("age")//
                .queryForList(Integer.class);

        assertEquals(Integer.valueOf(10), ageInt);
        assertEquals(Long.valueOf(10), ageLong);
        assertEquals("10", ageString);
        assertEquals(3, ages.size());
        assertEquals(Integer.valueOf(30), ages.get(2));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_ROW_CALLBACK)
    public void lambdaResultStreamRowsThroughRowCBHandler() throws SQLException {
        insertUsers("LRCallback", new int[] { 21, 22, 23, 24, 25 }, baseId() + 80);

        List<String> names = new ArrayList<>();
        AtomicInteger totalAge = new AtomicInteger();
        AtomicInteger count = new AtomicInteger();
        RowCallbackHandler handler = (rs, rowNum) -> {
            names.add(rs.getString("name"));
            totalAge.addAndGet(rs.getInt("age"));
            count.incrementAndGet();
        };

        lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "LRCallback%")//
                .between("id", baseId() + 80, baseId() + 84)//
                .orderBy("id")//
                .query(handler);

        assertEquals(5, count.get());
        assertEquals(115, totalAge.get());
        assertEquals("LRCallback1", names.get(0));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_EXTRACTOR_CUSTOM)
    public void lambdaResultCustomResultSetExtractor() throws SQLException {
        insertUsers("LRExtract", new int[] { 21, 22, 23 }, baseId() + 100);

        ResultSetExtractor<Map<Integer, String>> extractor = rs -> {
            Map<Integer, String> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getInt("id"), rs.getString("name"));
            }
            return result;
        };

        Map<Integer, String> result = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "LRExtract%")//
                .orderBy("id")//
                .query(extractor);

        assertEquals(3, result.size());
        assertEquals("LRExtract1", result.get(baseId() + 100));
        assertEquals("LRExtract3", result.get(baseId() + 102));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_EXTRACTOR_PAIRS)
    public void lambdaResultPairsFromStringAndLambdaCols() throws SQLException {
        insertUsers("LRPairs", new int[] { 24, 25, 26 }, baseId() + 120);

        Map<Integer, String> stringPairs = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "LRPairs%")//
                .queryForPairs("id", "name", Integer.class, String.class);
        Map<Integer, String> lambdaPairs = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "LRPairs%")//
                .queryForPairs(UserInfo::getId, UserInfo::getName, Integer.class, String.class);

        assertEquals(3, stringPairs.size());
        assertEquals("LRPairs1", stringPairs.get(baseId() + 120));
        assertEquals(stringPairs, lambdaPairs);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_EXTRACTOR_FILTER)
    public void lambdaResultFilterResultSetExtractor() throws SQLException {
        insertUsers("LRFilter", new int[] { 21, 22, 23, 24, 25, 26 }, baseId() + 140);

        ResultSetExtractor<List<UserInfo>> extractor = new FilterResultSetExtractor<>((rs, rowNum) -> {
            UserInfo user = new UserInfo();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            user.setAge(rs.getInt("age"));
            return user;
        }, user -> user.getAge() % 2 == 0);

        List<UserInfo> result = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "LRFilter%")//
                .orderBy("id")//
                .query(extractor);

        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(user -> user.getAge() % 2 == 0));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_EXTRACTOR_GROUPING)
    public void lambdaResultBuildGroupedStructuresFromExtractorAndGroupByMapper() throws SQLException {
        insertByJdbc(baseId() + 161, "LRGroup1", 30, "lr-group1@test.com");
        insertByJdbc(baseId() + 162, "LRGroup2", 30, "lr-group2@test.com");
        insertByJdbc(baseId() + 163, "LRGroup3", 31, "lr-group3@test.com");

        ResultSetExtractor<Map<Integer, List<String>>> groupingExtractor = rs -> {
            Map<Integer, List<String>> grouped = new LinkedHashMap<>();
            while (rs.next()) {
                grouped.computeIfAbsent(rs.getInt("age"), ignored -> new ArrayList<>()).add(rs.getString("name"));
            }
            return grouped;
        };
        Map<Integer, List<String>> grouped = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "LRGroup%")//
                .applySelect("age, name")//
                .orderBy("age")//
                .query(groupingExtractor);
        assertEquals(2, grouped.size());
        assertEquals(2, grouped.get(30).size());
        assertEquals("LRGroup3", grouped.get(31).get(0));

        RowMapper<AgeGroup> groupMapper = (rs, rowNum) -> new AgeGroup(rs.getInt("age"), rs.getLong("cnt"));
        List<AgeGroup> groups = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "LRGroup%")//
                .applySelect("age, count(*) as cnt")//
                .groupBy("age")//
                .orderBy("age")//
                .queryForList(groupMapper);
        assertEquals(2, groups.size());
        assertEquals(Integer.valueOf(30), groups.get(0).age);
        assertEquals(Long.valueOf(2), groups.get(0).count);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_NULL_AND_CALCULATED)
    public void lambdaResultNullsCalculatedColsAndMapToBean() throws SQLException {
        insertByJdbc(baseId() + 181, "LRNull", null, null);
        insertByJdbc(baseId() + 182, "LRCalc", 30, "lr-calc@test.com");

        RowMapper<UserInfo> nullSafeMapper = (rs, rowNum) -> {
            UserInfo user = new UserInfo();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            int age = rs.getInt("age");
            user.setAge(rs.wasNull() ? null : age);
            user.setEmail(rs.getString("email"));
            return user;
        };
        UserInfo nullUser = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 181)//
                .queryForObject(nullSafeMapper);
        assertEquals("LRNull", nullUser.getName());
        assertNull(nullUser.getAge());
        assertNull(nullUser.getEmail());

        Integer doubled = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 182)//
                .applySelect("age * 2 as doubled_age")//
                .queryForObject((rs, rowNum) -> rs.getInt("doubled_age"));
        assertEquals(Integer.valueOf(60), doubled);

        List<Map<String, Object>> maps = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 182)//
                .queryForMapList();
        assertEquals(1, maps.size());
        UserInfo bean = new UserInfo();
        bean.setId(((Number) getVal(maps.get(0), "id")).intValue());
        bean.setName((String) getVal(maps.get(0), "name"));
        bean.setAge(((Number) getVal(maps.get(0), "age")).intValue());
        assertEquals(Integer.valueOf(baseId() + 182), bean.getId());
        assertEquals("LRCalc", bean.getName());
        assertEquals(Integer.valueOf(30), bean.getAge());
    }

    private void insertUsers(String prefix, int[] ages, int startId) throws SQLException {
        for (int i = 0; i < ages.length; i++) {
            insertByJdbc(startId + i, prefix + (i + 1), ages[i], prefix.toLowerCase() + (i + 1) + "@test.com");
        }
    }

    private void insertByJdbc(int id, String name, Integer age, String email) throws SQLException {
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, email, new Date() });
    }

    private Object getVal(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        if (map.containsKey(key.toUpperCase())) {
            return map.get(key.toUpperCase());
        }
        if (map.containsKey(key.toLowerCase())) {
            return map.get(key.toLowerCase());
        }
        return null;
    }

    private static class AgeGroup {
        private final Integer age;
        private final Long    count;

        private AgeGroup(Integer age, Long count) {
            this.age = age;
            this.count = count;
        }
    }
}
