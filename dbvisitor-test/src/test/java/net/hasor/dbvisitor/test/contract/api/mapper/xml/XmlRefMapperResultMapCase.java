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
public abstract class XmlRefMapperResultMapCase extends XmlRefMapperSupport {
    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_RESULT_MAP)
    public void refMapper_shouldMapRowsWithoutAggregatePrerequisites() throws Exception {
        List<Map<String, Object>> rows = this.dao.selectAsMaps();
        assertEquals(4, rows.size());
        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            for (Map.Entry<String, Object> entry : expectedRow(i).entrySet()) {
                Object actual = value(row, entry.getKey());
                if (entry.getValue() instanceof Number) {
                    assertEquals(((Number) entry.getValue()).doubleValue(), ((Number) actual).doubleValue(), 0.0);
                } else {
                    assertEquals(entry.getValue(), actual);
                }
            }
            for (String column : presentColumns()) {
                assertNotNull(value(row, column));
            }
        }
    }

    protected Map<String, Object> expectedRow(int index) {
        int[] ages = { 22, 28, 35, 28 };
        char suffix = (char) ('A' + index);
        return Map.of("id", baseId() + index + 1, "name", "RefMap" + suffix, "age", ages[index],
                "email", "ref" + Character.toLowerCase(suffix) + "@nxn.test");
    }

    protected List<String> presentColumns() {
        return List.of("create_time");
    }
}
