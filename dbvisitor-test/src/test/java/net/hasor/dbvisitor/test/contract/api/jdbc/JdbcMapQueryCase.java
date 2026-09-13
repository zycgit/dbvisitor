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
public abstract class JdbcMapQueryCase extends JdbcQuerySupport {
    protected Map<String, Object> expectedMapRow(int offset) {
        return Map.of("id", baseId() + offset, "name", "NXN-JDBC-Query-" + offset, "age", 60 + offset);
    }

    protected Object[] mapSingleArguments() {
        return new Object[] { baseId() + 1 };
    }

    protected Object[] mapRangeArguments() {
        return new Object[] { baseId() + 1, baseId() + 3 };
    }

    private void assertMapRow(int offset, Map<String, Object> row) {
        for (Map.Entry<String, Object> expected : expectedMapRow(offset).entrySet()) {
            Object actual = value(row, expected.getKey());
            if (expected.getValue() instanceof Integer) {
                assertNumericField((Integer) expected.getValue(), actual);
            } else {
                assertEquals(expected.getValue(), actual);
            }
        }
    }

    @Test
    @Capability(CapabilityId.JDBC_QUERY_MAP)
    public void jdbcQueryForMap_shouldReturnOneRowAsMap() throws SQLException {
        seedUsers();

        Map<String, Object> row = jdbcTemplate.queryForMap(selectById("id, name, age", "?"), mapSingleArguments());

        assertMapRow(1, row);
    }

    @Test
    @Capability(CapabilityId.JDBC_QUERY_LIST)
    public void jdbcQueryForList_shouldReturnRowsAsMaps() throws SQLException {
        seedUsers();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectRange("id, name, age", "?", "?", true), //
                mapRangeArguments());

        assertEquals(3, rows.size());
        for (int i = 0; i < rows.size(); i++) {
            assertMapRow(i + 1, rows.get(i));
        }
    }
}
