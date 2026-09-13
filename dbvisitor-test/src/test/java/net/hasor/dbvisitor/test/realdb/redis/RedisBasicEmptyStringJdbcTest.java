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

public class RedisBasicEmptyStringJdbcTest extends RedisBasicTypeSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_REDIS_BASIC_EMPTY_STRING)
    public void emptyString_shouldRoundTripWithoutBecomingNull() throws SQLException {
        assertEquals("", roundTrip("empty", "", String.class));
    }
}
