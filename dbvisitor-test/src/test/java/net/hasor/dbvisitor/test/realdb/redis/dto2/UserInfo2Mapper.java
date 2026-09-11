/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.dto2;

import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

/**
 * UserInfo2 类型上没有任何注释，通过在 Mapper 自身描述序列化和反序列化。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
@SimpleMapper()
public interface UserInfo2Mapper {
    @Insert(value = "set #{'user_' + uid} #{info, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}")
    int saveUser(@Param("info") UserInfo2 info);

    @Query(value = "get #{'user_' + uid}", resultTypeHandler = JsonTypeHandler.class)
    UserInfo2 loadUser(@Param("uid") String uid);

    @Delete(value = "del #{'user_' + uid}")
    int deleteUser(@Param("uid") String uid);
}
