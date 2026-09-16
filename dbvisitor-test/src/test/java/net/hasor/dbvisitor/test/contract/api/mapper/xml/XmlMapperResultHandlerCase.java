/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.sql.SQLException;
import java.util.*;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.handler.ResultHandlerProbe;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class XmlMapperResultHandlerCase extends AbstractNxnContractTest {
    protected Session session;

    @Before
    public void createXmlMapperSession() throws Exception {
        Configuration config = newConfiguration();
        config.loadMapper(mapperResource());
        this.session = config.newSession(dataSource);
    }

    protected String mapperResource() {
        return "/mapper/XmlResultHandlerMapper.xml";
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
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                    new Object[] { row[0], row[1], row[2], row[3], new Date(timestamp()) });
        }
    }

    protected int baseId() {
        return 956000;
    }

    protected long timestamp() {
        return 1700000000000L;
    }

    // 能力归属：Mapper 文件 / 映射结果集。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_RESULT_HANDLER_RESULT_TYPE, column = "mapper-files/dynamic-sql-and-result-mapping/resultmap")
    public void resultHandler_shouldSupportEntityMapAndScalarResultTypes() throws Exception {
        List<?> entities = this.session.queryStatement("xmltest.ResultHandlerMapper.selectByResultType", mapOf("minAge", 20));
        List<Map<String, Object>> maps = this.session.queryStatement("xmltest.ResultHandlerMapper.selectByResultTypeMap", null);
        List<Integer> counts = this.session.queryStatement("xmltest.ResultHandlerMapper.countByResultType", null);
        List<String> names = this.session.queryStatement("xmltest.ResultHandlerMapper.selectNamesByResultType", null);

        assertEquals(3, entities.size());

        assertEquals(3, maps.size());

        assertEquals(1, counts.size());
        assertEquals(3, counts.get(0).intValue());

        assertEquals(3, names.size());
        assertEquals("ResHdl1", names.get(0));
        assertEquals("ResHdl3", names.get(2));
        assertEquals(List.of("ResHdl1", "ResHdl2", "ResHdl3"), names);
        assertEntities(entities);
        assertMaps(maps);
        assertEquals(List.of(3L), this.session.queryStatement("xmltest.ResultHandlerMapper.countByResultTypeLong", null));
        assertTrue(this.session.queryStatement("xmltest.ResultHandlerMapper.selectByResultType", mapOf("minAge", 99)).isEmpty());
    }

    // 能力归属：Mapper 文件 / 映射结果集。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_RESULT_HANDLER_RESULT_MAP, column = "mapper-files/dynamic-sql-and-result-mapping/resultmap")
    public void resultHandler_shouldSupportNamedResultMap() throws Exception {
        List<?> list = this.session.queryStatement("xmltest.ResultHandlerMapper.selectByResultMap", null);

        assertEquals(3, list.size());
        assertEntities(list);
    }

    // 能力归属：结果接收 / rowmapper / mapper-file。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_RESULT_HANDLER_ROW_MAPPER, column = "results/rowmapper/mapping", variants = { "mapper-file" })
    public void resultHandler_shouldSupportCustomRowMapper() throws Exception {
        List<Map<String, Object>> list = ResultHandlerProbe.verify(3, () -> this.session.queryStatement("xmltest.ResultHandlerMapper.selectByColumnMapRowMapper", null));

        assertEquals(3, list.size());
        assertMaps(list);
        assertTrue(ResultHandlerProbe.verify(0, () -> this.session.queryStatement("xmltest.ResultHandlerMapper.selectByColumnMapRowMapper", mapOf("minAge", 99))).isEmpty());
        ResultHandlerProbe.verifyFailure(() -> this.session.queryStatement("xmltest.ResultHandlerMapper.selectByColumnMapRowMapper", null));
    }

    // 能力归属：结果接收 / resultsetextractor / mapper-file。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_RESULT_HANDLER_EXTRACTOR, column = "results/resultsetextractor/extraction", variants = { "mapper-file" })
    @SuppressWarnings("unchecked")
    public void resultHandler_shouldSupportResultSetExtractors() throws Exception {
        Object columnMapResult = ResultHandlerProbe.verify(1, () -> this.session.queryStatement("xmltest.ResultHandlerMapper.selectByColumnMapExtractor", null));
        assertTrue(columnMapResult instanceof List);
        List<Map<String, Object>> rows = (List<Map<String, Object>>) columnMapResult;
        assertEquals(3, rows.size());
        assertMaps(rows);
        assertTrue(ResultHandlerProbe.verify(1, () -> this.session.queryStatement("xmltest.ResultHandlerMapper.selectByColumnMapExtractor", mapOf("minAge", 99))).isEmpty());
        ResultHandlerProbe.verifyFailure(() -> this.session.queryStatement("xmltest.ResultHandlerMapper.selectByColumnMapExtractor", null));

        List<Object> pairsWrappedAsList = this.session.queryStatement("xmltest.ResultHandlerMapper.selectIdNamePairs", null);
        assertEquals(1, pairsWrappedAsList.size());
        assertTrue(pairsWrappedAsList.get(0) instanceof Map);
        Map<Object, Object> pairs = (Map<Object, Object>) pairsWrappedAsList.get(0);
        assertEquals(3, pairs.size());
        for (Map.Entry<Object, Object> entry : expectedPairs().entrySet()) {
            assertEquals(entry.getValue(), pairValue(pairs, entry.getKey()));
        }
    }

    // 能力归属：结果接收 / RowCallbackHandler / Mapper 文件。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_RESULT_HANDLER_CALLBACK, column = "results/rowcallbackhandler/callback", variants = { "mapper-file" })
    public void resultHandler_shouldInvokeRowCallback() throws Exception {
        try {
            CountingCallback.COUNT.set(0);
            assertTrue(ResultHandlerProbe.verify(3, () -> this.session.queryStatement("xmltest.ResultHandlerMapper.selectByRowCallback", null)).isEmpty());
            assertEquals(3, CountingCallback.COUNT.get().intValue());
            CountingCallback.COUNT.set(0);
            assertTrue(ResultHandlerProbe.verify(0, () -> this.session.queryStatement("xmltest.ResultHandlerMapper.selectByRowCallback", mapOf("minAge", 99))).isEmpty());
            assertEquals(0, CountingCallback.COUNT.get().intValue());
            ResultHandlerProbe.verifyFailure(() -> this.session.queryStatement("xmltest.ResultHandlerMapper.selectByRowCallback", null));
        } finally {
            CountingCallback.COUNT.remove();
        }
    }

    public static class CountingCallback implements net.hasor.dbvisitor.jdbc.RowCallbackHandler {
        private static final ThreadLocal<Integer> COUNT = ThreadLocal.withInitial(() -> 0);

        @Override
        public void processRow(java.sql.ResultSet rs, int rowNum) throws SQLException {
            ResultHandlerProbe.record(rs);
            assertEquals(COUNT.get().intValue(), rowNum);
            assertNotNull(rs.getObject(1));
            COUNT.set(COUNT.get() + 1);
        }
    }

    private void assertEntities(List<?> rows) throws Exception {
        for (int i = 1; i <= rows.size(); i++) {
            Object row = rows.get(i - 1);
            for (Map.Entry<String, Object> entry : expectedEntity(i).entrySet()) {
                String name = entry.getKey();
                String getter = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
                assertEquals(name, entry.getValue(), row.getClass().getMethod(getter).invoke(row));
            }
        }
    }

    private void assertMaps(List<Map<String, Object>> rows) {
        for (int i = 1; i <= rows.size(); i++) {
            for (Map.Entry<String, Object> entry : expectedMap(i).entrySet()) {
                Object actual = value(rows.get(i - 1), entry.getKey());
                assertNotNull(entry.getKey(), actual);
                if (entry.getValue() instanceof Number) {
                    assertEquals(((Number) entry.getValue()).doubleValue(), ((Number) actual).doubleValue(), 0.0);
                } else {
                    assertEquals(entry.getValue(), actual);
                }
            }
        }
    }

    protected Map<String, Object> expectedEntity(int offset) {
        return Map.of("id", baseId() + offset, "name", "ResHdl" + offset, "age", 20 + offset * 5, "email", "hdl" + offset + "@nxn.test", "createTime", new Date(timestamp()));
    }

    protected Map<String, Object> expectedMap(int offset) {
        return Map.of("id", baseId() + offset, "name", "ResHdl" + offset);
    }

    protected Map<Object, Object> expectedPairs() {
        Map<Object, Object> pairs = new LinkedHashMap<>();
        for (int i = 1; i <= 3; i++) {
            pairs.put(baseId() + i, "ResHdl" + i);
        }
        return pairs;
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

    private Object pairValue(Map<Object, Object> pairs, Object key) {
        Object value = pairs.get(key);
        if (value != null) {
            return value;
        }
        for (Map.Entry<Object, Object> entry : pairs.entrySet()) {
            Object entryKey = entry.getKey();
            if (entryKey instanceof Number && key instanceof Number && ((Number) entryKey).longValue() == ((Number) key).longValue()) {
                return entry.getValue();
            }
        }
        return null;
    }
}
