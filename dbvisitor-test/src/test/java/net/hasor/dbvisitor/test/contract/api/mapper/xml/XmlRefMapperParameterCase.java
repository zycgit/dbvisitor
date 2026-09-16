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
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class XmlRefMapperParameterCase extends XmlRefMapperSupport {
    // 能力归属：Mapper 文件 / 调用文件 Mapper。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_REF_PARAMETER, column = "mapper-files/external-mapper-references/calls")
    public void refMapper_shouldBindMapAndBeanStyleParameters() throws Exception {
        Map<String, Object> range = rangeParameters();
        UserInfo sample = beanParameters();
        List<UserInfo> byRange = this.dao.selectByAgeRange(range);
        List<UserInfo> byBean = this.dao.selectByBean(sample);

        assertEquals(expectedRangeNames(), byRange.stream().map(UserInfo::getName).toList());
        assertEquals(expectedBeanNames(), byBean.stream().map(UserInfo::getName).toList());
    }

    protected Map<String, Object> rangeParameters() {
        return Map.of("minAge", 25, "maxAge", 30);
    }

    protected UserInfo beanParameters() {
        UserInfo sample = new UserInfo();
        sample.setName("RefMapB");
        sample.setAge(28);
        return sample;
    }

    protected List<String> expectedRangeNames() {
        return List.of("RefMapB", "RefMapD");
    }

    protected List<String> expectedBeanNames() {
        return List.of("RefMapB", "RefMapC", "RefMapD");
    }
}
