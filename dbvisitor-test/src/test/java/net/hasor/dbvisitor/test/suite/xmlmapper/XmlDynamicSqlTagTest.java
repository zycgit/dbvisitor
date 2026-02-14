package net.hasor.dbvisitor.test.suite.xmlmapper;

import java.util.*;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * XML Mapper 动态 SQL 标签测试
 * <p>测试要点：
 * <ul>
 *   <li>&lt;if test="..."&gt; 条件包含</li>
 *   <li>&lt;choose&gt;/&lt;when&gt;/&lt;otherwise&gt; 分支选择</li>
 *   <li>&lt;where&gt; 自动处理 AND/OR 前缀</li>
 *   <li>&lt;set&gt; 自动处理尾部逗号</li>
 *   <li>&lt;trim&gt; 自定义前后缀与覆盖</li>
 *   <li>&lt;foreach&gt; 遍历集合：IN 子句与批量 VALUES</li>
 *   <li>&lt;bind&gt; 变量绑定</li>
 *   <li>多标签组合使用</li>
 * </ul>
 */
public class XmlDynamicSqlTagTest extends AbstractOneApiTest {

    private Session session;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Configuration config = new Configuration();
        config.loadMapper("/mapper/XmlDynamicSqlMapper.xml");
        this.session = config.newSession(dataSource);
    }

    @Override
    protected void initData() {
        try {
            String[] names = { "DynSqlAlice", "DynSqlBob", "DynSqlCarol", "DynSqlDave", "DynSqlEve" };
            int[] ages = { 22, 28, 35, 42, 50 };
            for (int i = 0; i < 5; i++) {
                jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)", new Object[] { 57200 + i + 1, names[i], ages[i], names[i].toLowerCase() + "@test.com" });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ========== if 标签 ==========

    /** <if> 条件为 null 时不追加 SQL 片段 */
    @Test
    public void testIf_AllNull_ReturnAll() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectWithIf", params);
        assertEquals(5, list.size());
    }

    /** <if> 条件非 null 时追加 AND 片段 */
    @Test
    public void testIf_WithMinAge() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("minAge", 30);
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectWithIf", params);
        assertTrue(list.size() >= 3);
        for (UserInfo u : list) {
            assertTrue(u.getAge() >= 30);
        }
    }

    /** <if> 两个条件同时生效 */
    @Test
    public void testIf_WithMinAgeAndEmail() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("minAge", 20);
        params.put("email", "dynsqldave@test.com");
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectWithIf", params);
        assertEquals(1, list.size());
        assertEquals("DynSqlDave", list.get(0).getName());
    }

    // ========== choose/when/otherwise ==========

    /** <choose> 匹配第一个 <when> */
    @Test
    public void testChoose_OrderByName() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("orderBy", "name");
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectWithChoose", params);
        assertEquals(5, list.size());
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getName().compareTo(list.get(i).getName()) <= 0);
        }
    }

    /** <choose> 匹配第二个 <when> */
    @Test
    public void testChoose_OrderByAge() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("orderBy", "age");
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectWithChoose", params);
        assertEquals(5, list.size());
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getAge() >= list.get(i).getAge());
        }
    }

    /** <choose> 无匹配时走 <otherwise> */
    @Test
    public void testChoose_Otherwise() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("orderBy", "unknown");
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectWithChoose", params);
        assertEquals(5, list.size());
        // otherwise: ORDER BY id ASC
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getId() < list.get(i).getId());
        }
    }

    // ========== where 标签 ==========

    /** <where> 全部条件为 null：不生成 WHERE 子句 */
    @Test
    public void testWhere_AllNull() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectWithWhere", params);
        assertTrue(list.size() >= 5);
    }

    /** <where> 单一条件：自动去除前导 AND */
    @Test
    public void testWhere_SingleCondition() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "DynSqlAlice");
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectWithWhere", params);
        assertEquals(1, list.size());
        assertEquals("DynSqlAlice", list.get(0).getName());
    }

    /** <where> 多条件组合 */
    @Test
    public void testWhere_MultipleConditions() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "DynSqlBob");
        params.put("age", 28);
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectWithWhere", params);
        assertEquals(1, list.size());
    }

    // ========== set 标签 ==========

    /** <set> 自动去除末尾逗号，只更新非 null 字段 */
    @Test
    public void testSet_PartialUpdate() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57201);
        params.put("age", 99);
        // name 和 email 为 null，不更新
        session.executeStatement("xmltest.DynamicSqlMapper.updateWithSet", params);

        Map<String, Object> q = new HashMap<String, Object>();
        q.put("name", "DynSqlAlice");
        q.put("age", 99);
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectWithWhere", q);
        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(99), list.get(0).getAge());
        assertEquals("dynsqlalice@test.com", list.get(0).getEmail()); // 未被修改
    }

    /** <set> 更新多个字段 */
    @Test
    public void testSet_MultipleFields() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57202);
        params.put("name", "UpdatedBob");
        params.put("email", "bob_new@test.com");
        session.executeStatement("xmltest.DynamicSqlMapper.updateWithSet", params);

        Map<String, Object> q = new HashMap<String, Object>();
        q.put("name", "UpdatedBob");
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectWithWhere", q);
        assertEquals(1, list.size());
        assertEquals("bob_new@test.com", list.get(0).getEmail());
    }

    // ========== trim 标签 ==========

    /** <trim> 自动添加 WHERE 前缀并去除 AND 前缀 */
    @Test
    public void testTrim_WithConditions() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "DynSqlCarol");
        params.put("age", 35);
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectWithTrim", params);
        assertEquals(1, list.size());
        assertEquals("DynSqlCarol", list.get(0).getName());
    }

    /** <trim> 所有条件为 null 时不生成 WHERE */
    @Test
    public void testTrim_NoConditions() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectWithTrim", params);
        assertTrue(list.size() >= 5);
    }

    // ========== foreach 标签 ==========

    /** <foreach> 生成 IN (?, ?, ?) 子句 */
    @Test
    public void testForeach_InClause() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ids", Arrays.asList(57201, 57203, 57205));
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectByIdList", params);
        assertEquals(3, list.size());
        assertEquals("DynSqlAlice", list.get(0).getName());
        assertEquals("DynSqlCarol", list.get(1).getName());
        assertEquals("DynSqlEve", list.get(2).getName());
    }

    /** <foreach> 生成批量 VALUES 插入 */
    @Test
    public void testForeach_BatchInsert() throws Exception {
        List<Map<String, Object>> users = new ArrayList<Map<String, Object>>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> u = new HashMap<String, Object>();
            u.put("id", 57220 + i);
            u.put("name", "DynSqlBatch" + i);
            u.put("age", 30 + i);
            u.put("email", "batch" + i + "@test.com");
            users.add(u);
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("users", users);
        session.executeStatement("xmltest.DynamicSqlMapper.batchInsert", params);

        // 验证插入
        Map<String, Object> q = new HashMap<String, Object>();
        q.put("ids", Arrays.asList(57221, 57222, 57223));
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectByIdList", q);
        assertEquals(3, list.size());
        assertEquals("DynSqlBatch1", list.get(0).getName());
    }

    // ========== bind 标签 ==========

    /** <bind> 绑定变量用于 LIKE 查询 */
    @Test
    public void testBind_LikePattern() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "DynSql");
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectWithBind", params);
        assertEquals(5, list.size());
    }

    /** <bind> 绑定变量精确匹配部分名称 */
    @Test
    public void testBind_PartialMatch() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "Alice");
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectWithBind", params);
        assertEquals(1, list.size());
        assertEquals("DynSqlAlice", list.get(0).getName());
    }

    // ========== 组合测试 ==========

    /** where + if + foreach 组合 */
    @Test
    public void testComplex_WhereIfForeach() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "DynSql%");
        params.put("ids", Arrays.asList(57201, 57202));
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectComplex", params);
        assertEquals(2, list.size());
    }

    /** choose + where 组合 */
    @Test
    public void testComplex_ChooseWhere_Young() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("category", "young");
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectByAgeCategory", params);
        assertTrue(list.size() >= 2);
        for (UserInfo u : list) {
            assertTrue(u.getAge() < 30);
        }
    }

    /** choose + where 组合（otherwise 分支） */
    @Test
    public void testComplex_ChooseWhere_Senior() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("category", "senior");
        List<UserInfo> list = session.queryStatement("xmltest.DynamicSqlMapper.selectByAgeCategory", params);
        assertTrue(list.size() >= 2);
        for (UserInfo u : list) {
            assertTrue(u.getAge() > 40);
        }
    }
}
