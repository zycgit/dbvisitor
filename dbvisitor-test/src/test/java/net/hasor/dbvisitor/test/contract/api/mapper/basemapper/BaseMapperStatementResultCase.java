/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.cobble.ref.BeanMap;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class BaseMapperStatementResultCase extends BaseMapperStatementSupport {
    protected void prepareProjectionData() throws Exception {
        insert(baseId() + 11, "BaseStmtQuery1", 31);
        insert(baseId() + 12, "BaseStmtQuery2", 32);
        insert(baseId() + 13, "BaseStmtQuery3", 33);
    }

    protected Map<String, Object> expectedProjection(int id, String name) {
        return mapOf("id", id, "name", name);
    }

    protected List<String> requiredProjectionProperties() {
        return Collections.singletonList("createTime");
    }

    protected List<?> queryProjection(String statementId, Object parameters) {
        return this.mapper.queryStatement(NS + "." + statementId, parameters);
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_QUERY_RESULT)
    public void baseMapperStatement_shouldQueryResultMapAndResultTypeStatements() throws Exception {
        prepareProjectionData();

        List<?> byId = queryProjection("queryUserById", mapOf("id", baseId() + 11));
        assertEquals(1, byId.size());
        BeanMap properties = new BeanMap(byId.get(0));
        for (Map.Entry<String, Object> expected : expectedProjection(baseId() + 11, "BaseStmtQuery1").entrySet()) {
            assertEquals(expected.getValue(), properties.get(expected.getKey()));
        }
        for (String property : requiredProjectionProperties()) {
            assertNotNull(properties.get(property));
        }

        List<?> byName = queryProjection("queryUsersByName", mapOf("name", "BaseStmtQuery%"));
        assertEquals(3, byName.size());
        assertEquals(Integer.valueOf(baseId() + 11), new BeanMap(byName.get(0)).get("id"));
        for (int i = 0; i < byName.size(); i++) {
            BeanMap row = new BeanMap(byName.get(i));
            for (Map.Entry<String, Object> expected : expectedProjection(baseId() + 11 + i, "BaseStmtQuery" + (i + 1)).entrySet()) {
                assertEquals(expected.getValue(), row.get(expected.getKey()));
            }
        }

        List<?> all = queryProjection("queryAllUsers", null);
        assertTrue(all.size() >= 3);
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> expected = expectedProjection(baseId() + 10 + i, "BaseStmtQuery" + i);
            boolean found = false;
            for (Object item : all) {
                if (new BeanMap(item).entrySet().containsAll(expected.entrySet())) {
                    found = true;
                    break;
                }
            }
            assertTrue("resultType should map " + expected, found);
        }

        assertTrue(queryProjection("queryUserById", mapOf("id", baseId() + 999)).isEmpty());
    }
}
