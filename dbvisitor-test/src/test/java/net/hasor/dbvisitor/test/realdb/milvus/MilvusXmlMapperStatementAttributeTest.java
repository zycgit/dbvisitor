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
import java.util.HashMap;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusXmlMapperStatementAttributeTest extends MilvusXmlResultSqlSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_XML_STATEMENT_ATTRIBUTES)
    public void statementAttributesShouldPreserveNativeQueriesAndDml() throws Exception {
        try (Session session = prepareSession()) {
            List<String> expected = List.of("name-1", "name-2", "name-3");
            for (String statement : List.of("defaultPreparedAttributes", "timeoutAttribute", "fetchSizeAttribute", "forwardOnlyAttribute")) {
                assertEquals(expected, session.queryStatement("milvus.ResultHandlers." + statement, parameters(1)));
            }
            assertEquals(expected, session.queryStatement("milvus.ResultHandlers.preparedAttributes", parameters(1)));
            assertEquals(expected, session.queryStatement("milvus.ResultHandlers.statementAttributes", parameters(1)));
            assertTrue(session.queryStatement("milvus.ResultHandlers.preparedAttributes", parameters(99)).isEmpty());
            Map<String, Object> insert = new HashMap<>(parameters(1));
            insert.put("id", 4);
            insert.put("name", "prepared '\" 中文");
            insert.put("vector", new float[] { 4, 0 });
            assertEquals(1, session.executeStatement("milvus.ResultHandlers.preparedInsert", insert));
            assertEquals(List.of("prepared '\" 中文"), session.queryStatement("milvus.ResultHandlers.preparedAttributes", parameters(4)));
        }
    }
}
