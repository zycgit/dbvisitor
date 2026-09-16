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
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaResultHandlingSupport;
import net.hasor.dbvisitor.test.contract.material.handler.ResultHandlerProbe;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaRowCallbackCase extends LambdaResultHandlingSupport {
    // 能力归属：结果接收 / rowcallbackhandler / builder。
    @Test
    @Capability(value = CapabilityId.LAMBDA_RESULT_ROW_CALLBACK, column = "results/rowcallbackhandler/callback", variants = { "builder" })
    public void lambdaResult_shouldStreamRowsThroughRowCallbackHandler() throws SQLException {
        insertUsers("LRCallback", new int[] { 21, 22, 23, 24, 25 }, baseId() + 80);

        List<String> names = new ArrayList<>();
        AtomicInteger totalAge = new AtomicInteger();
        AtomicInteger count = new AtomicInteger();
        RowCallbackHandler handler = (rs, rowNum) -> {
            ResultHandlerProbe.record(rs);
            assertEquals(count.get(), rowNum);
            names.add(rs.getString("name"));
            totalAge.addAndGet(rs.getInt("age"));
            count.incrementAndGet();
        };

        ResultHandlerProbe.verify(5, () -> {
            orderRows(queryRows("LRCallback")//
                    .between("id", baseId() + 80, baseId() + 84), "id")//
                    .query(handler);
            return null;
        });

        assertEquals(5, count.get());
        assertEquals(115, totalAge.get());
        assertEquals("LRCallback1", names.get(0));
        assertEquals(List.of("LRCallback1", "LRCallback2", "LRCallback3", "LRCallback4", "LRCallback5"), names);
        ResultHandlerProbe.verify(0, () -> {
            queryRows("LRMissing").query(handler);
            return null;
        });
        assertEquals(5, count.get());
        ResultHandlerProbe.verifyFailure(() -> {
            queryRows("LRCallback").query(handler);
            return null;
        });
    }
}
