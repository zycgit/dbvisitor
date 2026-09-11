/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.session;

import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserOrder;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class SessionStatementCoordinationContractTest extends SessionStatementSupport {
    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_CROSS_TABLE)
    public void sessionStatement_shouldCoordinateCrossTableOperations() throws Exception {
        int userId = baseId() + 300;
        insertUser(userId, "StmtCross", 30, "cross@nxn.test");
        execute("insertOrder", orderParams(userId + 1, userId, "ORD-" + (userId + 1), "50.00"));
        execute("insertOrder", orderParams(userId + 2, userId, "ORD-" + (userId + 2), "100.00"));

        List<UserOrder> orders = this.session.queryStatement(NS + ".queryOrdersByUserId", mapOf("userId", userId));
        assertEquals(2, orders.size());

        assertEquals(1, execute("deleteOrderById", mapOf("id", userId + 1)));
        orders = this.session.queryStatement(NS + ".queryOrdersByUserId", mapOf("userId", userId));
        assertEquals(1, orders.size());
        assertEquals("ORD-" + (userId + 2), orders.get(0).getOrderNo());
    }
}
