package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.ResultMappingMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class AnnMapperResultMappingTest extends AbstractNxnContractTest {
    private static final String PATTERN = "AnnoResult%";

    private ResultMappingMapper mapper;

    @Before
    public void createAnnMapper() throws Exception {
        Configuration configuration = newConfiguration();
        Session session = configuration.newSession(dataSource);
        this.mapper = session.createMapper(ResultMappingMapper.class);
    }

    @Override
    protected void initData() throws SQLException {
        for (int i = 1; i <= 10; i++) {
            UserInfo user = new UserInfo();
            user.setId(baseId() + i);
            user.setName("AnnoResult" + i);
            user.setAge(20 + i);
            user.setEmail("anno-result" + i + "@nxn.test");
            user.setCreateTime(new Date());
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                    new Object[] { user.getId(), user.getName(), user.getAge(), user.getEmail(), user.getCreateTime() });
        }
    }

    protected int baseId() {
        return 951000;
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_ENTITY)
    public void queryResultFullAndPartialEntity() throws Exception {
        UserInfo full = this.mapper.selectUserById(baseId() + 1);
        UserInfo partial = this.mapper.selectUserPartial(baseId() + 5);

        assertNotNull(full);
        assertEquals(Integer.valueOf(baseId() + 1), full.getId());
        assertEquals("AnnoResult1", full.getName());
        assertEquals(Integer.valueOf(21), full.getAge());
        assertEquals("anno-result1@nxn.test", full.getEmail());
        assertNotNull(full.getCreateTime());

        assertNotNull(partial);
        assertEquals(Integer.valueOf(baseId() + 5), partial.getId());
        assertEquals("AnnoResult5", partial.getName());
        assertNull(partial.getAge());
        assertNull(partial.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_MAP)
    public void queryResultSingleAndListRowsToMap() throws Exception {
        Map<String, Object> row = this.mapper.selectUserAsMap(baseId() + 2);
        List<Map<String, Object>> rows = this.mapper.selectUsersAsMapList();

        assertEquals(baseId() + 2, number(row, "id").intValue());
        assertEquals("AnnoResult2", value(row, "name"));
        assertTrue(rows.size() >= 10);
        assertNotNull(value(rows.get(0), "id"));
        assertNotNull(value(rows.get(0), "name"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_SCALAR)
    public void queryResultSingleColToScalarTypes() throws Exception {
        assertEquals(Integer.valueOf(23), this.mapper.selectAgeById(baseId() + 3));
        assertEquals("AnnoResult4", this.mapper.selectNameById(baseId() + 4));
        assertTrue(this.mapper.selectCount() >= 10);
        assertNotNull(this.mapper.selectCreateTimeById(baseId() + 7));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_LIST)
    public void queryResultEntityAndScalarLists() throws Exception {
        List<UserInfo> users = this.mapper.selectUsersByAgeRange(21, 25);
        List<String> names = this.mapper.selectAllNames(PATTERN);
        List<Integer> ids = this.mapper.selectIdRange(baseId() + 1, baseId() + 10);
        List<Integer> distinctAges = this.mapper.selectDistinctAges(PATTERN);

        assertTrue(users.size() >= 5);
        for (UserInfo user : users) {
            assertTrue(user.getAge() >= 21 && user.getAge() <= 25);
        }
        assertTrue(names.contains("AnnoResult1"));
        assertTrue(ids.contains(baseId() + 1));
        assertEquals(distinctAges.size(), new HashSet<Integer>(distinctAges).size());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_NULL)
    public void queryResultRepresentNoRowsAndNullCols() throws Exception {
        UserInfo user = new UserInfo();
        user.setId(baseId() + 101);
        user.setName("AnnoResultNull");
        user.setAge(null);
        user.setEmail(null);
        user.setCreateTime(new Date());

        assertEquals(1, this.mapper.insertUser(user));

        UserInfo loaded = this.mapper.selectUserById(baseId() + 101);
        assertNull(this.mapper.selectUserById(baseId() + 999));
        assertNull(this.mapper.selectNameById(baseId() + 999));
        assertTrue(this.mapper.selectUsersByAgeRange(999, 1000).isEmpty());
        assertEquals("AnnoResultNull", loaded.getName());
        assertNull(loaded.getAge());
        assertNull(loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_AGGREGATE)
    public void queryResultAggregateScalarsAndMaps() throws Exception {
        Integer maxAge = this.mapper.selectMaxAge();
        Map<String, Object> stats = this.mapper.selectAgeStats(PATTERN);
        List<Map<String, Object>> grouped = this.mapper.selectCountByAge(PATTERN);

        assertTrue(maxAge >= 30);
        assertNotNull(value(stats, "minAge"));
        assertNotNull(value(stats, "maxAge"));
        assertNotNull(value(stats, "avgAge"));
        assertTrue(grouped.size() > 0);
        assertNotNull(value(grouped.get(0), "age"));
        assertNotNull(value(grouped.get(0), "cnt"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_PAGE)
    public void queryResultPageObjectToListResult() throws Exception {
        List<UserInfo> firstPage = this.mapper.selectUsersWithPagination(PATTERN, new PageObject(0, 5));
        List<UserInfo> secondPage = this.mapper.selectUsersWithPagination(PATTERN, new PageObject(1, 5));

        assertEquals(5, firstPage.size());
        assertEquals(5, secondPage.size());
        assertEquals("AnnoResult1", firstPage.get(0).getName());
        assertEquals("AnnoResult6", secondPage.get(0).getName());
        assertNotEquals(firstPage.get(0).getId(), secondPage.get(0).getId());
    }

    private Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        String lower = key.toLowerCase();
        if (row.containsKey(lower)) {
            return row.get(lower);
        }
        return row.get(key.toUpperCase());
    }

    private Number number(Map<String, Object> row, String key) {
        return (Number) value(row, key);
    }
}
