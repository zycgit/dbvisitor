/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;

public class Elastic6LambdaPageResultTest extends Elastic6LambdaPaginationSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_PAGINATION_MULTIPLE)
    public void multiplePagesShouldFollowNativePageSemantics() throws SQLException {
        verifyMultiplePages();
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_PAGINATION_EXACT)
    public void exactPagesShouldFollowNativePageSemantics() throws SQLException {
        verifyExactPages();
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_PAGINATION_SINGLE)
    public void singlePageShouldFollowNativePageSemantics() throws SQLException {
        verifySinglePage();
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_PAGINATION_SIZE_ONE)
    public void singleRowPagesShouldFollowNativePageSemantics() throws SQLException {
        verifySingleRowPages();
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_PAGINATION_BEYOND)
    public void beyondLastShouldFollowNativePageSemantics() throws SQLException {
        verifyBeyondLast();
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_PAGINATION_FACTORY)
    public void factoriesShouldFollowNativePageSemantics() throws SQLException {
        verifyFactories();
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_PAGINATION_ONE_BASED)
    public void oneBasedPagesShouldFollowNativePageSemantics() throws SQLException {
        verifyOneBasedPages();
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_PAGINATION_COUNT)
    public void countConsistencyShouldFollowNativePageSemantics() throws SQLException {
        verifyCountConsistency();
    }

    @Test
    @Capability(CapabilityId.ADAPTER_ELASTIC_PAGINATION_FILTER)
    public void filteredCountShouldFollowNativePageSemantics() throws SQLException {
        verifyFilteredCount();
    }
}
