/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.session;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.dao.SessionRefUserMapper;
import net.hasor.dbvisitor.test.contract.material.dao.SessionUserMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class SessionMapperInvocationCase extends SessionMapperSupport {
    @Test
    @Capability(CapabilityId.SESSION_MAPPER_SIMPLE)
    public void sessionCreateMapper_shouldCreateSimpleMapperProxyForCrudListAndScalarResults() throws Exception {
        SessionUserMapper mapper = createSession().createMapper(SessionUserMapper.class);
        int firstId = baseId() + 1;
        int secondId = baseId() + 2;

        assertNotNull(mapper);
        assertEquals(1, mapper.insertUser(user(firstId, "SimpleOne", 25, "simple1@nxn.test")));
        assertEquals(1, mapper.insertUser(user(secondId, "SimpleTwo", 26, "simple2@nxn.test")));

        assertEquals("SimpleOne", mapper.selectById(firstId).getName());
        assertEquals(2, mapper.selectAll().size());
        assertEquals(2, mapper.countAll());

        UserInfo update = user(firstId, "SimpleUpdated", 27, "simple-updated@nxn.test");
        assertEquals(1, mapper.updateUser(update));
        assertEquals("SimpleUpdated", mapper.selectById(firstId).getName());

        assertEquals(1, mapper.deleteById(secondId));
        assertNull(mapper.selectById(secondId));
    }

    @Test
    @Capability(CapabilityId.SESSION_MAPPER_REF)
    public void sessionCreateMapper_shouldCreateRefMapperProxyForXmlCrudListAndScalarResults() throws Exception {
        SessionRefUserMapper mapper = createSession().createMapper(SessionRefUserMapper.class);
        int firstId = baseId() + 10;
        int secondId = baseId() + 11;

        assertNotNull(mapper);
        assertEquals(1, mapper.insertUser(user(firstId, "RefOne", 30, "ref1@nxn.test")));
        assertEquals(1, mapper.insertUser(user(secondId, "RefTwo", 31, "ref2@nxn.test")));

        assertEquals("RefOne", mapper.queryUserById(firstId).getName());
        assertEquals(2, mapper.queryAllUsers().size());
        assertEquals(2, mapper.countUsers());

        assertEquals(1, mapper.updateUserEmail(firstId, "ref-new@nxn.test"));
        assertEquals("ref-new@nxn.test", mapper.queryUserById(firstId).getEmail());

        assertEquals(1, mapper.deleteUserById(secondId));
        assertEquals(1, mapper.countUsers());
    }
}
