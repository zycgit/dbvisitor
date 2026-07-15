package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class XmlMapperSqlFragmentContractTest extends AbstractNxnContractTest {
    private Session session;

    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration config = newConfiguration();
        config.loadMapper("/mapper/XmlSqlFragmentMapper.xml");
        this.session = config.newSession(dataSource);
    }

    @Override
    protected void initData() throws SQLException {
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", //
                    new Object[] { baseId() + i, "SqlFrag" + i, 20 + i * 5, "frag" + i + "@nxn.test" });
        }
    }

    protected int baseId() {
        return 953000;
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_FRAGMENT_COLUMNS)
    public void sqlFragment_shouldIncludeReusableColumnListInSelect() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.SqlFragmentMapper.selectWithColumnFragment", mapOf("id", baseId() + 1));

        assertEquals(1, list.size());
        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(baseId() + 1), user.getId());
        assertEquals("SqlFrag1", user.getName());
        assertEquals(Integer.valueOf(25), user.getAge());
        assertEquals("frag1@nxn.test", user.getEmail());
        assertNotNull(user.getCreateTime());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_FRAGMENT_DYNAMIC_CONDITION)
    public void sqlFragment_shouldIncludeDynamicConditionFragment() throws Exception {
        List<UserInfo> allRows = this.session.queryStatement("xmltest.SqlFragmentMapper.selectWithConditionFragment", new HashMap<String, Object>());
        List<UserInfo> byName = this.session.queryStatement("xmltest.SqlFragmentMapper.selectWithConditionFragment", mapOf("name", "SqlFrag1"));
        Map<String, Object> ageRange = mapOf("minAge", 30);
        ageRange.put("maxAge", 40);
        List<UserInfo> byAgeRange = this.session.queryStatement("xmltest.SqlFragmentMapper.selectWithConditionFragment", ageRange);
        Map<String, Object> allConditions = mapOf("name", "SqlFrag%");
        allConditions.put("minAge", 25);
        allConditions.put("maxAge", 35);
        List<UserInfo> combined = this.session.queryStatement("xmltest.SqlFragmentMapper.selectWithConditionFragment", allConditions);

        assertEquals(5, allRows.size());
        assertEquals(1, byName.size());
        assertEquals("SqlFrag1", byName.get(0).getName());
        assertEquals(3, byAgeRange.size());
        for (UserInfo user : byAgeRange) {
            assertTrue(user.getAge() >= 30 && user.getAge() <= 40);
        }
        assertEquals(3, combined.size());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_FRAGMENT_MULTIPLE)
    public void sqlFragment_shouldCombineColumnAndOrderFragments() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.SqlFragmentMapper.selectWithMultipleFragments", null);

        assertEquals(5, list.size());
        assertAscendingById(list);
        assertEquals("SqlFrag1", list.get(0).getName());
        assertNotNull(list.get(0).getCreateTime());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_FRAGMENT_ORDER)
    public void sqlFragment_shouldIncludeOrderFragmentOnly() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.SqlFragmentMapper.selectAllOrdered", null);

        assertEquals(5, list.size());
        assertAscendingById(list);
    }

    private void assertAscendingById(List<UserInfo> list) {
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getId() < list.get(i).getId());
        }
    }

    private Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
    }
}
