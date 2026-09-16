/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class AnnotationMapperSpecialTextParameterCase extends AnnotationMapperParameterBindingSupport {
    // 能力归属：参数传递与规则 / 名称参数 / 方法注解。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_PARAM_SPECIAL_STRING, column = "parameters/positional-and-named-parameters/named")
    public void namedParameters_shouldPreserveQuotesAndBackslashes() throws Exception {
        int specialId = baseId() + 14;
        String specialName = "Anno'Quote\"Double\\Slash";
        assertEquals(1, this.mapper.insertWithParam(specialId, specialName, 29, "special@nxn.test"));

        UserInfo special = this.mapper.selectById(specialId);
        assertEquals(specialName, special.getName());
    }
}
