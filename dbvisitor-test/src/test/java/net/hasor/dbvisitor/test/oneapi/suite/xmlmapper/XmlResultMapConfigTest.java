package net.hasor.dbvisitor.test.oneapi.suite.xmlmapper;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * XML Mapper ResultMap 配置功能测试
 * <p>测试要点：
 * <ul>
 *   <li>&lt;resultMap&gt; 的 &lt;id&gt; 和 &lt;result&gt; 子标签</li>
 *   <li>extends 属性（继承父 resultMap）</li>
 *   <li>javaType 属性指定列类型</li>
 *   <li>autoMapping 属性（自动映射未显式声明的列）</li>
 *   <li>caseInsensitive 属性（忽略列名大小写）</li>
 *   <li>mapUnderscoreToCamelCase 属性（下划线转驼峰）</li>
 *   <li>type=HashMap 实现列名重命名</li>
 * </ul>
 */
public class XmlResultMapConfigTest extends AbstractOneApiTest {

    private Session session;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Configuration config = new Configuration();
        config.loadMapper("/oneapi/mapper/XmlResultMapMapper.xml");
        this.session = config.newSession(dataSource);
    }

    @Override
    protected void initData() throws SQLException {
        for (int i = 1; i <= 3; i++) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",//
                    new Object[] { 57100 + i, "RmCfg" + i, 25 + i, "rmcfg" + i + "@test.com" });
        }
    }

    // ========== baseResultMap 测试（仅 id/name/age） ==========

    /** 使用仅映射 id/name/age 的基础 resultMap，email 和 createTime 应为 null */
    @Test
    public void testBaseResultMap_PartialMapping() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57101);

        List<UserInfo> list = session.queryStatement("xmltest.ResultMapMapper.selectByIdBase", params);
        assertEquals(1, list.size());

        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(57101), user.getId());
        assertEquals("RmCfg1", user.getName());
        assertEquals(Integer.valueOf(26), user.getAge());
        // 未映射的字段
        assertNull("email should be null (not mapped in baseResultMap)", user.getEmail());
        assertNull("createTime should be null (not mapped in baseResultMap)", user.getCreateTime());
    }

    // ========== extends 继承测试 ==========

    /** extendedResultMap 完整列映射（id/name/age/email/createTime） */
    @Test
    public void testExtendedResultMap_FullMapping() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57101);

        List<UserInfo> list = session.queryStatement("xmltest.ResultMapMapper.selectByIdExtended", params);
        assertEquals(1, list.size());

        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(57101), user.getId());
        assertEquals("RmCfg1", user.getName());
        assertEquals(Integer.valueOf(26), user.getAge());
        assertEquals("rmcfg1@test.com", user.getEmail());
        assertNotNull("createTime should be mapped", user.getCreateTime());
    }

    // ========== javaType 测试 ==========

    /** typedResultMap 通过 javaType 显式指定每个列的 Java 类型 */
    @Test
    public void testTypedResultMap_WithJavaType() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57102);

        List<UserInfo> list = session.queryStatement("xmltest.ResultMapMapper.selectByIdTyped", params);
        assertEquals(1, list.size());

        UserInfo user = list.get(0);
        assertTrue(user.getId() instanceof Integer);
        assertTrue(user.getName() instanceof String);
        assertTrue(user.getAge() instanceof Integer);
        assertEquals("rmcfg2@test.com", user.getEmail());
    }

    // ========== autoMapping 测试 ==========

    /** autoMapping=true + 显式映射所有列 */
    @Test
    public void testAutoMapping_AllColumnsMapped() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57101);

        List<UserInfo> list = session.queryStatement("xmltest.ResultMapMapper.selectByIdAutoMapping", params);
        assertEquals(1, list.size());

        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(57101), user.getId());
        assertEquals("RmCfg1", user.getName());
        assertEquals(Integer.valueOf(26), user.getAge());
        assertEquals("rmcfg1@test.com", user.getEmail());
    }

    // ========== caseInsensitive 测试 ==========

    /** caseInsensitive=true：resultMap 中用大写列名，仍能映射到实际小写列 */
    @Test
    public void testCaseInsensitiveResultMap() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57102);

        List<UserInfo> list = session.queryStatement("xmltest.ResultMapMapper.selectByIdCaseInsensitive", params);
        assertEquals(1, list.size());

        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(57102), user.getId());
        assertEquals("RmCfg2", user.getName());
        assertEquals(Integer.valueOf(27), user.getAge());
        assertEquals("rmcfg2@test.com", user.getEmail());
    }

    // ========== mapUnderscoreToCamelCase 测试 ==========

    /** mapUnderscoreToCamelCase=true + 显式映射 create_time → createTime */
    @Test
    public void testCamelCaseResultMap() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57101);

        List<UserInfo> list = session.queryStatement("xmltest.ResultMapMapper.selectByIdCamelCase", params);
        assertEquals(1, list.size());

        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(57101), user.getId());
        assertEquals("RmCfg1", user.getName());
        // create_time → createTime（显式映射 + mapUnderscoreToCamelCase）
        assertNotNull("create_time should be mapped to createTime", user.getCreateTime());
    }

    // ========== Map 类型 resultType + 列别名（列名重命名） ==========

    /** 使用 resultType="map" + SQL 列别名把数据库列名映射为自定义 Map key */
    @Test
    public void testMapResultType_ColumnRenaming() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57103);

        List<Map<String, Object>> list = session.queryStatement("xmltest.ResultMapMapper.selectByIdAsRenamedMap", params);
        assertEquals(1, list.size());

        Map<String, Object> row = list.get(0);
        // id AS user_id, name AS user_name, age AS user_age
        assertEquals(57103, ((Number) row.get("user_id")).intValue());
        assertEquals("RmCfg3", row.get("user_name"));
        assertEquals(28, ((Number) row.get("user_age")).intValue());
    }

    // ========== 列表查询（base） ==========

    /** baseResultMap 的列表查询 */
    @Test
    public void testSelectAllBase() throws Exception {
        List<UserInfo> list = session.queryStatement("xmltest.ResultMapMapper.selectAllBase", null);
        assertEquals(3, list.size());
        for (UserInfo u : list) {
            assertNotNull(u.getId());
            assertNotNull(u.getName());
            assertNotNull(u.getAge());
            assertNull("email not mapped in baseResultMap", u.getEmail());
        }
    }
}
