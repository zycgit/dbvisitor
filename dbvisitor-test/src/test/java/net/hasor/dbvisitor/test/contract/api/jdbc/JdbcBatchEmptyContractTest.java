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
public abstract class JdbcBatchEmptyContractTest extends JdbcBatchSupport {
    @Test
    @Capability(CapabilityId.JDBC_BATCH_EMPTY_ARGUMENTS)
    public void jdbcBatchEmpty_shouldNotExecuteStatements() throws SQLException {
        assertEquals(0, jdbcTemplate.executeBatch(new String[0]).length);
        // Invalid SQL makes accidental preparation/execution observable.
        assertEquals(0, jdbcTemplate.executeBatch("NOT A VALID SQL STATEMENT", new Object[0]).length);
    }
}
