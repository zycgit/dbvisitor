/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class AnnotationMapperNullValueCase extends AnnotationMapperBoundarySupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_NULL_VALUE)
    public void annotationMapperInsert_shouldPreserveNullColumnValues() throws Exception {
        int id = baseId() + 1;

        assertEquals(1, this.mapper.insertUserWithParams(id, "AnnoNull", null, null));

        UserInfo loaded = this.mapper.selectById(id);
        assertNotNull(loaded);
        assertEquals("AnnoNull", loaded.getName());
        assertNull(loaded.getAge());
        assertNull(loaded.getEmail());
    }
}
