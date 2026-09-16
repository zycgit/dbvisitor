/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.result;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.FilterResultSetExtractor;
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaResultHandlingSupport;
import net.hasor.dbvisitor.test.contract.material.handler.ResultHandlerProbe;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class LambdaResultExtractorCase extends LambdaResultHandlingSupport {
    // 能力归属：结果接收 / resultsetextractor / builder。
    @Test
    @Capability(value = CapabilityId.LAMBDA_RESULT_EXTRACTOR_CUSTOM, column = "results/resultsetextractor/extraction", variants = { "builder" })
    public void lambdaResult_shouldUseCustomResultSetExtractor() throws SQLException {
        insertUsers("LRExtract", new int[] { 21, 22, 23 }, baseId() + 100);

        ResultSetExtractor<Map<Integer, String>> extractor = rs -> {
            ResultHandlerProbe.record(rs);
            Map<Integer, String> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getInt("id"), rs.getString("name"));
            }
            return result;
        };

        Map<Integer, String> result = ResultHandlerProbe.verify(1, () -> orderRows(queryRows("LRExtract"), "id")//
                .query(extractor));

        assertEquals(3, result.size());
        assertEquals("LRExtract1", result.get(baseId() + 100));
        assertEquals("LRExtract3", result.get(baseId() + 102));
        assertEquals(Map.of(baseId() + 100, "LRExtract1", baseId() + 101, "LRExtract2", baseId() + 102, "LRExtract3"), result);
        assertTrue(ResultHandlerProbe.verify(1, () -> queryRows("LRMissing").query(extractor)).isEmpty());
        ResultHandlerProbe.verifyFailure(() -> queryRows("LRExtract").query(extractor));
    }

    // 能力归属：结果接收 / resultsetextractor / builder。
    @Test
    @Capability(value = CapabilityId.LAMBDA_RESULT_EXTRACTOR_PAIRS, column = "results/resultsetextractor/extraction", variants = { "builder" })
    public void lambdaResult_shouldReturnPairsFromStringAndLambdaColumns() throws SQLException {
        insertUsers("LRPairs", new int[] { 24, 25, 26 }, baseId() + 120);

        Map<Integer, String> stringPairs = queryRows("LRPairs")//
                .queryForPairs("id", "name", Integer.class, String.class);
        Map<Integer, String> lambdaPairs = queryRows("LRPairs")//
                .queryForPairs(UserInfo::getId, UserInfo::getName, Integer.class, String.class);

        assertEquals(3, stringPairs.size());
        assertEquals("LRPairs1", stringPairs.get(baseId() + 120));
        assertEquals(stringPairs, lambdaPairs);
        assertEquals(Map.of(baseId() + 120, "LRPairs1", baseId() + 121, "LRPairs2", baseId() + 122, "LRPairs3"), stringPairs);
        assertTrue(queryRows("LRMissing").queryForPairs("id", "name", Integer.class, String.class).isEmpty());
        assertTrue(queryRows("LRMissing").queryForPairs(UserInfo::getId, UserInfo::getName, Integer.class, String.class).isEmpty());
    }

    // 能力归属：结果接收 / resultsetextractor / builder。
    @Test
    @Capability(value = CapabilityId.LAMBDA_RESULT_EXTRACTOR_FILTER, column = "results/resultsetextractor/extraction", variants = { "builder" })
    public void lambdaResult_shouldUseFilterResultSetExtractor() throws SQLException {
        insertUsers("LRFilter", new int[] { 21, 22, 23, 24, 25, 26 }, baseId() + 140);

        ResultSetExtractor<List<UserInfo>> extractor = new FilterResultSetExtractor<>((rs, rowNum) -> {
            UserInfo user = new UserInfo();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            user.setAge(rs.getInt("age"));
            return user;
        }, user -> user.getAge() % 2 == 0);

        List<UserInfo> result = orderRows(queryRows("LRFilter"), "id")//
                .query(extractor);

        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(user -> user.getAge() % 2 == 0));
        assertEquals(List.of("LRFilter2", "LRFilter4", "LRFilter6"), result.stream().map(UserInfo::getName).toList());
        assertEquals(List.of(22, 24, 26), result.stream().map(UserInfo::getAge).toList());
    }

    // 能力归属：结果接收 / resultsetextractor / builder。
    @Test
    @Capability(value = CapabilityId.LAMBDA_RESULT_EXTRACTOR_GROUPING, column = "results/resultsetextractor/extraction", variants = { "builder" })
    public void lambdaResult_shouldBuildGroupedStructuresFromExtractor() throws SQLException {
        insertByJdbc(baseId() + 161, "LRGroup1", 30, "lr-group1@test.com");
        insertByJdbc(baseId() + 162, "LRGroup2", 30, "lr-group2@test.com");
        insertByJdbc(baseId() + 163, "LRGroup3", 31, "lr-group3@test.com");

        ResultSetExtractor<Map<Integer, List<String>>> groupingExtractor = rs -> {
            Map<Integer, List<String>> grouped = new LinkedHashMap<>();
            while (rs.next()) {
                grouped.computeIfAbsent(rs.getInt("age"), ignored -> new ArrayList<>()).add(rs.getString("name"));
            }
            return grouped;
        };
        Map<Integer, List<String>> grouped = orderRows(queryRows("LRGroup")//
                .select("age", "name"), "age")//
                .query(groupingExtractor);
        assertEquals(2, grouped.size());
        assertEquals(2, grouped.get(30).size());
        assertEquals("LRGroup3", grouped.get(31).get(0));
        assertEquals(List.of("LRGroup1", "LRGroup2"), grouped.get(30).stream().sorted().toList());
        assertEquals(List.of("LRGroup3"), grouped.get(31));
    }
}
