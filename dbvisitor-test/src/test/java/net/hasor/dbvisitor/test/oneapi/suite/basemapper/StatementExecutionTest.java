package net.hasor.dbvisitor.test.oneapi.suite.basemapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.UserInfo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * BaseMapper Statement Execution Test
 * 验证 BaseMapper 的 executeStatement / queryStatement 方法通过 XML mapper 执行 SQL
 */
public class StatementExecutionTest extends AbstractOneApiTest {

    private static final String NS = "oneapi.StatementTestMapper";

    private Session newSessionWithMapper() throws Exception {
        Configuration configuration = new Configuration();
        configuration.loadMapper("/oneapi/mapper/StatementTestMapper.xml");
        return configuration.newSession(dataSource);
    }

    // ========== executeStatement 测试 ==========

    /**
     * 测试 executeStatement - 执行插入语句（含主键）
     */
    @Test
    public void testExecuteStatement_Insert() throws Exception {
        Session session = newSessionWithMapper();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        Map<String, Object> params = new HashMap<>();
        params.put("id", 48001);
        params.put("name", "StmtInsert");
        params.put("age", 25);
        params.put("email", "stmt@test.com");

        Object result = mapper.executeStatement(NS + ".insertUserWithId", params);
        assertNotNull(result);
        assertEquals(1, ((Number) result).intValue());

        // 验证数据已插入
        UserInfo loaded = mapper.selectById(48001);
        assertNotNull(loaded);
        assertEquals("StmtInsert", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
    }

    /**
     * 测试 executeStatement - 执行更新语句
     */
    @Test
    public void testExecuteStatement_Update() throws Exception {
        Session session = newSessionWithMapper();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 先插入数据
        Map<String, Object> insertParams = new HashMap<>();
        insertParams.put("id", 48002);
        insertParams.put("name", "StmtUpdate");
        insertParams.put("age", 30);
        insertParams.put("email", "before@test.com");
        mapper.executeStatement(NS + ".insertUserWithId", insertParams);

        // 执行更新
        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("id", 48002);
        updateParams.put("email", "after@test.com");

        Object result = mapper.executeStatement(NS + ".updateUserEmail", updateParams);
        assertNotNull(result);
        assertEquals(1, ((Number) result).intValue());

        // 验证更新结果
        UserInfo loaded = mapper.selectById(48002);
        assertNotNull(loaded);
        assertEquals("after@test.com", loaded.getEmail());
    }

    /**
     * 测试 executeStatement - 执行删除语句
     */
    @Test
    public void testExecuteStatement_Delete() throws Exception {
        Session session = newSessionWithMapper();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 先插入数据
        Map<String, Object> insertParams = new HashMap<>();
        insertParams.put("id", 48003);
        insertParams.put("name", "StmtDelete");
        insertParams.put("age", 35);
        insertParams.put("email", "delete@test.com");
        mapper.executeStatement(NS + ".insertUserWithId", insertParams);

        // 执行删除
        Map<String, Object> deleteParams = new HashMap<>();
        deleteParams.put("id", 48003);

        Object result = mapper.executeStatement(NS + ".deleteUserById", deleteParams);
        assertNotNull(result);
        assertEquals(1, ((Number) result).intValue());

        // 验证已删除
        UserInfo loaded = mapper.selectById(48003);
        assertNull(loaded);
    }

    /**
     * 测试 executeStatement - 更新不存在的记录返回 0
     */
    @Test
    public void testExecuteStatement_UpdateNonExistent() throws Exception {
        Session session = newSessionWithMapper();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        Map<String, Object> params = new HashMap<>();
        params.put("id", 49999);
        params.put("email", "nope@test.com");

        Object result = mapper.executeStatement(NS + ".updateUserEmail", params);
        assertNotNull(result);
        assertEquals(0, ((Number) result).intValue());
    }

    /**
     * 测试 executeStatement - 删除不存在的记录返回 0
     */
    @Test
    public void testExecuteStatement_DeleteNonExistent() throws Exception {
        Session session = newSessionWithMapper();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        Map<String, Object> params = new HashMap<>();
        params.put("id", 49998);

        Object result = mapper.executeStatement(NS + ".deleteUserById", params);
        assertNotNull(result);
        assertEquals(0, ((Number) result).intValue());
    }

    // ========== queryStatement 测试 ==========

    /**
     * 测试 queryStatement - 按 ID 查询单条（resultMap）
     */
    @Test
    public void testQueryStatement_ById() throws Exception {
        Session session = newSessionWithMapper();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入数据
        Map<String, Object> insertParams = new HashMap<>();
        insertParams.put("id", 48010);
        insertParams.put("name", "QueryById");
        insertParams.put("age", 28);
        insertParams.put("email", "querybyid@test.com");
        mapper.executeStatement(NS + ".insertUserWithId", insertParams);

        // 查询
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("id", 48010);

        List<UserInfo> results = mapper.queryStatement(NS + ".queryUserById", queryParams);
        assertNotNull(results);
        assertEquals(1, results.size());

        UserInfo user = results.get(0);
        assertEquals("QueryById", user.getName());
        assertEquals(Integer.valueOf(28), user.getAge());
        assertEquals("querybyid@test.com", user.getEmail());
    }

    /**
     * 测试 queryStatement - 查询不存在的记录返回空列表
     */
    @Test
    public void testQueryStatement_NotFound() throws Exception {
        Session session = newSessionWithMapper();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        Map<String, Object> params = new HashMap<>();
        params.put("id", 49997);

        List<UserInfo> results = mapper.queryStatement(NS + ".queryUserById", params);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * 测试 queryStatement - 按名称模糊查询多条
     */
    @Test
    public void testQueryStatement_ByName() throws Exception {
        Session session = newSessionWithMapper();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入多条数据
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", 48020 + i);
            params.put("name", "QName" + i);
            params.put("age", 20 + i);
            params.put("email", "qname" + i + "@test.com");
            mapper.executeStatement(NS + ".insertUserWithId", params);
        }

        // 模糊查询
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("name", "QName%");

        List<UserInfo> results = mapper.queryStatement(NS + ".queryUsersByName", queryParams);
        assertNotNull(results);
        assertEquals(5, results.size());
    }

    /**
     * 测试 queryStatement - 查询全部（resultType）
     */
    @Test
    public void testQueryStatement_AllUsers() throws Exception {
        Session session = newSessionWithMapper();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入 3 条数据
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", 48030 + i);
            params.put("name", "QAll" + i);
            params.put("age", 30 + i);
            params.put("email", "qall" + i + "@test.com");
            mapper.executeStatement(NS + ".insertUserWithId", params);
        }

        // 查询全部
        List<UserInfo> results = mapper.queryStatement(NS + ".queryAllUsers", null);
        assertNotNull(results);
        assertTrue(results.size() >= 3);
    }

