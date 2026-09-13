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
public abstract class JdbcFragmentParameterCase extends JdbcParameterSupport {
    @Test
    @Capability(CapabilityId.JDBC_PARAM_TEXT_FRAGMENT)
    public void textReplacementParameters_shouldInjectSqlIdentifiersAndOrderClauses() throws SQLException {
        insert(baseId() + 8, "NXN-Param-Text-1", 20, "nxn-param-text-1@test.com");
        insert(baseId() + 9, "NXN-Param-Text-2", 34, "nxn-param-text-2@test.com");
        prepareFragmentOrder();

        Map<String, Object> params = new HashMap<>();
        params.put("tableName", fixtureTable());
        params.put("column", fixtureColumn("name"));
        params.put("name", "NXN-Param-Text-2");
        params.put("orderBy", orderFragment());

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(//
                command(JdbcParameterCommand.SELECT_TEXT_ORDER), params);
        Long count = jdbcTemplate.queryForObject(command(JdbcParameterCommand.COUNT_TEXT_COLUMN), params, Long.class);

        assertEquals(Long.valueOf(1), count);
        assertEquals(2, rows.size());
        assertNumericField(34, value(rows.get(0), "age"));
    }
}
