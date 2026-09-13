/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class XmlMapperCrudCase extends XmlMapperCrudSupport {
    @Test
    @Capability(CapabilityId.MAPPER_XML_CRUD_INSERT)
    public void xmlMapperInsert_shouldInsertAndReadStoredEntity() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", baseId() + 10);
        params.put("name", "XmlCrudInsert");
        params.put("age", 30);
        params.put("email", "insert@test.com");

        Object result = this.session.executeStatement("xmltest.CrudMapper.insertUser", params);
        assertEquals(1, ((Number) result).intValue());

        List<UserInfo> list = this.session.queryStatement("xmltest.CrudMapper.selectById", mapOf("id", baseId() + 10));
        assertEquals(1, list.size());
        assertEquals("XmlCrudInsert", list.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_CRUD_SELECT)
    public void xmlMapperSelect_shouldReturnStoredEntityAndMissingResult() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.CrudMapper.selectById", mapOf("id", baseId() + 1));
        List<UserInfo> missing = this.session.queryStatement("xmltest.CrudMapper.selectById", mapOf("id", baseId() + 999));

        assertEquals(1, list.size());
        UserInfo user = list.get(0);
        assertEquals(Integer.valueOf(baseId() + 1), user.getId());
        assertEquals("XmlCrud1", user.getName());
        assertEquals(Integer.valueOf(21), user.getAge());
        assertEquals("crud1@test.com", user.getEmail());
        assertNotNull(user.getCreateTime());
        assertTrue(missing.isEmpty());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_CRUD_UPDATE)
    public void xmlMapperUpdate_shouldUpdateSelectedRow() throws Exception {
        Map<String, Object> params = mapOf("id", baseId() + 2);
        params.put("email", "updated@test.com");

        Object result = this.session.executeStatement("xmltest.CrudMapper.updateEmail", params);
        assertEquals(1, ((Number) result).intValue());

        List<UserInfo> list = this.session.queryStatement("xmltest.CrudMapper.selectById", mapOf("id", baseId() + 2));
        assertEquals("updated@test.com", list.get(0).getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_CRUD_DELETE)
    public void xmlMapperDelete_shouldRemoveSelectedRow() throws Exception {
        Object result = this.session.executeStatement("xmltest.CrudMapper.deleteById", mapOf("id", baseId() + 3));
        assertEquals(1, ((Number) result).intValue());

        List<UserInfo> list = this.session.queryStatement("xmltest.CrudMapper.selectById", mapOf("id", baseId() + 3));
        assertTrue(list.isEmpty());
    }
}
