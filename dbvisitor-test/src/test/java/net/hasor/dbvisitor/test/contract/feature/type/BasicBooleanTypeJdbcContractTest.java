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

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class BasicBooleanTypeJdbcContractTest extends BasicTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_BASIC_BOOLEAN)
    public void basicBooleanTypes_shouldRoundTripTrueAndFalse() throws SQLException {
        int trueId = baseId() + 2;
        int falseId = baseId() + 8;
        String insertSql = insertCommand("basic_types_test", "id, bool_value");
        assertEquals(1, jdbcTemplate.executeUpdate(insertSql, new Object[] { trueId, true }));
        assertEquals(1, jdbcTemplate.executeUpdate(insertSql, new Object[] { falseId, false }));

        Boolean trueValue = jdbcTemplate.queryForObject(selectCommand("basic_types_test", "bool_value"), new Object[] { trueId }, Boolean.class);
        Boolean falseValue = jdbcTemplate.queryForObject(selectCommand("basic_types_test", "bool_value"), new Object[] { falseId }, Boolean.class);

        assertEquals(Boolean.TRUE, trueValue);
        assertEquals(Boolean.FALSE, falseValue);
    }
}
