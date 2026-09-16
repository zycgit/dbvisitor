/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.unit.mapper;

import net.hasor.dbvisitor.dialect.provider.H2Dialect;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.unit.support.NoDatabaseConnection;
import org.junit.Test;
import static org.junit.Assert.*;

public class BaseMapperAccessorsTest {
    @Test
    public void baseMapperAccessors_shouldExposeEntityTypeSessionJdbcAndLambdaApis() throws Exception {
        Configuration configuration = new Configuration(Options.of().dialect(H2Dialect.DEFAULT));
        try (Session session = configuration.newSession(NoDatabaseConnection.create())) {
            BaseMapper<UserInfo> accessors = session.createBaseMapper(UserInfo.class);
            assertEquals(UserInfo.class, accessors.entityType());
            assertNotNull(accessors.session());
            assertNotNull(accessors.lambda());
            assertNotNull(accessors.jdbc());
            assertSame(session, accessors.session());
        }
    }
}
