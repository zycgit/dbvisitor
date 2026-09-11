/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.session;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;

import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertNotNull;

public abstract class SessionStatementSupport extends AbstractNxnContractTest {
    protected static final String NS = "session.UserSessionMapper";

    protected Session session;

    @Before
    public void createStatementSession() throws Exception {
        Configuration configuration = newConfiguration();
        configuration.loadMapper("/session/UserSessionMapper.xml");
        this.session = configuration.newSession(dataSource);
    }

    protected int baseId() {
        return 814000;
    }

    protected int execute(String statementId, Map<String, Object> params) throws Exception {
        Object result = this.session.executeStatement(NS + "." + statementId, params);
        return ((Number) result).intValue();
    }

    protected void insertUser(int id, String name, int age, String email) throws Exception {
        execute("insertUser", userParams(id, name, age, email));
    }

    protected List<UserInfo> queryUsers(String statementId, Object params) throws Exception {
        return this.session.queryStatement(NS + "." + statementId, params);
    }

    protected Map<String, Object> userParams(int id, String name, int age, String email) {
        return mapOf("id", id, "name", name, "age", age, "email", email);
    }

    protected Map<String, Object> orderParams(int id, int userId, String orderNo, String amount) {
        return mapOf("id", id, "userId", userId, "orderNo", orderNo, "amount", new BigDecimal(amount));
    }

    protected PageObject page(int pageSize, int currentPage) {
        PageObject page = new PageObject();
        page.setPageSize(pageSize);
        page.setCurrentPage(currentPage);
        return page;
    }

    protected Map<String, Object> mapOf(Object... pairs) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], pairs[i + 1]);
        }
        return map;
    }

    protected void expectStatementFailure(StatementCall call) throws Exception {
        try {
            call.run();
        } catch (Exception e) {
            assertNotNull(e.getMessage());
            return;
        }
        throw new AssertionError("Expected statement call to fail.");
    }

    protected interface StatementCall {
        void run() throws Exception;
    }
}
