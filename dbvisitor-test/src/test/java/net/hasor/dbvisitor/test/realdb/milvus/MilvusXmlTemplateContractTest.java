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
import java.util.HashMap;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusXmlTemplateContractTest extends MilvusXmlResultSqlSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_XML_DYNAMIC_FILTERS)
    public void dynamicTagsAndRulesShouldPreserveMandatoryScope() throws Exception {
        try (Session session = prepareSession()) {
            for (String statement : List.of("dynamicTags", "dynamicRules")) {
                Map<String, Object> values = new HashMap<>(parameters(2));
                values.put("name", null);
                values.put("ids", null);
                List<Map<String, Object>> all = session.queryStatement("milvus.ResultHandlers." + statement, values);
                assertEquals(2, all.size());
                assertEquals(2L, ((Number) all.get(0).get("id")).longValue());
                values.put("ids", List.of(1, 3));
                List<Map<String, Object>> filtered = session.queryStatement("milvus.ResultHandlers." + statement, values);
                assertEquals(1, filtered.size());
                assertEquals(3L, ((Number) filtered.get(0).get("id")).longValue());
                values.put("ids", null);
                values.put("name", "name-1");
                assertTrue(session.queryStatement("milvus.ResultHandlers." + statement, values).isEmpty());
                values.put("name", "name-2");
                assertEquals(1, session.queryStatement("milvus.ResultHandlers." + statement, values).size());
            }
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_XML_FRAGMENTS)
    public void xmlFragmentsShouldComposeColumnsConditionsAndNativeOrdering() throws Exception {
        try (Session session = prepareSession()) {
            Map<String, Object> values = new HashMap<>(parameters(1));
            values.put("id", 1);
            List<UserInfo> columns = session.queryStatement("milvus.ResultHandlers.fragmentColumns", values);
            assertEquals(1, columns.size());
            UserInfo first = columns.get(0);
            assertEquals(Integer.valueOf(1), first.getId());
            assertEquals("name-1", first.getName());
            assertEquals(Integer.valueOf(21), first.getAge());
            assertEquals("row1@test.com", first.getEmail());
            assertEquals(1700000000123L, first.getCreateTime().getTime());

            List<UserInfo> all = session.queryStatement("milvus.ResultHandlers.fragmentConditions", values);
            assertEquals(3, all.size());
            values.put("name", "name-1");
            List<UserInfo> named = session.queryStatement("milvus.ResultHandlers.fragmentConditions", values);
            assertEquals(1, named.size());
            assertEquals("name-1", named.get(0).getName());
            values.put("name", null);
            values.put("minAge", 22);
            values.put("maxAge", 23);
            List<UserInfo> ranged = session.queryStatement("milvus.ResultHandlers.fragmentConditions", values);
            assertEquals(2, ranged.size());
            assertEquals(Integer.valueOf(22), ranged.get(0).getAge());
            assertEquals(Integer.valueOf(23), ranged.get(1).getAge());
            values.put("name", "name-3");
            List<UserInfo> combined = session.queryStatement("milvus.ResultHandlers.fragmentConditions", values);
            assertEquals(1, combined.size());
            assertEquals("name-3", combined.get(0).getName());

            List<UserInfo> multiple = session.queryStatement("milvus.ResultHandlers.fragmentMultiple", parameters(1));
            assertEquals(3, multiple.size());
            for (int i = 0; i < multiple.size(); i++) {
                assertEquals(Integer.valueOf(i + 1), multiple.get(i).getId());
                assertEquals("name-" + (i + 1), multiple.get(i).getName());
                assertEquals(1700000000123L, multiple.get(i).getCreateTime().getTime());
            }
            assertEquals(List.of("name-1", "name-2", "name-3"),
                    session.queryStatement("milvus.ResultHandlers.fragmentOrdering", parameters(1)));
        }
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_XML_DYNAMIC_UPDATE)
    public void dynamicSetShouldBindValueAndPreserveOtherRowsAndVector() throws Exception {
        try (Session session = prepareSession()) {
            String name = "changed '\"; 中文";
            Map<String, Object> values = new HashMap<>(parameters(1));
            values.put("id", 2);
            values.put("name", name);
            assertEquals(1, session.executeStatement("milvus.ResultHandlers.dynamicUpdate", values));
            assertEquals(List.of("name-1", name, "name-3"), session.queryStatement("milvus.ResultHandlers.names", parameters(1)));
            Map<String, Object> changed = this.jdbcTemplate.queryForMap("SELECT v FROM " + this.collection + " WHERE id = 2");
            assertEquals(List.of(2F, 0F), changed.get("v"));
        }
    }
}
