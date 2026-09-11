/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class XmlRefMapperTemplateContractTest extends XmlRefMapperSupport {
    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_DYNAMIC)
    public void refMapper_shouldRunXmlDynamicWhereStatements() throws Exception {
        List<UserInfo> all = this.dao.selectByCondition(null, null);
        List<UserInfo> byName = this.dao.selectByCondition("RefMapA", null);
        List<UserInfo> byNameAndAge = this.dao.selectByCondition("RefMap%", 30);

        assertEquals(4, all.size());
        assertEquals(1, byName.size());
        assertEquals("RefMapA", byName.get(0).getName());
        assertEquals(1, byNameAndAge.size());
        assertEquals("RefMapC", byNameAndAge.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_FOREACH)
    public void refMapper_shouldExpandForeachInClause() throws Exception {
        List<UserInfo> list = this.dao.selectByIds(Arrays.asList(baseId() + 1, baseId() + 3));

        assertEquals(2, list.size());
        assertEquals("RefMapA", list.get(0).getName());
        assertEquals("RefMapC", list.get(1).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_TEXT_AND_MAP)
    public void refMapper_shouldSupportTextReplacement() throws Exception {
        List<UserInfo> byId = this.dao.selectWithOrderBy(orderExpression("id"));
        List<UserInfo> byAge = this.dao.selectWithOrderBy(orderExpression("age"));

        assertEquals(4, byId.size());
        assertAscendingById(byId);

        assertEquals(4, byAge.size());
        for (int i = 1; i < byAge.size(); i++) {
            assertTrue(byAge.get(i - 1).getAge() <= byAge.get(i).getAge());
        }
    }
}
