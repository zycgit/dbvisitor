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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class AnnotationMapperCrudCase extends AnnotationMapperCrudSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_INSERT)
    public void annotationMapperInsert_shouldBindBeanParam() throws Exception {
        UserInfo user = user(baseId() + 1, "AnnoInsert", 25, "insert@test.com");
        user.setCreateTime(new Date());

        int result = this.mapper.insertUser(user);
        UserInfo loaded = this.mapper.selectById(baseId() + 1);

        assertEquals(1, result);
        assertNotNull(loaded);
        assertEquals("AnnoInsert", loaded.getName());
        assertEquals(Integer.valueOf(25), loaded.getAge());
        assertEquals("insert@test.com", loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_UPDATE)
    public void annotationMapperUpdate_shouldUpdateSingleAndMultipleFields() throws Exception {
        this.mapper.insertUserWithParams(baseId() + 4, "AnnoUpdate", 30, "update@test.com");

        assertEquals(1, this.mapper.updateUserAge(baseId() + 4, 35));
        assertEquals(Integer.valueOf(35), this.mapper.selectById(baseId() + 4).getAge());

        assertEquals(1, this.mapper.updateUserInfo(baseId() + 4, "AnnoUpdated", 36));
        UserInfo loaded = this.mapper.selectById(baseId() + 4);
        assertEquals("AnnoUpdated", loaded.getName());
        assertEquals(Integer.valueOf(36), loaded.getAge());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_DELETE)
    public void annotationMapperDelete_shouldDeleteById() throws Exception {
        this.mapper.insertUserWithParams(baseId() + 5, "AnnoDeleteOne", 41, "d1@test.com");

        assertEquals(1, this.mapper.deleteById(baseId() + 5));
        assertNull(this.mapper.selectById(baseId() + 5));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_CRUD_SELECT)
    public void annotationMapperSelect_shouldReturnStoredEntityAndMissingResult() throws Exception {
        this.mapper.insertUserWithParams(baseId() + 8, "AnnoQueryOne", 51, "q1@test.com");

        UserInfo loaded = this.mapper.selectById(baseId() + 8);
        assertNotNull(loaded);
        assertEquals(Integer.valueOf(baseId() + 8), loaded.getId());
        assertEquals("AnnoQueryOne", loaded.getName());
        assertEquals(Integer.valueOf(51), loaded.getAge());
        assertEquals("q1@test.com", loaded.getEmail());
        assertNull(this.mapper.selectById(baseId() + 999));
    }
}
