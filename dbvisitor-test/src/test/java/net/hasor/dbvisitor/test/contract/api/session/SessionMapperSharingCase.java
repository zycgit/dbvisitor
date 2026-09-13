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
import net.hasor.dbvisitor.test.contract.material.dao.DeclarativeOrderMapper;
import net.hasor.dbvisitor.test.contract.material.dao.SessionUserMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class SessionMapperSharingCase extends SessionMapperSupport {
    @Test
    @Capability(CapabilityId.SESSION_MAPPER_DECLARATIVE)
    public void sessionCreateMapper_shouldReuseDeclarativeOrderMapperInSameSession() throws Exception {
        Session session = createSession();
        BaseMapper<UserInfo> userMapper = session.createBaseMapper(UserInfo.class);
        DeclarativeOrderMapper orderMapper = session.createMapper(DeclarativeOrderMapper.class);
        int userId = baseId() + 30;

        assertEquals(1, userMapper.insert(user(userId, "OrderOwner", 35, "owner@nxn.test")));
        assertEquals(1, orderMapper.insertOrder(order(null, userId, "ORD-DECL-" + userId, "150.00")));

        Integer orderCount = session.jdbc().queryForObject("SELECT COUNT(*) FROM user_order WHERE user_id = ?", new Object[] { userId }, Integer.class);
        assertEquals(Integer.valueOf(1), orderCount);
        assertEquals(1, orderMapper.countAll());
    }

    @Test
    @Capability(CapabilityId.SESSION_MAPPER_MIXED)
    public void sessionCreateMapper_shouldAllowMapperTypesAndBaseMapperToShareOneSession() throws Exception {
        Session session = createSession();
        BaseMapper<UserInfo> baseMapper = session.createBaseMapper(UserInfo.class);
        SessionUserMapper userMapper = session.createMapper(SessionUserMapper.class);
        DeclarativeOrderMapper orderMapper = session.createMapper(DeclarativeOrderMapper.class);
        int userId = baseId() + 40;

        assertEquals(1, baseMapper.insert(user(userId, "MixedUser", 28, "mixed@nxn.test")));
        assertEquals("MixedUser", userMapper.selectById(userId).getName());

        assertEquals(1, userMapper.updateUser(user(userId, "MixedUpdated", 29, "mixed@nxn.test")));
        assertEquals("MixedUpdated", baseMapper.selectById(userId).getName());

        assertEquals(1, orderMapper.insertOrder(order(null, userId, "ORD-MIXED-" + userId, "75.00")));
        assertEquals(1, userMapper.countAll());
        assertEquals(1, orderMapper.countAll());
    }
}
