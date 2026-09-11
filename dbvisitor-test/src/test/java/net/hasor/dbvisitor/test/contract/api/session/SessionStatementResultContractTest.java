/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.session;

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
public abstract class SessionStatementResultContractTest extends SessionStatementSupport {
    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_QUERY_RESULT)
    public void sessionStatement_shouldQueryResultMapListScalarAndEmptyList() throws Exception {
        insertUser(baseId() + 10, "StmtQueryOne", 40, "q1@nxn.test");
        insertUser(baseId() + 11, "StmtQueryTwo", 40, "q2@nxn.test");
        insertUser(baseId() + 12, "StmtQueryThree", 41, "q3@nxn.test");

        List<UserInfo> byId = queryUsers("queryUserById", mapOf("id", baseId() + 10));
        assertEquals(1, byId.size());
        assertEquals(Integer.valueOf(baseId() + 10), byId.get(0).getId());
        assertNotNull(byId.get(0).getCreateTime());

        assertEquals(2, queryUsers("queryUsersByAge", mapOf("age", 40)).size());
        assertEquals(3, queryUsers("queryAllUsers", null).size());

        List<Integer> counts = this.session.queryStatement(NS + ".countUsers", null);
        assertEquals(1, counts.size());
        assertEquals(Integer.valueOf(3), counts.get(0));

        assertTrue(queryUsers("queryUserById", mapOf("id", baseId() + 999)).isEmpty());
    }
}
