/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.unit.session;

import java.util.HashMap;
import net.hasor.dbvisitor.dialect.provider.H2Dialect;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.unit.support.NoDatabaseConnection;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class SessionStatementLookupTest {
    @Test
    public void sessionStatement_shouldRejectUnknownStatementIdsForExecuteAndQuery() throws Exception {
        Configuration configuration = new Configuration(Options.of().dialect(H2Dialect.DEFAULT));
        configuration.loadMapper("/session/UserSessionMapper.xml");
        try (Session session = configuration.newSession(NoDatabaseConnection.create())) {
            String statementId = "session.UserSessionMapper.nonExistent";
            IllegalStateException executeFailure = assertThrows(IllegalStateException.class, () -> session.executeStatement(statementId, new HashMap<String, Object>()));
            assertEquals("statement '" + statementId + "' is not found.", executeFailure.getMessage());

            IllegalStateException queryFailure = assertThrows(IllegalStateException.class, () -> session.queryStatement(statementId, new HashMap<String, Object>()));
            assertEquals("statement '" + statementId + "' is not found.", queryFailure.getMessage());
        }
    }
}
