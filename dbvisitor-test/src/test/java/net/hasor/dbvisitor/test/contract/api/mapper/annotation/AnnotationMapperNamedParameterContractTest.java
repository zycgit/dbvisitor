/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.util.Date;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class AnnotationMapperNamedParameterContractTest extends AnnotationMapperParameterBindingSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_NAMED)
    public void namedParameters_shouldBindByParamAnnotation() throws Exception {
        int fullId = baseId() + 11;

        assertEquals(1, this.mapper.insertWithParam(fullId, "AnnoNamed", 31, "named@nxn.test"));

        UserInfo full = this.mapper.selectById(fullId);
        assertEquals("AnnoNamed", full.getName());
        assertEquals(Integer.valueOf(31), full.getAge());
        assertEquals("named@nxn.test", full.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_REUSE)
    public void reusedParameter_shouldBindSameValueInMultiplePlaces() throws Exception {
        int id = baseId() + 51;

        assertEquals(1, this.mapper.insertWithReuse(id, "AnnoReuse"));

        UserInfo loaded = this.mapper.selectById(id);
        assertEquals("AnnoReuse", loaded.getName());
        assertEquals("AnnoReuse", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_MANY)
    public void manyParameters_shouldBindReferencedParamsAndIgnoreExtraParams() throws Exception {
        int id = baseId() + 61;

        assertEquals(1, this.mapper.insertWithManyParams(id, "AnnoMany", 35, "many@nxn.test", new Date(), "extra1", "extra2"));

        UserInfo loaded = this.mapper.selectById(id);
        assertEquals("AnnoMany", loaded.getName());
        assertEquals(Integer.valueOf(35), loaded.getAge());
        assertEquals("many@nxn.test", loaded.getEmail());
    }
}
