package net.hasor.dbvisitor.test.suite.fluent.types;

import java.sql.SQLException;
import java.util.Arrays;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.types.BinaryTypesExplicitModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 二进制类型测试 - Fluent API (LambdaTemplate)
 * 对应 programmatic/types/BinaryTypesJdbcTest，使用 LambdaTemplate + BinaryTypesExplicitModel 映射
 * 测试范围：
 * 1. BINARY 固定长度二进制
 * 2. VARBINARY 可变长度二进制（小数据、中数据）
 * 3. LONGVARBINARY/BLOB 大对象处理
 * 4. 二进制内容完整性验证
 * 5. null 值处理
 * 6. 更新操作
 */
public class BinaryTypesFluentTest extends AbstractOneApiTest {

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

        BinaryTypesExplicitModel model = new BinaryTypesExplicitModel();
        model.setId(1);
        model.setBinaryValue(data);

        int rows = lambdaTemplate.insert(BinaryTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();
        assertEquals(1, rows);

        BinaryTypesExplicitModel loaded = lambdaTemplate.query(BinaryTypesExplicitModel.class)//
                .eq(BinaryTypesExplicitModel::getId, 1)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getBinaryValue());
        assertArrayEquals(data, loaded.getBinaryValue());
    }

    /**
     * 测试 VARBINARY 类型 - 可变长度二进制（小数据）
     */
    @Test
    public void testVarbinary_SmallData() throws SQLException {
        byte[] smallData = new byte[] { 1, 2, 3, 4, 5 };

        BinaryTypesExplicitModel model = new BinaryTypesExplicitModel();
        model.setId(2);
        model.setVarbinaryValue(smallData);

        int rows = lambdaTemplate.insert(BinaryTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();
        assertEquals(1, rows);

        BinaryTypesExplicitModel loaded = lambdaTemplate.query(BinaryTypesExplicitModel.class)//
                .eq(BinaryTypesExplicitModel::getId, 2)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getVarbinaryValue());
        assertArrayEquals(smallData, loaded.getVarbinaryValue());
    }

    /**
     * 测试 VARBINARY 类型 - 可变长度二进制（1KB）
     */
    @Test
    public void testVarbinary_MediumData() throws SQLException {
        byte[] mediumData = new byte[1024];
        Arrays.fill(mediumData, (byte) 0xAB);

        BinaryTypesExplicitModel model = new BinaryTypesExplicitModel();
        model.setId(3);
        model.setVarbinaryValue(mediumData);

        int rows = lambdaTemplate.insert(BinaryTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();
        assertEquals(1, rows);

        BinaryTypesExplicitModel loaded = lambdaTemplate.query(BinaryTypesExplicitModel.class)//
                .eq(BinaryTypesExplicitModel::getId, 3)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getVarbinaryValue());
        assertEquals(1024, loaded.getVarbinaryValue().length);
        assertArrayEquals(mediumData, loaded.getVarbinaryValue());
    }

    /**
     * 测试 BLOB 类型 - 大二进制对象（100KB）
     * 注意：PostgreSQL BLOB 需要 autocommit=false，这里使用 varbinaryValue 字段（BYTEA）代替
     */
    @Test
    public void testBlob_LargeData() throws SQLException {
        byte[] largeData = new byte[100 * 1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }

        BinaryTypesExplicitModel model = new BinaryTypesExplicitModel();
        model.setId(4);
        model.setVarbinaryValue(largeData);

        int rows = lambdaTemplate.insert(BinaryTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();
        assertEquals(1, rows);

        BinaryTypesExplicitModel loaded = lambdaTemplate.query(BinaryTypesExplicitModel.class)//
                .eq(BinaryTypesExplicitModel::getId, 4)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getVarbinaryValue());
        assertEquals(100 * 1024, loaded.getVarbinaryValue().length);

        // 验证内容完整性（抽样检查）
        for (int i = 0; i < 1000; i += 100) {
            assertEquals(largeData[i], loaded.getVarbinaryValue()[i]);
        }
    }

    /**
     * 测试二进制内容完整性 - 特殊模式验证
     * 使用 varbinaryValue（BYTEA）避免 PostgreSQL BLOB autocommit 限制
     */
    @Test
    public void testBinaryIntegrity_PatternVerification() throws SQLException {
        byte[] pattern = new byte[5000];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = (byte) (i * 7 % 256);
        }

        BinaryTypesExplicitModel model = new BinaryTypesExplicitModel();
        model.setId(5);
        model.setVarbinaryValue(pattern);

        int rows = lambdaTemplate.insert(BinaryTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();
        assertEquals(1, rows);

        BinaryTypesExplicitModel loaded = lambdaTemplate.query(BinaryTypesExplicitModel.class)//
                .eq(BinaryTypesExplicitModel::getId, 5)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getVarbinaryValue());
        assertEquals(5000, loaded.getVarbinaryValue().length);
        assertArrayEquals(pattern, loaded.getVarbinaryValue());
    }

    /**
     * 测试 null 值处理 - 所有二进制字段
     */
    @Test
    public void testNullValues_AllBinaryTypes() throws SQLException {
        BinaryTypesExplicitModel model = new BinaryTypesExplicitModel();
        model.setId(6);
        model.setBinaryValue(null);
        model.setVarbinaryValue(null);
        model.setLongvarbinaryValue(null);
        model.setBlobValue(null);

        int rows = lambdaTemplate.insert(BinaryTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();
        assertEquals(1, rows);

        BinaryTypesExplicitModel loaded = lambdaTemplate.query(BinaryTypesExplicitModel.class)//
                .eq(BinaryTypesExplicitModel::getId, 6)//
                .queryForObject();

        assertNotNull(loaded);
        assertNull(loaded.getBinaryValue());
        assertNull(loaded.getVarbinaryValue());
        assertNull(loaded.getLongvarbinaryValue());
        assertNull(loaded.getBlobValue());
    }

    /**
     * 测试更新二进制字段
     */
    @Test
    public void testUpdate_BinaryField() throws SQLException {
        // 初始插入
        byte[] originalData = new byte[] { 1, 2, 3 };
        BinaryTypesExplicitModel model = new BinaryTypesExplicitModel();
        model.setId(7);
        model.setVarbinaryValue(originalData);

        lambdaTemplate.insert(BinaryTypesExplicitModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        // 更新
        byte[] updatedData = new byte[] { 10, 20, 30, 40, 50 };
        int updated = lambdaTemplate.update(BinaryTypesExplicitModel.class)//
                .eq(BinaryTypesExplicitModel::getId, 7)//
                .updateTo(BinaryTypesExplicitModel::getVarbinaryValue, updatedData)//
                .doUpdate();
        assertEquals(1, updated);

        // 验证
        BinaryTypesExplicitModel loaded = lambdaTemplate.query(BinaryTypesExplicitModel.class)//
                .eq(BinaryTypesExplicitModel::getId, 7)//
                .queryForObject();

        assertNotNull(loaded);
        assertNotNull(loaded.getVarbinaryValue());
        assertArrayEquals(updatedData, loaded.getVarbinaryValue());
    }
}
