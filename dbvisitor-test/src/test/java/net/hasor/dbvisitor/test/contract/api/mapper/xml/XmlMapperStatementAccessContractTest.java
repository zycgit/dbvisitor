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

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class XmlMapperStatementAccessContractTest extends XmlMapperCrudSupport {
    @Test
    @Capability(CapabilityId.MAPPER_XML_LOAD)
    public void xmlMapperLoad_shouldExposeMappedStatement() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.CrudMapper.selectAll", null);

        assertEquals(5, list.size());
        assertEquals("XmlCrud1", list.get(0).getName());
        assertEquals("XmlCrud5", list.get(4).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_CRUD_EXECUTE)
    public void xmlMapperExecute_shouldRunDmlStatementAndReturnAffectedRows() throws Exception {
        Object result = this.session.executeStatement("xmltest.CrudMapper.deleteByName", mapOf("name", "XmlCrud1"));

        List<Integer> count = this.session.queryStatement("xmltest.CrudMapper.countAll", null);
        List<UserInfo> deleted = this.session.queryStatement("xmltest.CrudMapper.selectById", mapOf("id", baseId() + 1));

        assertEquals(1, ((Number) result).intValue());
        assertEquals(4, count.get(0).intValue());
        assertTrue(deleted.isEmpty());
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_NAMESPACE_MULTIPLE)
    public void xmlMapperNamespace_shouldResolveStatementsAcrossMultipleLoadedMappers() throws Exception {
        Session multiMapperSession = createMultiNamespaceSession();
        List<UserInfo> fromCrud = multiMapperSession.queryStatement("xmltest.CrudMapper.selectById", mapOf("id", baseId() + 1));
        List<UserInfo> fromResultMap = multiMapperSession.queryStatement("xmltest.ResultMapMapper.selectByIdExtended", mapOf("id", baseId() + 1));

        assertEquals(1, fromCrud.size());
        assertEquals(1, fromResultMap.size());
        assertEquals(fromCrud.get(0).getName(), fromResultMap.get(0).getName());
    }

    protected Session createMultiNamespaceSession() throws Exception {
        Configuration config = newConfiguration();
        config.loadMapper("/mapper/XmlCrudMapper.xml");
        config.loadMapper("/mapper/XmlResultMapMapper.xml");
        return config.newSession(dataSource);
    }
}
