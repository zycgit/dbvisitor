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

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.types.BasicTypesModel;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class BasicDecimalTypeJdbcCase extends BasicTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_BASIC_DECIMAL)
    public void decimalValue_shouldRoundTripWithoutPrecisionLoss() throws SQLException {
        int id = baseId() + 9;
        jdbcTemplate.executeUpdate(insertCommand("basic_types_test", "id, decimal_value"),
                new Object[] { id, new BigDecimal("12345.67") });
        BasicTypesModel loaded = jdbcTemplate.queryForObject(selectCommand("basic_types_test", "*"), new Object[] { id }, BasicTypesModel.class);
        assertNotNull(loaded);
        assertEquals("Decimal readback: " + loaded.getDecimalValue(), 0, new BigDecimal("12345.67").compareTo(loaded.getDecimalValue()));
    }
}
