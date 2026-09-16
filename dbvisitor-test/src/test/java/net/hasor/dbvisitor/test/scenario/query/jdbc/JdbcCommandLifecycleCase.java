/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.scenario.query.jdbc;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import org.junit.Test;
import static org.junit.Assert.*;

public abstract class JdbcCommandLifecycleCase extends AbstractNxnContractTest {
    protected String lifecycleObject() {
        return "nxn_jdbc_execute_temp";
    }

    protected void resetLifecycleFixture() throws SQLException {
        dropTableIfExists(lifecycleObject());
    }

    protected String createCommand() {
        return "CREATE TABLE " + lifecycleObject() + " (" + primaryKeyColumn("id", "INT") + ", name VARCHAR(100), age INT, create_time " + profile().datetimeColumnType() + ")";
    }

    protected String insertCommand() {
        return "INSERT INTO " + lifecycleObject() + " (id, name, age, create_time) VALUES (?, ?, ?, ?)";
    }

    protected String alterCommand() {
        return addColumnSql(lifecycleObject(), "email VARCHAR(100)");
    }

    protected String updateCommand() {
        return "UPDATE " + lifecycleObject() + " SET email = ? WHERE id = ?";
    }

    protected String readCommand(String column) {
        return "SELECT " + column + " FROM " + lifecycleObject() + " WHERE id = ?";
    }

    protected Object[] readArguments() {
        return new Object[] { 1 };
    }

    protected int expectedInsertCount() {
        return 1;
    }

    protected String retiredObjectQuery() {
        return null;
    }

    protected String dropCommand() {
        return "DROP TABLE " + lifecycleObject();
    }

    protected String missingObjectQuery() {
        return "SELECT COUNT(*) FROM " + lifecycleObject();
    }

    protected boolean missingObjectRaisesError() {
        return true;
    }

    @Test
    @Capability(CapabilityId.JDBC_CRUD_EXECUTE_DDL)
    public void jdbcExecute_shouldRunDdlAndDmlTableOperations() throws SQLException {
        resetLifecycleFixture();
        jdbcTemplate.execute(createCommand());
        int inserted = jdbcTemplate.executeUpdate(insertCommand(), new Object[] { 1, "NXN-JDBC-DDL", 25, new Date() });
        jdbcTemplate.execute(alterCommand());
        int updated = jdbcTemplate.executeUpdate(updateCommand(), new Object[] { "nxn-jdbc-ddl@test.com", 1 });

        assertEquals(expectedInsertCount(), inserted);
        assertEquals(1, updated);
        assertEquals("NXN-JDBC-DDL", jdbcTemplate.queryForString(readCommand("name"), readArguments()));
        assertEquals("nxn-jdbc-ddl@test.com", jdbcTemplate.queryForString(readCommand("email"), readArguments()));
        if (retiredObjectQuery() != null) {
            assertEquals(Long.valueOf(0), jdbcTemplate.queryForLong(retiredObjectQuery()));
        }

        jdbcTemplate.execute(dropCommand());
        if (missingObjectRaisesError()) {
            try {
                jdbcTemplate.queryForObject(missingObjectQuery(), Long.class);
                fail("Temporary object should be dropped");
            } catch (SQLException expected) {
                assertNotNull(expected.getMessage());
            }
        } else {
            assertEquals(Long.valueOf(0), jdbcTemplate.queryForLong(missingObjectQuery()));
        }
    }
}
