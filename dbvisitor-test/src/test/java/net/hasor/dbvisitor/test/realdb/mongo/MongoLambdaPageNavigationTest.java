/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

public class MongoLambdaPageNavigationTest extends MongoLambdaPaginationSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MONGO_PAGINATION_MUTABLE)
    public void mutablePageShouldFollowNativePageSemantics() throws SQLException {
        verifyMutablePage();
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MONGO_PAGINATION_TRAVERSAL)
    public void fullTraversalShouldFollowNativePageSemantics() throws SQLException {
        verifyFullTraversal();
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MONGO_PAGINATION_OFFSET)
    public void offsetShouldFollowNativePageSemantics() throws SQLException {
        verifyOffset();
    }
}
