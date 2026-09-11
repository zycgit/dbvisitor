/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class XmlRefMapperAggregateContractTest extends XmlRefMapperSupport {
    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_AGGREGATE_MAP)
    public void refMapper_shouldMapGroupedAggregateResults() throws Exception {
        List<Map<String, Object>> stats = this.dao.selectAgeStats();

        assertTrue(stats.size() >= 3);
        boolean foundAge28 = false;
        for (Map<String, Object> row : stats) {
            Number age = (Number) value(row, "age");
            if (age.intValue() == 28) {
                assertEquals(2L, ((Number) value(row, "cnt")).longValue());
                foundAge28 = true;
            }
        }
        assertTrue(foundAge28);
    }
}
