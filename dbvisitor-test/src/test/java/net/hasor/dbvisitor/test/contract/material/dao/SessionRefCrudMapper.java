/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.dao;

import java.util.List;

import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

/** XML CRUD material shared independently of join-query support. */
public interface SessionRefCrudMapper {
    UserInfo queryUserById(@Param("id") Integer id);

    List<UserInfo> queryAllUsers();

    int insertUser(UserInfo user);

    int updateUserEmail(@Param("id") Integer id, @Param("email") String email);

    int deleteUserById(@Param("id") Integer id);

    int countUsers();
}
