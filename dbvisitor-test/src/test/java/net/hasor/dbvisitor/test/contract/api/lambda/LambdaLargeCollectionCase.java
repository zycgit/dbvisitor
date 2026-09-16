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
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaLargeCollectionCase extends LambdaPredicateSupport {
    // 能力归属：构造器 API / 条件构造器。
    @Test
    @Capability(value = CapabilityId.LAMBDA_PREDICATE_IN_LARGE, column = "builder/condition-builders/predicates")
    public void lambdaPredicate_shouldSupportLargeInCollection() throws SQLException {
        for (int i = 1; i <= 50; i++) {
            insert(baseId() + 500 + i, "NXN-Predicate-In-Large-" + i, 20 + i, "in@nxn.test");
        }

        List<Integer> largeList = new ArrayList<>();
        for (int i = 1; i <= 1500; i++) {
            largeList.add(20 + i);
        }
        // Partition input explicitly when the server limits a single IN list.
        int batchSize = profile().supportsFeature(FeatureId.LARGE_IN_LIST) ? largeList.size() : 500;
        long large = 0;
        for (int start = 0; start < largeList.size(); start += batchSize) {
            List<Integer> batch = largeList.subList(start, Math.min(start + batchSize, largeList.size()));
            large += lambdaTemplate.query(UserInfo.class)//
                    .rangeBetween(UserInfo::getId, baseId() + 501, baseId() + 550)//
                    .in(UserInfo::getAge, batch)//
                    .queryForCount();
        }

        assertEquals(50, large);
    }
}
