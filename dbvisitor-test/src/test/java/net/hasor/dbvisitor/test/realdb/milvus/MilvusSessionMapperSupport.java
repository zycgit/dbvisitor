/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.util.List;
import net.hasor.dbvisitor.test.realdb.milvus.material.user.UserInfoMilvus1;

/** Shared entity material for Session Mapper scenarios. */
public abstract class MilvusSessionMapperSupport extends MilvusSessionSqlSupport {
    protected UserInfoMilvus1 user(String id, String name) {
        UserInfoMilvus1 user = new UserInfoMilvus1();
        user.setUid(id);
        user.setName(name);
        user.setLoginName("login");
        user.setLoginPassword("password");
        user.setV(List.of(1F, 0F));
        return user;
    }
}
