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
 * XML Mapper 结果处理方式测试
 * <p>测试要点：
 * <ul>
 *   <li>resultType：实体类 / map / int / string</li>
 *   <li>resultMap：命名 resultMap 引用</li>
 *   <li>resultRowMapper：ColumnMapRowMapper / SingleColumnRowMapper</li>
 *   <li>resultSetExtractor：ColumnMapResultSetExtractor / PairsResultSetExtractor</li>
 * </ul>
 */
public class XmlResultHandlerTest extends AbstractOneApiTest {

    private Session session;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Configuration config = new Configuration();
        config.loadMapper("/oneapi/mapper/XmlResultHandlerMapper.xml");
        this.session = config.newSession(dataSource);
    }

    @Override
    protected void initData() throws SQLException {
        Object[][] data = {//
                { 57801, "ResHdl1", 25, "hdl1@test.com" },//
                { 57802, "ResHdl2", 30, "hdl2@test.com" },//
                { 57803, "ResHdl3", 35, "hdl3@test.com" },//
        };
        for (Object[] d : data) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)",//
                    new Object[] { d[0], d[1], d[2], d[3] });
        }
    }

    // ========== resultType 测试 ==========

    /** resultType 实体类 */
    @Test
    public void testResultType_Entity() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("minAge", 20);
        List<UserInfo> list = session.queryStatement("xmltest.ResultHandlerMapper.selectByResultType", params);
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(57801), list.get(0).getId());
        assertEquals("ResHdl1", list.get(0).getName());
    }

    /** resultType="map" */
    @Test
    public void testResultType_Map() throws Exception {
        List<Map<String, Object>> list = session.queryStatement("xmltest.ResultHandlerMapper.selectByResultTypeMap", null);
        assertEquals(3, list.size());
        assertEquals("ResHdl1", list.get(0).get("name"));
    }

    /** resultType="int" 聚合 */
    @Test
    public void testResultType_Int() throws Exception {
        List<Integer> list = session.queryStatement("xmltest.ResultHandlerMapper.countByResultType", null);
        assertEquals(1, list.size());
        assertEquals(3, list.get(0).intValue());
    }

    /** resultType="string" 单列 */
    @Test
    public void testResultType_String() throws Exception {
        List<String> list = session.queryStatement("xmltest.ResultHandlerMapper.selectNamesByResultType", null);
        assertEquals(3, list.size());
        assertEquals("ResHdl1", list.get(0));
        assertEquals("ResHdl2", list.get(1));
        assertEquals("ResHdl3", list.get(2));
    }

    // ========== resultMap 测试 ==========

    /** resultMap 引用 */
    @Test
    public void testResultMap() throws Exception {
        List<UserInfo> list = session.queryStatement("xmltest.ResultHandlerMapper.selectByResultMap", null);
        assertEquals(3, list.size());

        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(57801), user.getId());
        assertEquals("ResHdl1", user.getName());
        assertEquals(Integer.valueOf(25), user.getAge());
        assertEquals("hdl1@test.com", user.getEmail());
        assertNotNull(user.getCreateTime());
    }

    // ========== resultRowMapper 测试 ==========

    /** resultRowMapper=ColumnMapRowMapper → 每行为 Map */
    @Test
    public void testResultRowMapper_ColumnMap() throws Exception {
        List<Map<String, Object>> list = session.queryStatement("xmltest.ResultHandlerMapper.selectByColumnMapRowMapper", null);
        assertEquals(3, list.size());
        assertTrue(list.get(0) instanceof Map);
        assertNotNull(list.get(0).get("name"));
    }

    // ========== resultSetExtractor 测试 ==========

    /** resultSetExtractor=ColumnMapResultSetExtractor → List<Map> */
    @Test
    public void testResultSetExtractor_ColumnMap() throws Exception {
        Object result = session.queryStatement("xmltest.ResultHandlerMapper.selectByColumnMapExtractor", null);
        assertTrue(result instanceof List);
        List<Map<String, Object>> list = (List<Map<String, Object>>) result;
        assertEquals(3, list.size());
        assertEquals("ResHdl1", list.get(0).get("name"));
    }

    /** resultSetExtractor=PairsResultSetExtractor → Map<K,V>（queryStatement 包裹为 List） */
    @Test
    @SuppressWarnings("unchecked")
    public void testResultSetExtractor_Pairs() throws Exception {
        // PairsResultSetExtractor.extractData() 返回 Map<K,V>，
        // 但 queryStatement 内部调用 asList() 将非 List 结果包装为单元素 List
        List<Object> list = session.queryStatement("xmltest.ResultHandlerMapper.selectIdNamePairs", null);
        assertEquals(1, list.size());
        assertTrue("First element should be a Map", list.get(0) instanceof Map);
        Map<Object, Object> pairs = (Map<Object, Object>) list.get(0);
        assertEquals(3, pairs.size());
        assertEquals("ResHdl1", pairs.get(57801));
    }
}
