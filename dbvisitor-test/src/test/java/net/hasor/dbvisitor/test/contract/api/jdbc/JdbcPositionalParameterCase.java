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

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class JdbcPositionalParameterCase extends JdbcParameterSupport {
    @Test
    @Capability(CapabilityId.JDBC_PARAM_POSITIONAL_ARRAY)
    public void positionalArrayParameters_shouldBindObjectArray() throws SQLException {
        int id = baseId() + 1;
        insert(id, "NXN-Param-Array", 25, "nxn-param-array@test.com");

        // Parameter binding does not require conversion of unrelated temporal fixture columns.
        UserInfo user = jdbcTemplate.queryForObject(command(JdbcParameterCommand.SELECT_USER), //
                new Object[] { "NXN-Param-Array", 20 }, UserInfo.class);

        assertNotNull(user);
        assertEquals(Integer.valueOf(id), user.getId());
        assertEquals(Integer.valueOf(25), user.getAge());
    }
}
