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
public abstract class JdbcMutationCountCase extends JdbcCrudSupport {
    // 能力归属：编程式 API / 更新。
    @Test
    @Capability(value = CapabilityId.JDBC_CRUD_INSERT_COUNT, column = "jdbc/updates/updates")
    public void insertResult_shouldReportOneAffectedRow() throws SQLException {
        int rows = insertUser(baseId() + 1, "NXN-JDBC-Insert", 31, "nxn-insert@test.com");
        assertEquals(1, rows);
    }

    // 能力归属：编程式 API / 更新。
    @Test
    @Capability(value = CapabilityId.JDBC_CRUD_UPDATE_COUNT, column = "jdbc/updates/updates")
    public void updateResult_shouldReportOneAffectedRow() throws SQLException {
        int id = baseId() + 3;
        insertUser(id, "NXN-JDBC-Update", 33, "nxn-update@test.com");
        int rows = jdbcTemplate.executeUpdate(command(JdbcCrudCommand.UPDATE_AGE), new Object[] { 34, id });
        assertEquals(1, rows);
    }

    // 能力归属：编程式 API / 更新。
    @Test
    @Capability(value = CapabilityId.JDBC_CRUD_DELETE_COUNT, column = "jdbc/updates/updates")
    public void deleteResult_shouldReportOneAffectedRow() throws SQLException {
        int id = baseId() + 4;
        insertUser(id, "NXN-JDBC-Delete", 35, "nxn-delete@test.com");
        int rows = jdbcTemplate.executeUpdate(command(JdbcCrudCommand.DELETE), new Object[] { id });
        assertEquals(1, rows);
    }
}
