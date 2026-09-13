/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class XmlMapperCallableCase extends AbstractNxnContractTest {
    private Session session;

    @Before
    public void createCallableMapperFixtures() throws Exception {
        if (!profile().supportsFeature(FeatureId.XML_MAPPER_CALLABLE)) {
            return;
        }

        createCallableDefinitions();

        Configuration config = newConfiguration();
        config.loadMapper(callableMapperPath());
        this.session = config.newSession(dataSource);
    }

    protected void createCallableDefinitions() throws Exception {
        throw new UnsupportedOperationException("XML callable fixture SQL must be provided by the concrete data source test.");
    }

    protected String callableMapperPath() {
        throw new UnsupportedOperationException("XML callable mapper must be provided by the concrete data source test.");
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_CALLABLE_IN)
    public void xmlCallable_shouldExecuteInOnlyProcedureThroughMapper() throws Exception {
        requiresNxnFeature(FeatureId.XML_MAPPER_CALLABLE);

        this.session.executeStatement("XmlCallableMapper.callInsertUser", mapOf("id", baseId() + 1, "name", "XmlCallableIn"));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_info WHERE id = ?", new Object[] { baseId() + 1 }, Integer.class);
        String name = jdbcTemplate.queryForObject("SELECT name FROM user_info WHERE id = ?", new Object[] { baseId() + 1 }, String.class);
        assertEquals(Integer.valueOf(1), count);
        assertEquals("XmlCallableIn", name);
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_CALLABLE_INOUT)
    public void xmlCallable_shouldReturnSingleInOutParameterThroughBindOut() throws Exception {
        requiresNxnFeature(FeatureId.XML_MAPPER_CALLABLE);

        Map<String, Object> result = executeForMap("XmlCallableMapper.callDoubleValue", mapOf("p_input", 7, "p_result", 0));

        assertEquals(1, result.size());
        assertTrue(result.containsKey("p_result"));
        assertEquals(14, ((Number) result.get("p_result")).intValue());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_CALLABLE_MULTI_INOUT)
    public void xmlCallable_shouldReturnMultipleInOutParametersThroughBindOut() throws Exception {
        requiresNxnFeature(FeatureId.XML_MAPPER_CALLABLE);

        Map<String, Object> params = mapOf("p_prefix", "Hello", "p_suffix", "World");
        params.put("p_concat", "");
        params.put("p_length", 0);

        Map<String, Object> result = executeForMap("XmlCallableMapper.callMultiInout", params);

        assertEquals(2, result.size());
        assertEquals("Hello-World", result.get("p_concat"));
        assertEquals(11, ((Number) result.get("p_length")).intValue());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_CALLABLE_OUTPUT_STATS)
    public void xmlCallable_shouldReturnOutputStatsThroughInOutParameters() throws Exception {
        requiresNxnFeature(FeatureId.XML_MAPPER_CALLABLE);
        insertUser(baseId() + 21, "XmlCallableAlice", 25);
        insertUser(baseId() + 22, "XmlCallableBob", 30);
        insertUser(baseId() + 23, "XmlCallableCharlie", 35);

        Map<String, Object> countResult = executeForMap("XmlCallableMapper.callUserCount", mapOf("p_count", 0));
        Map<String, Object> statsParams = mapOf("p_count", 0, "p_max_id", 0);
        statsParams.put("p_min_name", "");
        Map<String, Object> statsResult = executeForMap("XmlCallableMapper.callUserStats", statsParams);

        assertEquals(3, ((Number) countResult.get("p_count")).intValue());
        assertEquals(3, statsResult.size());
        assertEquals(3, ((Number) statsResult.get("p_count")).intValue());
        assertEquals(baseId() + 23, ((Number) statsResult.get("p_max_id")).intValue());
        assertEquals("XmlCallableAlice", statsResult.get("p_min_name"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_CALLABLE_REFCURSOR)
    public void xmlCallable_shouldReturnSingleAndMultipleRefcursorResultsInTransaction() throws Exception {
        requiresNxnFeature(FeatureId.XML_MAPPER_CALLABLE);
        requiresNxnFeature(FeatureId.PROCEDURE_CURSOR_RESULT);
        insertUser(baseId() + 31, "XmlCallableCursorMatch", 25);
        insertUser(baseId() + 32, "XmlCallableCursorOther1", 30);
        insertUser(baseId() + 33, "XmlCallableCursorOther2", 35);

        Map<String, Object> single = inTransaction(new TransactionalCall() {
            @Override
            public Map<String, Object> run() throws Exception {
                Map<String, Object> params = mapOf("p_name", "XmlCallableCursorMatch", "p_out", "");
                return executeForMap("XmlCallableMapper.callQueryUsers", params);
            }
        });

        Map<String, Object> multi = inTransaction(new TransactionalCall() {
            @Override
            public Map<String, Object> run() throws Exception {
                Map<String, Object> params = mapOf("p_name", "XmlCallableCursorMatch", "p_out_msg", "");
                return executeForMap("XmlCallableMapper.callQueryUsersMulti", params);
            }
        });

        assertEquals("found:XmlCallableCursorMatch", single.get("p_out"));
        assertTrue(single.get("res1") instanceof List);
        assertEquals(1, ((List<?>) single.get("res1")).size());
        assertEquals("query:XmlCallableCursorMatch", multi.get("p_out_msg"));
        assertTrue(multi.get("res_matched") instanceof List);
        assertTrue(multi.get("res_unmatched") instanceof List);
        assertEquals(1, ((List<?>) multi.get("res_matched")).size());
        assertEquals(2, ((List<?>) multi.get("res_unmatched")).size());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_CALLABLE_BIND_OUT)
    public void xmlCallable_shouldFilterBindOutAndReturnFullResultWhenBindOutIsAbsent() throws Exception {
        requiresNxnFeature(FeatureId.XML_MAPPER_CALLABLE);
        insertUser(baseId() + 41, "XmlCallableFullMap", 25);

        Map<String, Object> filtered = executeForMap("XmlCallableMapper.callDoubleValue", mapOf("p_input", 5, "p_result", 0));

        assertEquals(1, filtered.size());
        assertEquals(10, ((Number) filtered.get("p_result")).intValue());

        if (profile().supportsFeature(FeatureId.PROCEDURE_CURSOR_RESULT)) {
            Map<String, Object> full = inTransaction(new TransactionalCall() {
                @Override
                public Map<String, Object> run() throws Exception {
                    Map<String, Object> params = mapOf("p_name", "XmlCallableFullMap", "p_out", "");
                    return executeForMap("XmlCallableMapper.callQueryUsersNoBind", params);
                }
            });
            assertTrue(full.size() > 1);
        }
    }

    protected int baseId() {
        return 970000;
    }

    private void insertUser(int id, String name, int age) throws SQLException {
        jdbcTemplate.executeUpdate(//
                "INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, @{macro, currentTimestamp})", //
                new Object[] { id, name, age });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> executeForMap(String statementId, Map<String, Object> params) throws Exception {
        Object result = this.session.executeStatement(statementId, params);
        assertNotNull(result);
        assertTrue("Expected result Map from " + statementId, result instanceof Map);
        return (Map<String, Object>) result;
    }

    private Map<String, Object> inTransaction(TransactionalCall call) throws Exception {
        TransactionManager txManager = TransactionHelper.txManager(dataSource);
        TransactionStatus tx = txManager.begin(Propagation.REQUIRED);
        try {
            Map<String, Object> result = call.run();
            txManager.commit(tx);
            return result;
        } catch (Exception e) {
            txManager.rollBack(tx);
            throw e;
        }
    }

    private Map<String, Object> mapOf(Object... pairs) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], pairs[i + 1]);
        }
        return map;
    }

    private interface TransactionalCall {
        Map<String, Object> run() throws Exception;
    }
}
