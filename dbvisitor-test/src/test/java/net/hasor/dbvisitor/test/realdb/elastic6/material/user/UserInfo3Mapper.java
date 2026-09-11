/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6.material.user;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.test.realdb.elastic6.material.user.UserInfo1a;

@RefMapper("realdb/elastic6/material/user-mapper-3.xml")
public interface UserInfo3Mapper {
    int saveUser(@Param("info") UserInfo3 info);

    int updateName(@Param("uid") String uid, @Param("name") String name);

    UserInfo3 loadUser1(@Param("uid") String uid);

    UserInfo1a loadUser2(@Param("uid") String uid);

    int deleteUser(@Param("uid") String uid);
}
