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
public abstract class XmlMapperResultHandlerContractTest extends AbstractNxnContractTest {
    private Session session;

    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration config = newConfiguration();
        config.loadMapper("/mapper/XmlResultHandlerMapper.xml");
        this.session = config.newSession(dataSource);
    }

    @Override
    protected void initData() throws SQLException {
        // @formatter:off
        Object[][] data = {
            { baseId() + 1, "ResHdl1", 25, "hdl1@nxn.test" },
            { baseId() + 2, "ResHdl2", 30, "hdl2@nxn.test" },
            { baseId() + 3, "ResHdl3", 35, "hdl3@nxn.test" }
        };
        // @formatter:on
        for (Object[] row : data) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", //
                    row);
        }
    }

    protected int baseId() {
        return 956000;
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULT_HANDLER_RESULT_TYPE)
    public void resultHandler_shouldSupportEntityMapAndScalarResultTypes() throws Exception {
        List<UserInfo> entities = this.session.queryStatement("xmltest.ResultHandlerMapper.selectByResultType", mapOf("minAge", 20));
        List<Map<String, Object>> maps = this.session.queryStatement("xmltest.ResultHandlerMapper.selectByResultTypeMap", null);
        List<Integer> counts = this.session.queryStatement("xmltest.ResultHandlerMapper.countByResultType", null);
        List<String> names = this.session.queryStatement("xmltest.ResultHandlerMapper.selectNamesByResultType", null);

        assertEquals(3, entities.size());
        assertEquals(Integer.valueOf(baseId() + 1), entities.get(0).getId());
        assertEquals("ResHdl1", entities.get(0).getName());

        assertEquals(3, maps.size());
        assertEquals("ResHdl1", value(maps.get(0), "name"));

        assertEquals(1, counts.size());
        assertEquals(3, counts.get(0).intValue());

        assertEquals(3, names.size());
        assertEquals("ResHdl1", names.get(0));
        assertEquals("ResHdl3", names.get(2));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULT_HANDLER_RESULT_MAP)
    public void resultHandler_shouldSupportNamedResultMap() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.ResultHandlerMapper.selectByResultMap", null);

        assertEquals(3, list.size());
        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(baseId() + 1), user.getId());
        assertEquals("ResHdl1", user.getName());
        assertEquals(Integer.valueOf(25), user.getAge());
        assertEquals("hdl1@nxn.test", user.getEmail());
        assertNotNull(user.getCreateTime());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULT_HANDLER_ROW_MAPPER)
    public void resultHandler_shouldSupportCustomRowMapper() throws Exception {
        List<Map<String, Object>> list = this.session.queryStatement("xmltest.ResultHandlerMapper.selectByColumnMapRowMapper", null);

        assertEquals(3, list.size());
        assertNotNull(value(list.get(0), "id"));
        assertEquals("ResHdl1", value(list.get(0), "name"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULT_HANDLER_EXTRACTOR)
    @SuppressWarnings("unchecked")
    public void resultHandler_shouldSupportResultSetExtractors() throws Exception {
        Object columnMapResult = this.session.queryStatement("xmltest.ResultHandlerMapper.selectByColumnMapExtractor", null);
        assertTrue(columnMapResult instanceof List);
        List<Map<String, Object>> rows = (List<Map<String, Object>>) columnMapResult;
        assertEquals(3, rows.size());
        assertEquals("ResHdl1", value(rows.get(0), "name"));

        List<Object> pairsWrappedAsList = this.session.queryStatement("xmltest.ResultHandlerMapper.selectIdNamePairs", null);
        assertEquals(1, pairsWrappedAsList.size());
        assertTrue(pairsWrappedAsList.get(0) instanceof Map);
        Map<Object, Object> pairs = (Map<Object, Object>) pairsWrappedAsList.get(0);
        assertEquals(3, pairs.size());
        assertEquals("ResHdl1", pairValue(pairs, baseId() + 1));
    }

    private Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
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

    private Object pairValue(Map<Object, Object> pairs, int key) {
        Object value = pairs.get(key);
        if (value != null) {
            return value;
        }
        for (Map.Entry<Object, Object> entry : pairs.entrySet()) {
            Object entryKey = entry.getKey();
            if (entryKey instanceof Number && ((Number) entryKey).longValue() == key) {
                return entry.getValue();
            }
        }
        return null;
    }
}
