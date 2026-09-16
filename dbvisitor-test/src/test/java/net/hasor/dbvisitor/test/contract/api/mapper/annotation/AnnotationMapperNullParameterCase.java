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
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class AnnotationMapperNullParameterCase extends AnnotationMapperParameterBindingSupport {
    // 能力归属：参数传递与规则 / 名称参数 / 方法注解。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_PARAM_NULL, column = "parameters/positional-and-named-parameters/named")
    public void namedParameters_shouldPreserveNullValues() throws Exception {
        int nullId = baseId() + 12;
        assertEquals(1, this.mapper.insertWithParam(nullId, "AnnoNamedNull", null, null));

        UserInfo nulls = this.mapper.selectById(nullId);
        assertNull(nulls.getAge());
        assertNull(nulls.getEmail());
    }
}
