/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.service;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.transaction.Transactional;

/** Class defaults and method overrides exercise the real transaction proxy. */
@Transactional(readOnly = true)
public class ClassLevelTransactionService {
    private final LambdaTemplate lambdaTemplate;

    public ClassLevelTransactionService() {
        this.lambdaTemplate = null;
    }

    public ClassLevelTransactionService(LambdaTemplate lambdaTemplate) {
        this.lambdaTemplate = lambdaTemplate;
    }

    public int createUserWithClassDefaults(int id, String name) throws SQLException {
        return insertUser(id, name);
    }

    @Transactional
    public int createUserWithMethodOverride(int id, String name) throws SQLException {
        return insertUser(id, name);
    }

    private int insertUser(int id, String name) throws SQLException {
        UserInfo user = new UserInfo();
        user.setId(id);
        user.setName(name);
        user.setCreateTime(new Date());
        return this.lambdaTemplate.insert(UserInfo.class).applyEntity(user).executeSumResult();
    }
}
