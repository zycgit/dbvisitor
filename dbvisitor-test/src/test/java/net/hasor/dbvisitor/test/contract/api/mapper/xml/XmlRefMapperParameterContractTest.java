/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class XmlRefMapperParameterContractTest extends XmlRefMapperSupport {
    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_PARAMETER)
    public void refMapper_shouldBindMapAndBeanStyleParameters() throws Exception {
        Map<String, Object> range = new HashMap<>();
        range.put("minAge", 25);
        range.put("maxAge", 30);
        UserInfo sample = new UserInfo();
        sample.setName("RefMapB");
        sample.setAge(28);
        List<UserInfo> byRange = this.dao.selectByAgeRange(range);
        List<UserInfo> byBean = this.dao.selectByBean(sample);

        assertEquals(2, byRange.size());
        assertEquals("RefMapB", byRange.get(0).getName());
        assertEquals("RefMapD", byRange.get(1).getName());

        assertEquals(3, byBean.size());
        assertEquals("RefMapB", byBean.get(0).getName());
        assertEquals("RefMapC", byBean.get(1).getName());
        assertEquals("RefMapD", byBean.get(2).getName());
    }
}
