/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcPairsQueryCase extends JdbcQuerySupport {
    @Test
    @Capability(CapabilityId.JDBC_QUERY_PAIRS)
    public void jdbcQueryForPairs_shouldConvertNumericKeysAndStringValues() throws SQLException {
        seedUsers();

        Map<Integer, String> idToName = jdbcTemplate.queryForPairs(//
                selectRange("id, name", "?", "?", false), //
                Integer.class, String.class, new Object[] { baseId() + 1, baseId() + 3 });

        assertEquals(3, idToName.size());
        assertEquals("NXN-JDBC-Query-1", idToName.get(baseId() + 1));
        for (int i = 1; i <= 3; i++) {
            assertEquals("NXN-JDBC-Query-" + i, idToName.get(baseId() + i));
        }
    }

    @Test
    @Capability(CapabilityId.JDBC_QUERY_STRING_PAIRS)
    public void jdbcQueryForPairs_shouldConvertStringKeysAndNumericValues() throws SQLException {
        seedUsers();
        Map<String, Integer> nameToAge = jdbcTemplate.queryForPairs(//
                selectRange("name, age", ":minId", ":maxId", false), //
                String.class, Integer.class, params(baseId() + 1, baseId() + 3));

        assertEquals(Integer.valueOf(62), nameToAge.get("NXN-JDBC-Query-2"));
        assertEquals(3, nameToAge.size());
        for (int i = 1; i <= 3; i++) {
            assertEquals(Integer.valueOf(60 + i), nameToAge.get("NXN-JDBC-Query-" + i));
        }
    }
}
