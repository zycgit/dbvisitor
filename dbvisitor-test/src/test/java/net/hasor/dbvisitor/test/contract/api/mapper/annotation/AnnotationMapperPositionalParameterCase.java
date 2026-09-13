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

@NxnContract
public abstract class AnnotationMapperPositionalParameterCase extends AnnotationMapperParameterBindingSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_POSITIONAL)
    public void positionalParameters_shouldBindByDeclarationOrder() throws Exception {
        int id = baseId() + 1;
        assertEquals(1, this.mapper.insertByPosition(id, "AnnoPositional", 28));
        assertEquals(1, this.mapper.updateByPosition(29, id));

        UserInfo loaded = this.mapper.selectById(id);
        assertNotNull(loaded);
        assertEquals("AnnoPositional", loaded.getName());
        assertEquals(Integer.valueOf(29), loaded.getAge());
    }
}
