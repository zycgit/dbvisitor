/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusXmlResultHandlerContractTest extends MilvusXmlResultSqlSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_XML_RESULT_TYPES)
    public void xmlShouldMapEntitiesMapsScalarsAndNamedResultMap() throws Exception {
        try (Session session = prepareSession()) {
            Map<String, Object> parameters = parameters(1);
            List<UserInfo> entities = session.queryStatement("milvus.ResultHandlers.entities", parameters);
            assertEquals(3, entities.size());
            assertEquals(Integer.valueOf(1), entities.get(0).getId());
            assertEquals("name-3", entities.get(2).getName());
            for (UserInfo user : entities) {
                assertEquals("name-" + user.getId(), user.getName());
                assertEquals(Integer.valueOf(20 + user.getId()), user.getAge());
                assertEquals("row" + user.getId() + "@test.com", user.getEmail());
                assertEquals(1700000000123L, user.getCreateTime().getTime());
            }
            List<UserInfo> mapped = session.queryStatement("milvus.ResultHandlers.mapped", parameters);
            assertEquals(3, mapped.size());
            assertEquals(Integer.valueOf(2), mapped.get(1).getId());
            for (UserInfo user : mapped) {
                assertEquals("name-" + user.getId(), user.getName());
                assertEquals(Integer.valueOf(20 + user.getId()), user.getAge());
                assertEquals("row" + user.getId() + "@test.com", user.getEmail());
                assertEquals(1700000000123L, user.getCreateTime().getTime());
            }
            List<Map<String, Object>> maps = session.queryStatement("milvus.ResultHandlers.maps", parameters);
            assertEquals(3, maps.size());
            assertEquals("name-1", maps.get(0).get("name"));
            assertEquals(List.of("name-1", "name-2", "name-3"), session.queryStatement("milvus.ResultHandlers.names", parameters));
            assertEquals(List.of(3L), session.queryStatement("milvus.ResultHandlers.count", parameters));
            assertEquals(List.of(3), session.queryStatement("milvus.ResultHandlers.countInt", parameters));
            assertTrue(session.queryStatement("milvus.ResultHandlers.entities", parameters(99)).isEmpty());
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_XML_RESULT_HANDLERS)
    public void xmlShouldApplyRowMapperAndResultSetExtractors() throws Exception {
        try (Session session = prepareSession()) {
            Map<String, Object> parameters = parameters(1);
            for (String statement : List.of("rowMapper", "extractor")) {
                List<Map<String, Object>> rows = session.queryStatement("milvus.ResultHandlers." + statement, parameters);
                assertEquals(3, rows.size());
                assertEquals("name-1", rows.get(0).get("name"));
                assertEquals(3L, ((Number) rows.get(2).get("id")).longValue());
                assertTrue(session.queryStatement("milvus.ResultHandlers." + statement, parameters(99)).isEmpty());
            }
            List<Map<Object, Object>> pairs = session.queryStatement("milvus.ResultHandlers.pairs", parameters);
            assertEquals(1, pairs.size());
            assertEquals(3, pairs.get(0).size());
            for (Map.Entry<Object, Object> entry : pairs.get(0).entrySet()) {
                assertEquals("name-" + ((Number) entry.getKey()).longValue(), entry.getValue());
            }
        }
    }
}
