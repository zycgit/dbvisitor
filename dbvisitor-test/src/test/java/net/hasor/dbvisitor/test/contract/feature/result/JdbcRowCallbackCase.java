/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.result;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcResultHandlingSupport;
import net.hasor.dbvisitor.test.contract.material.handler.ResultHandlerProbe;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
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

    // 能力归属：结果接收 / rowcallbackhandler / jdbc。
    @Test
    @Capability(value = CapabilityId.JDBC_RESULT_ROW_CALLBACK, column = "results/rowcallbackhandler/callback", variants = { "jdbc" })
    public void rowCallbackHandler_shouldStreamRows() throws SQLException {
        seedUsers();
        AtomicInteger totalAge = new AtomicInteger();
        AtomicInteger count = new AtomicInteger();
        List<String> names = new ArrayList<>();

        RowCallbackHandler handler = (rs, rowNum) -> {
            ResultHandlerProbe.record(rs);
            assertEquals(count.get(), rowNum);
            names.add(rs.getString(callbackNameColumn()));
            totalAge.addAndGet(rs.getInt(callbackNumberColumn()));
            count.incrementAndGet();
        };

        ResultHandlerProbe.verify(5, () -> {
            jdbcTemplate.query(selectSql("*", "id BETWEEN ? AND ?", true), callbackArguments(), handler);
            return null;
        });

        assertEquals(5, count.get());
        assertEquals(115, totalAge.get());
        assertEquals("NXN-Result-1", names.get(0));
        for (int i = 0; i < names.size(); i++) {
            assertEquals("NXN-Result-" + (i + 1), names.get(i));
        }
        ResultHandlerProbe.verify(0, () -> {
            jdbcTemplate.query(emptyResultSql(), emptyResultArguments(), handler);
            return null;
        });
        assertEquals(5, count.get());
        ResultHandlerProbe.verifyFailure(() -> {
            jdbcTemplate.query(selectSql("*", "id BETWEEN ? AND ?", true), callbackArguments(), handler);
            return null;
        });
    }
}
