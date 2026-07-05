package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.sql.SQLException;
import java.util.Arrays;
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

import static org.junit.Assert.assertEquals;

public abstract class XmlMapperDynamicRuleContractTest extends AbstractNxnContractTest {
    private Session session;

    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration config = newConfiguration();
        config.loadMapper("/mapper/XmlDynamicRuleMapper.xml");
        this.session = config.newSession(dataSource);
    }

    @Override
    protected void initData() throws SQLException {
        String[] names = { "DynRuleA", "DynRuleB", "DynRuleC", "DynRuleD" };
        int[] ages = { 22, 28, 35, 45 };
        String[] emails = { "a@nxn.test", "b@nxn.test", "c@nxn.test", "d@nxn.test" };
        for (int i = 0; i < names.length; i++) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", //
                    new Object[] { baseId() + i + 1, names[i], ages[i], emails[i] });
        }
    }

    protected int baseId() {
        return 949000;
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_RULE_AND)
    public void dynamicRuleAnd_shouldAppendConditionsOnlyForPresentParameters() throws Exception {
        List<UserInfo> single = this.session.queryStatement("xmltest.DynamicRuleMapper.selectWithAndSingle", mapOf("age", 28));

        Map<String, Object> exactParams = mapOf("age", 35);
        exactParams.put("email", "c@nxn.test");
        List<UserInfo> exact = this.session.queryStatement("xmltest.DynamicRuleMapper.selectWithAndDouble", exactParams);

        List<UserInfo> allWhenNull = this.session.queryStatement("xmltest.DynamicRuleMapper.selectWithAndDouble", new HashMap<String, Object>());
        List<UserInfo> partial = this.session.queryStatement("xmltest.DynamicRuleMapper.selectWithAndDouble", mapOf("age", 28));

        assertEquals(1, single.size());
        assertEquals("DynRuleB", single.get(0).getName());
        assertEquals(1, exact.size());
        assertEquals("DynRuleC", exact.get(0).getName());
        assertEquals(4, allWhenNull.size());
        assertEquals(1, partial.size());
        assertEquals("DynRuleB", partial.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_RULE_OR)
    public void dynamicRuleOr_shouldAppendUnionConditions() throws Exception {
        Map<String, Object> oneMatch = mapOf("age", 22);
        oneMatch.put("email", "no-match@nxn.test");
        List<UserInfo> byAge = this.session.queryStatement("xmltest.DynamicRuleMapper.selectWithOrRule", oneMatch);

        Map<String, Object> twoMatches = mapOf("age", 22);
        twoMatches.put("email", "d@nxn.test");
        List<UserInfo> byAgeOrEmail = this.session.queryStatement("xmltest.DynamicRuleMapper.selectWithOrRule", twoMatches);

        assertEquals(1, byAge.size());
        assertEquals("DynRuleA", byAge.get(0).getName());
        assertEquals(2, byAgeOrEmail.size());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_RULE_IN)
    public void dynamicRuleIn_shouldExpandNestedInRuleInsideAndRule() throws Exception {
        List<UserInfo> selected = this.session.queryStatement("xmltest.DynamicRuleMapper.selectWithInRule", mapOf("ids", Arrays.asList(baseId() + 1, baseId() + 3)));

        assertEquals(2, selected.size());
        assertEquals("DynRuleA", selected.get(0).getName());
        assertEquals("DynRuleC", selected.get(1).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_RULE_MIXED)
    public void dynamicRuleMixed_shouldComposeRuleSyntaxWithXmlTags() throws Exception {
        Map<String, Object> params = mapOf("minAge", 25);
        params.put("email", "c@nxn.test");

        List<UserInfo> selected = this.session.queryStatement("xmltest.DynamicRuleMapper.selectMixed", params);

        assertEquals(1, selected.size());
        assertEquals("DynRuleC", selected.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_RULE_MULTIPLE)
    public void dynamicRuleMultiple_shouldCombineSeveralAndRules() throws Exception {
        Map<String, Object> params = mapOf("name", "DynRule%");
        params.put("minAge", 25);
        params.put("maxAge", 40);

        List<UserInfo> selected = this.session.queryStatement("xmltest.DynamicRuleMapper.selectMultipleAndRules", params);

        assertEquals(2, selected.size());
        assertEquals("DynRuleB", selected.get(0).getName());
        assertEquals("DynRuleC", selected.get(1).getName());
    }

    private Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
    }
}
