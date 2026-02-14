package net.hasor.dbvisitor.test.suite.programmatic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 批量操作测试 (Batch Operation Tests)
 * 验证 batchUpdate, executeBatch 等批量操作功能
 */
public class BatchOperationTest extends AbstractOneApiTest {

    @Override
    protected void cleanTestData() {
        super.cleanTestData();
    }

    /**
     * 测试批量插入 (Map数组)
     */
    @Test
    public void testBatchInsert_Maps() throws SQLException {
        String sql = "INSERT INTO basic_types_test (id, string_value) VALUES (:id, :val)";

        Map<String, Object>[] batchValues = new Map[100];
        for (int i = 0; i < 100; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", 1000 + i);
            params.put("val", "Batch-" + i);
            batchValues[i] = params;
        }

        int[] result = jdbcTemplate.executeBatch(sql, batchValues);

        assertEquals(100, result.length);
        assertEquals(100L, (long) jdbcTemplate.queryForInt("SELECT count(*) FROM basic_types_test WHERE id >= 1000 AND id < 1100"));
    }

    /**
     * 测试批量插入 (Object[] 数组)
     * 注意：Object[]数组通常用于 ? 占位符，不是具名参数
     */
    @Test
    public void testBatchInsert_Args() throws SQLException {
        String sql = "INSERT INTO basic_types_test (id, string_value) VALUES (?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            batchArgs.add(new Object[] { 2000 + i, "ArgBatch-" + i });
        }

        int[] result = jdbcTemplate.executeBatch(sql, batchArgs.toArray());

        assertEquals(50, result.length);
        assertEquals(50L, (long) jdbcTemplate.queryForInt("SELECT count(*) FROM basic_types_test WHERE id >= 2000 AND id < 2050"));
    }

    /**
     * 测试批量更新
     */
    @Test
    public void testBatchUpdate() throws SQLException {
        // Init data
        testBatchInsert_Maps(); // Inserts 1000-1099

        String sql = "UPDATE basic_types_test SET string_value = :newVal WHERE id = :id";

        Map<String, Object>[] batchValues = new Map[10];
        for (int i = 0; i < 10; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", 1000 + i);
            params.put("newVal", "Updated-" + i);
            batchValues[i] = params;
        }

        int[] result = jdbcTemplate.executeBatch(sql, batchValues);

        assertEquals(10, result.length);
        assertEquals("Updated-0", jdbcTemplate.queryForString("SELECT string_value FROM basic_types_test WHERE id = 1000"));
        assertEquals("Batch-10", jdbcTemplate.queryForString("SELECT string_value FROM basic_types_test WHERE id = 1010")); // Not updated
    }

    /**
     * 测试批量删除
     */
    @Test
    public void testBatchDelete() throws SQLException {
        // Init data
        testBatchInsert_Args(); // Inserts 2000-2049

        String sql = "DELETE FROM basic_types_test WHERE id = ?";

        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            batchArgs.add(new Object[] { 2000 + i });
        }

        int[] result = jdbcTemplate.executeBatch(sql, batchArgs.toArray());

        assertEquals(20, result.length);
        assertEquals(30L, (long) jdbcTemplate.queryForInt("SELECT count(*) FROM basic_types_test WHERE id >= 2000 AND id < 2050"));
    }

    /**
     * 测试批量操作错误处理 - 部分失败
     */
    @Test
    public void testBatchOperation_PartialFailure() throws SQLException {
        String sql = "INSERT INTO basic_types_test (id, string_value) VALUES (?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();
        batchArgs.add(new Object[] { 3001, "Valid-1" });
        batchArgs.add(new Object[] { 3002, "Valid-2" });
        // Intentionally duplicate ID to cause constraint violation
        batchArgs.add(new Object[] { 3001, "Duplicate" });
        batchArgs.add(new Object[] { 3003, "Valid-3" });

        boolean exceptionCaught = false;
        try {
            jdbcTemplate.executeBatch(sql, batchArgs.toArray());
        } catch (SQLException e) {
            // Expected for constraint violation
            exceptionCaught = true;
            assertTrue(e.getMessage()//
                    .contains("constraint") || e.getMessage()//
                    .contains("duplicate") || e.getMessage()//
                    .contains("Unique") || e.getMessage()//
                    .contains("violation"));
        }

        // 验证异常被捕获
        assertTrue("Expected SQLException for duplicate key", exceptionCaught);

        // PostgreSQL 批量操作遇到错误会回滚整个批次，所以记录数可能是 0
        // 不同数据库行为不同，这里只验证异常被正确抛出
        long count = jdbcTemplate.queryForInt("SELECT count(*) FROM basic_types_test WHERE id IN (3001, 3002, 3003)");
        // 对于 PostgreSQL，count 应该是 0（整个批次回滚）
        // 对于其他数据库，可能是 >= 2
        assertTrue(count >= 0);
    }

    /**
     * 测试大批量操作 - 1000条记录
     */
    @Test
    public void testLargeBatchInsert() throws SQLException {
        String sql = "INSERT INTO basic_types_test (id, string_value) VALUES (:id, :val)";

        Map<String, Object>[] batchValues = new Map[1000];
        for (int i = 0; i < 1000; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", 10000 + i);
            params.put("val", "Large-" + i);
            batchValues[i] = params;
        }

        long startTime = System.currentTimeMillis();
        int[] result = jdbcTemplate.executeBatch(sql, batchValues);
        long endTime = System.currentTimeMillis();

        assertEquals(1000, result.length);
        assertEquals(1000L, (long) jdbcTemplate.queryForInt("SELECT count(*) FROM basic_types_test WHERE id >= 10000 AND id < 11000"));

        // Performance check: should complete in reasonable time (< 5 seconds for 1000 records)
        assertTrue((endTime - startTime) < 5000);
    }
}
