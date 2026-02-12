package net.hasor.dbvisitor.test.oneapi.suite.xmlmapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * XML Mapper 基础 CRUD 及 Session API 测试
 * <p>测试要点：
 * <ul>
 *   <li>通过 Configuration.loadMapper() 加载 XML</li>
 *   <li>insert / select / update / delete / execute 五种 XML 标签</li>
 *   <li>resultType 的多种快捷值（entity、map、int、string）</li>
 *   <li>session.executeStatement / queryStatement / pageStatement</li>
 *   <li>命名空间解析（namespace.id）</li>
 * </ul>
 */
public class XmlCrudTest extends AbstractOneApiTest {

    private Session session;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Configuration config = new Configuration();
        config.loadMapper("/oneapi/mapper/XmlCrudMapper.xml");
        this.session = config.newSession(dataSource);
    }

    @Override
    protected void initData() {
        try {
            for (int i = 1; i <= 5; i++) {
                Map<String, Object> p = new HashMap<String, Object>();
                p.put("id", 57000 + i);
                p.put("name", "XmlCrud" + i);
                p.put("age", 20 + i);
                p.put("email", "crud" + i + "@test.com");
                jdbcTemplate.executeUpdate(//
                        "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",//
                        new Object[] { 57000 + i, "XmlCrud" + i, 20 + i, "crud" + i + "@test.com" });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ========== insert 测试 ==========

    /** 测试 <insert> 标签插入数据 */
    @Test
    public void testInsert() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57010);
        params.put("name", "XmlCrudInsert");
        params.put("age", 30);
        params.put("email", "insert@test.com");

        Object result = session.executeStatement("xmltest.CrudMapper.insertUser", params);
        assertEquals(1, ((Number) result).intValue());

        // 验证插入成功
        Map<String, Object> q = new HashMap<String, Object>();
        q.put("id", 57010);
        List<UserInfo> list = session.queryStatement("xmltest.CrudMapper.selectById", q);
        assertEquals(1, list.size());
        assertEquals("XmlCrudInsert", list.get(0).getName());
    }

    // ========== select 测试 ==========

    /** 测试 <select> + resultMap 查询单条记录 */
    @Test
    public void testSelectById_ResultMap() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57001);

        List<UserInfo> list = session.queryStatement("xmltest.CrudMapper.selectById", params);
        assertEquals(1, list.size());

        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(57001), user.getId());
        assertEquals("XmlCrud1", user.getName());
        assertEquals(Integer.valueOf(21), user.getAge());
        assertEquals("crud1@test.com", user.getEmail());
        assertNotNull(user.getCreateTime());
    }

    /** 测试 <select> + resultType=entity 查询列表 */
    @Test
    public void testSelectAll_ResultTypeEntity() throws Exception {
        List<UserInfo> list = session.queryStatement("xmltest.CrudMapper.selectAll", null);
        assertEquals(5, list.size());

        assertEquals("XmlCrud1", list.get(0).getName());
        assertEquals("XmlCrud5", list.get(4).getName());
    }

    /** 测试 <select> + resultType=map 返回 Map 列表 */
    @Test
    public void testSelectAll_ResultTypeMap() throws Exception {
        List<Map<String, Object>> list = session.queryStatement("xmltest.CrudMapper.selectAllAsMap", null);
        assertEquals(5, list.size());

        Map<String, Object> row = list.get(0);
        assertNotNull(row.get("id"));
        assertNotNull(row.get("name"));
    }

    /** 测试 <select> + resultType=int 返回聚合值 */
    @Test
    public void testSelectCount_ResultTypeInt() throws Exception {
        List<Integer> list = session.queryStatement("xmltest.CrudMapper.countAll", null);
        assertEquals(1, list.size());
        assertEquals(5, list.get(0).intValue());
    }

    /** 测试 <select> + resultType=string 返回单列 */
    @Test
    public void testSelectNames_ResultTypeString() throws Exception {
        List<String> list = session.queryStatement("xmltest.CrudMapper.selectNames", null);
        assertEquals(5, list.size());
        assertEquals("XmlCrud1", list.get(0));
        assertEquals("XmlCrud5", list.get(4));
    }

    // ========== update 测试 ==========

    /** 测试 <update> 标签修改数据 */
    @Test
    public void testUpdate() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57002);
        params.put("email", "updated@test.com");

        Object result = session.executeStatement("xmltest.CrudMapper.updateEmail", params);
        assertEquals(1, ((Number) result).intValue());

        // 验证更新
        Map<String, Object> q = new HashMap<String, Object>();
        q.put("id", 57002);
        List<UserInfo> list = session.queryStatement("xmltest.CrudMapper.selectById", q);
        assertEquals("updated@test.com", list.get(0).getEmail());
    }

    // ========== delete 测试 ==========

    /** 测试 <delete> 标签删除数据 */
    @Test
    public void testDelete() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57003);

        Object result = session.executeStatement("xmltest.CrudMapper.deleteById", params);
        assertEquals(1, ((Number) result).intValue());

        // 验证删除
        Map<String, Object> q = new HashMap<String, Object>();
        q.put("id", 57003);
        List<UserInfo> list = session.queryStatement("xmltest.CrudMapper.selectById", q);
        assertEquals(0, list.size());
    }

    // ========== execute 测试 ==========

    /** 测试 <execute> 标签执行 DML */
    @Test
    public void testExecute() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "XmlCrud1");

        Object result = session.executeStatement("xmltest.CrudMapper.deleteByName", params);
        assertTrue(((Number) result).intValue() >= 1);

        // 验证数据已被删除
        List<Integer> countList = session.queryStatement("xmltest.CrudMapper.countAll", null);
        assertTrue(countList.get(0) < 5);
    }

    // ========== 查询不存在的记录 ==========

    /** 查询不存在的 ID 应返回空列表 */
    @Test
    public void testSelectNotFound() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 99999);

        List<UserInfo> list = session.queryStatement("xmltest.CrudMapper.selectById", params);
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    // ========== 分页测试 ==========

    /** 测试 session.queryStatement 带 Page 参数 —— 第一页 */
    @Test
    public void testQueryWithPage_FirstPage() throws Exception {
        Page page = new PageObject(0, 3);

        List<UserInfo> list = session.queryStatement("xmltest.CrudMapper.selectForPage", null, page);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("XmlCrud1", list.get(0).getName());
        assertEquals("XmlCrud2", list.get(1).getName());
        assertEquals("XmlCrud3", list.get(2).getName());
    }

    /** 测试第二页（中间页） */
    @Test
    public void testQueryWithPage_SecondPage() throws Exception {
        Page page = new PageObject(1, 2);

        List<UserInfo> list = session.queryStatement("xmltest.CrudMapper.selectForPage", null, page);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("XmlCrud3", list.get(0).getName());
        assertEquals("XmlCrud4", list.get(1).getName());
    }

    /** 测试最后一页（不足一页） */
    @Test
    public void testQueryWithPage_LastPagePartial() throws Exception {
        Page page = new PageObject(2, 2); // 5条数据，每页2条，第三页只有1条

        List<UserInfo> list = session.queryStatement("xmltest.CrudMapper.selectForPage", null, page);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("XmlCrud5", list.get(0).getName());
    }

    /** 测试超出范围的页码应返回空列表 */
    @Test
    public void testQueryWithPage_BeyondRange() throws Exception {
        Page page = new PageObject(5, 2); // 远超数据总量

        List<UserInfo> list = session.queryStatement("xmltest.CrudMapper.selectForPage", null, page);
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    /** 测试页大小大于总数据量，一页返回全部 */
    @Test
    public void testQueryWithPage_PageSizeLargerThanTotal() throws Exception {
        Page page = new PageObject(0, 100);

        List<UserInfo> list = session.queryStatement("xmltest.CrudMapper.selectForPage", null, page);
        assertNotNull(list);
        assertEquals(5, list.size());
    }

    /** 测试每页 1 条记录 */
    @Test
    public void testQueryWithPage_PageSizeOne() throws Exception {
        Page page = new PageObject(0, 1);
        List<UserInfo> list0 = session.queryStatement("xmltest.CrudMapper.selectForPage", null, page);
        assertEquals(1, list0.size());
        assertEquals("XmlCrud1", list0.get(0).getName());

        page = new PageObject(4, 1);
        List<UserInfo> list4 = session.queryStatement("xmltest.CrudMapper.selectForPage", null, page);
        assertEquals(1, list4.size());
        assertEquals("XmlCrud5", list4.get(0).getName());
    }

    /** 测试 session.pageStatement 返回 PageResult —— 第一页 */
    @Test
    public void testPageStatement_FirstPage() throws Exception {
        Page page = new PageObject(0, 2);

        PageResult<UserInfo> result = session.pageStatement("xmltest.CrudMapper.selectForPage", null, page);
        assertNotNull(result);
        assertEquals(2, result.getData().size());
        assertEquals("XmlCrud1", result.getData().get(0).getName());
        assertEquals("XmlCrud2", result.getData().get(1).getName());
        assertEquals(5, result.getTotalCount());
        assertEquals(3, result.getTotalPage());
    }

    /** 测试 pageStatement 第二页 */
    @Test
    public void testPageStatement_SecondPage() throws Exception {
        Page page = new PageObject(1, 2);

        PageResult<UserInfo> result = session.pageStatement("xmltest.CrudMapper.selectForPage", null, page);
        assertNotNull(result);
        assertEquals(2, result.getData().size());
        assertEquals("XmlCrud3", result.getData().get(0).getName());
        assertEquals("XmlCrud4", result.getData().get(1).getName());
        assertEquals(5, result.getTotalCount());
        assertEquals(3, result.getTotalPage());
    }

    /** 测试 pageStatement 最后一页（不满一页） */
    @Test
    public void testPageStatement_LastPagePartial() throws Exception {
        Page page = new PageObject(2, 2);

        PageResult<UserInfo> result = session.pageStatement("xmltest.CrudMapper.selectForPage", null, page);
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals("XmlCrud5", result.getData().get(0).getName());
        assertEquals(5, result.getTotalCount());
        assertEquals(3, result.getTotalPage());
    }

    /** 测试 pageStatement 超出范围返回空数据但 totalCount 正确 */
    @Test
    public void testPageStatement_BeyondRange() throws Exception {
        Page page = new PageObject(10, 2);

        PageResult<UserInfo> result = session.pageStatement("xmltest.CrudMapper.selectForPage", null, page);
        assertNotNull(result);
        assertTrue(result.getData().isEmpty());
        assertEquals(5, result.getTotalCount());
        assertEquals(3, result.getTotalPage());
    }

    /** 测试 pageNumberOffset 使页码从 1 开始（偏移量为 1） */
    @Test
    public void testPageStatement_PageNumberOffset() throws Exception {
        // 设置 pageNumberOffset=1，使页码从1开始
        PageObject page = new PageObject();
        page.setPageNumberOffset(1);
        page.setPageSize(2);
        page.setCurrentPage(1); // 第一页（因为偏移量为1，实际内部currentPage=0）

        PageResult<UserInfo> result = session.pageStatement("xmltest.CrudMapper.selectForPage", null, page);
        assertNotNull(result);
        assertEquals(2, result.getData().size());
        assertEquals("XmlCrud1", result.getData().get(0).getName());
        assertEquals("XmlCrud2", result.getData().get(1).getName());

        // 取第二页
        page.setCurrentPage(2);
        PageResult<UserInfo> result2 = session.pageStatement("xmltest.CrudMapper.selectForPage", null, page);
        assertEquals(2, result2.getData().size());
        assertEquals("XmlCrud3", result2.getData().get(0).getName());
    }

    /** 测试页大小等于总数据量，恰好一页 */
    @Test
    public void testPageStatement_ExactOnePage() throws Exception {
        Page page = new PageObject(0, 5);

        PageResult<UserInfo> result = session.pageStatement("xmltest.CrudMapper.selectForPage", null, page);
        assertNotNull(result);
        assertEquals(5, result.getData().size());
        assertEquals(5, result.getTotalCount());
        assertEquals(1, result.getTotalPage());
    }

    // ========== 多命名空间测试 ==========

    /** 测试同时加载多个 XML 并通过不同命名空间访问 */
    @Test
    public void testMultipleNamespaces() throws Exception {
        Configuration config = new Configuration();
        config.loadMapper("/oneapi/mapper/XmlCrudMapper.xml");
        config.loadMapper("/oneapi/mapper/XmlResultMapMapper.xml");
        Session s = config.newSession(dataSource);

        // 通过不同命名空间执行
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57001);

        List<UserInfo> list1 = s.queryStatement("xmltest.CrudMapper.selectById", params);
        List<UserInfo> list2 = s.queryStatement("xmltest.ResultMapMapper.selectByIdExtended", params);

        assertEquals(1, list1.size());
        assertEquals(1, list2.size());
        assertEquals(list1.get(0).getName(), list2.get(0).getName());
    }
}
