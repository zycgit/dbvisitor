/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Before;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.ResultHandlerMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

public abstract class AnnotationMapperResultHandlerSupport extends AbstractNxnContractTest {
    protected static final String PATTERN = "AnnoHandler%";

    protected ResultHandlerMapper mapper;

    @Before
    public void createResultHandlerMapper() throws Exception {
        Configuration configuration = newConfiguration();
        Session session = configuration.newSession(dataSource);
        this.mapper = session.createMapper(ResultHandlerMapper.class);
        prepareRows();
    }

    protected void prepareRows() throws SQLException {
        for (int i = 1; i <= 10; i++) {
            UserInfo user = new UserInfo();
            user.setId(id(i));
            user.setName("AnnoHandler" + i);
            user.setAge(20 + i);
            user.setEmail("anno-handler" + i + "@nxn.test");
            user.setCreateTime(new Date());
            this.mapper.insertUser(user);
        }
    }

    protected int id(int index) {
        return 53100 + index;
    }
}
