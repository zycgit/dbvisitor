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
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class LambdaPageNavigationCase extends LambdaPaginationSupport {
    // 能力归属：构造器 API / 分页查询。
    @Test
    @Capability(value = CapabilityId.LAMBDA_PAGE_USE_PAGE, column = "builder/pagination-and-iteration/pagination")
    public void lambdaPagination_shouldUseMutablePageObjectForNavigation() throws SQLException {
        insertBatch("NXN-Page-Use-", 12, baseId() + 600);

        PageObject pageObject = new PageObject();
        pageObject.setPageSize(5);
        pageObject.setCurrentPage(0);

        List<? extends UserInfo> firstPage = orderRows(queryRows("NXN-Page-Use-")//
                .usePage(pageObject))//
                .queryForList();
        pageObject.setCurrentPage(1);
        List<? extends UserInfo> secondPage = orderRows(queryRows("NXN-Page-Use-")//
                .usePage(pageObject))//
                .queryForList();
        pageObject.setCurrentPage(2);
        List<? extends UserInfo> thirdPage = orderRows(queryRows("NXN-Page-Use-")//
                .usePage(pageObject))//
                .queryForList();

        assertEquals(5, firstPage.size());
        assertEquals("NXN-Page-Use-0", firstPage.get(0).getName());
        assertEquals(5, secondPage.size());
        assertEquals("NXN-Page-Use-5", secondPage.get(0).getName());
        assertEquals(2, thirdPage.size());
        assertEquals("NXN-Page-Use-10", thirdPage.get(0).getName());
        assertPageRows(firstPage, "NXN-Page-Use-", 0, 5);
        assertPageRows(secondPage, "NXN-Page-Use-", 5, 5);
        assertPageRows(thirdPage, "NXN-Page-Use-", 10, 2);
    }

    // 能力归属：构造器 API / 分页查询。
    @Test
    @Capability(value = CapabilityId.LAMBDA_PAGE_FULL_TRAVERSAL, column = "builder/pagination-and-iteration/pagination")
    public void lambdaPagination_shouldTraverseAllPagesWithoutOverlap() throws SQLException {
        int total = 23;
        int pageSize = 7;
        insertBatch("NXN-Page-Full-", total, baseId() + 1000);

        List<Integer> allIds = new ArrayList<>();
        int pageNumber = 0;
        while (true) {
            List<? extends UserInfo> rows = orderRows(queryRows("NXN-Page-Full-")//
                    .initPage(pageSize, pageNumber))//
                    .queryForList();
            if (rows.isEmpty()) {
                break;
            }
            for (UserInfo row : rows) {
                assertFalse(allIds.contains(row.getId()));
                allIds.add(row.getId());
            }
            pageNumber++;
            assertTrue("Traversal must finish within the known page count", pageNumber <= 4);
        }

        assertEquals(total, allIds.size());
        for (int i = 0; i < total; i++) {
            assertTrue(allIds.contains(baseId() + 1000 + i));
        }
    }

    // 能力归属：构造器 API / 分页查询。
    @Test
    @Capability(value = CapabilityId.LAMBDA_QUERY_PAGE, column = "builder/pagination-and-iteration/pagination")
    public void lambdaQueryPage_shouldLimitAndOffsetResults() throws SQLException {
        for (int i = 1; i <= 12; i++) {
            insert(baseId() + 60 + i, "PageQ" + i, 20 + i);
        }

        List<? extends UserInfo> users = orderRows(queryRows().rangeBetween(UserInfo::getId, baseId() + 61, baseId() + 72))//
                .initPage(5, 1)//
                .queryForList();

        assertNotNull(users);
        assertEquals(5, users.size());
        assertEquals(Integer.valueOf(baseId() + 66), users.get(0).getId());
        assertEquals(Integer.valueOf(baseId() + 70), users.get(4).getId());
        assertPageRows(users, "PageQ", 6, 5);
    }
}
