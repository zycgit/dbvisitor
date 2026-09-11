/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus.material.user;

import java.util.List;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;

@RefMapper("realdb/milvus/material/user-mapper-milvus-3.xml")
public interface UserInfoMilvus3Mapper {
    int insertUser(UserInfoMilvus3 user);

    List<UserInfoMilvus3> queryAll();

    int countAll();

    UserInfoMilvus3 selectUser(@Param("uid") String uid);

    int updateName(@Param("uid") String uid, @Param("name") String name);

    int deleteUser(@Param("uid") String uid);
}
