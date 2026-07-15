package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserBasicDTO;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class LambdaSelTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 816000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SELECT_SINGLE_COLUMN)
    public void lambdaSelectSingleProjectedColAsScalarList() throws Exception {
        insert(baseId() + 1, "SelectNameOne", 25, "select-name@nxn.test");

        List<String> names = lambdaTemplate.query(UserInfo.class)//
                .select(UserInfo::getName)//
                .eq(UserInfo::getId, baseId() + 1)//
                .queryForList(String.class);

        assertEquals(1, names.size());
        assertEquals("SelectNameOne", names.get(0));
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SELECT_MAP_DTO)
    public void lambdaSelectProjectMultiColsToMapAndDto() throws Exception {
        insert(baseId() + 10, "SelectMapDto", 30, "map-dto@nxn.test");

        List<Map<String, Object>> maps = lambdaTemplate.query(UserInfo.class)//
                .select(UserInfo::getName, UserInfo::getAge)//
                .eq(UserInfo::getId, baseId() + 10)//
                .queryForMapList();
        assertEquals(1, maps.size());
        Map<String, Object> map = maps.get(0);
        assertTrue(containsKey(map, "name"));
        assertTrue(containsKey(map, "age"));
        assertFalse(containsKey(map, "email"));
        assertEquals("SelectMapDto", getVal(map, "name"));
        assertEquals(30, ((Number) getVal(map, "age")).intValue());

        List<UserBasicDTO> dtos = lambdaTemplate.query(UserInfo.class)//
                .select(UserInfo::getName, UserInfo::getAge)//
                .eq(UserInfo::getId, baseId() + 10)//
                .queryForList(UserBasicDTO.class);
        assertEquals(1, dtos.size());
        assertEquals("SelectMapDto", dtos.get(0).getName());
        assertEquals(Integer.valueOf(30), dtos.get(0).getAge());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SELECT_DISTINCT)
    public void lambdaSelectDistinctToSingleAndMultiCols() throws Exception {
        insert(baseId() + 20, "DistinctOne", 20, "same@nxn.test");
        insert(baseId() + 21, "DistinctTwo", 20, "same@nxn.test");
        insert(baseId() + 22, "DistinctThree", 25, "same@nxn.test");
        insert(baseId() + 23, "DistinctFour", 25, "other@nxn.test");
        insert(baseId() + 24, "DistinctFive", 30, "other@nxn.test");

        List<Map<String, Object>> ages = lambdaTemplate.query(UserInfo.class)//
                .applySelect("distinct age")//
                .like(UserInfo::getName, "Distinct%")//
                .orderBy("age")//
                .queryForMapList();

        assertEquals(3, ages.size());
        assertEquals(20, ((Number) getVal(ages.get(0), "age")).intValue());
        assertEquals(25, ((Number) getVal(ages.get(1), "age")).intValue());
        assertEquals(30, ((Number) getVal(ages.get(2), "age")).intValue());

        List<Map<String, Object>> ageEmail = lambdaTemplate.query(UserInfo.class)//
                .applySelect("distinct age, email")//
                .like(UserInfo::getName, "Distinct%")//
                .queryForMapList();
        assertEquals(4, ageEmail.size());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SELECT_DISTINCT_COUNT)
    public void lambdaSelectDistinctValues() throws Exception {
        insert(baseId() + 30, "DistinctCountOne", 20, "dc1@nxn.test");
        insert(baseId() + 31, "DistinctCountTwo", 20, "dc2@nxn.test");
        insert(baseId() + 32, "DistinctCountThree", 25, "dc3@nxn.test");
        insert(baseId() + 33, "DistinctCountFour", 30, "dc4@nxn.test");

        List<Map<String, Object>> result = lambdaTemplate.query(UserInfo.class)//
                .applySelect("count(distinct age) as distinct_count")//
                .like(UserInfo::getName, "DistinctCount%")//
                .queryForMapList();

        assertEquals(1, result.size());
        assertEquals(3, ((Number) getVal(result.get(0), "distinct_count")).intValue());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SELECT_GROUP_BY)
    public void lambdaSelectRowsAndReturnAggregateMapResults() throws Exception {
        insert(baseId() + 40, "GroupOne", 10, "group1@nxn.test");
        insert(baseId() + 41, "GroupTwo", 20, "group2@nxn.test");
        insert(baseId() + 42, "GroupThree", 20, "group3@nxn.test");
        insert(baseId() + 43, "GroupFour", 30, "group4@nxn.test");

        List<Map<String, Object>> result = lambdaTemplate.query(UserInfo.class)//
                .applySelect("age, count(*) as cnt")//
                .like(UserInfo::getName, "Group%")//
                .groupBy("age")//
                .orderBy("age")//
                .queryForMapList();

        assertEquals(3, result.size());
        Map<String, Object> group20 = findByInt(result, "age", 20);
        assertNotNull(group20);
        assertEquals(2, ((Number) getVal(group20, "cnt")).intValue());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_SELECT_AGGREGATE)
    public void lambdaSelectAggregateScalarValues() throws Exception {
        insert(baseId() + 50, "AggregateOne", 10, "agg1@nxn.test");
        insert(baseId() + 51, "AggregateTwo", 20, "agg2@nxn.test");

        Long sumAge = lambdaTemplate.query(UserInfo.class)//
                .applySelect("sum(age)")//
                .like(UserInfo::getName, "Aggregate%")//
                .queryForObject(Long.class);
        assertEquals(30L, sumAge.longValue());

        Integer maxAge = lambdaTemplate.query(UserInfo.class)//
                .applySelect("max(age)")//
                .like(UserInfo::getName, "Aggregate%")//
                .queryForObject(Integer.class);
        assertEquals(20, maxAge.intValue());
    }

    private void insert(int id, String name, Integer age, String email) throws SQLException {
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, email, new Date() });
    }

    private Map<String, Object> findByInt(List<Map<String, Object>> rows, String key, int value) {
        for (Map<String, Object> row : rows) {
            Object rowValue = getVal(row, key);
            if (rowValue instanceof Number && ((Number) rowValue).intValue() == value) {
                return row;
            }
        }
        return null;
    }

    private boolean containsKey(Map<String, Object> map, String key) {
        return map.containsKey(key) || map.containsKey(key.toUpperCase()) || map.containsKey(key.toLowerCase());
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
}
