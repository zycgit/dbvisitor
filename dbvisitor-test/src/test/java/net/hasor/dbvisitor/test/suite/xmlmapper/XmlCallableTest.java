package net.hasor.dbvisitor.test.suite.xmlmapper;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import net.hasor.dbvisitor.transaction.Propagation;
import net.hasor.dbvisitor.transaction.TransactionManager;
import net.hasor.dbvisitor.transaction.TransactionStatus;
import net.hasor.dbvisitor.transaction.support.TransactionHelper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * XML Mapper 存储过程调用测试 + bindOut 属性测试
 * <p>覆盖：
 * <ul>
 *   <li>纯 IN 参数存储过程</li>
 *   <li>IN + INOUT 参数存储过程（单个和多个 INOUT）</li>
 *   <li>OUT 参数函数（PG 限制改为 INOUT 过程，单个和多个输出参数）</li>
 *   <li>refcursor 结果集返回（单个和多个 cursor）</li>
 *   <li>bindOut 属性筛选输出参数</li>
 *   <li>不设置 bindOut 返回完整多结果集 Map</li>
 * </ul>
 * <p>注意：存储过程测试需要 "procedure" feature，仅在支持存储过程的数据库上运行。
 * 当前仅为 PostgreSQL 提供了存储过程。
 * <p>PG 限制：<b>PROCEDURE 只支持 INOUT 参数，不支持 OUT 参数</b>。因此"输出参数"测试
 * 使用 INOUT + mode=INOUT 来模拟 OUT 行为，这是 PG 下存储过程的标准做法。
 */
public class XmlCallableTest extends AbstractOneApiTest {

    private Session session;

    @Before
    public void setUp() throws Exception {
        super.setup();
        requiresFeature("procedure");

        // PG 存储过程 DDL 包含 $$ 块，不能用 loadSplitSQL（它按字符分隔），需逐条 execute
        // 1. proc_insert_user: 纯 IN 参数
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_insert_user");
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE proc_insert_user(IN p_id INT, IN p_name VARCHAR) LANGUAGE plpgsql AS $$ BEGIN INSERT INTO user_info (id, name, age, create_time) VALUES (p_id, p_name, 0, CURRENT_TIMESTAMP); END; $$");

        // 2. proc_double_value: IN + INOUT（翻倍）
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_double_value");
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE proc_double_value(IN p_input INT, INOUT p_result INT) LANGUAGE plpgsql AS $$ BEGIN p_result := p_input * 2; END; $$");

        // 3. proc_multi_inout: IN + 多个 INOUT（拼接 + 长度）
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_multi_inout");
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE proc_multi_inout(IN p_prefix VARCHAR, IN p_suffix VARCHAR, INOUT p_concat VARCHAR, INOUT p_length INT) LANGUAGE plpgsql AS $$ BEGIN p_concat := p_prefix || '-' || p_suffix; p_length := LENGTH(p_concat); END; $$");

        // 4. proc_user_count: 单个输出参数（PG PROCEDURE 只支持 INOUT，不支持 OUT）
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_user_count");
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE proc_user_count(INOUT p_count INT) LANGUAGE plpgsql AS $$ BEGIN SELECT COUNT(*) INTO p_count FROM user_info; END; $$");

        // 5. proc_user_stats: 多个输出参数（PG PROCEDURE 只支持 INOUT，不支持 OUT）
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_user_stats");
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE proc_user_stats(INOUT p_count INT, INOUT p_max_id INT, INOUT p_min_name VARCHAR) LANGUAGE plpgsql AS $$ BEGIN SELECT COUNT(*), MAX(id) INTO p_count, p_max_id FROM user_info; SELECT name INTO p_min_name FROM user_info ORDER BY id LIMIT 1; END; $$");

        // 6. proc_query_users: 单个 refcursor
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_query_users");
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE proc_query_users(IN p_name VARCHAR, INOUT p_out VARCHAR, INOUT res1 refcursor) LANGUAGE plpgsql AS $$ BEGIN p_out := 'found:' || p_name; OPEN res1 FOR SELECT * FROM user_info WHERE name = p_name; END; $$");

        // 7. proc_query_users_multi: 多个 refcursor
        jdbcTemplate.execute("DROP PROCEDURE IF EXISTS proc_query_users_multi");
        jdbcTemplate.execute("CREATE OR REPLACE PROCEDURE proc_query_users_multi(IN p_name VARCHAR, INOUT p_out_msg VARCHAR, INOUT res_matched refcursor, INOUT res_unmatched refcursor) LANGUAGE plpgsql AS $$ BEGIN p_out_msg := 'query:' || p_name; OPEN res_matched FOR SELECT * FROM user_info WHERE name = p_name; OPEN res_unmatched FOR SELECT * FROM user_info WHERE name <> p_name; END; $$");

        Configuration config = new Configuration();
        config.loadMapper("/mapper/XmlCallableMapper.xml");
        this.session = config.newSession(dataSource);
    }

