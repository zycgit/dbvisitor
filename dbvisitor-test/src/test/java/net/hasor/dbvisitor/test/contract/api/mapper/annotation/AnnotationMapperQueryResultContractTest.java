/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class AnnotationMapperQueryResultContractTest extends AnnotationMapperCrudSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_QUERY)
    public void annotationMapperQuery_shouldReturnObjectListAndScalar() throws Exception {
        this.mapper.insertUserWithParams(baseId() + 8, "AnnoQueryOne", 51, "q1@test.com");
        this.mapper.insertUserWithParams(baseId() + 9, "AnnoQueryTwo", 51, "q2@test.com");
        this.mapper.insertUserWithParams(baseId() + 10, "OtherQuery", 52, "q3@test.com");

        List<UserInfo> byAge = this.mapper.selectByAge(51);
        List<UserInfo> byName = this.mapper.selectByNameLike("AnnoQuery%");

        assertEquals(2, byAge.size());
        assertEquals(2, this.mapper.countByAge(51));
        assertEquals(2, byName.size());
    }
}
