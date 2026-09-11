/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Before;

import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

public abstract class BaseMapperCrudSupport extends AbstractNxnContractTest {
    protected BaseMapper<UserInfo> mapper;

    @Before
    public void createBaseMapper() throws SQLException {
        this.mapper = newSession().createBaseMapper(UserInfo.class);
    }

    protected int baseId() {
        return 900000;
    }

    protected UserInfo user(Integer id, String name, Integer age, String email) {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        user.setCreateTime(new Date());
        return user;
    }

}
