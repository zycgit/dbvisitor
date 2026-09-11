/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class BaseMapperStatementResultContractTest extends BaseMapperStatementSupport {
    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_QUERY_RESULT)
    public void baseMapperStatement_shouldQueryResultMapAndResultTypeStatements() {
        insert(baseId() + 11, "BaseStmtQuery1", 31);
        insert(baseId() + 12, "BaseStmtQuery2", 32);
        insert(baseId() + 13, "BaseStmtQuery3", 33);

        UserInfo byId = queryOne("queryUserById", mapOf("id", baseId() + 11));
        assertEquals(Integer.valueOf(baseId() + 11), byId.getId());
        assertEquals("BaseStmtQuery1", byId.getName());
        assertNotNull(byId.getCreateTime());

        List<UserInfo> byName = query("queryUsersByName", mapOf("name", "BaseStmtQuery%"));
        assertEquals(3, byName.size());
        assertEquals(Integer.valueOf(baseId() + 11), byName.get(0).getId());

        List<UserInfo> all = this.mapper.queryStatement(NS + ".queryAllUsers", null);
        assertTrue(all.size() >= 3);

        assertTrue(query("queryUserById", mapOf("id", baseId() + 999)).isEmpty());
    }
}
