/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcScalarListQueryCase extends JdbcQuerySupport {
    @Test
    @Capability(CapabilityId.JDBC_QUERY_SCALAR_LIST)
    public void jdbcQueryForList_shouldReturnTypedScalarRows() throws SQLException {
        seedUsers();
        List<String> names = jdbcTemplate.queryForList(selectRange("name", "?", "?", true), //
                new Object[] { baseId() + 1, baseId() + 3 }, String.class);

        assertEquals(3, names.size());
        assertEquals("NXN-JDBC-Query-1", names.get(0));
        assertEquals("NXN-JDBC-Query-3", names.get(2));
        for (int i = 0; i < names.size(); i++) {
            assertEquals("NXN-JDBC-Query-" + (i + 1), names.get(i));
        }
    }
}
