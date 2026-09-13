/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class AnnotationMapperCollectionRuleCase extends AnnotationMapperParameterBindingSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_DYNAMIC_IN)
    public void annotationMapperDynamicIn_shouldExpandArrayParameter() throws Exception {
        this.mapper.insertWithParam(baseId() + 11, "AnnoInOne", 61, "in1@test.com");
        this.mapper.insertWithParam(baseId() + 12, "AnnoInTwo", 62, "in2@test.com");
        this.mapper.insertWithParam(baseId() + 13, "AnnoInThree", 63, "in3@test.com");

        List<UserInfo> users = this.mapper.selectByAgesArray(new Integer[] { 61, 63 });

        assertEquals(2, users.size());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_IN_LIST)
    public void inRule_shouldExpandArrayAndListParameters() throws Exception {
        this.mapper.insertWithParam(baseId() + 81, "AnnoInArray1", 61, "a1@nxn.test");
        this.mapper.insertWithParam(baseId() + 82, "AnnoInArray2", 62, "a2@nxn.test");
        this.mapper.insertWithParam(baseId() + 83, "AnnoInList1", 71, "l1@nxn.test");
        this.mapper.insertWithParam(baseId() + 84, "AnnoInList2", 72, "l2@nxn.test");

        List<UserInfo> arrayUsers = this.mapper.selectByAgesArray(new Integer[] { 61, 62 });
        List<UserInfo> listUsers = this.mapper.selectByAgesList(Arrays.asList(71, 72));

        assertEquals(2, arrayUsers.size());
        assertEquals(2, listUsers.size());
    }
}
