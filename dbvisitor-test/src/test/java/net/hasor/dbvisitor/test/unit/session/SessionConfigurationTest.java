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
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.material.model.UserRole;
import net.hasor.dbvisitor.test.unit.support.NoDatabaseConnection;
import org.junit.Test;
import static org.junit.Assert.*;

public class SessionConfigurationTest {
    @Test
    public void sessionBaseMapper_shouldRegisterEntityInCustomNamespace() throws Exception {
        Configuration configuration = new Configuration(Options.of().dialect(H2Dialect.DEFAULT));
        try (Session session = configuration.newSession(NoDatabaseConnection.create())) {
            assertNotNull(session.createBaseMapper(UserRole.class, "nxn.session.role"));
            assertNotNull(configuration.findBySpace("nxn.session.role", UserRole.class));
            assertNull(configuration.findByEntity(UserRole.class));
        }
    }

    @Test
    public void configuration_shouldExposeRegistriesOptionsAndClassLoading() throws Exception {
        Options options = Options.of();
        Configuration configuration = new Configuration(options);

        assertSame(options, configuration.options());
        assertNotNull(configuration.getTypeRegistry());
        assertNotNull(configuration.getMacroRegistry());
        assertNotNull(configuration.getRuleRegistry());
        assertNotNull(configuration.getMapperRegistry());
        assertNotNull(configuration.getMappingRegistry());
        assertNotNull(configuration.getClassLoader());

        configuration.loadEntityToSpace(UserInfo.class);
        assertNotNull(configuration.findByEntity(UserInfo.class));
        assertEquals(UserInfo.class, configuration.loadClass("net.hasor.dbvisitor.test.contract.material.model.UserInfo"));

        try {
            configuration.loadClass("com.nonexistent.SomeClass");
        } catch (ClassNotFoundException e) {
            return;
        }
        throw new AssertionError("Expected missing class lookup to throw ClassNotFoundException.");
    }
}
