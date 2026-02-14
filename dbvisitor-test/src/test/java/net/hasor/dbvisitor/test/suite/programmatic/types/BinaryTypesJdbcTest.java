package net.hasor.dbvisitor.test.suite.programmatic.types;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 二进制类型测试 - Programmatic API (JdbcTemplate)
 * 合并自 BinaryTypesExplicitTest + BinaryTypeHandlerTest
 * 测试范围：
 * 1. BINARY 固定长度二进制
 * 2. VARBINARY 可变长度二进制
 * 3. LONGVARBINARY/BLOB 大对象处理
 * 4. InputStream 映射
 * 5. 内容完整性验证
 * 6. null 值处理
 * 数据库初始化：通过 /sql/{dialect}/init.sql 脚本自动创建 binary_types_explicit_test 表
 */
public class BinaryTypesJdbcTest extends AbstractOneApiTest {

    @Override
    protected void cleanTestData() {
        try {
            jdbcTemplate.executeUpdate("DELETE FROM binary_types_explicit_test");
        } catch (Exception e) {
            // Ignore
        }
        super.cleanTestData();
    }

    /**
     * 测试 BINARY 类型 - 固定长度二进制
     */
    @Test
    public void testBinary_FixedLength() throws SQLException {
        byte[] data = new byte[16];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }

        String insertSql = "INSERT INTO binary_types_explicit_test (id, binary_value) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 1, data });

        String selectSql = "SELECT binary_value FROM binary_types_explicit_test WHERE id = ?";
        byte[] loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 1 }, byte[].class);

        assertNotNull(loaded);
        assertArrayEquals(data, loaded);
    }

    /**
     * 测试 VARBINARY 类型 - 可变长度二进制（小数据）
     */
    @Test
    public void testVarbinary_SmallData() throws SQLException {
        byte[] smallData = new byte[] { 1, 2, 3, 4, 5 };

        String insertSql = "INSERT INTO binary_types_explicit_test (id, varbinary_value) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 2, smallData });

        String selectSql = "SELECT varbinary_value FROM binary_types_explicit_test WHERE id = ?";
        byte[] loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 2 }, byte[].class);

        assertNotNull(loaded);
        assertArrayEquals(smallData, loaded);
    }

    /**
     * 测试 VARBINARY 类型 - 可变长度二进制（1KB）
     */
    @Test
    public void testVarbinary_MediumData() throws SQLException {
        byte[] mediumData = new byte[1024];
        Arrays.fill(mediumData, (byte) 0xAB);

        String insertSql = "INSERT INTO binary_types_explicit_test (id, varbinary_value) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 3, mediumData });

        String selectSql = "SELECT varbinary_value FROM binary_types_explicit_test WHERE id = ?";
        byte[] loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 3 }, byte[].class);

        assertNotNull(loaded);
        assertEquals(1024, loaded.length);
        assertArrayEquals(mediumData, loaded);
    }

    /**
     * 测试 BLOB 类型 - 大二进制对象（100KB）
     */
    @Test
    public void testBlob_LargeData() throws SQLException {
        byte[] largeData = new byte[100 * 1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }

        String insertSql = "INSERT INTO binary_types_explicit_test (id, blob_value) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 4, largeData });

        String selectSql = "SELECT blob_value FROM binary_types_explicit_test WHERE id = ?";
        byte[] loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 4 }, byte[].class);

        assertNotNull(loaded);
        assertEquals(100 * 1024, loaded.length);

        // 验证内容完整性（抽样检查）
        for (int i = 0; i < 1000; i += 100) {
            assertEquals(largeData[i], loaded[i]);
        }
    }

    /**
     * 测试 InputStream 映射到 BLOB
     */
    @Test
    public void testBlob_InputStream() throws SQLException {
        byte[] data = new byte[] { 10, 20, 30, 40, 50 };
        InputStream inputStream = new ByteArrayInputStream(data);

        String insertSql = "INSERT INTO binary_types_explicit_test (id, blob_value) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 5, inputStream });

        String selectSql = "SELECT blob_value FROM binary_types_explicit_test WHERE id = ?";
        byte[] loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 5 }, byte[].class);

        assertNotNull(loaded);
        assertArrayEquals(data, loaded);
    }

    /**
     * 测试二进制内容完整性 - 特殊模式验证
     */
    @Test
    public void testBinaryIntegrity_PatternVerification() throws SQLException {
        byte[] pattern = new byte[5000];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = (byte) (i * 7 % 256);
        }

        String insertSql = "INSERT INTO binary_types_explicit_test (id, blob_value) VALUES (?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 6, pattern });

        String selectSql = "SELECT blob_value FROM binary_types_explicit_test WHERE id = ?";
        byte[] loaded = jdbcTemplate.queryForObject(selectSql, new Object[] { 6 }, byte[].class);

        assertNotNull(loaded);
        assertEquals(5000, loaded.length);
        assertArrayEquals(pattern, loaded);
    }

    /**
     * 测试 null 值处理
     */
    @Test
    public void testNullValues_AllBinaryTypes() throws SQLException {
        String insertSql = "INSERT INTO binary_types_explicit_test (id, binary_value, varbinary_value, longvarbinary_value, blob_value) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.executeUpdate(insertSql, new Object[] { 7, null, null, null, null });

        String selectSql = "SELECT binary_value, varbinary_value, longvarbinary_value, blob_value FROM binary_types_explicit_test WHERE id = ?";
        Map<String, Object> result = jdbcTemplate.queryForMap(selectSql, new Object[] { 7 });

        assertNotNull(result);
        assertNull(result.get("binary_value"));
        assertNull(result.get("varbinary_value"));
        assertNull(result.get("longvarbinary_value"));
        assertNull(result.get("blob_value"));
    }
}
