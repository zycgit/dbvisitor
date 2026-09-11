/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@NxnContract
public abstract class LambdaPageNavigationContractTest extends LambdaPaginationSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_USE_PAGE)
    public void lambdaPagination_shouldUseMutablePageObjectForNavigation() throws SQLException {
        insertBatch("NXN-Page-Use-", 12, baseId() + 600);

        PageObject pageObject = new PageObject();
        pageObject.setPageSize(5);
        pageObject.setCurrentPage(0);

        List<UserInfo> firstPage = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-Use-%")//
                .usePage(pageObject)//
                .orderBy("id")//
                .queryForList();
        pageObject.setCurrentPage(1);
        List<UserInfo> secondPage = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-Use-%")//
                .usePage(pageObject)//
                .orderBy("id")//
                .queryForList();
        pageObject.setCurrentPage(2);
        List<UserInfo> thirdPage = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-Use-%")//
                .usePage(pageObject)//
                .orderBy("id")//
                .queryForList();

        assertEquals(5, firstPage.size());
        assertEquals("NXN-Page-Use-0", firstPage.get(0).getName());
        assertEquals(5, secondPage.size());
        assertEquals("NXN-Page-Use-5", secondPage.get(0).getName());
        assertEquals(2, thirdPage.size());
        assertEquals("NXN-Page-Use-10", thirdPage.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_FULL_TRAVERSAL)
    public void lambdaPagination_shouldTraverseAllPagesWithoutOverlap() throws SQLException {
        int total = 23;
        int pageSize = 7;
        insertBatch("NXN-Page-Full-", total, baseId() + 1000);

        List<Integer> allIds = new ArrayList<>();
        int pageNumber = 0;
        while (true) {
            List<UserInfo> rows = lambdaTemplate.query(UserInfo.class)//
                    .like(UserInfo::getName, "NXN-Page-Full-%")//
                    .initPage(pageSize, pageNumber)//
                    .orderBy("id")//
                    .queryForList();
            if (rows.isEmpty()) {
                break;
            }
            for (UserInfo row : rows) {
                assertFalse(allIds.contains(row.getId()));
                allIds.add(row.getId());
            }
            pageNumber++;
        }

        assertEquals(total, allIds.size());
    }
}
