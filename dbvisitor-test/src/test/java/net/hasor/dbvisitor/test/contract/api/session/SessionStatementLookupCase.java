/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.session;

import java.util.HashMap;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

@NxnContract
public abstract class SessionStatementLookupCase extends SessionStatementSupport {
    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_INVALID_ID)
    public void sessionStatement_shouldRejectUnknownStatementIdsForExecuteAndQuery() throws Exception {
        expectStatementFailure(new StatementCall() {
            @Override
            public void run() throws Exception {
                session.executeStatement(NS + ".nonExistent", new HashMap<String, Object>());
            }
        });
        expectStatementFailure(new StatementCall() {
            @Override
            public void run() throws Exception {
                session.queryStatement(NS + ".nonExistent", new HashMap<String, Object>());
            }
        });
    }
}
