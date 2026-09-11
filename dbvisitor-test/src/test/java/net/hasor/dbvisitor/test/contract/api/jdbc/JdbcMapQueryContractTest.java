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
public abstract class JdbcMapQueryContractTest extends JdbcQuerySupport {
    @Test
    @Capability(CapabilityId.JDBC_QUERY_MAP)
    public void jdbcQueryForMap_shouldReturnOneRowAsMap() throws SQLException {
        seedUsers();

        Map<String, Object> row = jdbcTemplate.queryForMap(selectById("id, name, age", "?"), new Object[] { baseId() + 1 });

        assertNumericField(baseId() + 1, value(row, "id"));
        assertEquals("NXN-JDBC-Query-1", value(row, "name"));
        assertNumericField(61, value(row, "age"));
    }

    @Test
    @Capability(CapabilityId.JDBC_QUERY_LIST)
    public void jdbcQueryForList_shouldReturnRowsAsMaps() throws SQLException {
        seedUsers();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectRange("id, name, age", "?", "?", true), //
                new Object[] { baseId() + 1, baseId() + 3 });

        assertEquals(3, rows.size());
        for (int i = 0; i < rows.size(); i++) {
            assertNumericField(baseId() + i + 1, value(rows.get(i), "id"));
            assertEquals("NXN-JDBC-Query-" + (i + 1), value(rows.get(i), "name"));
            assertNumericField(61 + i, value(rows.get(i), "age"));
        }
    }
}
