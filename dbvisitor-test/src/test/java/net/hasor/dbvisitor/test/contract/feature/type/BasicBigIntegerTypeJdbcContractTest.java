/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.type;

import java.math.BigInteger;
import java.sql.SQLException;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.types.BasicTypesModel;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class BasicBigIntegerTypeJdbcContractTest extends BasicTypeJdbcSupport {
    @Test
    @Capability(CapabilityId.TYPE_BASIC_BIG_INTEGER)
    public void bigIntegerValue_shouldRoundTripWithoutPrecisionLoss() throws SQLException {
        int id = baseId() + 10;
        jdbcTemplate.executeUpdate(insertCommand("basic_types_test", "id, big_int_value"),
                new Object[] { id, new BigInteger("9223372036854775807") });
        BasicTypesModel loaded = jdbcTemplate.queryForObject(selectCommand("basic_types_test", "*"), new Object[] { id }, BasicTypesModel.class);
        assertNotNull(loaded);
        assertEquals(new BigInteger("9223372036854775807"), loaded.getBigIntValue());
    }
}
