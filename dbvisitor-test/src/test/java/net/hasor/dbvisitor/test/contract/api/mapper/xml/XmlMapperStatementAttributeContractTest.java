/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class XmlMapperStatementAttributeContractTest extends XmlMapperStatementAttributeSupport {
    @Test
    @Capability(CapabilityId.MAPPER_XML_STATEMENT_TYPE)
    public void statementAttributes_shouldSupportPreparedAndStatementTypes() throws Exception {
        List<UserInfo> defaultPrepared = this.session.queryStatement("xmltest.StatementAttrMapper.selectPrepared", mapOf("id", baseId() + 1));
        List<UserInfo> explicitPrepared = this.session.queryStatement("xmltest.StatementAttrMapper.selectExplicitPrepared", mapOf("id", baseId() + 2));
        List<UserInfo> statement = this.session.queryStatement("xmltest.StatementAttrMapper.selectStatement", null);

        assertEquals(1, defaultPrepared.size());
        assertEquals("StmtAttr1", defaultPrepared.get(0).getName());
        assertEquals(1, explicitPrepared.size());
        assertEquals("StmtAttr2", explicitPrepared.get(0).getName());
        assertEquals(5, statement.size());
        assertAscendingById(statement);
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_STATEMENT_TIMEOUT)
    public void statementAttributes_shouldApplyTimeoutAttributeWithoutChangingResults() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.StatementAttrMapper.selectWithTimeout", null);

        assertEquals(5, list.size());
        assertAscendingById(list);
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_STATEMENT_FETCH_SIZE)
    public void statementAttributes_shouldApplyFetchSizeWithoutChangingResults() throws Exception {
        List<UserInfo> list = this.session.queryStatement("xmltest.StatementAttrMapper.selectWithFetchSize", null);

        assertEquals(5, list.size());
        assertAscendingById(list);
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_STATEMENT_FORWARD_ONLY)
    public void statementAttributes_shouldSupportForwardOnlyResults() throws Exception {
        List<UserInfo> forwardOnly = this.session.queryStatement("xmltest.StatementAttrMapper.selectForwardOnly", null);
        assertEquals(5, forwardOnly.size());
        assertAscendingById(forwardOnly);
    }

    @Test
    @Capability(CapabilityId.MAPPER_XML_STATEMENT_COMBINED)
    public void statementAttributes_shouldSupportCombinedAttributesAndDml() throws Exception {
        List<UserInfo> combined = this.session.queryStatement("xmltest.StatementAttrMapper.selectCombined", null);
        assertEquals(5, combined.size());
        for (UserInfo user : combined) {
            assertNotNull(user.getId());
            assertNotNull(user.getName());
        }

        Map<String, Object> params = mapOf("id", baseId() + 10);
        params.put("name", "StmtAttrInsert");
        params.put("age", 30);
        params.put("email", "stmt@nxn.test");

        Object result = this.session.executeStatement("xmltest.StatementAttrMapper.insertWithStatement", params);
        assertEquals(1, ((Number) result).intValue());
        List<UserInfo> inserted = this.session.queryStatement("xmltest.StatementAttrMapper.selectPrepared", mapOf("id", baseId() + 10));
        assertEquals(1, inserted.size());
        assertEquals("StmtAttrInsert", inserted.get(0).getName());
    }
}
