/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.util.List;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusXmlScrollableResultTest extends MilvusXmlResultSqlSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_XML_SCROLL_RESULT)
    public void xmlShouldRejectUnsupportedScrollableResults() throws Exception {
        try (Session session = prepareSession()) {
            assertEquals(List.of("name-1", "name-2", "name-3"),
                    session.queryStatement("milvus.ResultHandlers.forwardOnlyAttribute", parameters(1)));
            assertThrows(java.sql.SQLFeatureNotSupportedException.class,
                    () -> session.queryStatement("milvus.ResultHandlers.scrollAttributes", parameters(1)));
        }
    }
}
