/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcCountReadbackContractTest extends JdbcCrudSupport {
    @Test
    @Capability(CapabilityId.JDBC_CRUD_DELETE_REMAINING_COUNT)
    public void countReadback_shouldReturnZeroAfterDeletingMatchingRow() throws SQLException {
        int id = baseId() + 4;
        insertUser(id, "NXN-JDBC-Delete", 35, "nxn-delete@test.com");
        jdbcTemplate.executeUpdate(command(JdbcCrudCommand.DELETE), new Object[] { id });
        assertEquals(Integer.valueOf(0), jdbcTemplate.queryForObject(command(JdbcCrudCommand.COUNT_BY_ID), new Object[] { id }, Integer.class));
    }
}
