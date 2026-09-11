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
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@NxnContract
public abstract class JdbcCommandLifecycleContractTest extends AbstractNxnContractTest {
    @Test
    @Capability(CapabilityId.JDBC_CRUD_EXECUTE_DDL)
    public void jdbcExecute_shouldRunDdlAndDmlTableOperations() throws SQLException {
        String tableName = "nxn_jdbc_execute_temp";
        dropTableIfExists(tableName);
        jdbcTemplate.execute("CREATE TABLE " + tableName + " (" + primaryKeyColumn("id", "INT") + ", name VARCHAR(100), age INT, create_time " + profile().datetimeColumnType() + ")");

        int inserted = jdbcTemplate.executeUpdate("INSERT INTO " + tableName + " (id, name, age, create_time) VALUES (?, ?, ?, ?)", //
                new Object[] { 1, "NXN-JDBC-DDL", 25, new Date() });
        jdbcTemplate.execute(addColumnSql(tableName, "email VARCHAR(100)"));
        int updated = jdbcTemplate.executeUpdate("UPDATE " + tableName + " SET email = ? WHERE id = ?", //
                new Object[] { "nxn-jdbc-ddl@test.com", 1 });
        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT id, name, email FROM " + tableName + " WHERE id = ?", new Object[] { 1 });

        assertEquals(1, inserted);
        assertEquals(1, updated);
        assertEquals("NXN-JDBC-DDL", value(row, "name"));
        assertEquals("nxn-jdbc-ddl@test.com", value(row, "email"));

        jdbcTemplate.execute("DROP TABLE " + tableName);
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
            fail("Temporary DDL table should be dropped");
        } catch (SQLException expected) {
            assertNotNull(expected.getMessage());
        }
    }


    private Object value(Map<String, Object> row, String key) {
        assertNotNull(row);
        if (row.containsKey(key)) {
            return row.get(key);
        }
        if (row.containsKey(key.toUpperCase())) {
            return row.get(key.toUpperCase());
        }
        return row.get(key.toLowerCase());
    }
}
