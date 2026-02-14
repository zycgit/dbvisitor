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
 * XML Mapper 主键生成测试
 * <p>测试要点：
 * <ul>
 *   <li>useGeneratedKeys="true" + keyProperty：插入后回填自增主键</li>
 *   <li>useGeneratedKeys + keyColumn：指定数据库列名</li>
 *   <li>&lt;selectKey order="BEFORE"&gt;：先查询主键再插入</li>
 *   <li>&lt;selectKey order="AFTER"&gt;：插入后查询主键</li>
 * </ul>
 */
public class XmlKeyGenerationTest extends AbstractOneApiTest {

    private Session session;

    @Before
    public void setUp() throws Exception {
        super.setup();
        Configuration config = new Configuration();
        config.loadMapper("/mapper/XmlKeyGenerationMapper.xml");
        this.session = config.newSession(dataSource);
    }

    // ========== useGeneratedKeys 测试 ==========

    /** useGeneratedKeys=true：插入后自动回填 id */
    @Test
    public void testUseGeneratedKeys() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "KeyGenUser1");
        params.put("age", 25);
        params.put("email", "keygen1@test.com");

        Object result = session.executeStatement("xmltest.KeyGenerationMapper.insertWithGeneratedKeys", params);
        assertEquals(1, ((Number) result).intValue());

        // 验证 id 被回填
        Object generatedId = params.get("id");
        assertNotNull("Generated ID should be populated", generatedId);
        assertTrue("Generated ID should be positive", ((Number) generatedId).intValue() > 0);

        // 查询验证
        Map<String, Object> q = new HashMap<String, Object>();
        q.put("id", generatedId);
        List<UserInfo> list = session.queryStatement("xmltest.KeyGenerationMapper.selectById", q);
        assertEquals(1, list.size());
        assertEquals("KeyGenUser1", list.get(0).getName());
    }

    /** useGeneratedKeys + keyColumn：指定列名 */
    @Test
    public void testUseGeneratedKeys_WithKeyColumn() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "KeyGenUser2");
        params.put("age", 30);
        params.put("email", "keygen2@test.com");

        session.executeStatement("xmltest.KeyGenerationMapper.insertWithKeyColumn", params);

        Object generatedId = params.get("id");
        assertNotNull("Generated ID should be populated with keyColumn", generatedId);
        assertTrue(((Number) generatedId).intValue() > 0);
    }

    /** 多次使用 useGeneratedKeys，每次 id 应不同 */
    @Test
    public void testUseGeneratedKeys_MultipleInserts() throws Exception {
        int prevId = 0;
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("name", "KeyGenMulti" + i);
            params.put("age", 20 + i);
            params.put("email", "multi" + i + "@test.com");

            session.executeStatement("xmltest.KeyGenerationMapper.insertWithGeneratedKeys", params);

            Object generatedId = params.get("id");
            assertNotNull("Generated ID should be populated for insert #" + i, generatedId);
            int currentId = ((Number) generatedId).intValue();
            assertTrue("Each insert should get a unique increasing ID", currentId > prevId);
            prevId = currentId;
        }
    }

    // ========== selectKey 测试 ==========

    /** selectKey order=BEFORE：先获取序列值再插入 */
    @Test
    public void testSelectKey_Before() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "KeyGenBefore");
        params.put("age", 35);
        params.put("email", "before@test.com");

        session.executeStatement("xmltest.KeyGenerationMapper.insertWithSelectKeyBefore", params);

        Object generatedId = params.get("id");
        assertNotNull("ID should be set by selectKey BEFORE", generatedId);
        assertTrue(((Number) generatedId).intValue() > 0);

        // 查询验证
        Map<String, Object> q = new HashMap<String, Object>();
        q.put("id", generatedId);
        List<UserInfo> list = session.queryStatement("xmltest.KeyGenerationMapper.selectById", q);
        assertEquals(1, list.size());
        assertEquals("KeyGenBefore", list.get(0).getName());
    }

    /** selectKey order=AFTER：插入后获取主键 */
    @Test
    public void testSelectKey_After() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "KeyGenAfter");
        params.put("age", 40);
        params.put("email", "after@test.com");

        session.executeStatement("xmltest.KeyGenerationMapper.insertWithSelectKeyAfter", params);

        Object generatedId = params.get("id");
        assertNotNull("ID should be set by selectKey AFTER", generatedId);
        assertTrue(((Number) generatedId).intValue() > 0);

        // 查询验证
        Map<String, Object> q = new HashMap<String, Object>();
        q.put("id", generatedId);
        List<UserInfo> list = session.queryStatement("xmltest.KeyGenerationMapper.selectById", q);
        assertEquals(1, list.size());
        assertEquals("KeyGenAfter", list.get(0).getName());
    }

    // ========== 显式 ID（对照组） ==========

    /** 显式指定 ID 不涉及主键生成 */
    @Test
    public void testExplicitId_NoGeneration() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 57699);
        params.put("name", "KeyGenExplicit");
        params.put("age", 50);
        params.put("email", "explicit@test.com");

        session.executeStatement("xmltest.KeyGenerationMapper.insertWithExplicitId", params);

        Map<String, Object> q = new HashMap<String, Object>();
        q.put("id", 57699);
        List<UserInfo> list = session.queryStatement("xmltest.KeyGenerationMapper.selectById", q);
        assertEquals(1, list.size());
        assertEquals("KeyGenExplicit", list.get(0).getName());
    }
}
