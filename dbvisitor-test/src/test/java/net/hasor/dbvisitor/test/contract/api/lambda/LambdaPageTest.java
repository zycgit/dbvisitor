package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@NxnContract
public abstract class LambdaPageTest extends AbstractNxnContractTest {
    protected int baseId() {
        return 720000;
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_MULTI_PAGE)
    public void lambdaPageTraverseMultiPagesWithPageInfo() throws SQLException {
        insertBatch("NXN-Page-Multi-", 25, baseId() + 100);

        EntityQuery<UserInfo> firstQuery = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-Multi-%")//
                .initPage(10, 0)//
                .orderBy("id");
        List<UserInfo> firstPage = firstQuery.queryForList();
        Page firstInfo = firstQuery.pageInfo();

        assertEquals(10, firstPage.size());
        assertEquals("NXN-Page-Multi-0", firstPage.get(0).getName());
        assertEquals("NXN-Page-Multi-9", firstPage.get(9).getName());
        assertEquals(25, firstInfo.getTotalCount());
        assertEquals(3, firstInfo.getTotalPage());
        assertEquals(0, firstInfo.getCurrentPage());
        assertEquals(10, firstInfo.getPageSize());

        List<UserInfo> secondPage = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-Multi-%")//
                .initPage(10, 1)//
                .orderBy("id")//
                .queryForList();
        List<UserInfo> thirdPage = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-Multi-%")//
                .initPage(10, 2)//
                .orderBy("id")//
                .queryForList();

        assertEquals(10, secondPage.size());
        assertEquals("NXN-Page-Multi-10", secondPage.get(0).getName());
        assertEquals("NXN-Page-Multi-19", secondPage.get(9).getName());
        assertEquals(5, thirdPage.size());
        assertEquals("NXN-Page-Multi-20", thirdPage.get(0).getName());
        assertEquals("NXN-Page-Multi-24", thirdPage.get(4).getName());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_EXACT_DIVISION)
    public void lambdaPageCalculateExactDivisionPages() throws SQLException {
        insertBatch("NXN-Page-Exact-", 20, baseId() + 200);

        EntityQuery<UserInfo> query = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-Exact-%")//
                .initPage(10, 0)//
                .orderBy("id");
        query.queryForList();
        Page page = query.pageInfo();
        List<UserInfo> secondPage = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-Exact-%")//
                .initPage(10, 1)//
                .orderBy("id")//
                .queryForList();

        assertEquals(20, page.getTotalCount());
        assertEquals(2, page.getTotalPage());
        assertEquals(10, secondPage.size());
        assertEquals("NXN-Page-Exact-10", secondPage.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_SINGLE_PAGE)
    public void lambdaPageRepresentSinglePageWhenTotalIsBelowPageSize() throws SQLException {
        insertBatch("NXN-Page-Single-", 5, baseId() + 300);

        EntityQuery<UserInfo> query = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-Single-%")//
                .initPage(100, 0)//
                .orderBy("id");
        List<UserInfo> rows = query.queryForList();
        Page page = query.pageInfo();

        assertEquals(5, rows.size());
        assertEquals(5, page.getTotalCount());
        assertEquals(1, page.getTotalPage());
        assertEquals(0, page.getCurrentPage());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_SIZE_ONE)
    public void lambdaPagePageSizeOne() throws SQLException {
        insertBatch("NXN-Page-One-", 3, baseId() + 400);

        EntityQuery<UserInfo> firstQuery = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-One-%")//
                .initPage(1, 0)//
                .orderBy("id");
        List<UserInfo> firstPage = firstQuery.queryForList();
        Page firstInfo = firstQuery.pageInfo();
        List<UserInfo> thirdPage = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-One-%")//
                .initPage(1, 2)//
                .orderBy("id")//
                .queryForList();

        assertEquals(1, firstPage.size());
        assertEquals("NXN-Page-One-0", firstPage.get(0).getName());
        assertEquals(3, firstInfo.getTotalCount());
        assertEquals(3, firstInfo.getTotalPage());
        assertEquals(1, thirdPage.size());
        assertEquals("NXN-Page-One-2", thirdPage.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_BEYOND_LAST)
    public void lambdaPageEmptyRowsWhenRequestedPageIsBeyondLast() throws SQLException {
        insertBatch("NXN-Page-Beyond-", 5, baseId() + 500);

        EntityQuery<UserInfo> query = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-Beyond-%")//
                .initPage(10, 99)//
                .orderBy("id");
        List<UserInfo> rows = query.queryForList();
        Page page = query.pageInfo();

        assertEquals(0, rows.size());
        assertEquals(5, page.getTotalCount());
        assertEquals(1, page.getTotalPage());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_USE_PAGE)
    public void lambdaPageMutablePageObjectForNavigation() throws SQLException {
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
    @Capability(CapabilityId.LAMBDA_PAGE_FACTORY)
    public void lambdaPagePageObjectFactoryMethods() throws SQLException {
        insertBatch("NXN-Page-FactoryA-", 10, baseId() + 650);
        insertBatch("NXN-Page-FactoryB-", 8, baseId() + 670);

        Page firstPage = PageObject.of(0, 3);
        EntityQuery<UserInfo> firstQuery = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-FactoryA-%")//
                .usePage(firstPage)//
                .orderBy("id");
        List<UserInfo> firstRows = firstQuery.queryForList();
        Page firstInfo = firstQuery.pageInfo();

        Page offsetPage = PageObject.of(1, 3, 1);
        List<UserInfo> offsetRows = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-FactoryB-%")//
                .usePage(offsetPage)//
                .orderBy("id")//
                .queryForList();

        assertEquals(3, firstRows.size());
        assertEquals("NXN-Page-FactoryA-0", firstRows.get(0).getName());
        assertEquals(10, firstInfo.getTotalCount());
        assertEquals(4, firstInfo.getTotalPage());
        assertEquals(3, offsetRows.size());
        assertEquals("NXN-Page-FactoryB-0", offsetRows.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_NUMBER_OFFSET)
    public void lambdaPageHonorPageNumberOffset() throws SQLException {
        insertBatch("NXN-Page-Offset-", 15, baseId() + 700);

        PageObject pageObject = new PageObject();
        pageObject.setPageSize(5);
        pageObject.setPageNumberOffset(1);
        pageObject.setCurrentPage(1);

        EntityQuery<UserInfo> firstQuery = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-Offset-%")//
                .usePage(pageObject)//
                .orderBy("id");
        List<UserInfo> firstPage = firstQuery.queryForList();
        Page page = firstQuery.pageInfo();

        pageObject.setCurrentPage(3);
        List<UserInfo> thirdPage = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-Offset-%")//
                .usePage(pageObject)//
                .orderBy("id")//
                .queryForList();

        assertEquals(5, firstPage.size());
        assertEquals("NXN-Page-Offset-0", firstPage.get(0).getName());
        assertEquals(1, page.getCurrentPage());
        assertEquals(4, page.getTotalPage());
        assertEquals(5, thirdPage.size());
        assertEquals("NXN-Page-Offset-10", thirdPage.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_COUNT_CONSISTENCY)
    public void lambdaPagePageTotalCountConsistentWithQueryForCount() throws SQLException {
        insertBatch("NXN-Page-Count-", 17, baseId() + 800);

        long count = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-Count-%")//
                .queryForCount();
        EntityQuery<UserInfo> query = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-Count-%")//
                .initPage(5, 0);
        query.queryForList();
        Page page = query.pageInfo();

        assertEquals(count, page.getTotalCount());
        assertEquals(17, page.getTotalCount());
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_FILTER)
    public void lambdaPageOnlyFilteredRows() throws SQLException {
        for (int i = 0; i < 10; i++) {
            insert(baseId() + 900 + i, "NXN-Page-Filter-A-" + i, 40);
            insert(baseId() + 910 + i, "NXN-Page-Filter-B-" + i, 50);
        }

        EntityQuery<UserInfo> query = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "NXN-Page-Filter-%")//
                .eq(UserInfo::getAge, 40)//
                .initPage(3, 0)//
                .orderBy("id");
        List<UserInfo> rows = query.queryForList();
        Page page = query.pageInfo();

        assertEquals(3, rows.size());
        assertEquals(10, page.getTotalCount());
        assertEquals(4, page.getTotalPage());
        for (UserInfo row : rows) {
            assertEquals(Integer.valueOf(40), row.getAge());
        }
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PAGE_FULL_TRAVERSAL)
    public void lambdaPageTraverseAllPagesNoOverlap() throws SQLException {
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

    private void insertBatch(String prefix, int count, int startId) throws SQLException {
        for (int i = 0; i < count; i++) {
            insert(startId + i, prefix + i, 20 + i);
        }
    }

    private void insert(int id, String name, Integer age) throws SQLException {
        jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)", //
                new Object[] { id, name, age, name.toLowerCase() + "@nxn.test", new Date() });
    }
}
