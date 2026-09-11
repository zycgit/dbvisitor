/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class XmlRefMapperCrudContractTest extends XmlRefMapperSupport {
    @Test
    @Capability(CapabilityId.MAPPER_XML_REF_CRUD)
    public void refMapper_shouldBindXmlStatementsToDaoCrudMethods() throws Exception {
        assertEquals(1, this.dao.insertUser(baseId() + 10, "RefMapNew", 33, "new@nxn.test"));

        UserInfo inserted = this.dao.selectById(baseId() + 10);
        assertEquals(Integer.valueOf(baseId() + 10), inserted.getId());
        assertEquals("RefMapNew", inserted.getName());
        assertEquals("new@nxn.test", inserted.getEmail());

        List<UserInfo> all = this.dao.selectAll();
        assertEquals(5, all.size());
        assertEquals("RefMapA", all.get(0).getName());

        assertEquals(1, this.dao.updateEmail(baseId() + 1, "updated@nxn.test"));
        assertEquals("updated@nxn.test", this.dao.selectById(baseId() + 1).getEmail());

        assertEquals(1, this.dao.deleteById(baseId() + 4));
        assertNull(this.dao.selectById(baseId() + 4));
    }
}
