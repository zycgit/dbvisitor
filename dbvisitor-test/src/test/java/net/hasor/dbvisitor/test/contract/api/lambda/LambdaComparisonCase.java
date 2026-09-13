/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaComparisonCase extends LambdaPredicateSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_COMPARISON_DYNAMIC)
    public void lambdaPredicate_shouldHonorDynamicComparisonFlags() throws SQLException {
        insertAgeSet("NXN-Predicate-Dynamic-", baseId() + 10);

        long geEnabled = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 10, baseId() + 14)//
                .ge(true, UserInfo::getAge, 30)//
                .queryForCount();
        long geDisabled = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 10, baseId() + 14)//
                .ge(false, UserInfo::getAge, 30)//
                .queryForCount();
        long ltEnabled = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 10, baseId() + 14)//
                .lt(true, UserInfo::getAge, 22)//
                .queryForCount();
        long leEnabled = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 10, baseId() + 14)//
                .le(true, UserInfo::getAge, 22)//
                .queryForCount();
        long neEnabled = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 10, baseId() + 14)//
                .ne(true, UserInfo::getAge, 25)//
                .queryForCount();

        assertEquals(2, geEnabled);
        assertEquals(5, geDisabled);
        assertEquals(1, ltEnabled);
        assertEquals(2, leEnabled);
        assertEquals(4, neEnabled);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_COMPARISON_MIXED)
    public void lambdaPredicate_shouldCombineComparisonOperators() throws SQLException {
        insertAgeSet("NXN-Predicate-Mixed-", baseId() + 30);

        long closed = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 30, baseId() + 34)//
                .ge(UserInfo::getAge, 20)//
                .le(UserInfo::getAge, 30)//
                .queryForCount();
        long open = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 30, baseId() + 34)//
                .gt(UserInfo::getAge, 18)//
                .lt(UserInfo::getAge, 35)//
                .queryForCount();
        long mixed = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 30, baseId() + 34)//
                .ge(UserInfo::getAge, 22)//
                .lt(UserInfo::getAge, 35)//
                .ne(UserInfo::getName, "NXN-Predicate-Mixed-22")//
                .queryForCount();

        assertEquals(3, closed);
        assertEquals(3, open);
        assertEquals(2, mixed);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_QUERY_COMPARE)
    public void lambdaQueryCompare_shouldApplyRangePredicates() throws SQLException {
        insertUsers("Cmp", new int[] { 18, 22, 25, 30, 35 }, baseId() + 10);

        long count = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 10, baseId() + 14)//
                .ge(UserInfo::getAge, 20)//
                .le(UserInfo::getAge, 30)//
                .queryForCount();

        assertEquals(3, count);
    }
}
