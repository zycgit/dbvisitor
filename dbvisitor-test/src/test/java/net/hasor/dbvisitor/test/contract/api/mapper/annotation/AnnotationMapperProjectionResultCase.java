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
        List<Integer> distinctAges = this.mapper.selectDistinctAges(PATTERN);
        assertEquals(distinctAges.size(), new HashSet<Integer>(distinctAges).size());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_AGGREGATE)
    public void queryResult_shouldMapAggregateScalarsAndMaps() throws Exception {
        Integer maxAge = this.mapper.selectMaxAge();
        Map<String, Object> stats = this.mapper.selectAgeStats(PATTERN);
        List<Map<String, Object>> grouped = this.mapper.selectCountByAge(PATTERN);

        assertTrue(maxAge >= 30);
        assertNotNull(value(stats, "minAge"));
        assertNotNull(value(stats, "maxAge"));
        assertNotNull(value(stats, "avgAge"));
        assertTrue(grouped.size() > 0);
        assertNotNull(value(grouped.get(0), "age"));
        assertNotNull(value(grouped.get(0), "cnt"));
    }
}
