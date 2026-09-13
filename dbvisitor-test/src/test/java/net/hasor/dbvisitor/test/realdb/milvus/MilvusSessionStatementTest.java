/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus3;
import org.junit.Test;
import static org.junit.Assert.*;

/** Named-statement execution through Session with native Milvus commands. */
public class MilvusSessionStatementTest extends MilvusSessionSqlSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_SESSION_STATEMENT)
    public void sessionStatementIdsShouldExecuteDmlAndMapResults() throws Exception {
        assertEquals(1, ((Number) this.session.executeStatement(NAMESPACE + ".insertUser", parameters("statement"))).intValue());
        List<UserInfoMilvus3> rows = this.session.queryStatement(NAMESPACE + ".queryAll", null);
        assertEquals(1, rows.size());
        assertEquals("statement", rows.get(0).getUid());
        assertEquals("login", rows.get(0).getLoginName());
        assertEquals(List.of(1F, 0F), rows.get(0).getV());
        assertEquals(1, ((Number) this.session.executeStatement(NAMESPACE + ".deleteUser", Map.of("uid", "statement"))).intValue());
        assertTrue(this.session.queryStatement(NAMESPACE + ".queryAll", null).isEmpty());
        // Milvus 2.6.2 simple PK deletes count submitted keys, even when the entity is already absent.
        assertEquals(1, ((Number) this.session.executeStatement(NAMESPACE + ".deleteUser", Map.of("uid", "statement"))).intValue());
        assertTrue(this.session.queryStatement(NAMESPACE + ".queryAll", null).isEmpty());
        IllegalStateException invalidExecute = assertThrows(IllegalStateException.class,
                () -> this.session.executeStatement(NAMESPACE + ".missing", null));
        IllegalStateException invalidQuery = assertThrows(IllegalStateException.class,
                () -> this.session.queryStatement(NAMESPACE + ".missing", null));
        assertTrue(invalidExecute.getMessage().contains(NAMESPACE + ".missing"));
        assertTrue(invalidQuery.getMessage().contains(NAMESPACE + ".missing"));
    }
}
