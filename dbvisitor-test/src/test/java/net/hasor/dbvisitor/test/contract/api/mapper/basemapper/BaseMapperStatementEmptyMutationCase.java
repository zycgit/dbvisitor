/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class BaseMapperStatementEmptyMutationCase extends BaseMapperStatementSupport {
    // 能力归属：Mapper API / 引用文件 Mapper。
    @Test
    @Capability(value = CapabilityId.BASEMAPPER_STATEMENT_UPDATE_NO_MATCH, column = "mapper/file-mapper/calls")
    public void baseMapperStatement_shouldReportNoRowsForMissingUpdate() {
        assertMutationRows(0, execute("updateUserEmail", mapOf("id", baseId() + 999, "email", "missing@nxn.test")));
    }

    // 能力归属：Mapper API / 引用文件 Mapper。
    @Test
    @Capability(value = CapabilityId.BASEMAPPER_STATEMENT_DELETE_NO_MATCH, column = "mapper/file-mapper/calls")
    public void baseMapperStatement_shouldReportNoRowsForRepeatedDelete() {
        int id = baseId() + 1;
        insert(id, "BaseStmtDeleted", 25);
        assertEquals(1, execute("deleteUserById", mapOf("id", id)));
        assertTrue(query("queryUserById", mapOf("id", id)).isEmpty());
        assertMutationRows(0, execute("deleteUserById", mapOf("id", id)));
    }
}
