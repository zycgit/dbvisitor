/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class AnnotationMapperCommandAssemblyCase extends AnnotationMapperCrudSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_INSERT_NAMED_PARAMS)
    public void annotationMapperInsert_shouldBindNamedParamsAndMultilineSql() throws Exception {
        int first = this.mapper.insertUserWithParams(baseId() + 2, "AnnoParams", 28, "params@test.com");
        int second = this.mapper.insertUserMultiLine(user(baseId() + 3, "AnnoMultiline", 27, "multi@test.com"));

        assertEquals(1, first);
        assertEquals(1, second);
        assertEquals("AnnoParams", this.mapper.selectById(baseId() + 2).getName());
        assertEquals("AnnoMultiline", this.mapper.selectById(baseId() + 3).getName());
    }
}
