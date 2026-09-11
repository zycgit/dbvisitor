/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcMixedResultAccessContractTest extends JdbcResultHandlingSupport {
    @Test
    @Capability(CapabilityId.JDBC_RESULT_MAP_AND_SCALAR)
    public void resultShortcuts_shouldReturnMapListAndScalarValues() throws SQLException {
        seedUsers();

        Map<String, Object> row = jdbcTemplate.queryForMap(selectSql("id, name, age", "id = ?", false), new Object[] { baseId() + 4 });
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql("id, name, age", "age BETWEEN ? AND ?", true), new Object[] { 23, 26 });
        Integer age = jdbcTemplate.queryForObject(selectSql("age", "id = ?", false), new Object[] { baseId() + 5 }, Integer.class);
        List<Integer> ages = jdbcTemplate.queryForList(selectSql("age", "id BETWEEN ? AND ?", true), new Object[] { baseId() + 6, baseId() + 8 }, Integer.class);

        assertEquals(baseId() + 4, ((Number) value(row, "id")).intValue());
        assertEquals("NXN-Result-4", value(row, "name"));
        assertEquals(4, rows.size());
        assertEquals(Integer.valueOf(25), age);
        assertEquals(3, ages.size());
        assertEquals(Integer.valueOf(26), ages.get(0));
    }
}
