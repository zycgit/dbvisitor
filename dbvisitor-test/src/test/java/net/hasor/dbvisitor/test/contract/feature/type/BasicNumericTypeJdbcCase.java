/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.sql.SQLException;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.types.BasicTypesModel;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class BasicNumericTypeJdbcCase extends BasicTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_BASIC_NUMERIC)
    public void basicNumericTypes_shouldRoundTripThroughJdbcTemplate() throws SQLException {
        int id = baseId() + 1;
        jdbcTemplate.executeUpdate(//
                insertCommand("basic_types_test", "id, byte_value, short_value, int_value, long_value, float_value, double_value"), //
                new Object[] { id, Byte.MAX_VALUE, Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, 3.14f, 2.718281828d });

        BasicTypesModel loaded = jdbcTemplate.queryForObject(selectCommand("basic_types_test", "*"), new Object[] { id }, BasicTypesModel.class);

        assertNotNull(loaded);
        assertEquals(Byte.valueOf(Byte.MAX_VALUE), loaded.getByteValue());
        assertEquals(Short.valueOf(Short.MAX_VALUE), loaded.getShortValue());
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), loaded.getIntValue());
        assertEquals(Long.valueOf(Long.MAX_VALUE), loaded.getLongValue());
        assertEquals(3.14f, loaded.getFloatValue(), 0.001f);
        assertEquals(2.718281828d, loaded.getDoubleValue(), 0.000001d);
    }
}
