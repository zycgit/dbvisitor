/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.session;

import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class SessionStatementParameterCase extends SessionStatementSupport {
    @Test
    @Capability(CapabilityId.SESSION_STATEMENT_DYNAMIC_PARAMETER)
    public void sessionStatement_shouldBindDynamicMapAndBeanParameters() throws Exception {
        insertUser(baseId() + 20, "StmtDyn", 28, "dyn@nxn.test");
        insertUser(baseId() + 21, "StmtOther", 29, "other@nxn.test");

        List<UserInfo> dynamic = queryUsers("queryUsersByCondition", mapOf("name", "StmtDyn", "age", 28));
        assertEquals(1, dynamic.size());
        assertEquals(Integer.valueOf(baseId() + 20), dynamic.get(0).getId());

        UserInfo bean = new UserInfo();
        bean.setName("StmtDyn");
        bean.setAge(28);
        List<UserInfo> byBean = this.session.queryStatement(NS + ".queryUserByBean", bean);
        assertEquals(1, byBean.size());
        assertEquals(Integer.valueOf(baseId() + 20), byBean.get(0).getId());
    }
}
