/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

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
public abstract class LambdaRowCallbackCase extends LambdaResultHandlingSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_ROW_CALLBACK)
    public void lambdaResult_shouldStreamRowsThroughRowCallbackHandler() throws SQLException {
        insertUsers("LRCallback", new int[] { 21, 22, 23, 24, 25 }, baseId() + 80);

        List<String> names = new ArrayList<>();
        AtomicInteger totalAge = new AtomicInteger();
        AtomicInteger count = new AtomicInteger();
        RowCallbackHandler handler = (rs, rowNum) -> {
            names.add(rs.getString("name"));
            totalAge.addAndGet(rs.getInt("age"));
            count.incrementAndGet();
        };

        orderRows(queryRows("LRCallback")//
                .between("id", baseId() + 80, baseId() + 84), "id")//
                .query(handler);

        assertEquals(5, count.get());
        assertEquals(115, totalAge.get());
        assertEquals("LRCallback1", names.get(0));
    }
}
