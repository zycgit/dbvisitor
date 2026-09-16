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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterCommand;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcParameterSupport;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcNamedParameterCase extends JdbcParameterSupport {
    // 能力归属：参数传递与规则 / 通用规则 / 按条件生成原生命令并绑定参数。
    @Test
    @Capability(value = CapabilityId.JDBC_PARAM_RULE_CONDITIONAL_COMMAND, column = "parameters/command-rules/conditional-command")
    public void rules_shouldSelectNativeCommandAndBindOnlyActiveParameters() throws SQLException {
        int id = baseId() + 8;
        String name = "NXN-Param-Conditional";
        insert(id, name, 34, "conditional@nxn.test");

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("name", name);
        String query = command(JdbcParameterCommand.COUNT_BY_ID_NAME);
        String template = "@{if, enabled, " + query + "} @{if, !enabled, " + query + "}" + " @{if, false, INVALID_COMMAND #{missing.value}}";

        params.put("enabled", true);
        assertEquals(Long.valueOf(1), jdbcTemplate.queryForObject(template, params, Long.class));
        params.put("enabled", false);
        assertEquals(Long.valueOf(1), jdbcTemplate.queryForObject(template, params, Long.class));
        params.put("id", id + 1);
        assertEquals(Long.valueOf(0), jdbcTemplate.queryForObject(template, params, Long.class));
    }

    // 能力归属：参数传递与规则 / 通用规则 / CASE 分支与嵌套 IF 原生命令。
    @Test
    @Capability(value = CapabilityId.JDBC_PARAM_RULE_CASE, column = "parameters/command-rules/conditional-command")
    public void rules_shouldEvaluateOnlySelectedCaseAndNestedIfBranches() throws SQLException {
        int id = baseId() + 8;
        String name = "NXN-Param-Conditional";
        insert(id, name, 34, "conditional@nxn.test");

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("name", name);
        params.put("enabled", true);
        String query = command(JdbcParameterCommand.COUNT_BY_ID_NAME);
        String matched = "@{case, enabled, @{when, true, @{if, enabled, " + query + "}}" + " @{when, missing.value, INVALID_COMMAND} @{else, INVALID_COMMAND #{missing.value}}}";
        assertEquals(Long.valueOf(1), jdbcTemplate.queryForObject(matched, params, Long.class));

        params.put("enabled", false);
        String fallback = "@{case, , @{when, enabled, INVALID_COMMAND #{missing.value}}" + " @{else, @{if, !enabled, " + query + "}}}";
        assertEquals(Long.valueOf(1), jdbcTemplate.queryForObject(fallback, params, Long.class));
        params.put("id", id + 1);
        assertEquals(Long.valueOf(0), jdbcTemplate.queryForObject(fallback, params, Long.class));
    }

    // 能力归属：参数传递 / 位置参数与名称参数 / 名称参数。
    @Test
    @Capability(value = CapabilityId.JDBC_PARAM_NAMED_COLON, column = "parameters/positional-and-named-parameters/named")
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

    // 能力归属：参数传递 / 位置参数与名称参数 / 与号名称参数。
    @Test
    @Capability(value = CapabilityId.JDBC_PARAM_NAMED_AMPERSAND, column = "parameters/positional-and-named-parameters/named")
    public void namedAmpersandParameters_shouldBindMapAndBeanValues() throws SQLException {
        int id = baseId() + 5;
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("name", "NXN-Param-Colon");
        params.put("age", 31);
        params.put("email", "nxn-param-ampersand@test.com");
        params.put("createTime", new Date());
        String insert = command(JdbcParameterCommand.INSERT_COLON);
        for (String key : params.keySet()) {
            insert = insert.replace(":" + key, "&" + key);
        }
        writeParameters(insert, params);

        UserInfo bean = new UserInfo();
        bean.setName("NXN-Param-Colon");
        bean.setAge(30);
        String query = command(JdbcParameterCommand.COUNT_BY_NAME_AGE).replace(":name", "&name").replace(":age", "&age");
        assertEquals(Long.valueOf(1), jdbcTemplate.queryForObject(query, bean, Long.class));
        bean.setAge(40);
        assertEquals(Long.valueOf(0), jdbcTemplate.queryForObject(query, bean, Long.class));
    }

    // 能力归属：参数传递 / 位置参数与名称参数 / 名称参数。
    @Test
    @Capability(value = CapabilityId.JDBC_PARAM_NAMED_BRACE, column = "parameters/positional-and-named-parameters/named")
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

    // 能力归属：参数传递 / 位置参数与名称参数 / 名称参数。
    @Test
    @Capability(value = CapabilityId.JDBC_PARAM_OGNL, column = "parameters/positional-and-named-parameters/named")
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

        params.put("ids", Arrays.asList(id + 1, id));
        assertEquals(Long.valueOf(0), jdbcTemplate.queryForObject(command(JdbcParameterCommand.COUNT_BY_NESTED), params, Long.class));
        params.put("ids", Arrays.asList(id, id + 1));
        params.put("names", new String[] { "Other", "NXN-Param-Ognl" });
        assertEquals(Long.valueOf(0), jdbcTemplate.queryForObject(command(JdbcParameterCommand.COUNT_BY_NESTED), params, Long.class));
        params.put("names", new String[] { "NXN-Param-Ognl", "Other" });
        params.put("offset", 1);
        String expression = command(JdbcParameterCommand.COUNT_BY_NESTED).replace(":user.info.age", "#{user.info.age + offset}");
        assertEquals(Long.valueOf(0), jdbcTemplate.queryForObject(expression, params, Long.class));
        params.put("offset", 0);
        assertEquals(Long.valueOf(1), jdbcTemplate.queryForObject(expression, params, Long.class));
    }
}
