/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.util.Set;
import java.util.stream.Collectors;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaCollectionBindingContractTest extends LambdaPredicateSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_IN_SINGLE)
    public void lambdaPredicate_shouldSupportSingleElementInCollection() throws SQLException {
        insert(baseId() + 401, "NXN-Predicate-In-Single-25", 25, "in@nxn.test");
        insert(baseId() + 402, "NXN-Predicate-In-Single-30", 30, "in@nxn.test");

        long single = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 401, baseId() + 402)//
                .in(UserInfo::getAge, Arrays.asList(25))//
                .queryForCount();
        assertEquals(1, single);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_QUERY_IN)
    public void lambdaQueryIn_shouldExpandCollectionParameter() throws SQLException {
        insertUsers("InQ", new int[] { 20, 25, 30, 35 }, baseId() + 30);

        List<UserInfo> users = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 30, baseId() + 33)//
                .in(UserInfo::getAge, Arrays.asList(20, 30, 35))//
                .queryForList();

        assertEquals(3, users.size());
        assertEquals(Set.of(20, 30, 35), users.stream().map(UserInfo::getAge).collect(Collectors.toSet()));
        assertEquals(Set.of(baseId() + 30, baseId() + 32, baseId() + 33), users.stream().map(UserInfo::getId).collect(Collectors.toSet()));
    }
}
