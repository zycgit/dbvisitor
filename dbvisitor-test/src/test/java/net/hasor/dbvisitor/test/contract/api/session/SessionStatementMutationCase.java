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
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class SessionStatementMutationCase extends SessionStatementSupport {
    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_EXECUTE_DML)
    public void sessionStatement_shouldExecuteInsertUpdateDeleteAndReturnAffectedRows() throws Exception {
        int id = baseId() + 1;
        assertEquals(1, execute("insertUser", userParams(id, "StmtDml", 25, "stmt-dml@nxn.test")));

        List<UserInfo> inserted = queryUsers("queryUserById", mapOf("id", id));
        assertEquals(1, inserted.size());
        assertEquals("StmtDml", inserted.get(0).getName());

        assertEquals(1, execute("updateUserEmail", mapOf("id", id, "email", "stmt-dml-new@nxn.test")));
        assertEquals("stmt-dml-new@nxn.test", queryUsers("queryUserById", mapOf("id", id)).get(0).getEmail());

        assertEquals(1, execute("deleteUserById", mapOf("id", id)));
        assertTrue(queryUsers("queryUserById", mapOf("id", id)).isEmpty());
    }
}
