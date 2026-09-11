/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7.material.user;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.test.realdb.elastic7.material.user.UserInfo1a;

@RefMapper("realdb/elastic7/material/user-mapper-4.xml")
public interface UserInfo4Mapper {
    int saveUser(@Param("info") UserInfo1a info);

    UserInfo1a loadUser(@Param("uid") String uid);

    int deleteUser(@Param("uid") String uid);
}
