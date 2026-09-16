/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.parameter;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterCommand;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterSupport;
import net.hasor.dbvisitor.test.contract.material.handler.ExplicitVarcharTypeHandler;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.handler.number.IntegerTypeHandler;
import net.hasor.dbvisitor.types.handler.string.StringTypeHandler;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcExplicitTypeParameterCase extends JdbcParameterSupport {
    // 能力归属：参数传递 / 接口方式与参数选项 / 显式类型。
    @Test
    @Capability(value = CapabilityId.JDBC_PARAM_SQLARG, column = "parameters/parameter-interfaces-and-options/explicit-types")
    public void sqlArgParameters_shouldUseExplicitTypeHandlers() throws SQLException {
        int id = baseId() + 3;
        writeParameters(command(JdbcParameterCommand.INSERT_POSITIONAL), //
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
        assertEquals("nxn-param-sqlarg@test.com", readEmail(command(JdbcParameterCommand.SELECT_EMAIL_BY_NAME), new Object[] { "NXN-Param-SqlArg" }));
    }

    // 能力归属：参数传递 / 接口方式与参数选项 / 命名 Map 内的显式类型参数。
    @Test
    @Capability(value = CapabilityId.JDBC_PARAM_SQLARG_NAMED_MAP, column = "parameters/parameter-interfaces-and-options/explicit-types")
    public void namedSqlArgParameters_shouldRetainTypesAndHandlerAlongsidePlainValues() throws SQLException {
        int id = baseId() + 3;
        Map<String, Object> params = new HashMap<>();
        params.put("id", SqlArg.valueOf(id, Types.INTEGER));
        params.put("name", SqlArg.valueOf("NXN-Param-SqlArg-Map", String.class));
        params.put("age", 30);
        params.put("email", new SqlArg("nxn-param-sqlarg-map@test.com", Types.VARCHAR, new ExplicitVarcharTypeHandler()));
        params.put("createTime", new Date());
        writeParameters(command(JdbcParameterCommand.INSERT_BRACE), params);

        assertEquals("NXN-PARAM-SQLARG-MAP@TEST.COM", readEmail(command(JdbcParameterCommand.SELECT_EMAIL_BY_ID), new Object[] { id }));
        assertEquals("NXN-PARAM-SQLARG-MAP@TEST.COM", readEmail(command(JdbcParameterCommand.SELECT_EMAIL_BY_NAME), new Object[] { "NXN-Param-SqlArg-Map" }));
    }

    // 能力归属：参数传递 / 接口方式与参数选项 / 花括号中的 javaType、jdbcType 与 typeHandler。
    @Test
    @Capability(value = CapabilityId.JDBC_PARAM_BRACE_OPTIONS, column = "parameters/parameter-interfaces-and-options/explicit-types")
    public void braceParameterOptions_shouldApplyJavaJdbcAndHandlerOptions() throws SQLException {
        int id = baseId() + 3;
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("name", "NXN-Param-Brace-Options");
        params.put("age", 30);
        params.put("email", "nxn-param-brace-options@test.com");
        params.put("createTime", new Date());
        String insert = command(JdbcParameterCommand.INSERT_BRACE).replace("#{id}", "#{id,javaType=java.lang.Integer,jdbcType=integer}").replace("#{name}", "#{name,javaType=java.lang.String,jdbcType=12}").replace("#{email}", "#{email,javaType=java.lang.String,jdbcType=varchar,typeHandler=" + ExplicitVarcharTypeHandler.class.getName() + "}");
        writeParameters(insert, params);

        assertEquals("NXN-PARAM-BRACE-OPTIONS@TEST.COM", readEmail(command(JdbcParameterCommand.SELECT_EMAIL_BY_ID), new Object[] { id }));
        assertEquals("NXN-PARAM-BRACE-OPTIONS@TEST.COM", readEmail(command(JdbcParameterCommand.SELECT_EMAIL_BY_NAME), new Object[] { "NXN-Param-Brace-Options" }));
    }
}
