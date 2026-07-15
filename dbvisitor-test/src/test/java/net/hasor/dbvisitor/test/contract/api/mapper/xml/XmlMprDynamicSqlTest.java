package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.sql.SQLException;
import java.util.ArrayList;
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
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class XmlMprDynamicSqlTest extends AbstractNxnContractTest {
    private Session session;

    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration config = newConfiguration();
        config.loadMapper("/mapper/XmlDynamicSqlMapper.xml");
        this.session = config.newSession(dataSource);
    }

    @Override
    protected void initData() throws SQLException {
        String[] names = { "DynSqlAlice", "DynSqlBob", "DynSqlCarol", "DynSqlDave", "DynSqlEve" };
        int[] ages = { 22, 28, 35, 42, 50 };
        for (int i = 0; i < names.length; i++) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", //
                    new Object[] { baseId() + i + 1, names[i], ages[i], names[i].toLowerCase() + "@test.com" });
        }
    }

    protected int baseId() {
        return 940000;
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_IF)
    public void dynamicIfAppendOnlyMatchedConds() throws Exception {
        List<UserInfo> all = this.session.queryStatement("xmltest.DynamicSqlMapper.selectWithIf", new HashMap<String, Object>());
        List<UserInfo> minAge = this.session.queryStatement("xmltest.DynamicSqlMapper.selectWithIf", mapOf("minAge", 30));
        Map<String, Object> exactParams = mapOf("minAge", 20);
        exactParams.put("email", "dynsqldave@test.com");
        List<UserInfo> exact = this.session.queryStatement("xmltest.DynamicSqlMapper.selectWithIf", exactParams);

        assertEquals(5, all.size());
        assertEquals(3, minAge.size());
        for (UserInfo user : minAge) {
            assertTrue(user.getAge() >= 30);
        }
        assertEquals(1, exact.size());
        assertEquals("DynSqlDave", exact.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_CHOOSE)
    public void dynamicChooseSelectExpectedOrderBranch() throws Exception {
        List<UserInfo> byAge = this.session.queryStatement("xmltest.DynamicSqlMapper.selectWithChoose", mapOf("orderBy", "age"));
        List<UserInfo> fallback = this.session.queryStatement("xmltest.DynamicSqlMapper.selectWithChoose", mapOf("orderBy", "unknown"));

        assertEquals(5, byAge.size());
        assertEquals("DynSqlEve", byAge.get(0).getName());
        assertEquals("DynSqlAlice", byAge.get(4).getName());
        assertEquals(Integer.valueOf(baseId() + 1), fallback.get(0).getId());
        assertEquals(Integer.valueOf(baseId() + 5), fallback.get(4).getId());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_WHERE)
    public void dynamicWhereAndTrimRemoveLeadingCondOperators() throws Exception {
        List<UserInfo> whereSingle = this.session.queryStatement("xmltest.DynamicSqlMapper.selectWithWhere", mapOf("name", "DynSqlAlice"));
        Map<String, Object> trimParams = mapOf("name", "DynSqlCarol");
        trimParams.put("age", 35);
        List<UserInfo> trimSingle = this.session.queryStatement("xmltest.DynamicSqlMapper.selectWithTrim", trimParams);

        assertEquals(1, whereSingle.size());
        assertEquals("DynSqlAlice", whereSingle.get(0).getName());
        assertEquals(1, trimSingle.size());
        assertEquals("DynSqlCarol", trimSingle.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_SET)
    public void dynamicSetOnlyNonNullFields() throws Exception {
        Map<String, Object> update = mapOf("id", baseId() + 1);
        update.put("age", 99);
        Object result = this.session.executeStatement("xmltest.DynamicSqlMapper.updateWithSet", update);

        Map<String, Object> query = mapOf("name", "DynSqlAlice");
        query.put("age", 99);
        List<UserInfo> loaded = this.session.queryStatement("xmltest.DynamicSqlMapper.selectWithWhere", query);

        assertEquals(1, ((Number) result).intValue());
        assertEquals(1, loaded.size());
        assertEquals("dynsqlalice@test.com", loaded.get(0).getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_FOREACH)
    public void dynamicForeachInClauseAndBatchValues() throws Exception {
        requiresNxnFeature(FeatureId.XML_FOREACH_BATCH_INSERT_VALUES);
        List<UserInfo> selected = this.session.queryStatement("xmltest.DynamicSqlMapper.selectByIdList", mapOf("ids", Arrays.asList(baseId() + 1, baseId() + 3, baseId() + 5)));
        assertEquals(3, selected.size());
        assertEquals("DynSqlAlice", selected.get(0).getName());
        assertEquals("DynSqlCarol", selected.get(1).getName());
        assertEquals("DynSqlEve", selected.get(2).getName());

        List<Map<String, Object>> users = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", baseId() + 20 + i);
            user.put("name", "DynSqlBatch" + i);
            user.put("age", 30 + i);
            user.put("email", "batch" + i + "@test.com");
            users.add(user);
        }

        Object result = this.session.executeStatement("xmltest.DynamicSqlMapper.batchInsert", mapOf("users", users));
        List<UserInfo> inserted = this.session.queryStatement("xmltest.DynamicSqlMapper.selectByIdList", mapOf("ids", Arrays.asList(baseId() + 21, baseId() + 22, baseId() + 23)));

        assertEquals(3, ((Number) result).intValue());
        assertEquals(3, inserted.size());
        assertEquals("DynSqlBatch1", inserted.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_BIND)
    public void dynamicBindBoundVariableInLikePattern() throws Exception {
        List<UserInfo> all = this.session.queryStatement("xmltest.DynamicSqlMapper.selectWithBind", mapOf("name", "DynSql"));
        List<UserInfo> alice = this.session.queryStatement("xmltest.DynamicSqlMapper.selectWithBind", mapOf("name", "Alice"));

        assertEquals(5, all.size());
        assertEquals(1, alice.size());
        assertEquals("DynSqlAlice", alice.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_DYNAMIC_COMPLEX)
    public void dynamicComplexCombineWhereIfForeachAndChoose() throws Exception {
        Map<String, Object> params = mapOf("name", "DynSql%");
        params.put("ids", Arrays.asList(baseId() + 1, baseId() + 2));
        List<UserInfo> selected = this.session.queryStatement("xmltest.DynamicSqlMapper.selectComplex", params);
        List<UserInfo> young = this.session.queryStatement("xmltest.DynamicSqlMapper.selectByAgeCategory", mapOf("category", "young"));
        List<UserInfo> senior = this.session.queryStatement("xmltest.DynamicSqlMapper.selectByAgeCategory", mapOf("category", "senior"));

        assertEquals(2, selected.size());
        assertEquals(2, young.size());
        for (UserInfo user : young) {
            assertTrue(user.getAge() < 30);
        }
        assertEquals(2, senior.size());
        for (UserInfo user : senior) {
            assertTrue(user.getAge() > 40);
        }
    }

    private Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
    }
}
