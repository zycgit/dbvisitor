/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.session;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserOrderDTO;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class SessionStatementJoinQueryCase extends SessionStatementSupport {
    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_JOIN)
    public void sessionStatement_shouldQueryJoinResultIntoDto() throws Exception {
        int id = baseId() + 30;
        insertUser(id, "StmtJoin", 30, "join@nxn.test");
        execute("insertOrder", orderParams(id, id, "ORD-STMT-" + id, "199.50"));

        List<UserOrderDTO> list = this.session.queryStatement(NS + ".queryOrderWithUser", mapOf("orderId", id));
        assertEquals(1, list.size());

        UserOrderDTO dto = list.get(0);
        assertEquals(Integer.valueOf(id), dto.getOrderId());
        assertEquals("ORD-STMT-" + id, dto.getOrderNo());
        assertEquals(0, new BigDecimal("199.50").compareTo(dto.getAmount()));
        assertEquals("StmtJoin", dto.getUserName());
        assertEquals("join@nxn.test", dto.getUserEmail());
    }
}
