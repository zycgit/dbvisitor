/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaNullCollectionCase extends LambdaPredicateSupport {
    // 能力归属：构造器 API / 条件构造器。
    @Test
    @Capability(value = CapabilityId.LAMBDA_PREDICATE_NOT_IN_NULL, column = "builder/condition-builders/predicates")
    public void lambdaPredicate_shouldUseSqlNotInNullSemantics() throws SQLException {
        insert(baseId() + 611, "NXN-Predicate-NotInNull-20", 20, "notin@nxn.test");
        insert(baseId() + 612, "NXN-Predicate-NotInNull-25", 25, "notin@nxn.test");
        insert(baseId() + 613, "NXN-Predicate-NotInNull-Null", null, "notin@nxn.test");

        if (profile().supportsFeature(FeatureId.SQL_NOT_IN_NULL_SEMANTICS)) {
            long count = lambdaTemplate.query(UserInfo.class)//
                    .rangeBetween(UserInfo::getId, baseId() + 611, baseId() + 613)//
                    .notIn(UserInfo::getAge, Arrays.asList(20, null))//
                    .queryForCount();
            assertEquals(0, count);
        }

        // Exclude NULL explicitly instead of requiring SQL's three-valued NOT IN semantics.
        long count = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 611, baseId() + 613)//
                .notIn(UserInfo::getAge, List.of(20))//
                .isNotNull(UserInfo::getAge)//
                .queryForCount();

        assertEquals(1, count);
    }
}
