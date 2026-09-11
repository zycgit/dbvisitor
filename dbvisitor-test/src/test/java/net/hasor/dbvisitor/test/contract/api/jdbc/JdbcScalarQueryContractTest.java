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
public abstract class JdbcScalarQueryContractTest extends JdbcQuerySupport {
    @Test
    @Capability(CapabilityId.JDBC_QUERY_SCALAR)
    public void jdbcQueryForObject_shouldReturnScalarValue() throws SQLException {
        seedUsers();

        Integer age = jdbcTemplate.queryForObject(selectById("age", "?"), new Object[] { baseId() + 3 }, Integer.class);

        assertEquals(Integer.valueOf(63), age);
    }

    @Test
    @Capability(CapabilityId.JDBC_QUERY_SCALAR_SHORTCUTS)
    public void jdbcScalarShortcuts_shouldSupportLongIntAndStringQueries() throws SQLException {
        seedUsers();

        Long firstAge = jdbcTemplate.queryForLong(selectById("age", ":minId"), //
                params(baseId() + 1, baseId() + 3));
        Integer secondAge = jdbcTemplate.queryForInt(selectById("age", "?"), //
                new Object[] { baseId() + 2 });
        Integer lastAge = jdbcTemplate.queryForInt(selectById("age", ":maxId"), //
                params(baseId() + 1, baseId() + 3));
        String name = jdbcTemplate.queryForString(selectById("name", "?"), new Object[] { baseId() + 2 });
        String lastName = jdbcTemplate.queryForString(selectById("name", ":maxId"), //
                params(baseId() + 1, baseId() + 3));

        assertEquals(Long.valueOf(61), firstAge);
        assertEquals(Integer.valueOf(62), secondAge);
        assertEquals(Integer.valueOf(63), lastAge);
        assertEquals("NXN-JDBC-Query-2", name);
        assertEquals("NXN-JDBC-Query-3", lastName);
    }
}
