/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.parameter;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterCommand;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterSupport;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class JdbcNullParameterCase extends JdbcParameterSupport {
    // 能力归属：参数传递 / 位置参数与名称参数 / 位置参数。
    @Test
    @Capability(value = CapabilityId.JDBC_PARAM_NULL, column = "parameters/positional-and-named-parameters/positional")
    public void positionalArrayParameters_shouldBindNullValues() throws SQLException {
        int id = baseId() + 2;
        jdbcTemplate.executeUpdate(command(JdbcParameterCommand.INSERT_POSITIONAL), //
                new Object[] { id, "NXN-Param-Null", null, "nxn-param-null@test.com", new Date() });

        Map<String, Object> row = jdbcTemplate.queryForMap(command(JdbcParameterCommand.SELECT_NULL_ROW), new Object[] { id });

        assertEquals("NXN-Param-Null", value(row, "name"));
        assertNull(value(row, "age"));
    }
}
