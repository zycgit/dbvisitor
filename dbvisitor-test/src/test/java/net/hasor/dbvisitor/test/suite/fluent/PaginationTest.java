package net.hasor.dbvisitor.test.suite.fluent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.lambda.EntityQuery;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Pagination Test
 * 覆盖 initPage / usePage / pageInfo 等分页 API 的各种使用场景
 */
public class PaginationTest extends AbstractOneApiTest {

    // ==================== 基础分页 ====================

    /** 多页遍历: 25 条数据, pageSize=10, 验证第 1/2/3 页的内容和页信息 */
    @Test
    public void testMultiPageTraversal() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertBatch(lambda, "MPT", 25, 30000);

        // 第 1 页 (page=0)
        EntityQuery<UserInfo> q1 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "MPT%")//
                .initPage(10, 0).orderBy("id");
        List<UserInfo> list1 = q1.queryForList();
        Page page1 = q1.pageInfo();

        assertEquals(10, list1.size());
        assertEquals("MPT0", list1.get(0).getName());
        assertEquals("MPT9", list1.get(9).getName());
        assertEquals(25, page1.getTotalCount());
        assertEquals(3, page1.getTotalPage());
        assertEquals(0, page1.getCurrentPage());
        assertEquals(10, page1.getPageSize());

        // 第 2 页 (page=1)
        EntityQuery<UserInfo> q2 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "MPT%")//
                .initPage(10, 1).orderBy("id");
        List<UserInfo> list2 = q2.queryForList();

        assertEquals(10, list2.size());
        assertEquals("MPT10", list2.get(0).getName());
        assertEquals("MPT19", list2.get(9).getName());

        // 第 3 页 (page=2, 尾页只有 5 条)
        EntityQuery<UserInfo> q3 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "MPT%")//
                .initPage(10, 2).orderBy("id");
        List<UserInfo> list3 = q3.queryForList();

        assertEquals(5, list3.size());
        assertEquals("MPT20", list3.get(0).getName());
        assertEquals("MPT24", list3.get(4).getName());
    }

    /** 刚好整除: 20 条数据, pageSize=10, 正好 2 页 */
    @Test
    public void testExactDivision() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertBatch(lambda, "ED", 20, 30100);

        EntityQuery<UserInfo> q1 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "ED%")//
                .initPage(10, 0).orderBy("id");
        q1.queryForList();
        Page page = q1.pageInfo();

        assertEquals(20, page.getTotalCount());
        assertEquals(2, page.getTotalPage());

        // 第 2 页正好 10 条
        EntityQuery<UserInfo> q2 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "ED%")//
                .initPage(10, 1).orderBy("id");
        List<UserInfo> list2 = q2.queryForList();
        assertEquals(10, list2.size());
    }

    /** 单页: 数据量 < pageSize, 只有 1 页 */
    @Test
    public void testSinglePage() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertBatch(lambda, "SP", 5, 30200);

        EntityQuery<UserInfo> q = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "SP%")//
                .initPage(100, 0).orderBy("id");
        List<UserInfo> list = q.queryForList();
        Page page = q.pageInfo();

        assertEquals(5, list.size());
        assertEquals(5, page.getTotalCount());
        assertEquals(1, page.getTotalPage());
        assertEquals(0, page.getCurrentPage());
    }

    /** 空结果分页: 无匹配数据时分页信息应正确 */
    @Test
    public void testEmptyResultPagination() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        EntityQuery<UserInfo> q = lambda.query(UserInfo.class)//
                .eq(UserInfo::getName, "NonExistPagination999")//
                .initPage(10, 0);
        List<UserInfo> list = q.queryForList();
        Page page = q.pageInfo();

        assertEquals(0, list.size());
        assertEquals(0, page.getTotalCount());
        assertEquals(0, page.getTotalPage());
    }

    /** 每页 1 条: pageSize=1, 总页数 = 总记录数 */
    @Test
    public void testPageSizeOne() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertBatch(lambda, "PS1", 3, 30300);

        EntityQuery<UserInfo> q = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "PS1%")//
                .initPage(1, 0).orderBy("id");
        List<UserInfo> list = q.queryForList();
        Page page = q.pageInfo();

        assertEquals(1, list.size());
        assertEquals("PS10", list.get(0).getName());
        assertEquals(3, page.getTotalCount());
        assertEquals(3, page.getTotalPage());

        // 第 3 页(page=2)
        EntityQuery<UserInfo> q3 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "PS1%")//
                .initPage(1, 2).orderBy("id");
        List<UserInfo> list3 = q3.queryForList();
        assertEquals(1, list3.size());
        assertEquals("PS12", list3.get(0).getName());
    }

    // ==================== 超出范围 ====================

    /** 超出页码: 请求不存在的页, 应返回空列表 */
    @Test
    public void testBeyondLastPage() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertBatch(lambda, "BLP", 5, 30400);

        EntityQuery<UserInfo> q = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "BLP%")//
                .initPage(10, 99).orderBy("id");
        List<UserInfo> list = q.queryForList();
        Page page = q.pageInfo();

        assertEquals(0, list.size());
        assertEquals(5, page.getTotalCount());
        assertEquals(1, page.getTotalPage());
    }

    // ==================== usePage API ====================

    /** 使用 usePage(PageObject) 方式配置分页 */
    @Test
    public void testUsePage() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertBatch(lambda, "UP", 15, 30500);

        PageObject pageObj = new PageObject();
        pageObj.setPageSize(5);
        pageObj.setCurrentPage(0);

        EntityQuery<UserInfo> q = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "UP%")//
                .usePage(pageObj).orderBy("id");
        List<UserInfo> list = q.queryForList();
        Page page = q.pageInfo();

        assertEquals(5, list.size());
        assertEquals("UP0", list.get(0).getName());
        assertEquals(15, page.getTotalCount());
        assertEquals(3, page.getTotalPage());
    }

    /** usePage 翻页: 通过修改 PageObject 的 currentPage 实现翻页 */
    @Test
    public void testUsePageNavigation() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertBatch(lambda, "UPN", 12, 30600);

        PageObject pageObj = new PageObject();
        pageObj.setPageSize(5);
        pageObj.setCurrentPage(0);

        // 第 1 页
        EntityQuery<UserInfo> q1 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "UPN%")//
                .usePage(pageObj).orderBy("id");
        List<UserInfo> list1 = q1.queryForList();
        assertEquals(5, list1.size());
        assertEquals("UPN0", list1.get(0).getName());

        // 翻到第 2 页
        pageObj.setCurrentPage(1);
        EntityQuery<UserInfo> q2 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "UPN%")//
                .usePage(pageObj).orderBy("id");
        List<UserInfo> list2 = q2.queryForList();
        assertEquals(5, list2.size());
        assertEquals("UPN5", list2.get(0).getName());

        // 翻到第 3 页(尾页)
        pageObj.setCurrentPage(2);
        EntityQuery<UserInfo> q3 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "UPN%")//
                .usePage(pageObj).orderBy("id");
        List<UserInfo> list3 = q3.queryForList();
        assertEquals(2, list3.size());
        assertEquals("UPN10", list3.get(0).getName());
    }

    // ==================== pageNumberOffset ====================

    /** 页码偏移量=1: 通过 usePage 设置 pageNumberOffset=1，第一页页码为 1 而非 0 */
    @Test
    public void testPageNumberOffset() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertBatch(lambda, "PNO", 15, 30700);

        PageObject pageObj = new PageObject();
        pageObj.setPageSize(5);
        pageObj.setPageNumberOffset(1); // 从 1 开始计页
        pageObj.setCurrentPage(1);      // 第一页

        EntityQuery<UserInfo> q1 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "PNO%")//
                .usePage(pageObj).orderBy("id");
        List<UserInfo> list1 = q1.queryForList();
        Page page1 = q1.pageInfo();

        assertEquals(5, list1.size());
        assertEquals("PNO0", list1.get(0).getName());
        assertEquals(1, page1.getCurrentPage());           // 当前页码 = 1
        assertEquals(1 + 3, page1.getTotalPage());         // totalPage 含偏移量

        // 翻到第 3 页 (page=3, 偏移=1, 实际第三页)
        pageObj.setCurrentPage(3);
        EntityQuery<UserInfo> q3 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "PNO%")//
                .usePage(pageObj).orderBy("id");
        List<UserInfo> list3 = q3.queryForList();
        assertEquals(5, list3.size());
        assertEquals("PNO10", list3.get(0).getName());
    }

    // ==================== PageObject.of 静态工厂 ====================

    /** 使用 PageObject.of() 工厂方法创建分页 */
    @Test
    public void testPageObjectOf() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertBatch(lambda, "POF", 10, 30800);

        Page pageObj = PageObject.of(0, 3);

        EntityQuery<UserInfo> q = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "POF%")//
                .usePage(pageObj).orderBy("id");
        List<UserInfo> list = q.queryForList();
        Page page = q.pageInfo();

        assertEquals(3, list.size());
        assertEquals("POF0", list.get(0).getName());
        assertEquals(10, page.getTotalCount());
        assertEquals(4, page.getTotalPage()); // ceil(10/3) = 4
    }

    /** 使用 PageObject.of(pageNumber, pageSize, offset) 带偏移量 */
    @Test
    public void testPageObjectOfWithOffset() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertBatch(lambda, "POFO", 8, 30900);

        Page pageObj = PageObject.of(1, 3, 1); // 页码 1(偏移=1 即第一页), pageSize=3

        EntityQuery<UserInfo> q = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "POFO%")//
                .usePage(pageObj).orderBy("id");
        List<UserInfo> list = q.queryForList();

        assertEquals(3, list.size());
        assertEquals("POFO0", list.get(0).getName()); // 第一页数据
    }

    // ==================== Page 导航方法 ====================

    /** 手动 setCurrentPage 分页导航（非 nextPage/previousPage API） */
    @Test
    public void testPageNavigation() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertBatch(lambda, "PN", 20, 31000);

        PageObject pageObj = new PageObject();
        pageObj.setPageSize(5);
        pageObj.setCurrentPage(0);

        // 查询第 1 页, 获取 totalCount
        EntityQuery<UserInfo> q0 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "PN%")//
                .usePage(pageObj).orderBy("id");
        q0.queryForList();
        Page pageInfo = q0.pageInfo();
        assertEquals(0, pageInfo.getCurrentPage());

        // 切换到第 2 页 (page=1)
        pageObj.setCurrentPage(1);
        EntityQuery<UserInfo> q1 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "PN%")//
                .usePage(pageObj).orderBy("id");
        List<UserInfo> list1 = q1.queryForList();
        assertEquals("PN5", list1.get(0).getName());

        // 切换到第 4 页 (page=3)
        pageObj.setCurrentPage(3);
        EntityQuery<UserInfo> q3 = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "PN%")//
                .usePage(pageObj).orderBy("id");
        List<UserInfo> list3 = q3.queryForList();
        assertEquals(5, list3.size());
        assertEquals("PN15", list3.get(0).getName());

        // 切换回第 1 页 (page=0)
        pageObj.setCurrentPage(0);
        EntityQuery<UserInfo> qFirst = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "PN%")//
                .usePage(pageObj).orderBy("id");
        List<UserInfo> listFirst = qFirst.queryForList();
        assertEquals("PN0", listFirst.get(0).getName());
    }

    // ==================== queryForCount + 分页 ====================

    /** 分页查询与 queryForCount 的 totalCount 一致 */
    @Test
    public void testTotalCountConsistency() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        insertBatch(lambda, "TCC", 17, 31100);

        // queryForCount
        long count = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "TCC%")//
                .queryForCount();

        // 分页查询的 totalCount
        EntityQuery<UserInfo> q = lambda.query(UserInfo.class)//
                .like(UserInfo::getName, "TCC%")//
                .initPage(5, 0);
        q.queryForList();
        Page page = q.pageInfo();

        assertEquals(count, page.getTotalCount());
        assertEquals(17, page.getTotalCount());
    }

    // ==================== 分页与条件过滤组合 ====================

    /** 分页 + 条件过滤: 只对满足条件的记录分页 */
    @Test
    public void testPaginationWithFilter() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        // 插入 10 个 age=40, 10 个 age=50
        for (int i = 0; i < 10; i++) {
            lambda.insert(UserInfo.class).applyEntity(createUser(31200 + i, "PF_A" + i, 40)).executeSumResult();
            lambda.insert(UserInfo.class).applyEntity(createUser(31210 + i, "PF_B" + i, 50)).executeSumResult();
        }

        // 只分页 age=40 的记录
        EntityQuery<UserInfo> q = lambda.query(UserInfo.class)//
                .in(UserInfo::getId, idRange(31200, 31220))//
                .eq(UserInfo::getAge, 40)//
                .initPage(3, 0).orderBy("id");
        List<UserInfo> list = q.queryForList();
        Page page = q.pageInfo();

        assertEquals(3, list.size());
        assertEquals(10, page.getTotalCount()); // 只计 age=40 的
        assertEquals(4, page.getTotalPage());   // ceil(10/3)=4

        // 所有返回记录的 age 都应该是 40
        for (UserInfo u : list) {
            assertEquals(Integer.valueOf(40), u.getAge());
        }
    }

    // ==================== 分页数据不重叠不遗漏 ====================

    /** 完整遍历: 逐页收集所有记录, 验证数量和不重叠 */
    @Test
    public void testFullTraversalNoOverlap() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int total = 23;
        int pageSize = 7;
        insertBatch(lambda, "FT", total, 31300);

        List<Integer> allIds = new ArrayList<>();
        int pageNum = 0;

        while (true) {
            EntityQuery<UserInfo> q = lambda.query(UserInfo.class)//
                    .like(UserInfo::getName, "FT%")//
                    .initPage(pageSize, pageNum).orderBy("id");
            List<UserInfo> list = q.queryForList();

            if (list.isEmpty()) {
                break;
            }
            for (UserInfo u : list) {
                assertFalse("ID should not repeat: " + u.getId(), allIds.contains(u.getId()));
                allIds.add(u.getId());
            }
            pageNum++;
        }

        assertEquals("Should collect all records", total, allIds.size());
    }

    // ==================== Helper ====================

    private void insertBatch(LambdaTemplate lambda, String prefix, int count, int baseId) throws SQLException {
        List<UserInfo> batch = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            batch.add(createUser(baseId + i, prefix + i, 20 + i));
        }
        lambda.insert(UserInfo.class)//
                .applyEntity(batch)//
                .executeSumResult();
    }

    private List<Integer> idRange(int from, int toExclusive) {
        List<Integer> ids = new ArrayList<>();
        for (int i = from; i < toExclusive; i++) {
            ids.add(i);
        }
        return ids;
    }

    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@page.com");
        u.setCreateTime(new Date());
        return u;
    }
}
