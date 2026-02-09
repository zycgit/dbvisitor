package net.hasor.dbvisitor.test.oneapi.suite.fluent.types;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.types.EnumTypesModel;
import net.hasor.dbvisitor.test.oneapi.model.types.StatusEnum;
import net.hasor.dbvisitor.test.oneapi.model.types.StatusEnumOfCode;
import net.hasor.dbvisitor.test.oneapi.model.types.StatusEnumOfValue;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 枚举类型测试 - Fluent API (LambdaTemplate)
 * 对应 programmatic/types/EnumTypesJdbcTest，使用 LambdaTemplate 映射
 * 测试范围：
 * 1. StatusEnum name() 映射到 VARCHAR 字段（LambdaTemplate + statusString）
 * 2. StatusEnumOfCode 自定义字符串映射（LambdaTemplate + statusEnumCode）
 * 3. StatusEnumOfValue 数值映射（LambdaTemplate + statusOrdinal）
 * 4. null 值处理
 * 5. 更新操作
 * 6. 无效值处理
 */
public class EnumTypesFluentTest extends AbstractOneApiTest {

    @Override
    protected void cleanTestData() {
        try {
            jdbcTemplate.executeUpdate("DELETE FROM enum_types_explicit_test");
        } catch (Exception e) {
            // Ignore
        }
        super.cleanTestData();
    }

    /**
     * 测试 StatusEnum 存储为字符串（status_string 字段）
     * StatusEnum 通过 name() 映射到 VARCHAR
     */
    @Test
    public void testEnumAsString_AllValues() throws SQLException {
        // 插入所有枚举值
        EnumTypesModel model1 = new EnumTypesModel();
        model1.setId(1);
        model1.setStatusString(StatusEnum.ACTIVE);

        EnumTypesModel model2 = new EnumTypesModel();
        model2.setId(2);
        model2.setStatusString(StatusEnum.INACTIVE);

        EnumTypesModel model3 = new EnumTypesModel();
        model3.setId(3);
        model3.setStatusString(StatusEnum.PENDING);

        EnumTypesModel model4 = new EnumTypesModel();
        model4.setId(4);
        model4.setStatusString(StatusEnum.DELETED);

        lambdaTemplate.insert(EnumTypesModel.class).applyEntity(model1).executeSumResult();
        lambdaTemplate.insert(EnumTypesModel.class).applyEntity(model2).executeSumResult();
        lambdaTemplate.insert(EnumTypesModel.class).applyEntity(model3).executeSumResult();
        lambdaTemplate.insert(EnumTypesModel.class).applyEntity(model4).executeSumResult();

        // 查询并验证
        EnumTypesModel loaded1 = lambdaTemplate.query(EnumTypesModel.class)//
                .eq(EnumTypesModel::getId, 1)//
                .queryForObject();
        assertNotNull(loaded1);
        assertEquals(StatusEnum.ACTIVE, loaded1.getStatusString());

        EnumTypesModel loaded2 = lambdaTemplate.query(EnumTypesModel.class)//
                .eq(EnumTypesModel::getId, 2)//
                .queryForObject();
        assertEquals(StatusEnum.INACTIVE, loaded2.getStatusString());

        EnumTypesModel loaded3 = lambdaTemplate.query(EnumTypesModel.class)//
                .eq(EnumTypesModel::getId, 3)//
                .queryForObject();
        assertEquals(StatusEnum.PENDING, loaded3.getStatusString());

        EnumTypesModel loaded4 = lambdaTemplate.query(EnumTypesModel.class)//
                .eq(EnumTypesModel::getId, 4)//
                .queryForObject();
        assertEquals(StatusEnum.DELETED, loaded4.getStatusString());
    }

