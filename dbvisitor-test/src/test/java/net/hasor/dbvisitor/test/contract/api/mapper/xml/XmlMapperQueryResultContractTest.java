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
public abstract class XmlMapperQueryResultContractTest extends XmlMapperCrudSupport {
    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULT_MAP)
    public void xmlMapperResultTypeMap_shouldReturnColumnMap() throws Exception {
        List<Map<String, Object>> list = this.session.queryStatement("xmltest.CrudMapper.selectAllAsMap", null);

        assertEquals(5, list.size());
        Map<String, Object> row = list.get(0);
        assertNotNull(value(row, "id"));
        assertNotNull(value(row, "name"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_RESULT_SCALAR)
    public void xmlMapperResultTypeScalar_shouldReturnCountAndNames() throws Exception {
        List<Integer> count = this.session.queryStatement("xmltest.CrudMapper.countAll", null);
        List<String> names = this.session.queryStatement("xmltest.CrudMapper.selectNames", null);

        assertEquals(1, count.size());
        assertEquals(5, count.get(0).intValue());
        assertEquals(5, names.size());
        assertEquals("XmlCrud1", names.get(0));
        assertEquals("XmlCrud5", names.get(4));
    }
}
