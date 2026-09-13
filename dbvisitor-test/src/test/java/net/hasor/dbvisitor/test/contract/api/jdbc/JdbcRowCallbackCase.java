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
    protected String callbackNameColumn() {
        return "name";
    }

    protected String callbackNumberColumn() {
        return "age";
    }

    protected Object[] callbackArguments() {
        return new Object[] { baseId() + 1, baseId() + 5 };
    }

    @Test
    @Capability(CapabilityId.JDBC_RESULT_ROW_CALLBACK)
    public void rowCallbackHandler_shouldStreamRows() throws SQLException {
        seedUsers();
        AtomicInteger totalAge = new AtomicInteger();
        AtomicInteger count = new AtomicInteger();
        List<String> names = new ArrayList<>();

        RowCallbackHandler handler = (rs, rowNum) -> {
            assertEquals(count.get(), rowNum);
            names.add(rs.getString(callbackNameColumn()));
            totalAge.addAndGet(rs.getInt(callbackNumberColumn()));
            count.incrementAndGet();
        };

        jdbcTemplate.query(selectSql("*", "id BETWEEN ? AND ?", true), callbackArguments(), handler);

        assertEquals(5, count.get());
        assertEquals(115, totalAge.get());
        assertEquals("NXN-Result-1", names.get(0));
        for (int i = 0; i < names.size(); i++) {
            assertEquals("NXN-Result-" + (i + 1), names.get(i));
        }
    }
}
