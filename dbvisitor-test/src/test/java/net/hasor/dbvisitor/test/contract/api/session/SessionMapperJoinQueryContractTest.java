/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.session;

import org.junit.Test;

import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.SessionRefUserMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserOrder;
import net.hasor.dbvisitor.test.contract.material.model.UserOrderDTO;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class SessionMapperJoinQueryContractTest extends SessionMapperSupport {
    @Test
    @Capability(CapabilityId.SESSION_MAPPER_REF_JOIN)
    public void sessionCreateMapper_shouldLetRefMapperJoinWithBaseMapperDataInSameSession() throws Exception {
        Session session = createSession();
        SessionRefUserMapper mapper = session.createMapper(SessionRefUserMapper.class);
        BaseMapper<UserOrder> orderMapper = session.createBaseMapper(UserOrder.class);
        int id = baseId() + 20;

        assertEquals(1, mapper.insertUser(user(id, "RefJoinUser", 32, "refjoin@nxn.test")));
        assertEquals(1, orderMapper.insert(order(id, id, "ORD-REF-" + id, "299.99")));

        UserOrderDTO dto = mapper.queryOrderWithUser(id);
        assertNotNull(dto);
        assertEquals(Integer.valueOf(id), dto.getOrderId());
        assertEquals("ORD-REF-" + id, dto.getOrderNo());
        assertEquals("RefJoinUser", dto.getUserName());
        assertEquals("refjoin@nxn.test", dto.getUserEmail());
    }
}
