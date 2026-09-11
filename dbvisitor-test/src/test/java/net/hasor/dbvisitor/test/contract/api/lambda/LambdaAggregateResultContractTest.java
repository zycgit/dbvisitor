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

import org.junit.Test;

import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaAggregateResultContractTest extends LambdaResultHandlingSupport {
    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_AGGREGATE_SCALAR)
    public void lambdaResult_shouldMapAggregateScalarValue() throws SQLException {
        insertUsers("LRMap", new int[] { 21, 22, 23, 24, 25 }, baseId() + 10);
        Integer maxAge = queryRows("LRMap")//
                .applySelect("MAX(age)")//
                .queryForObject(Integer.class);
        assertEquals(Integer.valueOf(25), maxAge);
    }

    @Test
    @Capability(CapabilityId.LAMBDA_RESULT_AGGREGATE_ROW_MAPPER)
    public void lambdaResult_shouldMapServerAggregateRows() throws SQLException {
        insertByJdbc(baseId() + 161, "LRGroup1", 30, "lr-group1@test.com");
        insertByJdbc(baseId() + 162, "LRGroup2", 30, "lr-group2@test.com");
        insertByJdbc(baseId() + 163, "LRGroup3", 31, "lr-group3@test.com");
        RowMapper<AgeGroup> groupMapper = (rs, rowNum) -> new AgeGroup(rs.getInt("age"), rs.getLong("cnt"));
        List<AgeGroup> groups = orderRows(queryRows("LRGroup")//
                .applySelect("age, count(*) as cnt")//
                .groupBy("age"), "age")//
                .queryForList(groupMapper);
        assertEquals(2, groups.size());
        assertEquals(Integer.valueOf(30), groups.get(0).age);
        assertEquals(Long.valueOf(2), groups.get(0).count);
    }
}
