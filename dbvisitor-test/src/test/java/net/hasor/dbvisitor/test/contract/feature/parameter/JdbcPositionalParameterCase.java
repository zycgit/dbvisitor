/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.parameter;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterCommand;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterSupport;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class JdbcPositionalParameterCase extends JdbcParameterSupport {
    protected Class<?> positionalBeanType() {
        return UserInfo.class;
    }

    protected Object[] positionalQueryArguments() {
        return new Object[] { positionalName(), 20 };
    }

    protected String positionalName() {
        return "NXN-Param-Array";
    }

    protected Integer positionalId(Object bean) {
        return ((UserInfo) bean).getId();
    }

    protected Integer positionalAge(Object bean) {
        return ((UserInfo) bean).getAge();
    }

    protected String positionalEmailColumn() {
        return "email";
    }

    // 能力归属：参数传递 / 位置参数与名称参数 / 位置参数。
    @Test
    @Capability(value = CapabilityId.JDBC_PARAM_POSITIONAL_ARRAY, column = "parameters/positional-and-named-parameters/positional")
    public void positionalArrayParameters_shouldBindObjectArray() throws SQLException {
        int id = baseId() + 1;
        insert(id, positionalName(), 25, "nxn-param-array@test.com");

        // Parameter binding does not require conversion of unrelated temporal fixture columns.
        Object user = jdbcTemplate.queryForObject(command(JdbcParameterCommand.SELECT_USER), //
                positionalQueryArguments(), positionalBeanType());

        assertNotNull(user);
        assertEquals(Integer.valueOf(id), positionalId(user));
        assertEquals(Integer.valueOf(25), positionalAge(user));
    }

    // 能力归属：参数传递 / 位置参数与名称参数 / List 位置参数。
    @Test
    @Capability(value = CapabilityId.JDBC_PARAM_POSITIONAL_LIST, column = "parameters/positional-and-named-parameters/positional")
    public void positionalListParameters_shouldBindValuesInListOrder() throws SQLException {
        int id = baseId() + 1;
        insert(id, positionalName(), 25, "nxn-param-list@test.com");

        Object user = jdbcTemplate.queryForObject(command(JdbcParameterCommand.SELECT_USER), Arrays.asList(positionalQueryArguments()), positionalBeanType());

        assertNotNull(user);
        assertEquals(Integer.valueOf(id), positionalId(user));
        assertEquals(Integer.valueOf(25), positionalAge(user));
    }

    // 能力归属：参数传递 / 位置参数与名称参数 / 省略容器的单个位置参数。
    @Test
    @Capability(value = CapabilityId.JDBC_PARAM_POSITIONAL_SINGLE, column = "parameters/positional-and-named-parameters/positional")
    public void singlePositionalParameter_shouldBindScalarWithoutArray() throws SQLException {
        int id = baseId() + 1;
        insert(id, positionalName(), 25, "nxn-param-single@test.com");

        Map<String, Object> row = jdbcTemplate.queryForMap(command(JdbcParameterCommand.SELECT_EMAIL_BY_ID), id);

        assertEquals("nxn-param-single@test.com", value(row, positionalEmailColumn()));
    }
}
