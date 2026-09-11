/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.xml;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.Before;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.XmlRefMapperDao;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertTrue;

public abstract class XmlRefMapperSupport extends AbstractNxnContractTest {
    protected XmlRefMapperDao dao;

    @Before
    public void createRefMapper() throws Exception {
        Configuration config = newConfiguration();
        Session session = config.newSession(dataSource);
        this.dao = session.createMapper(XmlRefMapperDao.class);
    }

    @Override
    protected void initData() throws SQLException {
        // @formatter:off
        Object[][] data = {
            { baseId() + 1, "RefMapA", 22, "refa@nxn.test" },
            { baseId() + 2, "RefMapB", 28, "refb@nxn.test" },
            { baseId() + 3, "RefMapC", 35, "refc@nxn.test" },
            { baseId() + 4, "RefMapD", 28, "refd@nxn.test" }
        };
        // @formatter:on
        for (Object[] row : data) {
            insertUser(row);
        }
    }

    protected void insertUser(Object[] values) throws SQLException {
        jdbcTemplate.executeUpdate(
                "INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, @{macro, currentTimestamp})", values);
    }

    protected String orderExpression(String field) {
        return field;
    }

    protected int baseId() {
        return 954000;
    }

    protected void assertAscendingById(List<UserInfo> list) {
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getId() < list.get(i).getId());
        }
    }

    protected Object value(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        String lower = key.toLowerCase();
        if (row.containsKey(lower)) {
            return row.get(lower);
        }
        return row.get(key.toUpperCase());
    }
}
