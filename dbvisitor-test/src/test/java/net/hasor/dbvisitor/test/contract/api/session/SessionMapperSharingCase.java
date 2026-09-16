/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.session;

import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.SessionRefCrudMapper;
import net.hasor.dbvisitor.test.contract.material.dao.SessionUserMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class SessionMapperSharingCase extends SessionMapperSupport {
    // 能力归属：Mapper API / Mapper 读写。
    @Test
    @Capability(value = CapabilityId.SESSION_MAPPER_DECLARATIVE, column = "mapper/base-mapper/operations")
    public void sessionCreateMapper_shouldShareDataBetweenDeclarativeMapperAndSession() throws Exception {
        Session session = createSession();
        BaseMapper<UserInfo> userMapper = session.createBaseMapper(UserInfo.class);
        SessionUserMapper mapper = simpleMapper(session);
        int userId = baseId() + 30;

        assertEquals(1, userMapper.insert(user(userId, "BaseUser", 35, "owner@nxn.test")));
        assertEquals("BaseUser", mapper.selectById(userId).getName());
        assertEquals(1, mapper.insertUser(user(userId + 1, "MapperUser", 36, "mapper@nxn.test")));
        assertEquals("MapperUser", userMapper.selectById(userId + 1).getName());
        assertEquals(Integer.valueOf(2), session.jdbc().queryForObject(countUsersCommand(), Integer.class));
        assertEquals(2, mapper.countAll());
    }

    // 能力归属：Mapper API / Mapper 读写。
    @Test
    @Capability(value = CapabilityId.SESSION_MAPPER_MIXED, column = "mapper/base-mapper/operations")
    public void sessionCreateMapper_shouldAllowMapperTypesAndBaseMapperToShareOneSession() throws Exception {
        Session session = createSession();
        BaseMapper<UserInfo> baseMapper = session.createBaseMapper(UserInfo.class);
        SessionUserMapper userMapper = simpleMapper(session);
        SessionRefCrudMapper xmlMapper = refMapper(session);
        int userId = baseId() + 40;

        assertEquals(1, baseMapper.insert(user(userId, "MixedUser", 28, "mixed@nxn.test")));
        assertEquals("MixedUser", userMapper.selectById(userId).getName());

        assertEquals(1, userMapper.updateUser(user(userId, "MixedUpdated", 29, "mixed@nxn.test")));
        assertEquals("MixedUpdated", baseMapper.selectById(userId).getName());

        assertEquals("MixedUpdated", xmlMapper.queryUserById(userId).getName());
        assertEquals(1, xmlMapper.queryAllUsers().size());
        assertEquals(1, userMapper.countAll());
        assertEquals(1, xmlMapper.countUsers());
        assertEquals(1, baseMapper.deleteById(userId));
        assertNull(userMapper.selectById(userId));
        assertTrue(xmlMapper.queryAllUsers().isEmpty());
        UnsupportedOperationException invalid = assertThrows(UnsupportedOperationException.class, () -> session.createMapper(Runnable.class));
        assertTrue(invalid.getMessage().contains("java.lang.Runnable"));
    }
}
