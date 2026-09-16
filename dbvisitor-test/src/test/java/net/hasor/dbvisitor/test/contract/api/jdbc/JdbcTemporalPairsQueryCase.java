/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class JdbcTemporalPairsQueryCase extends JdbcQuerySupport {
    // 能力归属：编程式 API / 查询键值对。
    @Test
    @Capability(value = CapabilityId.JDBC_QUERY_TEMPORAL_PAIRS, column = "jdbc/queries/pairs-queries")
    public void jdbcQueryForPairs_shouldConvertLongKeysAndDateValues() throws SQLException {
        seedUsers();
        Map<Long, Date> idToDate = jdbcTemplate.queryForPairs(//
                selectRange("id, create_time", "?", "?", false), //
                Long.class, Date.class, new Object[] { baseId() + 1, baseId() + 2 });

        assertEquals(2, idToDate.size());
        assertNotNull(idToDate.get((long) baseId() + 1));
        assertNotNull(idToDate.get((long) baseId() + 2));
    }
}
