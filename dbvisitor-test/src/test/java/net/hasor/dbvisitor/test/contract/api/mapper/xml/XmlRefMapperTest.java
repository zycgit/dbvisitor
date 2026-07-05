package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.XmlRefMapperDao;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class XmlRefMapperTest extends AbstractNxnContractTest {
    private XmlRefMapperDao dao;

    @Before
    public void createRefMapper() throws Exception {
        Configuration config = newConfiguration();
        Session session = config.newSession(dataSource);
        this.dao = session.createMapper(XmlRefMapperDao.class);
    }

    @Override
    protected void initData() throws SQLException {
        Object[][] data = { //
                { baseId() + 1, "RefMapA", 22, "refa@nxn.test" }, //
                { baseId() + 2, "RefMapB", 28, "refb@nxn.test" }, //
                { baseId() + 3, "RefMapC", 35, "refc@nxn.test" }, //
                { baseId() + 4, "RefMapD", 28, "refd@nxn.test" } //
        };
        for (Object[] row : data) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", //
                    row);
        }
    }

    protected int baseId() {
        return 954000;
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_CRUD)
    public void refMapperXmlStmtsToDaoCrudMethods() throws Exception {
        assertEquals(1, this.dao.insertUser(baseId() + 10, "RefMapNew", 33, "new@nxn.test"));

        UserInfo inserted = this.dao.selectById(baseId() + 10);
        assertEquals(Integer.valueOf(baseId() + 10), inserted.getId());
        assertEquals("RefMapNew", inserted.getName());
        assertEquals("new@nxn.test", inserted.getEmail());

        List<UserInfo> all = this.dao.selectAll();
        assertEquals(5, all.size());
        assertEquals("RefMapA", all.get(0).getName());

        assertEquals(1, this.dao.updateEmail(baseId() + 1, "updated@nxn.test"));
        assertEquals("updated@nxn.test", this.dao.selectById(baseId() + 1).getEmail());

        assertEquals(1, this.dao.deleteById(baseId() + 4));
        assertNull(this.dao.selectById(baseId() + 4));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_DYNAMIC)
    public void refMapperXmlDynamicWhereStmts() throws Exception {
        List<UserInfo> all = this.dao.selectByCondition(null, null);
        List<UserInfo> byName = this.dao.selectByCondition("RefMapA", null);
        List<UserInfo> byNameAndAge = this.dao.selectByCondition("RefMap%", 30);

        assertEquals(4, all.size());
        assertEquals(1, byName.size());
        assertEquals("RefMapA", byName.get(0).getName());
        assertEquals(1, byNameAndAge.size());
        assertEquals("RefMapC", byNameAndAge.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_FOREACH)
    public void refMapperExpandForeachInClause() throws Exception {
        List<UserInfo> list = this.dao.selectByIds(Arrays.asList(baseId() + 1, baseId() + 3));

        assertEquals(2, list.size());
        assertEquals("RefMapA", list.get(0).getName());
        assertEquals("RefMapC", list.get(1).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_PARAMETER)
    public void refMapperMapAndBeanStyleParams() throws Exception {
        List<UserInfo> byRange = this.dao.selectByAgeRange(25, 30);
        List<UserInfo> byBean = this.dao.selectByBean("RefMap%", 28);

        assertEquals(2, byRange.size());
        assertEquals("RefMapB", byRange.get(0).getName());
        assertEquals("RefMapD", byRange.get(1).getName());

        assertEquals(3, byBean.size());
        assertEquals("RefMapB", byBean.get(0).getName());
        assertEquals("RefMapC", byBean.get(1).getName());
        assertEquals("RefMapD", byBean.get(2).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_TEXT_AND_MAP)
    public void refMapperTextReplacementAndMapResults() throws Exception {
        List<UserInfo> byId = this.dao.selectWithOrderBy("id");
        List<UserInfo> byAge = this.dao.selectWithOrderBy("age");
        List<Map<String, Object>> stats = this.dao.selectAgeStats();

        assertEquals(4, byId.size());
        assertAscendingById(byId);

        assertEquals(4, byAge.size());
        for (int i = 1; i < byAge.size(); i++) {
            assertTrue(byAge.get(i - 1).getAge() <= byAge.get(i).getAge());
        }

        assertTrue(stats.size() >= 3);
        boolean foundAge28 = false;
        for (Map<String, Object> row : stats) {
            Number age = (Number) value(row, "age");
            if (age.intValue() == 28) {
                assertEquals(2L, ((Number) value(row, "cnt")).longValue());
                foundAge28 = true;
            }
        }
        assertTrue(foundAge28);
    }

    private void assertAscendingById(List<UserInfo> list) {
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getId() < list.get(i).getId());
        }
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
}
