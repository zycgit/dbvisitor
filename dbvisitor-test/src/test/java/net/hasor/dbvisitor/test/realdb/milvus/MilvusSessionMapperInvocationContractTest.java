/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.util.List;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus1Mapper;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus3;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus3Mapper;
import org.junit.Test;
import static org.junit.Assert.*;

/** Annotation and XML proxy invocation, mutation results, lists and scalars. */
public class MilvusSessionMapperInvocationContractTest extends MilvusSessionMapperSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SESSION_MAPPER_SIMPLE)
    public void annotationProxyShouldReturnMutationListAndScalarResults() throws Exception {
        UserInfoMilvus1Mapper mapper = this.session.createMapper(UserInfoMilvus1Mapper.class);
        assertNotNull(mapper);
        assertEquals(1, mapper.insertUser(user("first", "First")));
        assertEquals(1, mapper.insertUser(user("second", "Second")));
        assertEquals("First", mapper.selectUser("first").getName());
        assertEquals(2, mapper.queryAll().size());
        assertEquals(2, mapper.countAll());
        assertEquals(1, mapper.updateUser("first", "Updated", "new-login"));
        assertEquals("Updated", mapper.selectUser("first").getName());
        assertEquals("new-login", mapper.selectUser("first").getLoginName());
        assertEquals(1, mapper.deleteUser("second"));
        assertNull(mapper.selectUser("second"));
        assertEquals(1, mapper.countAll());
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SESSION_MAPPER_REF)
    public void xmlProxyShouldReturnMutationListAndScalarResults() throws Exception {
        UserInfoMilvus3Mapper mapper = this.session.createMapper(UserInfoMilvus3Mapper.class);
        assertNotNull(mapper);
        UserInfoMilvus3 first = xmlUser("first", "First");
        assertEquals(1, mapper.insertUser(first));
        assertEquals(1, mapper.insertUser(xmlUser("second", "Second")));
        assertEquals("First", mapper.selectUser("first").getName());
        assertEquals(2, mapper.queryAll().size());
        assertEquals(2, mapper.countAll());
        assertEquals(1, mapper.updateName("first", "Updated"));
        assertEquals("Updated", mapper.selectUser("first").getName());
        assertEquals(first.getV(), mapper.selectUser("first").getV());
        assertEquals(1, mapper.deleteUser("second"));
        assertEquals(1, mapper.countAll());
        assertNull(mapper.selectUser("second"));
    }

    private UserInfoMilvus3 xmlUser(String id, String name) {
        UserInfoMilvus3 user = new UserInfoMilvus3();
        user.setUid(id);
        user.setName(name);
        user.setLoginName("login");
        user.setLoginPassword("password");
        user.setV(List.of(1F, 0F));
        return user;
    }
}
