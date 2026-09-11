/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class AnnotationMapperStructuredParameterContractTest extends AnnotationMapperParameterBindingSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_BEAN)
    public void beanParameter_shouldExpandProperties() throws Exception {
        int id = baseId() + 21;
        UserInfo user = user(id, "AnnoBean", 32, "bean@nxn.test");

        assertEquals(1, this.mapper.insertBean(user));

        UserInfo loaded = this.mapper.selectById(id);
        assertNotNull(loaded);
        assertEquals("AnnoBean", loaded.getName());
        assertEquals(Integer.valueOf(32), loaded.getAge());
        assertNotNull(loaded.getCreateTime());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_MAP)
    public void mapParameter_shouldBindByMapKeys() throws Exception {
        int id = baseId() + 31;
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("name", "AnnoMap");
        params.put("age", 33);
        params.put("email", "map@nxn.test");

        assertEquals(1, this.mapper.insertByMap(params));

        UserInfo loaded = this.mapper.selectById(id);
        assertEquals("AnnoMap", loaded.getName());
        assertEquals(Integer.valueOf(33), loaded.getAge());
        assertEquals("map@nxn.test", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_PARAM_MIXED)
    public void mixedParameters_shouldBindNestedBeanAndScalarParams() throws Exception {
        int id = baseId() + 41;
        UserInfo user = user(id, "AnnoMixed", 34, null);

        assertEquals(1, this.mapper.insertMixed(user, "mixed@nxn.test"));

        UserInfo loaded = this.mapper.selectById(id);
        assertEquals("AnnoMixed", loaded.getName());
        assertEquals(Integer.valueOf(34), loaded.getAge());
        assertEquals("mixed@nxn.test", loaded.getEmail());
    }
}
