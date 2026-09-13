/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class RedisBasicNumericTypeJdbcTest extends RedisBasicTypeSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_BASIC_NUMERIC)
    public void numericValues_shouldRoundTripWithoutPrecisionLoss() throws SQLException {
        assertEquals(Byte.valueOf(Byte.MAX_VALUE), roundTrip("byte", Byte.MAX_VALUE, Byte.class));
        assertEquals(Short.valueOf(Short.MAX_VALUE), roundTrip("short", Short.MAX_VALUE, Short.class));
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), roundTrip("int", Integer.MAX_VALUE, Integer.class));
        assertEquals(Long.valueOf(Long.MAX_VALUE), roundTrip("long", Long.MAX_VALUE, Long.class));
        assertEquals(3.14f, roundTrip("float", 3.14f, Float.class), 0.001f);
        assertEquals(2.718281828d, roundTrip("double", 2.718281828d, Double.class), 0.000001d);
    }
}
