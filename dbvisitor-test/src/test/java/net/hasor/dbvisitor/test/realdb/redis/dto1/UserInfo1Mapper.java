/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.dto1;
import net.hasor.dbvisitor.mapper.*;

/**
 * UserInfo1 类型上通过 BindTypeHandler 注释来绑定数据的序列化和反序列化。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
@SimpleMapper()
public interface UserInfo1Mapper {
    @Insert("set #{'user_' + info.uid} #{info}")
    int saveUser(@Param("info") UserInfo1 info);

    @Update("set #{'user_' + info.uid} #{info}")
    int updateUser(@Param("info") UserInfo1 info);

    @Query("get #{'user_' + uid}")
    UserInfo1 loadUser(@Param("uid") String uid);

    @Delete("del #{'user_' + uid}")
    int deleteUser(@Param("uid") String uid);
}
