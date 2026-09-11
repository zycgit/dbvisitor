/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.adapter;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.MapQuery;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;

import static org.junit.Assert.*;

/** The same page API boundaries with datasource-native ordering and Map row fixtures. */
public abstract class NativeLambdaPaginationSupport extends AdapterContractTest {
    protected LambdaTemplate lambda;
    protected String collection;

    protected abstract MapQuery order(MapQuery query);

    private MapQuery orderedQuery() {
        return order(this.lambda.queryFreedom(this.collection));
    }

    private MapQuery firstRows(int count) {
        return order(this.lambda.queryFreedom(this.collection).lt("seq", count));
    }

    protected void insertPageRows() throws SQLException {
        for (int i = 0; i < 25; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("uid", "row-" + i);
            row.put("name", "name-" + i);
            row.put("seq", i);
            row.put("group_id", i % 2 == 0 ? "even" : "odd");
            row.put("v", new float[] { i, 0 });
            this.lambda.insertFreedom(this.collection).applyMap(row).executeSumResult();
        }
    }

    protected void verifyMultiplePages() throws SQLException {
        MapQuery firstQuery = orderedQuery().initPage(10, 0);
        assertPageRows(firstQuery.queryForMapList(), 0, 10);
        Page first = firstQuery.pageInfo();
        assertEquals(25, first.getTotalCount());
        assertEquals(3, first.getTotalPage());
        assertEquals(0, first.getCurrentPage());
        assertEquals(10, first.getPageSize());
        assertPageRows(orderedQuery().initPage(10, 1).queryForMapList(), 10, 10);
        assertPageRows(orderedQuery().initPage(10, 2).queryForMapList(), 20, 5);
    }

    protected void verifyExactPages() throws SQLException {
        MapQuery exact = firstRows(20).initPage(10, 0);
        assertPageRows(exact.queryForMapList(), 0, 10);
        assertEquals(20, exact.pageInfo().getTotalCount());
        assertEquals(2, exact.pageInfo().getTotalPage());
        assertPageRows(firstRows(20).initPage(10, 1).queryForMapList(), 10, 10);
    }

    protected void verifySinglePage() throws SQLException {
        MapQuery single = firstRows(5).initPage(100, 0);
        assertPageRows(single.queryForMapList(), 0, 5);
        assertEquals(5, single.pageInfo().getTotalCount());
        assertEquals(1, single.pageInfo().getTotalPage());
        assertEquals(0, single.pageInfo().getCurrentPage());
    }

    protected void verifySingleRowPages() throws SQLException {
        MapQuery one = firstRows(3).initPage(1, 0);
        assertPageRows(one.queryForMapList(), 0, 1);
        assertEquals(3, one.pageInfo().getTotalCount());
        assertEquals(3, one.pageInfo().getTotalPage());
        assertPageRows(firstRows(3).initPage(1, 2).queryForMapList(), 2, 1);
    }

    protected void verifyBeyondLast() throws SQLException {
        MapQuery beyond = firstRows(5).initPage(10, 99);
        assertTrue(beyond.queryForMapList().isEmpty());
        assertEquals(5, beyond.pageInfo().getTotalCount());
        assertEquals(1, beyond.pageInfo().getTotalPage());
    }

    protected void verifyMutablePage() throws SQLException {
        PageObject page = new PageObject();
        page.setPageSize(5);
        page.setCurrentPage(0);
        assertPageRows(firstRows(12).usePage(page).queryForMapList(), 0, 5);
        page.setCurrentPage(1);
        assertPageRows(firstRows(12).usePage(page).queryForMapList(), 5, 5);
        page.setCurrentPage(2);
        assertPageRows(firstRows(12).usePage(page).queryForMapList(), 10, 2);
    }

    protected void verifyFactories() throws SQLException {
        MapQuery factory = firstRows(10).usePage(PageObject.of(0, 3));
        assertPageRows(factory.queryForMapList(), 0, 3);
        assertEquals(10, factory.pageInfo().getTotalCount());
        assertEquals(4, factory.pageInfo().getTotalPage());
        assertPageRows(firstRows(8).usePage(PageObject.of(1, 3, 1)).queryForMapList(), 0, 3);
    }

    protected void verifyOneBasedPages() throws SQLException {
        PageObject page = new PageObject();
        page.setPageSize(5);
        page.setPageNumberOffset(1);
        page.setCurrentPage(1);
        MapQuery firstQuery = firstRows(15).usePage(page);
        assertPageRows(firstQuery.queryForMapList(), 0, 5);
        Page firstInfo = firstQuery.pageInfo();
        page.setCurrentPage(3);
        assertPageRows(firstRows(15).usePage(page).queryForMapList(), 10, 5);
        assertEquals(1, firstInfo.getCurrentPage());
        assertEquals(4, firstInfo.getTotalPage());
    }

    protected void verifyCountConsistency() throws SQLException {
        long count = firstRows(17).queryForCount();
        MapQuery query = firstRows(17).initPage(5, 0);
        assertPageRows(query.queryForMapList(), 0, 5);
        assertEquals(count, query.pageInfo().getTotalCount());
        assertEquals(17, query.pageInfo().getTotalCount());
    }

    protected void verifyFilteredCount() throws SQLException {
        MapQuery filtered = order(this.lambda.queryFreedom(this.collection).eq("group_id", "even")).initPage(3, 0);
        List<Map<String, Object>> rows = filtered.queryForMapList();
        assertEquals(3, rows.size());
        assertEquals(13, filtered.pageInfo().getTotalCount());
        assertEquals(5, filtered.pageInfo().getTotalPage());
        for (int i = 0; i < rows.size(); i++) {
            assertEquals(i * 2, sequence(rows.get(i)));
            assertEquals("even", rows.get(i).get("group_id"));
        }
    }

    protected void verifyFullTraversal() throws SQLException {
        Set<Integer> seen = new HashSet<>();
        int page = 0;
        while (true) {
            List<Map<String, Object>> rows = firstRows(23).initPage(7, page).queryForMapList();
            if (rows.isEmpty()) {
                break;
            }
            for (Map<String, Object> row : rows) {
                assertTrue("No overlapping rows between pages", seen.add(sequence(row)));
            }
            page++;
            assertTrue("Traversal must finish within the known page count", page <= 4);
        }
        assertEquals(23, seen.size());
        for (int i = 0; i < 23; i++) {
            assertTrue(seen.contains(i));
        }
    }

    protected void verifyOffset() throws SQLException {
        assertPageRows(firstRows(12).initPage(5, 1).queryForMapList(), 5, 5);
    }

    private void assertPageRows(List<Map<String, Object>> rows, int start, int size) {
        assertEquals(size, rows.size());
        for (int i = 0; i < size; i++) {
            assertEquals(start + i, sequence(rows.get(i)));
            assertEquals("name-" + (start + i), rows.get(i).get("name"));
        }
    }

    private int sequence(Map<String, Object> row) {
        return Integer.parseInt(row.get("seq").toString());
    }
}
