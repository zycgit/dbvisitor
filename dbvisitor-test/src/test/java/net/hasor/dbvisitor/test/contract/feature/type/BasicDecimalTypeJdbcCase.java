/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;
import java.math.BigDecimal;
import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.material.model.types.BasicTypesModel;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@NxnContract
public abstract class BasicDecimalTypeJdbcCase extends BasicTypeJdbcSupport {
    // 能力归属：类型处理器 / 数字与布尔 / 高精度小数。
    @Test
    @Capability(value = CapabilityId.TYPE_BASIC_DECIMAL, column = "types/basic-types/values")
    public void decimalValue_shouldRoundTripWithoutPrecisionLoss() throws SQLException {
        BigDecimal loaded = roundTripDecimalValue();
        assertNotNull(loaded);
        assertEquals(0, new BigDecimal("12345.67").compareTo(loaded), "Decimal readback: " + loaded);
    }

    protected BigDecimal roundTripDecimalValue() throws SQLException {
        int id = baseId() + 9;
        jdbcTemplate.executeUpdate(insertCommand("basic_types_test", "id, decimal_value"), new Object[] { id, new BigDecimal("12345.67") });
        BasicTypesModel loaded = jdbcTemplate.queryForObject(selectCommand("basic_types_test", "*"), new Object[] { id }, BasicTypesModel.class);
        assertNotNull(loaded);
        return loaded.getDecimalValue();
    }
}
