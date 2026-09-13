/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcScalarQueryCase extends JdbcQuerySupport {
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

    @Test
    @Capability(CapabilityId.JDBC_CRUD_INSERT_SCALAR)
    public void scalarReadback_shouldReadInsertedNameAsString() throws SQLException {
        int id = baseId() + 1;
        insertUser(id, "NXN-JDBC-Insert", 31, "nxn-insert@test.com", new Date());
        assertEquals("NXN-JDBC-Insert", jdbcTemplate.queryForString(command(JdbcCrudCommand.SELECT_NAME), new Object[] { id }));
    }

    @Test
    @Capability(CapabilityId.JDBC_CRUD_QUERY_SCALAR)
    public void scalarReadback_shouldReadStoredAgeAsInteger() throws SQLException {
        int id = baseId() + 2;
        insertUser(id, "NXN-JDBC-Query", 32, "nxn-query@test.com", new Date());
        Integer age = jdbcTemplate.queryForObject(command(JdbcCrudCommand.SELECT_AGE), new Object[] { id }, Integer.class);
        assertEquals(Integer.valueOf(32), age);
    }

    @Test
    @Capability(CapabilityId.JDBC_CRUD_UPDATE_SCALAR)
    public void scalarReadback_shouldReadUpdatedAgeAsInteger() throws SQLException {
        int id = baseId() + 3;
        insertUser(id, "NXN-JDBC-Update", 33, "nxn-update@test.com", new Date());
        jdbcTemplate.executeUpdate(command(JdbcCrudCommand.UPDATE_AGE), new Object[] { 34, id });
        assertEquals(Integer.valueOf(34), jdbcTemplate.queryForObject(command(JdbcCrudCommand.SELECT_AGE), new Object[] { id }, Integer.class));
    }
}
