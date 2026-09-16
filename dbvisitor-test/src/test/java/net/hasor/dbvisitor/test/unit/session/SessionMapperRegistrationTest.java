/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.unit.session;

import net.hasor.dbvisitor.dialect.provider.H2Dialect;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.SessionRefUserMapper;
import net.hasor.dbvisitor.test.contract.material.dao.SessionUserMapper;
import net.hasor.dbvisitor.test.unit.support.NoDatabaseConnection;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class SessionMapperRegistrationTest {
    @Test
    public void sessionCreateMapper_shouldRegisterAndCreateAnnotationMapper() throws Exception {
        Configuration configuration = new Configuration(Options.of().dialect(H2Dialect.DEFAULT));
        try (Session session = configuration.newSession(NoDatabaseConnection.create())) {
            configuration.loadMapper(SessionUserMapper.class);
            assertNotNull(session.createMapper(SessionUserMapper.class));
            assertNotNull(session.createMapper(SessionUserMapper.class));
        }
    }

    @Test
    public void sessionCreateMapper_shouldRegisterAndCreateXmlReferencedMapper() throws Exception {
        Configuration configuration = new Configuration(Options.of().dialect(H2Dialect.DEFAULT));
        try (Session session = configuration.newSession(NoDatabaseConnection.create())) {
            configuration.loadMapper(SessionRefUserMapper.class);
            assertNotNull(session.createMapper(SessionRefUserMapper.class));
            assertNotNull(session.createMapper(SessionRefUserMapper.class));
        }
    }

    @Test
    public void sessionCreateMapper_shouldRejectInterfacesWithoutMapperAnnotation() throws Exception {
        Configuration configuration = new Configuration(Options.of().dialect(H2Dialect.DEFAULT));
        try (Session session = configuration.newSession(NoDatabaseConnection.create())) {
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
