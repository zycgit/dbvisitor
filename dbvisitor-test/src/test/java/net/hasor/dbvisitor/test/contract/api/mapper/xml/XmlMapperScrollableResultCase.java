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
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class XmlMapperScrollableResultCase extends XmlMapperStatementAttributeSupport {
    // 能力归属：Mapper 文件 / 执行选项。
    @Test
    @Capability(value = CapabilityId.MAPPER_XML_STATEMENT_RESULT_SET_TYPE, column = "mapper-files/statements/options")
    public void statementAttributes_shouldSupportScrollableResults() throws Exception {
        List<UserInfo> scrollInsensitive = queryWithObservedOptions("xmltest.StatementAttrMapper.selectScrollInsensitive", null);
        assertResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE);
        assertEquals(5, scrollInsensitive.size());
        assertAscendingById(scrollInsensitive);
    }
}
