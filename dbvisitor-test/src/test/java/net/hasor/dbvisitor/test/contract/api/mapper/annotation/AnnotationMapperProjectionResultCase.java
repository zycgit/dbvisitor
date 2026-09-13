/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class AnnotationMapperProjectionResultCase extends AnnotationMapperResultMappingSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_DISTINCT_LIST)
    public void queryResult_shouldMapDistinctScalarProjection() throws Exception {
        List<Integer> distinctAges = distinctValues();
        assertEquals(distinctAges.size(), new HashSet<Integer>(distinctAges).size());
        if (expectedDistinctValues() != null) {
            assertEquals(new HashSet<>(expectedDistinctValues()), new HashSet<>(distinctAges));
        }
    }

    protected List<Integer> distinctValues() throws Exception {
        return this.mapper.selectDistinctAges(PATTERN);
    }

    protected List<Integer> expectedDistinctValues() { return null; }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_AGGREGATE)
    public void queryResult_shouldMapAggregateScalarsAndMaps() throws Exception {
        prepareAggregateRows();
        Number scalar = aggregateScalar();
        assertNotNull(scalar);
        assertTrue(scalar.doubleValue() >= minimumAggregateScalar());
        if (expectedAggregateScalar() != null) {
            assertEquals(expectedAggregateScalar().doubleValue(), scalar.doubleValue(), 0.0);
        }
        Map<String, Object> stats = aggregateMap();
        for (String column : aggregateMapColumns()) {
            assertNotNull(value(stats, column));
        }
        assertExpectedValues(expectedAggregateMap(), stats);
        List<List<Map<String, Object>>> groups = aggregateGroups();
        assertTrue(groups.size() > 0);
        List<List<Map<String, Object>>> expected = expectedAggregateGroups();
        if (expected != null) {
            assertEquals(expected.size(), groups.size());
        }
        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            List<Map<String, Object>> rows = groups.get(groupIndex);
            assertTrue(rows.size() > 0);
            for (Map<String, Object> row : rows) {
                for (String column : aggregateGroupColumns()) {
                    assertNotNull(value(row, column));
                }
            }
            if (expected != null) {
                assertEquals(expected.get(groupIndex).size(), rows.size());
                for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                    assertExpectedValues(expected.get(groupIndex).get(rowIndex), rows.get(rowIndex));
                }
            }
        }
    }

    private void assertExpectedValues(Map<String, Object> expected, Map<String, Object> actual) {
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            Object result = value(actual, entry.getKey());
            if (entry.getValue() instanceof Number) {
                assertTrue(result instanceof Number);
                assertEquals(((Number) entry.getValue()).doubleValue(), ((Number) result).doubleValue(), 0.0);
            } else {
                assertEquals(entry.getValue(), result);
            }
        }
    }

    protected void prepareAggregateRows() throws Exception { }
    protected Number aggregateScalar() throws Exception { return this.mapper.selectMaxAge(); }
    protected double minimumAggregateScalar() { return 30; }
    protected Number expectedAggregateScalar() { return null; }
    protected Map<String, Object> aggregateMap() throws Exception { return this.mapper.selectAgeStats(PATTERN); }
    protected List<String> aggregateMapColumns() { return List.of("minAge", "maxAge", "avgAge"); }
    protected Map<String, Object> expectedAggregateMap() { return Map.of(); }
    protected List<List<Map<String, Object>>> aggregateGroups() throws Exception { return List.of(this.mapper.selectCountByAge(PATTERN)); }
    protected List<String> aggregateGroupColumns() { return List.of("age", "cnt"); }
    protected List<List<Map<String, Object>>> expectedAggregateGroups() { return null; }
}
