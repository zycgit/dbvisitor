/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.material.model.types.BasicTypesModel;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class BasicNumericTypeJdbcCase extends BasicTypeJdbcSupport {
    // 能力归属：类型处理器 / 数字与布尔 / 整数与浮点。
    @Test
    @Capability(value = CapabilityId.TYPE_BASIC_NUMERIC, column = "types/basic-types/values")
    public void basicNumericTypes_shouldRoundTripThroughJdbcTemplate() throws SQLException {
        BasicTypesModel loaded = roundTripNumericValues();

        assertNotNull(loaded);
        assertEquals(Byte.valueOf(Byte.MAX_VALUE), loaded.getByteValue());
        assertEquals(Short.valueOf(Short.MAX_VALUE), loaded.getShortValue());
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), loaded.getIntValue());
        assertEquals(Long.valueOf(Long.MAX_VALUE), loaded.getLongValue());
        assertEquals(3.14f, loaded.getFloatValue(), 0.001f);
        assertEquals(2.718281828d, loaded.getDoubleValue(), 0.000001d);
    }

    protected BasicTypesModel roundTripNumericValues() throws SQLException {
        int id = baseId() + 1;
        jdbcTemplate.executeUpdate(//
                insertCommand("basic_types_test", "id, byte_value, short_value, int_value, long_value, float_value, double_value"), //
                new Object[] { id, Byte.MAX_VALUE, Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, 3.14f, 2.718281828d });

        return jdbcTemplate.queryForObject(selectCommand("basic_types_test", "*"), new Object[] { id }, BasicTypesModel.class);
    }

    // 能力归属：类型处理器 / 基础类型 / 数字包装类型的 NULL 读取。
    @Test
    @Capability(value = CapabilityId.TYPE_BASIC_NUMERIC_NULL, column = "types/basic-types/values")
    public void basicNumericNulls_shouldNotBecomePrimitiveDefaults() throws SQLException {
        int id = baseId() + 11;
        jdbcTemplate.executeUpdate(insertCommand("basic_types_test", //
                        "id, byte_value, short_value, int_value, long_value, float_value, double_value, decimal_value, big_int_value"), //
                new Object[] { id, null, null, null, null, null, null, null, null });

        BasicTypesModel loaded = jdbcTemplate.queryForObject(selectCommand("basic_types_test", "*"), new Object[] { id }, BasicTypesModel.class);
        assertNotNull(loaded);
        assertEquals(Integer.valueOf(id), loaded.getId());
        assertNull(loaded.getByteValue());
        assertNull(loaded.getShortValue());
        assertNull(loaded.getIntValue());
        assertNull(loaded.getLongValue());
        assertNull(loaded.getFloatValue());
        assertNull(loaded.getDoubleValue());
        assertNull(loaded.getDecimalValue());
        assertNull(loaded.getBigIntValue());
        String[] columns = { "byte_value", "short_value", "int_value", "long_value", "float_value", "double_value", "decimal_value", "big_int_value" };
        Class<?>[] types = { Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, java.math.BigDecimal.class, java.math.BigInteger.class };
        for (int i = 0; i < columns.length; i++) {
            assertNull(columns[i], jdbcTemplate.queryForObject(selectCommand("basic_types_test", columns[i]), new Object[] { id }, types[i]));
        }
    }
}
