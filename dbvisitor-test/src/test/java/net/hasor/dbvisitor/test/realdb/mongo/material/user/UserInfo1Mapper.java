/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo.material.user;
import net.hasor.dbvisitor.mapper.*;

@SimpleMapper()
public interface UserInfo1Mapper {
    @Insert("test.user_info.insert(#{info, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler})")
    int saveUser(@Param("info") UserInfo1 info);

    @Update("test.user_info.update({uid: #{uid}}, {$set: {name: #{name}}})")
    int updateName(@Param("uid") String uid, @Param("name") String name);

    @Update("test.user_info.update({uid: #{uid}}, {$set: {name: #{name}, loginName: #{loginName}}})")
    int updateUser(@Param("uid") String uid, @Param("name") String name, @Param("loginName") String loginName);

    @Query("test.user_info.find({uid: #{uid}})")
    UserInfo1 loadUser(@Param("uid") String uid);

    @Delete("test.user_info.remove({uid: #{uid}})")
    int deleteUser(@Param("uid") String uid);
}
