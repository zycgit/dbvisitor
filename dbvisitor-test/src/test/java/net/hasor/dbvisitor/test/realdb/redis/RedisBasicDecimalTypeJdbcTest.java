/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.SQLException;
import java.math.BigDecimal;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class RedisBasicDecimalTypeJdbcTest extends RedisBasicTypeSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_BASIC_DECIMAL)
    public void decimalValue_shouldRoundTripWithoutPrecisionLoss() throws SQLException {
        BigDecimal value = roundTrip("decimal", new BigDecimal("12345.67"), BigDecimal.class);
        assertEquals("Decimal readback: " + value, 0, new BigDecimal("12345.67").compareTo(value));
    }
}
