/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.session;

import org.junit.Test;

import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.api.adapter.AdapterContractTest;
import net.hasor.dbvisitor.test.contract.material.dao.SessionRefUserMapper;
import net.hasor.dbvisitor.test.contract.material.dao.SessionUserMapper;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class SessionMapperRegistrationContractTest extends AdapterContractTest {
    @Test
    @Capability(CapabilityId.SESSION_MAPPER_REGISTER_SIMPLE)
    public void sessionCreateMapper_shouldRegisterAndCreateAnnotationMapper() throws Exception {
        try (Session session = newConfiguration().newSession(newAdapterConnection())) {
            session.getConfiguration().loadMapper(SessionUserMapper.class);
            assertNotNull(session.createMapper(SessionUserMapper.class));
            assertNotNull(session.createMapper(SessionUserMapper.class));
        }
    }

    @Test
    @Capability(CapabilityId.SESSION_MAPPER_REGISTER_REF)
    public void sessionCreateMapper_shouldRegisterAndCreateXmlReferencedMapper() throws Exception {
        try (Session session = newConfiguration().newSession(newAdapterConnection())) {
            session.getConfiguration().loadMapper(SessionRefUserMapper.class);
            assertNotNull(session.createMapper(SessionRefUserMapper.class));
            assertNotNull(session.createMapper(SessionRefUserMapper.class));
        }
    }

    @Test
    @Capability(CapabilityId.SESSION_MAPPER_INVALID)
    public void sessionCreateMapper_shouldRejectInterfacesWithoutMapperAnnotation() throws Exception {
        try (Session session = newConfiguration().newSession(newAdapterConnection())) {
            try {
                session.createMapper(Runnable.class);
            } catch (Exception e) {
                assertNotNull(e.getMessage());
                return;
            }
            throw new AssertionError("Expected plain interface mapper creation to fail.");
        }
    }
}
