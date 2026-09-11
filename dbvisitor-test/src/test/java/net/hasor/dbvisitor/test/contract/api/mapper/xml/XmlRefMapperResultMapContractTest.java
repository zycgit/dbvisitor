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
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class XmlRefMapperResultMapContractTest extends XmlRefMapperSupport {
    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_RESULT_MAP)
    public void refMapper_shouldMapRowsWithoutAggregatePrerequisites() throws Exception {
        List<Map<String, Object>> rows = this.dao.selectAsMaps();
        assertEquals(4, rows.size());
        int[] ages = { 22, 28, 35, 28 };
        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            char suffix = (char) ('A' + i);
            assertEquals(baseId() + i + 1, ((Number) value(row, "id")).intValue());
            assertEquals("RefMap" + suffix, value(row, "name"));
            assertEquals(ages[i], ((Number) value(row, "age")).intValue());
            assertEquals("ref" + Character.toLowerCase(suffix) + "@nxn.test", value(row, "email"));
            assertNotNull(value(row, "create_time"));
        }
    }
}
