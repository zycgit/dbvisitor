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
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcRuleParameterCase extends JdbcParameterSupport {
    @Test
    @Capability(CapabilityId.JDBC_PARAM_RULE_AND_IN_SET)
    public void ruleParameters_shouldExpandAndInClauses() throws SQLException {
        insert(baseId() + 10, "NXN-Param-Rule-1", 24, "nxn-param-rule-1@test.com");
        insert(baseId() + 11, "NXN-Param-Rule-2", 36, "nxn-param-rule-2@test.com");

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("minAge", 20);
        queryParams.put("names", Arrays.asList("NXN-Param-Rule-1", "NXN-Param-Rule-2"));
        Long count = jdbcTemplate.queryForObject(//
                command(JdbcParameterCommand.COUNT_RULE_NAMES), queryParams, Long.class);

        assertEquals(Long.valueOf(2), count);
    }

    @Test
    @Capability(CapabilityId.JDBC_PARAM_RULE_SET)
    public void ruleParameters_shouldExpandSetClauses() throws SQLException {
        insert(baseId() + 10, "NXN-Param-Rule-1", 24, "nxn-param-rule-1@test.com");

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("id", baseId() + 10);
        updateParams.put("age", 25);
        updateParams.put("email", null);
        int updated = jdbcTemplate.executeUpdate(command(JdbcParameterCommand.UPDATE_RULE_FIELDS), updateParams);
        Integer newAge = jdbcTemplate.queryForObject(command(JdbcParameterCommand.SELECT_AGE), new Object[] { baseId() + 10 }, Integer.class);

        assertEquals(1, updated);
        assertEquals(Integer.valueOf(25), newAge);
    }
}
