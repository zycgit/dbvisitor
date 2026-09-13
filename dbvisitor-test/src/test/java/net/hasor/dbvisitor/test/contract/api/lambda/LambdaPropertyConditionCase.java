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
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaPropertyConditionCase extends LambdaPredicateSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_STRING_COMPARISON)
    public void lambdaPredicate_shouldSupportStringPropertyComparisonOperators() throws SQLException {
        insertAgeSet("NXN-Predicate-String-Compare-", baseId() + 1000);

        long eq = lambdaTemplate.query(UserInfo.class)//
                .eq("name", "NXN-Predicate-String-Compare-18")//
                .queryForCount();
        long ne = lambdaTemplate.query(UserInfo.class)//
                .between("id", baseId() + 1000, baseId() + 1004)//
                .ne("age", 18)//
                .queryForCount();
        long gt = lambdaTemplate.query(UserInfo.class)//
                .between("id", baseId() + 1000, baseId() + 1004)//
                .gt("age", 25)//
                .queryForCount();
        long ge = lambdaTemplate.query(UserInfo.class)//
                .between("id", baseId() + 1000, baseId() + 1004)//
                .ge("age", 25)//
                .queryForCount();
        long lt = lambdaTemplate.query(UserInfo.class)//
                .between("id", baseId() + 1000, baseId() + 1004)//
                .lt("age", 25)//
                .queryForCount();
        long le = lambdaTemplate.query(UserInfo.class)//
                .between("id", baseId() + 1000, baseId() + 1004)//
                .le("age", 25)//
                .queryForCount();

        assertEquals(1, eq);
        assertEquals(4, ne);
        assertEquals(2, gt);
        assertEquals(3, ge);
        assertEquals(2, lt);
        assertEquals(3, le);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_STRING_DYNAMIC)
    public void lambdaPredicate_shouldHonorStringPropertyDynamicFlags() throws SQLException {
        insertAgeSet("NXN-Predicate-String-Dynamic-", baseId() + 1100);

        long enabled = lambdaTemplate.query(UserInfo.class)//
                .between("id", baseId() + 1100, baseId() + 1104)//
                .ge(true, "age", 25)//
                .notIn(true, "age", Arrays.asList(30, 35))//
                .queryForCount();
        long disabled = lambdaTemplate.query(UserInfo.class)//
                .between("id", baseId() + 1100, baseId() + 1104)//
                .ge(false, "age", 25)//
                .notIn(false, "age", Arrays.asList(30, 35))//
                .queryForCount();
        long nullSkipped = lambdaTemplate.query(UserInfo.class)//
                .between("id", baseId() + 1100, baseId() + 1104)//
                .eq(false, "name", "missing")//
                .isNull(false, "email")//
                .queryForCount();

        assertEquals(1, enabled);
        assertEquals(5, disabled);
        assertEquals(5, nullSkipped);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_PREDICATE_STRING_COLLECTION_NULL_RANGE)
    public void lambdaPredicate_shouldSupportStringPropertyCollectionNullRangeAndSampleMap() throws SQLException {
        insert(baseId() + 1301, "NXN-Predicate-String-Mixed-A", 18, "mixed-a@nxn.test");
        insert(baseId() + 1302, "NXN-Predicate-String-Mixed-B", 22, "mixed-b@nxn.test");
        insert(baseId() + 1303, "NXN-Predicate-String-Mixed-C", 25, null);
        insert(baseId() + 1304, "NXN-Predicate-String-Mixed-D", 30, "mixed-d@nxn.test");
        insert(baseId() + 1305, "NXN-Predicate-String-Mixed-E", 35, null);

        long inCount = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1301, 1302, 1303, 1304, 1305))//
                .in("age", Arrays.asList(18, 25, 35))//
                .queryForCount();
        long notInCount = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1301, 1302, 1303, 1304, 1305))//
                .notIn("age", Arrays.asList(18, 25, 35))//
                .queryForCount();
        long nullCount = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1301, 1302, 1303, 1304, 1305))//
                .isNull("email")//
                .queryForCount();
        long notNullCount = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1301, 1302, 1303, 1304, 1305))//
                .isNotNull("email")//
                .queryForCount();
        long between = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1301, 1302, 1303, 1304, 1305))//
                .between("age", 20, 30)//
                .queryForCount();
        long notBetween = lambdaTemplate.query(UserInfo.class)//
                .in("id", ids(1301, 1302, 1303, 1304, 1305))//
                .notBetween("age", 20, 30)//
                .queryForCount();

        Map<String, Object> sample = new HashMap<>();
        sample.put("name", "NXN-Predicate-String-Mixed-C");
        sample.put("age", 25);
        long sampleMap = lambdaTemplate.query(UserInfo.class)//
                .eqBySampleMap(sample)//
                .queryForCount();

        assertEquals(3, inCount);
        assertEquals(2, notInCount);
        assertEquals(2, nullCount);
        assertEquals(3, notNullCount);
        assertEquals(3, between);
        assertEquals(2, notBetween);
        assertEquals(1, sampleMap);
    }
}
