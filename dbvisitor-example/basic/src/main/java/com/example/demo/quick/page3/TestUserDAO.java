/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package com.example.demo.quick.page3;

import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.dialect.PageResult;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;

import java.util.List;

@RefMapper("/mapper/quick_page3/TestUserMapper.xml")
public interface TestUserDAO extends BaseMapper<TestUser> {

    List<TestUser> queryByAge1(@Param("beginAge") int beginAge, @Param("endAge") int endAge, Page pageInfo);

    PageResult<TestUser> queryByAge2(@Param("beginAge") int beginAge, @Param("endAge") int endAge, Page pageInfo);
}
