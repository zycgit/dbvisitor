/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcTextParameterContractTest extends JdbcParameterSupport {
    @Test
    @Capability(CapabilityId.JDBC_PARAM_TEXT_REPLACEMENT)
    public void textReplacementParameters_shouldCombineIdentifiersAndBoundValues() throws SQLException {
        String name = "NXN-Param-Reader's text";
        insert(baseId() + 8, name, 34, "nxn-param-text@test.com");
        insert(baseId() + 9, "NXN-Param-Other", 20, "nxn-param-other@test.com");

        Map<String, Object> params = new HashMap<>();
        // Only trusted fixture identifiers are substituted; values remain bound parameters.
        params.put("tableName", fixtureTable());
        params.put("column", fixtureColumn("name"));
        params.put("name", name);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                command(JdbcParameterCommand.SELECT_TEXT_VALUE), params);
        Long count = jdbcTemplate.queryForObject(
                command(JdbcParameterCommand.COUNT_TEXT_VALUE), params, Long.class);

        assertEquals(Long.valueOf(1), count);
        assertEquals(1, rows.size());
        assertEquals(name, value(rows.get(0), "name"));
        assertNumericField(34, value(rows.get(0), "age"));
    }
}
