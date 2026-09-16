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
import net.hasor.dbvisitor.test.contract.material.model.types.BasicTypesModel;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class BasicBigIntegerTypeJdbcCase extends BasicTypeJdbcSupport {
    // 能力归属：类型处理器 / 数字与布尔 / 大整数。
    @Test
    @Capability(value = CapabilityId.TYPE_BASIC_BIG_INTEGER, column = "types/basic-types/values")
    public void bigIntegerValue_shouldRoundTripWithoutPrecisionLoss() throws SQLException {
        assertEquals(new BigInteger("9223372036854775807"), roundTripBigIntegerValue());
    }

    protected BigInteger roundTripBigIntegerValue() throws SQLException {
        int id = baseId() + 10;
        jdbcTemplate.executeUpdate(insertCommand("basic_types_test", "id, big_int_value"), new Object[] { id, new BigInteger("9223372036854775807") });
        BasicTypesModel loaded = jdbcTemplate.queryForObject(selectCommand("basic_types_test", "*"), new Object[] { id }, BasicTypesModel.class);
        assertNotNull(loaded);
        return loaded.getBigIntValue();
    }
}
