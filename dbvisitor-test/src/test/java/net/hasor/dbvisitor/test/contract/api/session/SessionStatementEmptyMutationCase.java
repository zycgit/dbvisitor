/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.session;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class SessionStatementEmptyMutationCase extends SessionStatementSupport {
    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_UPDATE_NO_MATCH)
    public void sessionStatement_shouldReportNoRowsForMissingUpdate() throws Exception {
        assertMutationRows(0, execute("updateUserEmail", mapOf("id", baseId() + 999, "email", "none@nxn.test")));
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_DELETE_NO_MATCH)
    public void sessionStatement_shouldReportNoRowsForRepeatedDelete() throws Exception {
        int id = baseId() + 1;
        insertUser(id, "StmtDeleted", 25, "stmt-deleted@nxn.test");
        assertEquals(1, execute("deleteUserById", mapOf("id", id)));
        assertTrue(queryUsers("queryUserById", mapOf("id", id)).isEmpty());
        assertMutationRows(0, execute("deleteUserById", mapOf("id", id)));
    }
}
