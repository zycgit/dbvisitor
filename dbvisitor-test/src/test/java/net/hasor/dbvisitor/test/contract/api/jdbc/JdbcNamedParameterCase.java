/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcNamedParameterCase extends JdbcParameterSupport {
    @Test
    @Capability(CapabilityId.JDBC_PARAM_NAMED_COLON)
    public void namedColonParameters_shouldBindMapAndBeanValues() throws SQLException {
        int id = baseId() + 5;
        Map<String, Object> insertParams = new HashMap<>();
        insertParams.put("id", id);
        insertParams.put("name", "NXN-Param-Colon");
        insertParams.put("age", 31);
        insertParams.put("email", "nxn-param-colon@test.com");
        insertParams.put("createTime", new Date());
        writeParameters(command(JdbcParameterCommand.INSERT_COLON), insertParams);

        UserInfo queryBean = new UserInfo();
        queryBean.setName("NXN-Param-Colon");
        queryBean.setAge(30);
        Long count = jdbcTemplate.queryForObject(command(JdbcParameterCommand.COUNT_BY_NAME_AGE), queryBean, Long.class);

        assertEquals(Long.valueOf(1), count);
    }

    @Test
    @Capability(CapabilityId.JDBC_PARAM_NAMED_BRACE)
    public void namedBraceParameters_shouldBindMapValues() throws SQLException {
        int id = baseId() + 6;
        Map<String, Object> insertParams = new HashMap<>();
        insertParams.put("id", id);
        insertParams.put("name", "NXN-Param-Brace");
        insertParams.put("age", 32);
        insertParams.put("email", "nxn-param-brace@test.com");
        insertParams.put("createTime", new Date());
        writeParameters(command(JdbcParameterCommand.INSERT_BRACE), insertParams);

        Long count = jdbcTemplate.queryForObject(command(JdbcParameterCommand.COUNT_BY_ID_NAME), insertParams, Long.class);

        assertEquals(Long.valueOf(1), count);
    }

    @Test
    @Capability(CapabilityId.JDBC_PARAM_OGNL)
    public void namedParameters_shouldResolveOgnlNestedAndIndexedValues() throws SQLException {
        int id = baseId() + 7;
        insert(id, "NXN-Param-Ognl", 33, "nxn-param-ognl@test.com");

        Map<String, Object> user = new HashMap<>();
        user.put("name", "NXN-Param-Ognl");
        user.put("info", new HashMap<String, Object>() {{
            put("age", 33);
        }});
        Map<String, Object> params = new HashMap<>();
        params.put("user", user);
        params.put("names", new String[] { "NXN-Param-Ognl", "Other" });
        params.put("ids", Arrays.asList(id, id + 1));

        Long count = jdbcTemplate.queryForObject(command(JdbcParameterCommand.COUNT_BY_NESTED), params, Long.class);

        assertEquals(Long.valueOf(1), count);
    }
}
