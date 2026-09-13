/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaLargeCollectionCase extends LambdaPredicateSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_IN_LARGE)
    public void lambdaPredicate_shouldSupportLargeInCollection() throws SQLException {
        requiresNxnFeature(FeatureId.LARGE_IN_LIST);
        for (int i = 1; i <= 50; i++) {
            insert(baseId() + 500 + i, "NXN-Predicate-In-Large-" + i, 20 + i, "in@nxn.test");
        }

        List<Integer> largeList = new ArrayList<>();
        for (int i = 1; i <= 1500; i++) {
            largeList.add(20 + i);
        }
        long large = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 501, baseId() + 550)//
                .in(UserInfo::getAge, largeList)//
                .queryForCount();

        assertEquals(50, large);
    }
}
