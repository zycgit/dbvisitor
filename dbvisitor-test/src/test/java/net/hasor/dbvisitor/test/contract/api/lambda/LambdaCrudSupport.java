/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import java.util.Date;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcCrudSupport;

public abstract class LambdaCrudSupport extends JdbcCrudSupport {
    protected int baseId() {
        return 620000;
    }

    protected void insertByJdbc(int id, String name, int age, String email) throws SQLException {
        insertUser(id, name, age, email);
    }

    protected UserInfo user(Integer id, String name, int age, String email) {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        user.setCreateTime(new Date());
        return user;
    }
}
