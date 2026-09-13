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
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class BaseMapperStatementPaginationCase extends BaseMapperStatementSupport {
    @Test
    @Capability(CapabilityId.BASEMAPPER_STATEMENT_QUERY_PAGE)
    public void baseMapperStatement_shouldApplyPageObjectToQueryStatement() {
        for (int i = 1; i <= 10; i++) {
            insert(baseId() + 100 + i, "BaseStmtPage" + i, 20 + i);
        }

        List<UserInfo> first = this.mapper.queryStatement(NS + ".queryUsersByName", mapOf("name", "BaseStmtPage%"), page(3, 0));
        List<UserInfo> second = this.mapper.queryStatement(NS + ".queryUsersByName", mapOf("name", "BaseStmtPage%"), page(4, 1));
        List<UserInfo> beyond = this.mapper.queryStatement(NS + ".queryUsersByName", mapOf("name", "BaseStmtPage%"), page(3, 5));

        assertEquals(3, first.size());
        assertEquals(Integer.valueOf(baseId() + 101), first.get(0).getId());
        assertEquals(4, second.size());
        assertEquals(Integer.valueOf(baseId() + 105), second.get(0).getId());
        assertTrue(beyond.isEmpty());
    }
}
