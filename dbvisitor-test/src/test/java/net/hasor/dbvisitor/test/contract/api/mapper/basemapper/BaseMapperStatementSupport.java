/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;

import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class BaseMapperStatementSupport extends AbstractNxnContractTest {
    protected static final String NS = "StatementTestMapper";

    protected BaseMapper<UserInfo> mapper;

    @Before
    public void createBaseMapperWithStatements() throws Exception {
        Configuration configuration = newConfiguration();
        configuration.loadMapper("/mapper/StatementTestMapper.xml");
        Session session = configuration.newSession(dataSource);
        this.mapper = session.createBaseMapper(UserInfo.class);
    }

    protected int baseId() {
        return 919000;
    }

    protected void insert(int id, String name, int age) {
        assertEquals(1, execute("insertUserWithId", userParams(id, name, age, name.toLowerCase() + "@nxn.test")));
    }

    protected int execute(String statementId, Map<String, Object> params) {
        Object result = this.mapper.executeStatement(NS + "." + statementId, params);
        return ((Number) result).intValue();
    }

    protected UserInfo queryOne(String statementId, Object params) {
        List<UserInfo> list = query(statementId, params);
        assertEquals(1, list.size());
        return list.get(0);
    }

    protected List<UserInfo> query(String statementId, Object params) {
        return this.mapper.queryStatement(NS + "." + statementId, params);
    }

    protected Map<String, Object> userParams(int id, String name, int age, String email) {
        return mapOf("id", id, "name", name, "age", age, "email", email);
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

    protected void expectStatementFailure(StatementCall call) {
        try {
            call.run();
        } catch (Exception e) {
            assertNotNull(e.getMessage());
            return;
        }
        throw new AssertionError("Expected statement call to fail.");
    }

    protected interface StatementCall {
        void run();
    }
}
