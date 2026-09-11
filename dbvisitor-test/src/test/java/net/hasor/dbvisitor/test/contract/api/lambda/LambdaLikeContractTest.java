/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.sql.SQLException;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaLikeContractTest extends LambdaPredicateSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_LIKE_VARIANTS)
    public void lambdaPredicate_shouldApplyLikeLeftRightAndContains() throws SQLException {
        insert(baseId() + 701, "NXN-Predicate-Like-TestUser", 25, "like@nxn.test");
        insert(baseId() + 702, "NXN-Predicate-Like-UserAccount", 30, "like@nxn.test");
        insert(baseId() + 703, "NXN-Predicate-Like-MyUser", 35, "like@nxn.test");
        insert(baseId() + 704, "NXN-Predicate-Like-Admin", 40, "like@nxn.test");

        long contains = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(701, 702, 703, 704))//
                .like(UserInfo::getName, "User")//
                .queryForCount();
        long right = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(701, 702, 703, 704))//
                .likeRight(UserInfo::getName, "NXN-Predicate-Like-User")//
                .queryForCount();
        long left = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(701, 702, 703, 704))//
                .likeLeft(UserInfo::getName, "User")//
                .queryForCount();

        assertEquals(3, contains);
        assertEquals(1, right);
        assertEquals(2, left);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_NOT_LIKE_VARIANTS)
    public void lambdaPredicate_shouldApplyNotLikeLeftRightAndContains() throws SQLException {
        insert(baseId() + 801, "NXN-Predicate-NotLike-TestUser", 25, "like@nxn.test");
        insert(baseId() + 802, "NXN-Predicate-NotLike-MyTest", 30, "like@nxn.test");
        insert(baseId() + 803, "NXN-Predicate-NotLike-Admin", 35, "like@nxn.test");
        insert(baseId() + 804, "NXN-Predicate-NotLike-User", 40, "like@nxn.test");

        long notContains = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(801, 802, 803, 804))//
                .notLike(UserInfo::getName, "Test")//
                .queryForCount();
        long notRight = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(801, 802, 803, 804))//
                .notLikeRight(UserInfo::getName, "NXN-Predicate-NotLike-Test")//
                .queryForCount();
        long notLeft = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(801, 802, 803, 804))//
                .notLikeLeft(UserInfo::getName, "User")//
                .queryForCount();

        assertEquals(2, notContains);
        assertEquals(3, notRight);
        assertEquals(2, notLeft);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_LIKE_NULL_AND_MULTI)
    public void lambdaPredicate_shouldHandleLikeNullsAndMultipleLikePredicates() throws SQLException {
        insert(baseId() + 901, "NXN-Predicate-LikeNull-Test", 25, "ln@test.com");
        insert(baseId() + 902, null, 30, "ln@test.com");
        insert(baseId() + 911, "NXN-Predicate-LikeMulti-Test", 25, "multi@test.com");
        insert(baseId() + 912, "NXN-Predicate-LikeMulti-Test", 30, "multi@other.com");
        insert(baseId() + 913, "NXN-Predicate-LikeMulti-User", 35, "multi@test.com");

        long nullSkipped = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(901, 902))//
                .like(UserInfo::getName, "Test")//
                .queryForCount();
        long multi = lambdaTemplate.query(UserInfo.class)//
                .in(UserInfo::getId, ids(911, 912, 913))//
                .like(UserInfo::getName, "Test")//
                .like(UserInfo::getEmail, "@test")//
                .queryForCount();

        assertEquals(1, nullSkipped);
        assertEquals(1, multi);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_STRING_LIKE)
    public void lambdaPredicate_shouldSupportStringPropertyLikeVariants() throws SQLException {
        insert(baseId() + 1201, "NXN-Predicate-String-Name-Alice", 18, "string-like@nxn.test");
        insert(baseId() + 1202, "NXN-Predicate-String-Name-Bob", 22, "string-like@nxn.test");
        insert(baseId() + 1203, "NXN-Predicate-String-Name-Charlie", 25, "string-like@nxn.test");
        insert(baseId() + 1204, "NXN-Predicate-String-Name-Diana", 30, "string-like@nxn.test");
        insert(baseId() + 1205, "NXN-Predicate-String-Name-Eve", 35, "string-like@nxn.test");

        long contains = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1201, 1202, 1203, 1204, 1205))//
                .like("name", "li")//
                .queryForCount();
        long notContains = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1201, 1202, 1203, 1204, 1205))//
                .notLike("name", "li")//
                .queryForCount();
        long right = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1201, 1202, 1203, 1204, 1205))//
                .likeRight(true, "name", "NXN-Predicate-String-Name-Ch")//
                .queryForCount();
        long notRight = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1201, 1202, 1203, 1204, 1205))//
                .notLikeRight(true, "name", "NXN-Predicate-String-Name-Ch")//
                .queryForCount();
        long left = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1201, 1202, 1203, 1204, 1205))//
                .likeLeft(true, "name", "e")//
                .queryForCount();
        long notLeft = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1201, 1202, 1203, 1204, 1205))//
                .notLikeLeft(true, "name", "e")//
                .queryForCount();

        assertEquals(2, contains);
        assertEquals(3, notContains);
        assertEquals(1, right);
        assertEquals(4, notRight);
        assertEquals(3, left);
        assertEquals(2, notLeft);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_QUERY_LIKE)
    public void lambdaQueryLike_shouldMatchStringPrefix() throws SQLException {
        insert(baseId() + 21, "LikeAlpha", 21, "like1@test.com");
        insert(baseId() + 22, "LikeBeta", 22, "like2@test.com");
        insert(baseId() + 23, "OtherWord", 23, "like3@test.com");

        List<UserInfo> users = lambdaTemplate.query(UserInfo.class)//
                .like(UserInfo::getName, "Like%")//
                .queryForList();

        assertEquals(2, users.size());
        assertEquals(Set.of("LikeAlpha", "LikeBeta"), users.stream().map(UserInfo::getName).collect(Collectors.toSet()));
    }
}
