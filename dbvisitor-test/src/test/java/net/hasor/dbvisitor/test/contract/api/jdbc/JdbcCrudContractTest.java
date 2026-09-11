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
public abstract class JdbcCrudContractTest extends JdbcCrudSupport {

    @Test
    @Capability(CapabilityId.JDBC_CRUD_INSERT)
    public void jdbcInsert_shouldPersistOneUser() throws SQLException {
        int id = baseId() + 1;
        insertUser(id, "NXN-JDBC-Insert", 31, "nxn-insert@test.com");
        assertEquals("NXN-JDBC-Insert", storedValue(JdbcCrudCommand.SELECT_NAME, "name", id));
    }

    @Test
    @Capability(CapabilityId.JDBC_CRUD_QUERY)
    public void jdbcQuery_shouldReadUserById() throws SQLException {
        int id = baseId() + 2;
        insertUser(id, "NXN-JDBC-Query", 32, "nxn-query@test.com");
        assertEquals("32", storedValue(JdbcCrudCommand.SELECT_AGE, "age", id));
    }

    @Test
    @Capability(CapabilityId.JDBC_CRUD_UPDATE)
    public void jdbcUpdate_shouldChangeUser() throws SQLException {
        int id = baseId() + 3;
        insertUser(id, "NXN-JDBC-Update", 33, "nxn-update@test.com");
        jdbcTemplate.executeUpdate(command(JdbcCrudCommand.UPDATE_AGE), new Object[] { 34, id });
        assertEquals("34", storedValue(JdbcCrudCommand.SELECT_AGE, "age", id));
    }

    @Test
    @Capability(CapabilityId.JDBC_CRUD_DELETE)
    public void jdbcDelete_shouldRemoveUser() throws SQLException {
        int id = baseId() + 4;
        insertUser(id, "NXN-JDBC-Delete", 35, "nxn-delete@test.com");
        jdbcTemplate.executeUpdate(command(JdbcCrudCommand.DELETE), new Object[] { id });
        assertUserAbsent(id);
    }
}
