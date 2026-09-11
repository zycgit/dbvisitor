/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package com.example.demo.quick.dao3;

import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;

import java.util.List;

@RefMapper("/mapper/quick_dao3/TestUserMapper.xml")
public interface TestUserDAO extends BaseMapper<TestUser> {

    int insertUser(@Param("name") String name, @Param("age") int age);

    int updateAge(@Param("id") int userId, @Param("age") int newAge);

    int deleteByAge(@Param("age") int age);

    List<TestUser> queryByAge(@Param("beginAge") int beginAge, @Param("endAge") int endAge);

    List<TestUser> queryAll();
}
