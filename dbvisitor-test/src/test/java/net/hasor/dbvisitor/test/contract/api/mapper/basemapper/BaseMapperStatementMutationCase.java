/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class BaseMapperStatementMutationCase extends BaseMapperStatementSupport {
    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_EXECUTE_DML)
    public void baseMapperStatement_shouldExecuteDmlStatements() {
        int id = baseId() + 1;

        assertEquals(1, execute("insertUserWithId", userParams(id, "BaseStmtDml", 25, "base-stmt@nxn.test")));
        assertEquals("BaseStmtDml", queryOne("queryUserById", mapOf("id", id)).getName());

        assertEquals(1, execute("updateUserEmail", mapOf("id", id, "email", "base-stmt-new@nxn.test")));
        assertEquals("base-stmt-new@nxn.test", queryOne("queryUserById", mapOf("id", id)).getEmail());

        assertEquals(1, execute("deleteUserById", mapOf("id", id)));
        assertTrue(query("queryUserById", mapOf("id", id)).isEmpty());
    }

    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_BATCH_DELETE)
    public void baseMapperStatement_shouldExecuteBatchDeleteByStatement() {
        for (int i = 1; i <= 5; i++) {
            insert(baseId() + 200 + i, "BaseStmtDelete" + i, 40 + i);
        }

        int deleted = execute("deleteUsersByName", mapOf("name", "BaseStmtDelete%"));

        assertMutationRows(5, deleted);
        assertTrue(query("queryUsersByName", mapOf("name", "BaseStmtDelete%")).isEmpty());
    }
}
