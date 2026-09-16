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
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class AnnotationMapperCollectionRuleCase extends AnnotationMapperParameterBindingSupport {
    // 能力归属：参数传递与规则 / SQL 片段规则 / 方法注解集合展开。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_DYNAMIC_IN, column = "parameters/command-rules/sql-fragments")
    public void annotationMapperDynamicIn_shouldExpandArrayParameter() throws Exception {
        this.mapper.insertWithParam(baseId() + 11, "AnnoInOne", 61, "in1@test.com");
        this.mapper.insertWithParam(baseId() + 12, "AnnoInTwo", 62, "in2@test.com");
        this.mapper.insertWithParam(baseId() + 13, "AnnoInThree", 63, "in3@test.com");

        List<UserInfo> users = this.mapper.selectByAgesArray(new Integer[] { 61, 63 });

        assertEquals(2, users.size());
        assertEquals(new HashSet<>(Arrays.asList(baseId() + 11, baseId() + 13)), ids(users));
    }

    // 能力归属：参数传递与规则 / SQL 片段规则 / 方法注解集合展开。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_PARAM_IN_LIST, column = "parameters/command-rules/sql-fragments")
    public void inRule_shouldExpandArrayAndListParameters() throws Exception {
        this.mapper.insertWithParam(baseId() + 81, "AnnoInArray1", 61, "a1@nxn.test");
        this.mapper.insertWithParam(baseId() + 82, "AnnoInArray2", 62, "a2@nxn.test");
        this.mapper.insertWithParam(baseId() + 83, "AnnoInList1", 71, "l1@nxn.test");
        this.mapper.insertWithParam(baseId() + 84, "AnnoInList2", 72, "l2@nxn.test");

        List<UserInfo> arrayUsers = this.mapper.selectByAgesArray(new Integer[] { 61, 62 });
        List<UserInfo> listUsers = this.mapper.selectByAgesList(Arrays.asList(71, 72));

        assertEquals(2, arrayUsers.size());
        assertEquals(2, listUsers.size());
        assertEquals(new HashSet<>(Arrays.asList(baseId() + 81, baseId() + 82)), ids(arrayUsers));
        assertEquals(new HashSet<>(Arrays.asList(baseId() + 83, baseId() + 84)), ids(listUsers));
    }

    private Set<Integer> ids(List<UserInfo> users) {
        Set<Integer> ids = new HashSet<>();
        for (UserInfo user : users) {
            ids.add(user.getId());
        }
        return ids;
    }
}
