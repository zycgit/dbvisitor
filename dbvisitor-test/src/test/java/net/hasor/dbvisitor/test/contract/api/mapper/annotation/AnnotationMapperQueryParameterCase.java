/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class AnnotationMapperQueryParameterCase extends AnnotationMapperParameterBindingSupport {
    // 能力归属：参数传递与规则 / 名称参数 / 方法注解。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_PARAM_RANGE, column = "parameters/positional-and-named-parameters/named")
    public void rangeQuery_shouldBindMultipleNamedParams() throws Exception {
        this.mapper.insertWithParam(baseId() + 71, "AnnoRange1", 20, "r1@nxn.test");
        this.mapper.insertWithParam(baseId() + 72, "AnnoRange2", 25, "r2@nxn.test");
        this.mapper.insertWithParam(baseId() + 73, "AnnoRange3", 30, "r3@nxn.test");
        this.mapper.insertWithParam(baseId() + 74, "AnnoRangeBelow", 19, "below@nxn.test");
        this.mapper.insertWithParam(baseId() + 75, "AnnoRangeAbove", 31, "above@nxn.test");

        List<UserInfo> users = this.mapper.selectByAgeRange(20, 30);

        assertTrue(users.size() >= 3);
        Set<Integer> ids = new HashSet<>();
        for (UserInfo user : users) {
            assertTrue(user.getAge() >= 20 && user.getAge() <= 30);
            ids.add(user.getId());
        }
        assertTrue(ids.containsAll(Arrays.asList(baseId() + 71, baseId() + 72, baseId() + 73)));
    }
}