    /**
     * 测试使用 LambdaTemplate 插入后查询验证 StatusEnum 映射
     */
    @Test
    public void testStatusEnum_ByName_WithLambdaTemplate() throws SQLException {
        // 通过 LambdaTemplate 插入（枚举存为字符串）
        EnumTypesModel model = new EnumTypesModel();
        model.setId(6);
        model.setStatusString(StatusEnum.ACTIVE);

        lambdaTemplate.insert(EnumTypesModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        // 使用 LambdaTemplate 查询验证
        EnumTypesModel loaded = lambdaTemplate.query(EnumTypesModel.class)//
                .eq(EnumTypesModel::getId, 6)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals(StatusEnum.ACTIVE, loaded.getStatusString());
    }

    /**
     * 测试 EnumOfCode - LambdaTemplate 通过 EnumTypesModel.statusEnumCode 验证自定义字符串映射
     * StatusEnumOfCode 通过 codeName() 映射到 VARCHAR（"active"/"inactive"/"deleted"）
     */
    @Test
    public void testEnumOfCode_CustomStringMapping() throws SQLException {
        // 通过 LambdaTemplate 插入 EnumOfCode 枚举值
        EnumTypesModel model1 = new EnumTypesModel();
        model1.setId(7);
        model1.setStatusEnumCode(StatusEnumOfCode.ACTIVE);

        EnumTypesModel model2 = new EnumTypesModel();
        model2.setId(8);
        model2.setStatusEnumCode(StatusEnumOfCode.INACTIVE);

        EnumTypesModel model3 = new EnumTypesModel();
        model3.setId(9);
        model3.setStatusEnumCode(StatusEnumOfCode.DELETED);

        lambdaTemplate.insert(EnumTypesModel.class).applyEntity(model1).executeSumResult();
        lambdaTemplate.insert(EnumTypesModel.class).applyEntity(model2).executeSumResult();
        lambdaTemplate.insert(EnumTypesModel.class).applyEntity(model3).executeSumResult();

        // 通过 LambdaTemplate 查询验证 codeName 映射
        EnumTypesModel loaded1 = lambdaTemplate.query(EnumTypesModel.class)//
                .eq(EnumTypesModel::getId, 7)//
                .queryForObject();
        assertNotNull(loaded1);
        assertEquals(StatusEnumOfCode.ACTIVE, loaded1.getStatusEnumCode());
        assertEquals("active", loaded1.getStatusEnumCode().codeName());

        EnumTypesModel loaded2 = lambdaTemplate.query(EnumTypesModel.class)//
                .eq(EnumTypesModel::getId, 8)//
                .queryForObject();
        assertEquals(StatusEnumOfCode.INACTIVE, loaded2.getStatusEnumCode());
        assertEquals("inactive", loaded2.getStatusEnumCode().codeName());

        EnumTypesModel loaded3 = lambdaTemplate.query(EnumTypesModel.class)//
                .eq(EnumTypesModel::getId, 9)//
                .queryForObject();
        assertEquals(StatusEnumOfCode.DELETED, loaded3.getStatusEnumCode());
        assertEquals("deleted", loaded3.getStatusEnumCode().codeName());
    }

    /**
     * 测试 EnumOfValue - LambdaTemplate 通过 EnumTypesModel 验证数值映射
     * StatusEnumOfValue 通过 codeValue() 映射到 INTEGER（ACTIVE=1, INACTIVE=0, DELETED=-1）
     */
    @Test
    public void testEnumOfValue_IntegerMapping() throws SQLException {
        // 通过 LambdaTemplate + EnumTypesModel 插入 EnumOfValue 枚举值
        EnumTypesModel model1 = new EnumTypesModel();
        model1.setId(10);
        model1.setStatusOrdinal(StatusEnumOfValue.ACTIVE);

        EnumTypesModel model2 = new EnumTypesModel();
        model2.setId(11);
        model2.setStatusOrdinal(StatusEnumOfValue.INACTIVE);

        EnumTypesModel model3 = new EnumTypesModel();
        model3.setId(12);
        model3.setStatusOrdinal(StatusEnumOfValue.DELETED);

        lambdaTemplate.insert(EnumTypesModel.class).applyEntity(model1).executeSumResult();
        lambdaTemplate.insert(EnumTypesModel.class).applyEntity(model2).executeSumResult();
        lambdaTemplate.insert(EnumTypesModel.class).applyEntity(model3).executeSumResult();

        // 通过 LambdaTemplate 查询验证
        EnumTypesModel loaded1 = lambdaTemplate.query(EnumTypesModel.class)//
                .eq(EnumTypesModel::getId, 10)//
                .queryForObject();
        assertNotNull(loaded1);
        assertEquals(StatusEnumOfValue.ACTIVE, loaded1.getStatusOrdinal());
        assertEquals(1, loaded1.getStatusOrdinal().codeValue());

        EnumTypesModel loaded2 = lambdaTemplate.query(EnumTypesModel.class)//
                .eq(EnumTypesModel::getId, 11)//
                .queryForObject();
        assertEquals(StatusEnumOfValue.INACTIVE, loaded2.getStatusOrdinal());
        assertEquals(0, loaded2.getStatusOrdinal().codeValue());

        EnumTypesModel loaded3 = lambdaTemplate.query(EnumTypesModel.class)//
                .eq(EnumTypesModel::getId, 12)//
                .queryForObject();
        assertEquals(StatusEnumOfValue.DELETED, loaded3.getStatusOrdinal());
        assertEquals(-1, loaded3.getStatusOrdinal().codeValue());
    }

    /**
     * 测试枚举 null 值处理
     */
    @Test
    public void testEnumNullValues() throws SQLException {
        EnumTypesModel model = new EnumTypesModel();
        model.setId(20);
        model.setStatusString(null);
        model.setStatusOrdinal(null);
        model.setStatusCode(null);

        lambdaTemplate.insert(EnumTypesModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        EnumTypesModel loaded = lambdaTemplate.query(EnumTypesModel.class)//
                .eq(EnumTypesModel::getId, 20)//
                .queryForObject();

        assertNotNull(loaded);
        assertNull(loaded.getStatusString());
        assertNull(loaded.getStatusOrdinal());
        assertNull(loaded.getStatusCode());
    }

    /**
     * 测试更新枚举字段
     */
    @Test
    public void testEnumUpdate() throws SQLException {
        // 初始插入
        EnumTypesModel model = new EnumTypesModel();
        model.setId(21);
        model.setStatusString(StatusEnum.ACTIVE);

        lambdaTemplate.insert(EnumTypesModel.class)//
                .applyEntity(model)//
                .executeSumResult();

        // 更新
        int updated = lambdaTemplate.update(EnumTypesModel.class)//
                .eq(EnumTypesModel::getId, 21)//
                .updateTo(EnumTypesModel::getStatusString, StatusEnum.DELETED)//
                .doUpdate();
        assertEquals(1, updated);

        // 验证更新
        EnumTypesModel loaded = lambdaTemplate.query(EnumTypesModel.class)//
                .eq(EnumTypesModel::getId, 21)//
                .queryForObject();

        assertNotNull(loaded);
        assertEquals(StatusEnum.DELETED, loaded.getStatusString());
    }

    /**
     * 测试枚举无效值处理
     * 通过 SQL 插入无效字符串，用 LambdaTemplate 通过 EnumTypesModel 查询，
     * StatusEnum 字段映射应触发异常
     */
    @Test
    public void testEnumInvalidValues() throws SQLException {
        // 插入无效的枚举名称（绕过 LambdaTemplate 防止插入时就报错）
        jdbcTemplate.executeUpdate("INSERT INTO enum_types_explicit_test (id, status_string) VALUES (?, ?)",//
                new Object[] { 22, "UNKNOWN" });

        try {
            // 使用 LambdaTemplate 查询，映射 StatusEnum 时应抛异常
            lambdaTemplate.query(EnumTypesModel.class)//
                    .eq(EnumTypesModel::getId, 22)//
                    .queryForObject();
            fail("Should throw exception for invalid enum value");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("UNKNOWN") || e.getCause().getMessage().contains("UNKNOWN"));
        }
    }
}