    private long countById(int id) throws SQLException {
        return lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, id).queryForCount();
    }

    // ==================== IN 参数 ====================

    /**
     * 纯 IN 参数存储过程 — 调用 proc_insert_user 插入用户
     * <p>XML: statementType="CALLABLE"，无 bindOut
     * <p>验证：调用无返回值过程，数据被正确插入到 user_info 表
     */
    @Test
    public void testCallable_InOnly_InsertUser() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", 70001);
        params.put("name", "ProcUser1");

        session.executeStatement("XmlCallableMapper.callInsertUser", params);

        assertEquals(1, countById(70001));

        UserInfo user = lambdaTemplate.query(UserInfo.class).eq(UserInfo::getId, 70001).queryForObject();
        assertNotNull(user);
        assertEquals("ProcUser1", user.getName());
    }

    // ==================== INOUT 参数 ====================

    /**
     * IN + INOUT 参数 — 调用 proc_double_value，输入值翻倍后通过 INOUT 返回
     * <p>XML: bindOut="p_result" 仅输出 p_result
     * <p>验证：输入 7 → 输出 14
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testCallable_Inout_DoubleValue() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("p_input", 7);
        params.put("p_result", 0);

        Object result = session.executeStatement("XmlCallableMapper.callDoubleValue", params);
        assertNotNull(result);
        assertTrue("结果应为 Map", result instanceof Map);

        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertTrue("bindOut 应包含 p_result", resultMap.containsKey("p_result"));
        assertEquals(14, ((Number) resultMap.get("p_result")).intValue());
    }

    /**
     * IN + 多个 INOUT 参数 — 调用 proc_multi_inout，拼接字符串 + 计算长度
     * <p>XML: bindOut="p_concat,p_length" 输出两个参数
     * <p>验证：输入 "Hello","World" → p_concat="Hello-World", p_length=11
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testCallable_MultiInout_ConcatAndLength() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("p_prefix", "Hello");
        params.put("p_suffix", "World");
        params.put("p_concat", "");
        params.put("p_length", 0);

        Object result = session.executeStatement("XmlCallableMapper.callMultiInout", params);
        assertNotNull(result);
        assertTrue("结果应为 Map", result instanceof Map);

        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertEquals("Hello-World", resultMap.get("p_concat"));
        assertEquals(11, ((Number) resultMap.get("p_length")).intValue());
        // bindOut 只输出指定的两个参数
        assertEquals(2, resultMap.size());
    }

    // ==================== OUT 参数（函数） ====================

    /**
     * 单个输出参数 — proc_user_count 返回用户总数（PG PROCEDURE 用 INOUT 替代 OUT）
     * <p>XML: <execute> + statementType="CALLABLE" + bindOut="p_count"
     * <p>验证：先插入 3 条数据，过程返回 count=3
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testCallable_Out_UserCount() throws Exception {
        // 准备数据
        for (int i = 1; i <= 3; i++) {
            jdbcTemplate.executeUpdate(//
                    "INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",//
                    new Object[]{ 70010 + i, "CountUser" + i, 20 + i });
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("p_count", 0);

        Object result = session.executeStatement("XmlCallableMapper.callUserCount", params);
        assertNotNull(result);
        assertTrue("结果应为 Map", result instanceof Map);

        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertTrue("bindOut 应包含 p_count", resultMap.containsKey("p_count"));
        assertEquals(3, ((Number) resultMap.get("p_count")).intValue());
    }

    /**
     * 多个输出参数 — proc_user_stats 返回 count, max_id, min_name（PG PROCEDURE 用 INOUT 替代 OUT）
     * <p>XML: <execute> + bindOut="p_count,p_max_id,p_min_name"
     * <p>验证：插入 3 条用户后获取聚合统计
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testCallable_MultiOut_UserStats() throws Exception {
        // 准备数据
        jdbcTemplate.executeUpdate(
                "INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",//
                new Object[]{ 70021, "Alice", 25 });
        jdbcTemplate.executeUpdate(
                "INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",//
                new Object[]{ 70022, "Bob", 30 });
        jdbcTemplate.executeUpdate(
                "INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",//
                new Object[]{ 70023, "Charlie", 35 });

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("p_count", 0);
        params.put("p_max_id", 0);
        params.put("p_min_name", "");

        Object result = session.executeStatement("XmlCallableMapper.callUserStats", params);
        assertNotNull(result);
        assertTrue("结果应为 Map", result instanceof Map);

        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertEquals("bindOut 应只包含 3 个参数", 3, resultMap.size());
        assertEquals(3, ((Number) resultMap.get("p_count")).intValue());
        assertEquals(70023, ((Number) resultMap.get("p_max_id")).intValue());
        assertEquals("Alice", resultMap.get("p_min_name"));
    }

    // ==================== refcursor 结果集 ====================

    /**
     * 单个 refcursor — proc_query_users 返回匹配用户的结果集 + INOUT 消息
     * <p>XML: bindOut="p_out,res1"
     * <p>注意：PG refcursor 只能在事务内使用，需要编程式事务管理
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testCallable_Cursor_SingleResultSet() throws Exception {
        // 准备数据
        jdbcTemplate.executeUpdate(
                "INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
                new Object[]{ 70031, "CursorUser", 25 });
        jdbcTemplate.executeUpdate(
                "INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
                new Object[]{ 70032, "OtherUser", 30 });

        // PG refcursor 必须在事务内使用
        TransactionManager tm = TransactionHelper.txManager(dataSource);
        TransactionStatus tx = tm.begin(Propagation.REQUIRED);
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("p_name", "CursorUser");
            params.put("p_out", "");

            Object result = session.executeStatement("XmlCallableMapper.callQueryUsers", params);
            assertNotNull(result);
            assertTrue("结果应为 Map", result instanceof Map);

            Map<String, Object> resultMap = (Map<String, Object>) result;
            assertEquals("found:CursorUser", resultMap.get("p_out"));

            assertTrue("res1 应为 List", resultMap.get("res1") instanceof List);
            List<?> res1 = (List<?>) resultMap.get("res1");
            assertEquals("匹配用户应 1 条", 1, res1.size());

            tm.commit(tx);
        } catch (Exception e) {
            tm.rollBack(tx);
            throw e;
        }
    }

    /**
     * 多个 refcursor — proc_query_users_multi 返回匹配 + 不匹配两个结果集
     * <p>XML: bindOut="p_out_msg,res_matched,res_unmatched"
     * <p>验证：bindOut 筛选出 3 个输出参数，两个 cursor 各有正确数量的记录
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testCallable_Cursor_MultipleResultSets() throws Exception {
        // 准备 3 条数据
        jdbcTemplate.executeUpdate(
                "INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
                new Object[]{ 70041, "MatchMe", 25 });
        jdbcTemplate.executeUpdate(
                "INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
                new Object[]{ 70042, "NotMatch1", 30 });
        jdbcTemplate.executeUpdate(
                "INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
                new Object[]{ 70043, "NotMatch2", 35 });

        TransactionManager tm = TransactionHelper.txManager(dataSource);
        TransactionStatus tx = tm.begin(Propagation.REQUIRED);
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("p_name", "MatchMe");
            params.put("p_out_msg", "");

            Object result = session.executeStatement("XmlCallableMapper.callQueryUsersMulti", params);
            assertNotNull(result);
            assertTrue("结果应为 Map", result instanceof Map);

            Map<String, Object> resultMap = (Map<String, Object>) result;
            assertEquals("bindOut 应只包含 3 个参数", 3, resultMap.size());
            assertEquals("query:MatchMe", resultMap.get("p_out_msg"));

            assertTrue("res_matched 应为 List", resultMap.get("res_matched") instanceof List);
            List<?> matched = (List<?>) resultMap.get("res_matched");
            assertEquals("匹配用户应 1 条", 1, matched.size());

            assertTrue("res_unmatched 应为 List", resultMap.get("res_unmatched") instanceof List);
            List<?> unmatched = (List<?>) resultMap.get("res_unmatched");
            assertEquals("不匹配用户应 2 条", 2, unmatched.size());

            tm.commit(tx);
        } catch (Exception e) {
            tm.rollBack(tx);
            throw e;
        }
    }

    // ==================== bindOut 行为对比 ====================

    /**
     * 不设置 bindOut — 返回完整多结果集 Map（包含 #result-set-N、#update-count-N 等内部 key）
     * <p>XML: 与 callQueryUsers 相同的 SQL，但不设置 bindOut
     * <p>验证：返回的 Map 包含框架自动编号的结果集（res1 等）和 INOUT 参数值
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testCallable_NoBind_ReturnsFullResultMap() throws Exception {
        jdbcTemplate.executeUpdate(//==
                "INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",//
                new Object[]{ 70051, "FullMapUser", 25 });

        TransactionManager tm = TransactionHelper.txManager(dataSource);
        TransactionStatus tx = tm.begin(Propagation.REQUIRED);
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("p_name", "FullMapUser");
            params.put("p_out", "");

            Object result = session.executeStatement("XmlCallableMapper.callQueryUsersNoBind", params);
            assertNotNull(result);
            assertTrue("结果应为 Map", result instanceof Map);

            Map<String, Object> resultMap = (Map<String, Object>) result;
            // 不设置 bindOut 时返回完整结果，包含 p_out、res1 等以及内部的结果集 key
            assertTrue("完整结果应包含多个 key（至少 > 1）", resultMap.size() > 1);

            tm.commit(tx);
        } catch (Exception e) {
            tm.rollBack(tx);
            throw e;
        }
    }

    /**
     * bindOut 筛选 vs 无 bindOut — 对比验证 bindOut 只返回指定参数
     * <p>使用 callDoubleValue（有 bindOut）验证返回的 Map 只包含 p_result 一个 key
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testCallable_BindOut_FiltersOutput() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("p_input", 5);
        params.put("p_result", 0);

        Object result = session.executeStatement("XmlCallableMapper.callDoubleValue", params);
        Map<String, Object> resultMap = (Map<String, Object>) result;

        // bindOut="p_result" → 只有一个 key
        assertEquals("bindOut 应只保留 p_result", 1, resultMap.size());
        assertTrue(resultMap.containsKey("p_result"));
        assertEquals(10, ((Number) resultMap.get("p_result")).intValue());
    }
}
