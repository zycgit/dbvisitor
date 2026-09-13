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
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class JdbcNullParameterCase extends JdbcParameterSupport {
    @Test
    @Capability(CapabilityId.JDBC_PARAM_NULL)
    public void positionalArrayParameters_shouldBindNullValues() throws SQLException {
        int id = baseId() + 2;
        jdbcTemplate.executeUpdate(command(JdbcParameterCommand.INSERT_POSITIONAL), //
                new Object[] { id, "NXN-Param-Null", null, "nxn-param-null@test.com", new Date() });

        Map<String, Object> row = jdbcTemplate.queryForMap(command(JdbcParameterCommand.SELECT_NULL_ROW), new Object[] { id });

        assertEquals("NXN-Param-Null", value(row, "name"));
        assertNull(value(row, "age"));
    }
}
