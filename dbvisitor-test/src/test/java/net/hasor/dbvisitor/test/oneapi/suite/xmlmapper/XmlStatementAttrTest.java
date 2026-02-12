package net.hasor.dbvisitor.test.oneapi.suite.xmlmapper;

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
import static org.junit.Assert.assertNotNull;

/**
 * XML Mapper 语句属性配置测试
 * <p>测试要点：
 * <ul>
 *   <li>statementType="PREPARED" / "STATEMENT"</li>
 *   <li>timeout 超时配置</li>
 *   <li>fetchSize 每次提取行数</li>
 *   <li>resultSetType (FORWARD_ONLY / SCROLL_INSENSITIVE)</li>
 *   <li>多属性组合</li>
 * </ul>
 */
public class XmlStatementAttrTest extends AbstractOneApiTest {

    private Session session;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Configuration config = new Configuration();
        config.loadMapper("/oneapi/mapper/XmlStatementAttrMapper.xml");
        this.session = config.newSession(dataSource);
    }

    @Override
    protected void initData() {
        try {
            for (int i = 1; i <= 5; i++) {
                jdbcTemplate.executeUpdate(//
                        "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",//
                        new Object[] { 57500 + i, "StmtAttr" + i, 20 + i, "attr" + i + "@test.com" });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ========== statementType 测试 ==========

    /** 默认 PREPARED 类型正常查询 */
    @Test
    public void testStatementType_DefaultPrepared() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57501);

        List<UserInfo> list = session.queryStatement("xmltest.StatementAttrMapper.selectPrepared", params);
        assertEquals(1, list.size());
        assertEquals("StmtAttr1", list.get(0).getName());
    }

    /** 显式 PREPARED 类型 */
    @Test
    public void testStatementType_ExplicitPrepared() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57502);

        List<UserInfo> list = session.queryStatement("xmltest.StatementAttrMapper.selectExplicitPrepared", params);
        assertEquals(1, list.size());
        assertEquals("StmtAttr2", list.get(0).getName());
    }

    /** STATEMENT 类型（非预编译） */
    @Test
    public void testStatementType_Statement() throws Exception {
        List<UserInfo> list = session.queryStatement("xmltest.StatementAttrMapper.selectStatement", null);
        assertEquals(5, list.size());
    }

    // ========== timeout 测试 ==========

    /** 有 timeout 属性的查询应正常执行（简单 SQL 不会超时） */
    @Test
    public void testTimeout() throws Exception {
        List<UserInfo> list = session.queryStatement("xmltest.StatementAttrMapper.selectWithTimeout", null);
        assertEquals(5, list.size());
    }

    // ========== fetchSize 测试 ==========

    /** fetchSize=5，结果集应完整返回（fetchSize 不影响最终结果数量） */
    @Test
    public void testFetchSize() throws Exception {
        List<UserInfo> list = session.queryStatement("xmltest.StatementAttrMapper.selectWithFetchSize", null);
        assertEquals(5, list.size());
    }

    // ========== resultSetType 测试 ==========

    /** resultSetType=FORWARD_ONLY */
    @Test
    public void testResultSetType_ForwardOnly() throws Exception {
        List<UserInfo> list = session.queryStatement("xmltest.StatementAttrMapper.selectForwardOnly", null);
        assertEquals(5, list.size());
    }

    /** resultSetType=SCROLL_INSENSITIVE */
    @Test
    public void testResultSetType_ScrollInsensitive() throws Exception {
        List<UserInfo> list = session.queryStatement("xmltest.StatementAttrMapper.selectScrollInsensitive", null);
        assertEquals(5, list.size());
    }

    // ========== 组合属性测试 ==========

    /** 同时设置 statementType + timeout + fetchSize + resultSetType */
    @Test
    public void testCombinedAttributes() throws Exception {
        List<UserInfo> list = session.queryStatement("xmltest.StatementAttrMapper.selectCombined", null);
        assertEquals(5, list.size());
        for (UserInfo u : list) {
            assertNotNull(u.getId());
            assertNotNull(u.getName());
        }
    }

    // ========== insert 的 statementType 测试 ==========

    /** 使用 STATEMENT 类型的 insert（${} 文本替换参数） */
    @Test
    public void testInsertWithStatementType() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57510);
        params.put("name", "StmtAttrInsert");
        params.put("age", 30);
        params.put("email", "stmt@test.com");

        session.executeStatement("xmltest.StatementAttrMapper.insertWithStatement", params);

        Map<String, Object> q = new HashMap<String, Object>();
        q.put("id", 57510);
        List<UserInfo> list = session.queryStatement("xmltest.StatementAttrMapper.selectPrepared", q);
        assertEquals(1, list.size());
        assertEquals("StmtAttrInsert", list.get(0).getName());
    }
}