    // ========== queryStatement 带分页测试 ==========

    /**
     * 测试 queryStatement 带分页 - 第一页
     */
    @Test
    public void testQueryStatement_WithPage() throws Exception {
        Session session = newSessionWithMapper();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入 10 条数据
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", 48040 + i);
            params.put("name", "QPage" + i);
            params.put("age", 20 + i);
            params.put("email", "qpage" + i + "@test.com");
            mapper.executeStatement(NS + ".insertUserWithId", params);
        }

        // 分页查询 - 每页 3 条
        PageObject page = new PageObject();
        page.setPageSize(3);
        page.setCurrentPage(0);

        List<UserInfo> results = mapper.queryStatement(NS + ".queryAllUsers", null, page);
        assertNotNull(results);
        assertEquals(3, results.size());
    }

    /**
     * 测试 queryStatement 带分页 - 第二页
     */
    @Test
    public void testQueryStatement_WithPageSecond() throws Exception {
        Session session = newSessionWithMapper();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入 10 条数据
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", 48050 + i);
            params.put("name", "QPage2_" + i);
            params.put("age", 20 + i);
            params.put("email", "qpage2_" + i + "@test.com");
            mapper.executeStatement(NS + ".insertUserWithId", params);
        }

        // 分页查询 - 每页 4 条，第二页
        PageObject page = new PageObject();
        page.setPageSize(4);
        page.setCurrentPage(1);

        List<UserInfo> results = mapper.queryStatement(NS + ".queryAllUsers", null, page);
        assertNotNull(results);
        assertEquals(4, results.size());
    }

    /**
     * 测试 queryStatement 带分页 - 模糊查询分页
     */
    @Test
    public void testQueryStatement_ByNameWithPage() throws Exception {
        Session session = newSessionWithMapper();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入 8 条数据
        for (int i = 1; i <= 8; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", 48060 + i);
            params.put("name", "QPaged" + i);
            params.put("age", 20 + i);
            params.put("email", "qpaged" + i + "@test.com");
            mapper.executeStatement(NS + ".insertUserWithId", params);
        }

        // 分页模糊查询 - 每页 3 条
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("name", "QPaged%");

        PageObject page = new PageObject();
        page.setPageSize(3);
        page.setCurrentPage(0);

        List<UserInfo> results = mapper.queryStatement(NS + ".queryUsersByName", queryParams, page);
        assertNotNull(results);
        assertEquals(3, results.size());
    }

    // ========== 异常场景测试 ==========

    /**
     * 测试 executeStatement - 引用不存在的 statement ID 抛出异常
     */
    @Test
    public void testExecuteStatement_InvalidStatementId() throws Exception {
        Session session = newSessionWithMapper();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        try {
            mapper.executeStatement(NS + ".nonExistentStatement", new HashMap<>());
            fail("应抛出异常：引用不存在的 statement ID");
        } catch (Exception e) {
            // 期望抛出异常
            assertNotNull(e.getMessage());
        }
    }

    /**
     * 测试 queryStatement - 引用不存在的 statement ID 抛出异常
     */
    @Test
    public void testQueryStatement_InvalidStatementId() throws Exception {
        Session session = newSessionWithMapper();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        try {
            mapper.queryStatement(NS + ".nonExistentStatement", new HashMap<>());
            fail("应抛出异常：引用不存在的 statement ID");
        } catch (Exception e) {
            // 期望抛出异常
            assertNotNull(e.getMessage());
        }
    }

    /**
     * 测试 executeStatement - 批量删除（deleteUsersByName）
     */
    @Test
    public void testExecuteStatement_BatchDelete() throws Exception {
        Session session = newSessionWithMapper();
        BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);

        // 插入 5 条同名前缀数据
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", 48070 + i);
            params.put("name", "BatchDel" + i);
            params.put("age", 20 + i);
            params.put("email", "batchdel" + i + "@test.com");
            mapper.executeStatement(NS + ".insertUserWithId", params);
        }

        // 批量删除
        Map<String, Object> deleteParams = new HashMap<>();
        deleteParams.put("name", "BatchDel%");

        Object result = mapper.executeStatement(NS + ".deleteUsersByName", deleteParams);
        assertNotNull(result);
        assertEquals(5, ((Number) result).intValue());

        // 验证全部已删除
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("name", "BatchDel%");
        List<UserInfo> remaining = mapper.queryStatement(NS + ".queryUsersByName", queryParams);
        assertTrue(remaining.isEmpty());
    }
}
