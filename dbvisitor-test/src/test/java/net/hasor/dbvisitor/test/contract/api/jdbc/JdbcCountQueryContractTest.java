/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcCountQueryContractTest extends JdbcQuerySupport {
    @Test
    @Capability(CapabilityId.JDBC_QUERY_COUNT_VALUE)
    public void jdbcCountResult_shouldSupportIntAndLongShortcuts() throws SQLException {
        seedUsers();
        int count = jdbcTemplate.queryForInt(countRange("?", "?"), new Object[] { baseId() + 1, baseId() + 3 });
        Long longCount = jdbcTemplate.queryForLong(countRange("?", "?"), new Object[] { baseId() + 1, baseId() + 3 });

        assertEquals(3, count);
        assertEquals(Long.valueOf(3), longCount);
    }
}
