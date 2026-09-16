/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo.material;

import java.util.List;
import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.test.contract.material.dao.SessionUserMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

@SimpleMapper
public interface MongoSessionUserMapper extends SessionUserMapper {
    @Override
    @Insert("@{macro, mongoSource}.insert({id: #{id}, name: #{name}, age: #{age}, email: #{email}})")
    int insertUser(UserInfo user);

    @Override
    @Query("@{macro, mongoSource}.find({id: #{id}}, {_id: 0})")
    UserInfo selectById(@Param("id") Integer id);

    @Override
    @Query("@{macro, mongoSource}.find({}, {_id: 0}).sort({id: 1})")
    List<UserInfo> selectAll();

    @Override
    @Update("@{macro, mongoSource}.update({id: #{id}}, {$set: {name: #{name}, age: #{age}}})")
    int updateUser(UserInfo user);

    @Override
    @Delete("@{macro, mongoSource}.remove({id: #{id}})")
    int deleteById(@Param("id") Integer id);

    @Override
    @Query("@{macro, mongoSource}.count({})")
    int countAll();
}

