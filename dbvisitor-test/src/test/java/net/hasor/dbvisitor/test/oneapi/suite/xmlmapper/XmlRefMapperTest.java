package net.hasor.dbvisitor.test.oneapi.suite.xmlmapper;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.dao.XmlRefMapperDao;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @RefMapper + XML Mapper 混合测试
 * <p>测试要点：
 * <ul>
 *   <li>@RefMapper 接口通过命名空间绑定 XML 中的语句</li>
 *   <li>通过 DAO 接口调用 CRUD</li>
 *   <li>DAO 动态 SQL（where/if/foreach）</li>
 *   <li>Map 参数和 Bean 参数</li>
 *   <li>${} 文本替换</li>
 *   <li>resultType="map" 聚合查询</li>
 * </ul>
 */
public class XmlRefMapperTest extends AbstractOneApiTest {
    private Session         session;
    private XmlRefMapperDao dao;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Configuration config = new Configuration();
        // @RefMapper on XmlRefMapperDao points to the XML resource path;
        // createMapper() calls loadMapper(Class) internally which loads the XML
        this.session = config.newSession(dataSource);
        this.dao = session.createMapper(XmlRefMapperDao.class);
    }

    @Override
    protected void initData() throws SQLException {
        Object[][] data = {//
                { 57901, "RefMapA", 22, "refa@test.com" },//
                { 57902, "RefMapB", 28, "refb@test.com" },//
                { 57903, "RefMapC", 35, "refc@test.com" },//
                { 57904, "RefMapD", 28, "refd@test.com" },// 与 B 同 age，用于聚合
        };
        for (Object[] d : data) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",//
                    new Object[] { d[0], d[1], d[2], d[3] });
        }
    }

    // ========== CRUD ==========

    /** insert + selectById */
    @Test
    public void testInsertAndSelectById() throws Exception {
        int rows = dao.insertUser(57910, "RefMapNew", 33, "new@test.com");
        assertEquals(1, rows);

        UserInfo user = dao.selectById(57910);
        assertNotNull(user);
        assertEquals(Integer.valueOf(57910), user.getId());
        assertEquals("RefMapNew", user.getName());
        assertEquals(Integer.valueOf(33), user.getAge());
        assertEquals("new@test.com", user.getEmail());
    }

    /** selectAll */
    @Test
    public void testSelectAll() throws Exception {
        List<UserInfo> list = dao.selectAll();
        assertEquals(4, list.size());
        assertEquals("RefMapA", list.get(0).getName());
    }

    /** updateEmail */
    @Test
    public void testUpdateEmail() throws Exception {
        int rows = dao.updateEmail(57901, "updated@test.com");
        assertEquals(1, rows);

        UserInfo user = dao.selectById(57901);
        assertEquals("updated@test.com", user.getEmail());
    }

    /** deleteById */
    @Test
    public void testDeleteById() throws Exception {
        int rows = dao.deleteById(57904);
        assertEquals(1, rows);

        UserInfo user = dao.selectById(57904);
        assertNull(user);
    }

    // ========== 动态 SQL ==========

    /** selectByCondition：无条件 → 返回所有匹配 */
    @Test
    public void testSelectByCondition_NoCondition() throws Exception {
        List<UserInfo> list = dao.selectByCondition(null, null);
        assertTrue(list.size() >= 4);
    }

    /** selectByCondition：仅 name 条件 */
    @Test
    public void testSelectByCondition_ByName() throws Exception {
        List<UserInfo> list = dao.selectByCondition("RefMapA", null);
        assertEquals(1, list.size());
        assertEquals("RefMapA", list.get(0).getName());
    }

    /** selectByCondition：name + minAge */
    @Test
    public void testSelectByCondition_ByNameAndAge() throws Exception {
        List<UserInfo> list = dao.selectByCondition("RefMap%", 30);
        assertEquals(1, list.size());
        assertEquals("RefMapC", list.get(0).getName());
    }

    // ========== foreach IN ==========

    /** selectByIds：IN 子句 */
    @Test
    public void testSelectByIds() throws Exception {
        List<UserInfo> list = dao.selectByIds(Arrays.asList(57901, 57903));
        assertEquals(2, list.size());
        assertEquals("RefMapA", list.get(0).getName());
        assertEquals("RefMapC", list.get(1).getName());
    }

    // ========== 范围查询 ==========

    /** selectByAgeRange：BETWEEN */
    @Test
    public void testSelectByAgeRange() throws Exception {
        List<UserInfo> list = dao.selectByAgeRange(25, 30);
        assertEquals(2, list.size()); // B(28) + D(28)
    }

    // ========== Bean 参数 ==========

    /** selectByBean：LIKE + >= */
    @Test
    public void testSelectByBean() throws Exception {
        List<UserInfo> list = dao.selectByBean("RefMap%", 28);
        assertEquals(3, list.size()); // B(28), C(35), D(28)
    }

    // ========== ${} 文本替换 ==========

    /** selectWithOrderBy：${orderColumn} 动态 ORDER BY */
    @Test
    public void testSelectWithOrderBy_ById() throws Exception {
        List<UserInfo> list = dao.selectWithOrderBy("id");
        assertEquals(4, list.size());
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getId() < list.get(i).getId());
        }
    }

    /** selectWithOrderBy：按 age 排序 */
    @Test
    public void testSelectWithOrderBy_ByAge() throws Exception {
        List<UserInfo> list = dao.selectWithOrderBy("age");
        assertEquals(4, list.size());
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getAge() <= list.get(i).getAge());
        }
    }

    // ========== resultType map 聚合 ==========

    /** selectAgeStats：GROUP BY + COUNT */
    @Test
    public void testSelectAgeStats() throws Exception {
        List<Map<String, Object>> list = dao.selectAgeStats();
        assertTrue(list.size() >= 3);
        // age=28 应有 2 条
        boolean found28 = false;
        for (Map<String, Object> row : list) {
            if (((Number) row.get("age")).intValue() == 28) {
                assertEquals(2L, ((Number) row.get("cnt")).longValue());
                found28 = true;
            }
        }
        assertTrue("Should find age=28 group", found28);
    }
}
