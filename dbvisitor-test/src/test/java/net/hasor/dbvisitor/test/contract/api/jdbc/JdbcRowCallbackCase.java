/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class JdbcRowCallbackCase extends JdbcResultHandlingSupport {
    @Test
    @Capability(CapabilityId.JDBC_RESULT_ROW_CALLBACK)
    public void rowCallbackHandler_shouldStreamRows() throws SQLException {
        seedUsers();
        AtomicInteger totalAge = new AtomicInteger();
        AtomicInteger count = new AtomicInteger();
        List<String> names = new ArrayList<>();

        RowCallbackHandler handler = (rs, rowNum) -> {
            names.add(rs.getString("name"));
            totalAge.addAndGet(rs.getInt("age"));
            count.incrementAndGet();
        };

        jdbcTemplate.query(selectSql("*", "id BETWEEN ? AND ?", true), new Object[] { baseId() + 1, baseId() + 5 }, handler);

        assertEquals(5, count.get());
        assertEquals(115, totalAge.get());
        assertEquals("NXN-Result-1", names.get(0));
    }
}
