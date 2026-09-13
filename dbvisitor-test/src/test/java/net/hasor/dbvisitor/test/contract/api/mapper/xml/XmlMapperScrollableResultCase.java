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

@NxnContract
public abstract class XmlMapperScrollableResultCase extends XmlMapperStatementAttributeSupport {
    @Test
    @Capability(CapabilityId.MAPPER_XML_STATEMENT_RESULT_SET_TYPE)
    public void statementAttributes_shouldSupportScrollableResults() throws Exception {
        List<UserInfo> scrollInsensitive = this.session.queryStatement("xmltest.StatementAttrMapper.selectScrollInsensitive", null);
        assertEquals(5, scrollInsensitive.size());
        assertAscendingById(scrollInsensitive);
    }
}
