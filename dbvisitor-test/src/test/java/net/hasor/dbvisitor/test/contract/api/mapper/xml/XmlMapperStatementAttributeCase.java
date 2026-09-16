/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.*;

@NxnContract
public abstract class XmlMapperStatementAttributeCase extends XmlMapperStatementAttributeSupport {
    // 能力归属：Mapper 文件 / 执行选项。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_STATEMENT_TYPE, column = "mapper-files/statements/options")
    public void statementAttributes_shouldSupportPreparedAndStatementTypes() throws Exception {
        List<UserInfo> defaultPrepared = queryWithObservedOptions("xmltest.StatementAttrMapper.selectPrepared", mapOf("id", baseId() + 1));
        assertStatementFactory("prepareStatement");
        List<UserInfo> explicitPrepared = queryWithObservedOptions("xmltest.StatementAttrMapper.selectExplicitPrepared", mapOf("id", baseId() + 2));
        assertStatementFactory("prepareStatement");
        List<UserInfo> statement = queryWithObservedOptions("xmltest.StatementAttrMapper.selectStatement", null);
        assertStatementFactory("createStatement");

        assertEquals(1, defaultPrepared.size());
        assertEquals("StmtAttr1", defaultPrepared.get(0).getName());
        assertEquals(1, explicitPrepared.size());
        assertEquals("StmtAttr2", explicitPrepared.get(0).getName());
        assertEquals(5, statement.size());
        assertAscendingById(statement);
        assertTrue(this.session.queryStatement("xmltest.StatementAttrMapper.selectExplicitPrepared", mapOf("id", baseId() + 99)).isEmpty());
    }

    // 能力归属：Mapper 文件 / 执行选项。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_STATEMENT_TIMEOUT, column = "mapper-files/statements/options")
    public void statementAttributes_shouldApplyTimeoutAttributeWithoutChangingResults() throws Exception {
        List<UserInfo> list = queryWithObservedOptions("xmltest.StatementAttrMapper.selectWithTimeout", null);

        assertQueryTimeout(expectedQueryTimeout());
        assertEquals(5, list.size());
        assertAscendingById(list);
    }

    // 能力归属：Mapper 文件 / 执行选项。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_STATEMENT_FETCH_SIZE, column = "mapper-files/statements/options")
    public void statementAttributes_shouldApplyFetchSizeWithoutChangingResults() throws Exception {
        List<UserInfo> list = queryWithObservedOptions("xmltest.StatementAttrMapper.selectWithFetchSize", null);

        assertFetchSize(expectedFetchSize());
        assertEquals(5, list.size());
        assertTrue("fetchSize is a read hint, not a row limit", list.size() > expectedFetchSize());
        assertAscendingById(list);
    }

    // 能力归属：Mapper 文件 / 执行选项。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_STATEMENT_FORWARD_ONLY, column = "mapper-files/statements/options")
    public void statementAttributes_shouldSupportForwardOnlyResults() throws Exception {
        List<UserInfo> forwardOnly = queryWithObservedOptions("xmltest.StatementAttrMapper.selectForwardOnly", null);
        assertResultSetType(ResultSet.TYPE_FORWARD_ONLY);
        assertEquals(5, forwardOnly.size());
        assertAscendingById(forwardOnly);
    }

    // 能力归属：Mapper 文件 / 执行选项。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_STATEMENT_COMBINED, column = "mapper-files/statements/options")
    public void statementAttributes_shouldSupportCombinedAttributesAndDml() throws Exception {
        List<UserInfo> combined = queryWithObservedOptions("xmltest.StatementAttrMapper.selectCombined", null);
        assertStatementFactory("prepareStatement");
        assertQueryTimeout(expectedQueryTimeout());
        assertFetchSize(expectedCombinedFetchSize());
        assertResultSetType(ResultSet.TYPE_FORWARD_ONLY);
        assertEquals(5, combined.size());
        for (UserInfo user : combined) {
            assertNotNull(user.getId());
            assertNotNull(user.getName());
        }

        Map<String, Object> params = mapOf("id", baseId() + 10);
        params.put("name", "StmtAttrInsert '\" 中文");
        params.put("age", 30);
        params.put("email", "stmt@nxn.test");

        Object result = executeWithObservedOptions("xmltest.StatementAttrMapper.insertWithStatement", params);
        assertStatementFactory("prepareStatement");
        assertEquals(1, ((Number) result).intValue());
        List<UserInfo> inserted = this.session.queryStatement("xmltest.StatementAttrMapper.selectPrepared", mapOf("id", baseId() + 10));
        assertEquals(1, inserted.size());
        assertEquals("StmtAttrInsert '\" 中文", inserted.get(0).getName());
    }
}
