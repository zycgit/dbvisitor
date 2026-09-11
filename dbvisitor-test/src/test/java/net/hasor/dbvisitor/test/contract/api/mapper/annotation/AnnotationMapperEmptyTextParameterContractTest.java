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
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class AnnotationMapperEmptyTextParameterContractTest extends AnnotationMapperParameterBindingSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_EMPTY_STRING)
    public void namedParameters_shouldDistinguishEmptyStringsFromNulls() throws Exception {
        requiresNxnFeature(FeatureId.DISTINCT_EMPTY_STRING);
        int emptyId = baseId() + 13;
        int nullId = baseId() + 15;
        assertEquals(1, this.mapper.insertWithParam(emptyId, "", 28, ""));
        assertEquals(1, this.mapper.insertWithParam(nullId, null, 28, null));

        UserInfo empty = this.mapper.selectById(emptyId);
        UserInfo nulls = this.mapper.selectById(nullId);
        assertNotNull(empty);
        assertNotNull(nulls);
        assertEquals("", empty.getName());
        assertEquals("", empty.getEmail());
        assertNull(nulls.getName());
        assertNull(nulls.getEmail());
    }
}
