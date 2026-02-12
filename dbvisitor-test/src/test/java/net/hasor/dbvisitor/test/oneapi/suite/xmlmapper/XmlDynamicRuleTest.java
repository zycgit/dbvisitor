package net.hasor.dbvisitor.test.oneapi.suite.xmlmapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * XML Mapper 动态规则（@{...}）测试
 * <p>测试要点：
 * <ul>
 *   <li>@{and, condition}：追加 AND 条件，参数为 null 时自动跳过</li>
 *   <li>@{or, condition}：追加 OR 条件</li>
 *   <li>@{and, ... IN @{in, :ids}}：嵌套 @{in} 列表展开</li>
 *   <li>@{...} 与 XML 动态标签混合使用</li>
 *   <li>多 @{and} 规则组合（where 1=1 模式）</li>
 * </ul>
 */
public class XmlDynamicRuleTest extends AbstractOneApiTest {

    private Session session;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Configuration config = new Configuration();
        config.loadMapper("/oneapi/mapper/XmlDynamicRuleMapper.xml");
        this.session = config.newSession(dataSource);
    }

    @Override
    protected void initData() {
        try {
            String[] names = { "DynRuleA", "DynRuleB", "DynRuleC", "DynRuleD" };
            int[] ages = { 22, 28, 35, 45 };
            String[] emails = { "a@test.com", "b@test.com", "c@test.com", "d@test.com" };
            for (int i = 0; i < 4; i++) {
                jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)", new Object[] { 57401 + i, names[i], ages[i], emails[i] });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ========== @{and} 单条件 ==========

    /** @{and, age = :age} 追加 AND 条件 */
    @Test
    public void testAndRule_SingleCondition() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("age", 28);
        List<UserInfo> list = session.queryStatement("xmltest.DynamicRuleMapper.selectWithAndSingle", params);
        assertEquals(1, list.size());
        assertEquals("DynRuleB", list.get(0).getName());
    }

    /** @{and} 查不同值 */
    @Test
    public void testAndRule_DifferentValue() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("age", 45);
        List<UserInfo> list = session.queryStatement("xmltest.DynamicRuleMapper.selectWithAndSingle", params);
        assertEquals(1, list.size());
        assertEquals("DynRuleD", list.get(0).getName());
    }

    // ========== @{and} 双条件 ==========

    /** @{and} 多条件同时匹配 */
    @Test
    public void testAndRule_DoubleCondition() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("age", 35);
        params.put("email", "c@test.com");
        List<UserInfo> list = session.queryStatement("xmltest.DynamicRuleMapper.selectWithAndDouble", params);
        assertEquals(1, list.size());
        assertEquals("DynRuleC", list.get(0).getName());
    }

    // ========== @{and} null 参数自动跳过 ==========

    /** @{and} 参数全 null 时条件自动跳过，返回所有记录 */
    @Test
    public void testAndRule_AllNull() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        List<UserInfo> list = session.queryStatement("xmltest.DynamicRuleMapper.selectWithAndDouble", params);
        assertEquals(4, list.size());
    }

    /** @{and} 部分 null：仅 age 有值，email 的条件自动跳过 */
    @Test
    public void testAndRule_PartialNull() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("age", 28);
        List<UserInfo> list = session.queryStatement("xmltest.DynamicRuleMapper.selectWithAndDouble", params);
        assertEquals(1, list.size());
        assertEquals("DynRuleB", list.get(0).getName());
    }

    // ========== @{or} 规则 ==========

    /** @{or} 参数非空时追加 OR 条件 */
    @Test
    public void testOrRule_WithAge() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("age", 22);
        params.put("email", "no_match@test.com");
        List<UserInfo> list = session.queryStatement("xmltest.DynamicRuleMapper.selectWithOrRule", params);
        assertEquals(1, list.size());
        assertEquals("DynRuleA", list.get(0).getName());
    }

    /** @{or} 多个条件满足时取并集 */
    @Test
    public void testOrRule_WithAgeOrEmail() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("age", 22);
        params.put("email", "d@test.com");
        List<UserInfo> list = session.queryStatement("xmltest.DynamicRuleMapper.selectWithOrRule", params);
        assertEquals(2, list.size());
    }

    // ========== @{and, ... IN @{in, :ids}} ==========

    /** @{and, id IN @{in, :ids}} 嵌套规则生成 IN 子句 */
    @Test
    public void testInRule() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ids", Arrays.asList(57401, 57403));
        List<UserInfo> list = session.queryStatement("xmltest.DynamicRuleMapper.selectWithInRule", params);
        assertEquals(2, list.size());
        assertEquals("DynRuleA", list.get(0).getName());
        assertEquals("DynRuleC", list.get(1).getName());
    }

    // ========== 混合使用 ==========

    /** @{and} + &lt;if&gt; + XML 动态标签混合 */
    @Test
    public void testMixed_RuleAndXmlTag() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("minAge", 25);
        params.put("email", "c@test.com");
        List<UserInfo> list = session.queryStatement("xmltest.DynamicRuleMapper.selectMixed", params);
        assertEquals(1, list.size());
        assertEquals("DynRuleC", list.get(0).getName());
    }

    /** @{and} 多条件组合（where 1=1 模式） */
    @Test
    public void testMultipleAndRules() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "DynRule%");
        params.put("minAge", 25);
        params.put("maxAge", 40);
        List<UserInfo> list = session.queryStatement("xmltest.DynamicRuleMapper.selectMultipleAndRules", params);
        assertEquals(2, list.size()); // DynRuleB(28) + DynRuleC(35)
    }
}
