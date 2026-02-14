package net.hasor.dbvisitor.test.suite.xmlmapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * XML Mapper SQL 片段（&lt;sql&gt; / &lt;include&gt;）测试
 * <p>测试要点：
 * <ul>
 *   <li>&lt;sql id="..."&gt; 定义可复用的 SQL 片段</li>
 *   <li>&lt;include refid="..."&gt; 在 SELECT 列、WHERE、ORDER BY 等位置引用片段</li>
 *   <li>片段中包含动态 SQL（&lt;if&gt;）</li>
 *   <li>同一语句中使用多个 include</li>
 * </ul>
 */
public class XmlSqlFragmentTest extends AbstractOneApiTest {

    private Session session;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Configuration config = new Configuration();
        config.loadMapper("/mapper/XmlSqlFragmentMapper.xml");
        this.session = config.newSession(dataSource);
    }

    @Override
    protected void initData() {
        try {
            for (int i = 1; i <= 5; i++) {
                jdbcTemplate.executeUpdate(//
                        "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",//
                        new Object[] { 57300 + i, "SqlFrag" + i, 20 + i * 5, "frag" + i + "@test.com" });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ========== 列名片段 ==========

    /** 在 SELECT 列中使用 <include refid="baseColumns"/> */
    @Test
    public void testColumnFragment_InSelect() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57301);

        List<UserInfo> list = session.queryStatement("xmltest.SqlFragmentMapper.selectWithColumnFragment", params);
        assertEquals(1, list.size());

        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(57301), user.getId());
        assertEquals("SqlFrag1", user.getName());
        assertEquals(Integer.valueOf(25), user.getAge());
        assertEquals("frag1@test.com", user.getEmail());
        assertNotNull(user.getCreateTime());
    }

    // ========== 条件片段（含动态 SQL） ==========

    /** 引用包含 <if> 的条件片段：无额外条件 */
    @Test
    public void testConditionFragment_NoCondition() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        List<UserInfo> list = session.queryStatement("xmltest.SqlFragmentMapper.selectWithConditionFragment", params);
        assertTrue(list.size() >= 5);
    }

    /** 引用包含 <if> 的条件片段：仅 name */
    @Test
    public void testConditionFragment_WithName() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "SqlFrag1");
        List<UserInfo> list = session.queryStatement("xmltest.SqlFragmentMapper.selectWithConditionFragment", params);
        assertEquals(1, list.size());
        assertEquals("SqlFrag1", list.get(0).getName());
    }

    /** 引用包含 <if> 的条件片段：年龄范围 */
    @Test
    public void testConditionFragment_WithAgeRange() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("minAge", 30);
        params.put("maxAge", 40);
        List<UserInfo> list = session.queryStatement("xmltest.SqlFragmentMapper.selectWithConditionFragment", params);
        assertTrue(list.size() >= 2);
        for (UserInfo u : list) {
            assertTrue(u.getAge() >= 30 && u.getAge() <= 40);
        }
    }

    /** 条件片段：所有条件联合过滤 */
    @Test
    public void testConditionFragment_AllConditions() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "SqlFrag%");
        params.put("minAge", 25);
        params.put("maxAge", 35);
        List<UserInfo> list = session.queryStatement("xmltest.SqlFragmentMapper.selectWithConditionFragment", params);
        assertTrue(list.size() >= 2);
    }

    // ========== 多片段组合 ==========

    /** 同时引用列名片段和排序片段 */
    @Test
    public void testMultipleFragments() throws Exception {
        List<UserInfo> list = session.queryStatement("xmltest.SqlFragmentMapper.selectWithMultipleFragments", null);
        assertEquals(5, list.size());
        // ORDER BY id ASC
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getId() < list.get(i).getId());
        }
    }

    /** 仅使用排序片段 */
    @Test
    public void testOrderFragment_Only() throws Exception {
        List<UserInfo> list = session.queryStatement("xmltest.SqlFragmentMapper.selectAllOrdered", null);
        assertEquals(5, list.size());
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getId() < list.get(i).getId());
        }
    }
}
