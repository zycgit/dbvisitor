/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.parameter;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterCommand;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterSupport;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcStatementSetterParameterCase extends JdbcParameterSupport {
    // 能力归属：参数传递 / 接口方式与参数选项 / 参数设置接口。
    @Test
    @Capability(value = CapabilityId.JDBC_PARAM_STATEMENT_SETTER, column = "parameters/parameter-interfaces-and-options/statement-setter")
    public void preparedStatementSetter_shouldBindParameters() throws SQLException {
        int id = baseId() + 4;
        writeParameters(command(JdbcParameterCommand.INSERT_POSITIONAL), ps -> {
            ps.setInt(1, id);
            ps.setString(2, "NXN-Param-Setter");
            ps.setInt(3, 28);
            ps.setString(4, "nxn-param-setter@test.com");
            ps.setDate(5, new java.sql.Date(System.currentTimeMillis()));
        });

        String email = readEmail(command(JdbcParameterCommand.SELECT_EMAIL_BY_NAME), ps -> ps.setString(1, "NXN-Param-Setter"));

        assertEquals("nxn-param-setter@test.com", email);
    }
}
