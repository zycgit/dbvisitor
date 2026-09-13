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

public class RedisBasicBooleanTypeJdbcTest extends RedisBasicTypeSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_BASIC_BOOLEAN)
    public void booleanValues_shouldRoundTripTrueAndFalse() throws SQLException {
        assertEquals(Boolean.TRUE, roundTrip("true", true, Boolean.class));
        assertEquals(Boolean.FALSE, roundTrip("false", false, Boolean.class));
    }
}
