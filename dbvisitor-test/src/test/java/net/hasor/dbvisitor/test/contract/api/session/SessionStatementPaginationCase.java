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

import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class SessionStatementPaginationCase extends SessionStatementSupport {
    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_QUERY_PAGE)
    public void sessionStatement_shouldApplyPageObjectToQueryStatement() throws Exception {
        for (int i = 1; i <= 10; i++) {
            insertUser(baseId() + 100 + i, "StmtPage" + i, 20 + i, "page" + i + "@nxn.test");
        }

        PageObject first = page(3, 0);
        List<UserInfo> firstPage = this.session.queryStatement(NS + ".queryAllUsers", null, first);
        assertEquals(3, firstPage.size());
        assertEquals(Integer.valueOf(baseId() + 101), firstPage.get(0).getId());

        PageObject second = page(4, 1);
        List<UserInfo> secondPage = this.session.queryStatement(NS + ".queryAllUsers", null, second);
        assertEquals(4, secondPage.size());
        assertEquals(Integer.valueOf(baseId() + 105), secondPage.get(0).getId());

        PageObject beyond = page(3, 5);
        List<UserInfo> beyondPage = this.session.queryStatement(NS + ".queryAllUsers", null, beyond);
        assertTrue(beyondPage.isEmpty());
    }

    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_PAGE_RESULT)
    public void sessionStatement_shouldReturnPageResultWithTotalCount() throws Exception {
        for (int i = 1; i <= 6; i++) {
            insertUser(baseId() + 200 + i, "StmtPageResult" + i, 35, "pageres" + i + "@nxn.test");
        }
        insertUser(baseId() + 210, "StmtOtherAge", 99, "other-age@nxn.test");

        PageResult<UserInfo> first = this.session.pageStatement(NS + ".queryAllUsers", null, page(3, 0));
        assertEquals(3, first.getData().size());
        assertEquals(7, first.getTotalCount());

        PageResult<UserInfo> filtered = this.session.pageStatement(NS + ".queryUsersByAge", mapOf("age", 35), page(2, 0));
        assertEquals(2, filtered.getData().size());
        assertEquals(6, filtered.getTotalCount());

        PageResult<UserInfo> empty = this.session.pageStatement(NS + ".queryUsersByAge", mapOf("age", 9999), page(10, 0));
        assertTrue(empty.getData().isEmpty());
        assertEquals(0, empty.getTotalCount());
    }
}
