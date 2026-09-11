/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6.material.user;
import java.util.List;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.page.Page;

@RefMapper("realdb/elastic6/material/user-mapper-5.xml")
public interface UserInfo5Mapper {
    int saveUser(@Param("info") UserInfo5 info);

    UserInfo5 loadUser(@Param("userId") String userId);

    int deleteUser(@Param("userId") String userId);

    List<UserInfo5> listByUserName(@Param("userName") String userName, Page page);
}
