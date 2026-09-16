/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class LambdaRangeCase extends LambdaPredicateSupport {
    // 能力归属：构造器 API / 条件构造器。
    @Test
    @Capability(value = CapabilityId.LAMBDA_PREDICATE_RANGE_BOUNDARIES, column = "builder/condition-builders/predicates")
    public void lambdaPredicate_shouldApplyAllRangeBoundaryVariants() throws SQLException {
        insertRangeSet("NXN-Predicate-Range-", baseId() + 100);

        long closedClosed = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 100, baseId() + 104)//
                .rangeClosedClosed(UserInfo::getAge, 20, 30)//
                .queryForCount();
        long openOpen = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 100, baseId() + 104)//
                .rangeOpenOpen(UserInfo::getAge, 20, 30)//
                .queryForCount();
        long openClosed = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 100, baseId() + 104)//
                .rangeOpenClosed(UserInfo::getAge, 20, 30)//
                .queryForCount();
        long closedOpen = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 100, baseId() + 104)//
                .rangeClosedOpen(UserInfo::getAge, 20, 30)//
                .queryForCount();
        long equalBounds = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 100, baseId() + 104)//
                .rangeBetween(UserInfo::getAge, 25, 25)//
                .queryForCount();

        assertEquals(3, closedClosed);
        assertEquals(1, openOpen);
        assertEquals(2, openClosed);
        assertEquals(2, closedOpen);
        assertEquals(1, equalBounds);
    }

    // 能力归属：构造器 API / 条件构造器。
    @Test
    @Capability(value = CapabilityId.LAMBDA_PREDICATE_RANGE_NOT_VARIANTS, column = "builder/condition-builders/predicates")
    public void lambdaPredicate_shouldApplyNotBetweenAndReversedBounds() throws SQLException {
        insertRangeSet("NXN-Predicate-NotRange-", baseId() + 200);

        long notBetween = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 200, baseId() + 204)//
                .rangeNotBetween(UserInfo::getAge, 20, 30)//
                .queryForCount();
        long reversedBetween = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 200, baseId() + 204)//
                .rangeBetween(UserInfo::getAge, 30, 20)//
                .queryForCount();

        assertEquals(2, notBetween);
        assertEquals(0, reversedBetween);
    }

    // 能力归属：构造器 API / 条件构造器。
    @Test
    @Capability(value = CapabilityId.LAMBDA_PREDICATE_RANGE_NOT_HALF_OPEN, column = "builder/condition-builders/predicates")
    public void lambdaPredicate_shouldNegateHalfOpenRanges() throws SQLException {
        insertRangeSet("NXN-Predicate-NotRange-", baseId() + 200);
        long notOpenClosed = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 200, baseId() + 204)//
                .rangeNotOpenClosed(UserInfo::getAge, 20, 30)//
                .queryForCount();
        long notClosedOpen = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 200, baseId() + 204)//
                .rangeNotClosedOpen(UserInfo::getAge, 20, 30)//
                .queryForCount();
        assertEquals(3, notOpenClosed);
        assertEquals(3, notClosedOpen);
    }

    // 能力归属：构造器 API / 条件构造器。
    @Test
    @Capability(value = CapabilityId.LAMBDA_PREDICATE_RANGE_DYNAMIC, column = "builder/condition-builders/predicates")
    public void lambdaPredicate_shouldHonorDynamicRangeFlags() throws SQLException {
        insertRangeSet("NXN-Predicate-DynRange-", baseId() + 300);

        long betweenEnabled = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 300, baseId() + 304)//
                .rangeBetween(true, UserInfo::getAge, 20, 25)//
                .queryForCount();
        long betweenDisabled = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 300, baseId() + 304)//
                .rangeBetween(false, UserInfo::getAge, 20, 25)//
                .queryForCount();
        long notBetweenEnabled = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 300, baseId() + 304)//
                .rangeNotBetween(true, UserInfo::getAge, 20, 25)//
                .queryForCount();
        long notBetweenDisabled = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 300, baseId() + 304)//
                .rangeNotBetween(false, UserInfo::getAge, 20, 25)//
                .queryForCount();

        assertEquals(2, betweenEnabled);
        assertEquals(5, betweenDisabled);
        assertEquals(3, notBetweenEnabled);
        assertEquals(5, notBetweenDisabled);
    }

    // 能力归属：构造器 API / 条件构造器。
    @Test
    @Capability(value = CapabilityId.LAMBDA_EMPTY_REVERSED_BETWEEN, column = "builder/condition-builders/predicates")
    public void lambdaBetween_shouldReturnEmptyListForReversedRange() throws SQLException {
        insert(baseId() + 71, "NXN-Lambda-Empty-Between", 25, "between@nxn.test");

        List<UserInfo> result = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getName, "NXN-Lambda-Empty-Between")//
                .rangeBetween(UserInfo::getAge, 100, 50)//
                .queryForList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
