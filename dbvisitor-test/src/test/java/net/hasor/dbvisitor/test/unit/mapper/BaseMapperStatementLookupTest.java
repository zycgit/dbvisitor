/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.unit.mapper;

import java.util.HashMap;
import net.hasor.dbvisitor.dialect.provider.H2Dialect;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.unit.support.NoDatabaseConnection;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class BaseMapperStatementLookupTest {
    @Test
    public void baseMapperStatement_shouldRejectUnknownStatementIds() throws Exception {
        Configuration configuration = new Configuration(Options.of().dialect(H2Dialect.DEFAULT));
        configuration.loadMapper("/mapper/StatementTestMapper.xml");
        try (Session session = configuration.newSession(NoDatabaseConnection.create())) {
            BaseMapper<UserInfo> mapper = session.createBaseMapper(UserInfo.class);
            IllegalStateException executeFailure = assertThrows(IllegalStateException.class, () -> mapper.executeStatement("StatementTestMapper.missingExecute", new HashMap<String, Object>()));
            assertEquals("statement 'StatementTestMapper.missingExecute' is not found.", executeFailure.getMessage());

            IllegalStateException queryFailure = assertThrows(IllegalStateException.class, () -> mapper.queryStatement("StatementTestMapper.missingQuery", new HashMap<String, Object>()));
            assertEquals("statement 'StatementTestMapper.missingQuery' is not found.", queryFailure.getMessage());
        }
    }
}
