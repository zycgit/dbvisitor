/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaPageResultCase extends LambdaPaginationSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_MULTI_PAGE)
    public void lambdaPagination_shouldTraverseMultiplePagesWithPageInfo() throws SQLException {
        insertBatch("NXN-Page-Multi-", 25, baseId() + 100);

        EntityQuery<? extends UserInfo> firstQuery = orderRows(queryRows("NXN-Page-Multi-")//
                .initPage(10, 0));
        List<? extends UserInfo> firstPage = firstQuery.queryForList();
        Page firstInfo = firstQuery.pageInfo();

        assertEquals(10, firstPage.size());
        assertEquals("NXN-Page-Multi-0", firstPage.get(0).getName());
        assertEquals("NXN-Page-Multi-9", firstPage.get(9).getName());
        assertEquals(25, firstInfo.getTotalCount());
        assertEquals(3, firstInfo.getTotalPage());
        assertEquals(0, firstInfo.getCurrentPage());
        assertEquals(10, firstInfo.getPageSize());

        List<? extends UserInfo> secondPage = orderRows(queryRows("NXN-Page-Multi-")//
                .initPage(10, 1))//
                .queryForList();
        List<? extends UserInfo> thirdPage = orderRows(queryRows("NXN-Page-Multi-")//
                .initPage(10, 2))//
                .queryForList();

        assertEquals(10, secondPage.size());
        assertEquals("NXN-Page-Multi-10", secondPage.get(0).getName());
        assertEquals("NXN-Page-Multi-19", secondPage.get(9).getName());
        assertEquals(5, thirdPage.size());
        assertEquals("NXN-Page-Multi-20", thirdPage.get(0).getName());
        assertEquals("NXN-Page-Multi-24", thirdPage.get(4).getName());
        assertPageRows(firstPage, "NXN-Page-Multi-", 0, 10);
        assertPageRows(secondPage, "NXN-Page-Multi-", 10, 10);
        assertPageRows(thirdPage, "NXN-Page-Multi-", 20, 5);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_EXACT_DIVISION)
    public void lambdaPagination_shouldCalculateExactDivisionPages() throws SQLException {
        insertBatch("NXN-Page-Exact-", 20, baseId() + 200);

        EntityQuery<? extends UserInfo> query = orderRows(queryRows("NXN-Page-Exact-")//
                .initPage(10, 0));
        assertPageRows(query.queryForList(), "NXN-Page-Exact-", 0, 10);
        Page page = query.pageInfo();
        List<? extends UserInfo> secondPage = orderRows(queryRows("NXN-Page-Exact-")//
                .initPage(10, 1))//
                .queryForList();

        assertEquals(20, page.getTotalCount());
        assertEquals(2, page.getTotalPage());
        assertEquals(10, secondPage.size());
        assertEquals("NXN-Page-Exact-10", secondPage.get(0).getName());
        assertPageRows(secondPage, "NXN-Page-Exact-", 10, 10);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_SINGLE_PAGE)
    public void lambdaPagination_shouldRepresentSinglePageWhenTotalIsBelowPageSize() throws SQLException {
        insertBatch("NXN-Page-Single-", 5, baseId() + 300);

        EntityQuery<? extends UserInfo> query = orderRows(queryRows("NXN-Page-Single-")//
                .initPage(100, 0));
        List<? extends UserInfo> rows = query.queryForList();
        Page page = query.pageInfo();

        assertEquals(5, rows.size());
        assertEquals(5, page.getTotalCount());
        assertEquals(1, page.getTotalPage());
        assertEquals(0, page.getCurrentPage());
        assertPageRows(rows, "NXN-Page-Single-", 0, 5);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_SIZE_ONE)
    public void lambdaPagination_shouldSupportPageSizeOne() throws SQLException {
        insertBatch("NXN-Page-One-", 3, baseId() + 400);

        EntityQuery<? extends UserInfo> firstQuery = orderRows(queryRows("NXN-Page-One-")//
                .initPage(1, 0));
        List<? extends UserInfo> firstPage = firstQuery.queryForList();
        Page firstInfo = firstQuery.pageInfo();
        List<? extends UserInfo> thirdPage = orderRows(queryRows("NXN-Page-One-")//
                .initPage(1, 2))//
                .queryForList();

        assertEquals(1, firstPage.size());
        assertEquals("NXN-Page-One-0", firstPage.get(0).getName());
        assertEquals(3, firstInfo.getTotalCount());
        assertEquals(3, firstInfo.getTotalPage());
        assertEquals(1, thirdPage.size());
        assertEquals("NXN-Page-One-2", thirdPage.get(0).getName());
        assertPageRows(firstPage, "NXN-Page-One-", 0, 1);
        assertPageRows(thirdPage, "NXN-Page-One-", 2, 1);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_BEYOND_LAST)
    public void lambdaPagination_shouldReturnEmptyRowsWhenRequestedPageIsBeyondLast() throws SQLException {
        insertBatch("NXN-Page-Beyond-", 5, baseId() + 500);

        EntityQuery<? extends UserInfo> query = orderRows(queryRows("NXN-Page-Beyond-")//
                .initPage(10, 99));
        List<? extends UserInfo> rows = query.queryForList();
        Page page = query.pageInfo();

        assertEquals(0, rows.size());
        assertEquals(5, page.getTotalCount());
        assertEquals(1, page.getTotalPage());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_FACTORY)
    public void lambdaPagination_shouldUsePageObjectFactoryMethods() throws SQLException {
        insertBatch("NXN-Page-FactoryA-", 10, baseId() + 650);
        insertBatch("NXN-Page-FactoryB-", 8, baseId() + 670);

        Page firstPage = PageObject.of(0, 3);
        EntityQuery<? extends UserInfo> firstQuery = orderRows(queryRows("NXN-Page-FactoryA-")//
                .usePage(firstPage));
        List<? extends UserInfo> firstRows = firstQuery.queryForList();
        Page firstInfo = firstQuery.pageInfo();

        Page offsetPage = PageObject.of(1, 3, 1);
        List<? extends UserInfo> offsetRows = orderRows(queryRows("NXN-Page-FactoryB-")//
                .usePage(offsetPage))//
                .queryForList();

        assertEquals(3, firstRows.size());
        assertEquals("NXN-Page-FactoryA-0", firstRows.get(0).getName());
        assertEquals(10, firstInfo.getTotalCount());
        assertEquals(4, firstInfo.getTotalPage());
        assertEquals(3, offsetRows.size());
        assertEquals("NXN-Page-FactoryB-0", offsetRows.get(0).getName());
        assertPageRows(firstRows, "NXN-Page-FactoryA-", 0, 3);
        assertPageRows(offsetRows, "NXN-Page-FactoryB-", 0, 3);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_NUMBER_OFFSET)
    public void lambdaPagination_shouldHonorPageNumberOffset() throws SQLException {
        insertBatch("NXN-Page-Offset-", 15, baseId() + 700);

        PageObject pageObject = new PageObject();
        pageObject.setPageSize(5);
        pageObject.setPageNumberOffset(1);
        pageObject.setCurrentPage(1);

        EntityQuery<? extends UserInfo> firstQuery = orderRows(queryRows("NXN-Page-Offset-")//
                .usePage(pageObject));
        List<? extends UserInfo> firstPage = firstQuery.queryForList();
        Page page = firstQuery.pageInfo();

        pageObject.setCurrentPage(3);
        List<? extends UserInfo> thirdPage = orderRows(queryRows("NXN-Page-Offset-")//
                .usePage(pageObject))//
                .queryForList();

        assertEquals(5, firstPage.size());
        assertEquals("NXN-Page-Offset-0", firstPage.get(0).getName());
        assertEquals(1, page.getCurrentPage());
        assertEquals(4, page.getTotalPage());
        assertEquals(5, thirdPage.size());
        assertEquals("NXN-Page-Offset-10", thirdPage.get(0).getName());
        assertPageRows(firstPage, "NXN-Page-Offset-", 0, 5);
        assertPageRows(thirdPage, "NXN-Page-Offset-", 10, 5);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_COUNT_CONSISTENCY)
    public void lambdaPagination_shouldKeepPageTotalCountConsistentWithQueryForCount() throws SQLException {
        insertBatch("NXN-Page-Count-", 17, baseId() + 800);

        long count = queryRows("NXN-Page-Count-")//
                .queryForCount();
        EntityQuery<? extends UserInfo> query = orderRows(queryRows("NXN-Page-Count-").initPage(5, 0));
        assertPageRows(query.queryForList(), "NXN-Page-Count-", 0, 5);
        Page page = query.pageInfo();

        assertEquals(count, page.getTotalCount());
        assertEquals(17, page.getTotalCount());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_FILTER)
    public void lambdaPagination_shouldCountOnlyFilteredRows() throws SQLException {
        for (int i = 0; i < 10; i++) {
            insert(baseId() + 900 + i, "NXN-Page-Filter-A-" + i, 40);
            insert(baseId() + 910 + i, "NXN-Page-Filter-B-" + i, 50);
        }

        EntityQuery<? extends UserInfo> query = orderRows(queryRows("NXN-Page-Filter-")//
                .eq(UserInfo::getAge, 40)//
                .initPage(3, 0));
        List<? extends UserInfo> rows = query.queryForList();
        Page page = query.pageInfo();

        assertEquals(3, rows.size());
        assertEquals(10, page.getTotalCount());
        assertEquals(4, page.getTotalPage());
        assertPageRows(rows, "NXN-Page-Filter-A-", 0, 3);
        for (UserInfo row : rows) {
            assertEquals(Integer.valueOf(40), row.getAge());
        }
    }
}
