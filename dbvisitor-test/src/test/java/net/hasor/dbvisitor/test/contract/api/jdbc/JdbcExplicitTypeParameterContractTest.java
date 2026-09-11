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
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.number.IntegerTypeHandler;
import net.hasor.dbvisitor.types.handler.string.StringTypeHandler;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcExplicitTypeParameterContractTest extends JdbcParameterSupport {
    @Test
    @Capability(CapabilityId.JDBC_PARAM_SQLARG)
    public void sqlArgParameters_shouldUseExplicitTypeHandlers() throws SQLException {
        int id = baseId() + 3;
        jdbcTemplate.executeUpdate(command(JdbcParameterCommand.INSERT_POSITIONAL), //
                // @formatter:off
                new SqlArg[] {
                    SqlArg.valueOf(id, new IntegerTypeHandler()),
                    SqlArg.valueOf("NXN-Param-SqlArg", new StringTypeHandler()),
                    SqlArg.valueOf(30, new IntegerTypeHandler()),
                    SqlArg.valueOf("nxn-param-sqlarg@test.com"),
                    SqlArg.valueOf(new Date())
                });
                // @formatter:on

        assertEquals("nxn-param-sqlarg@test.com", readEmail(command(JdbcParameterCommand.SELECT_EMAIL_BY_ID), new Object[] { id }));
    }
}
