/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.dto1;

import java.util.List;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

@SimpleMapper
public interface RedisRangeParameterMapper extends RedisParameterBindingMapper {
    @Override
    @Query("@{macro, nxnRangeInsert}")
    int insertWithParam(@Param("id") Integer id, @Param("name") String name, @Param("age") Integer age, @Param("email") String email);

    @Override
    @Query(value = "@{macro, nxnSelectRange}", resultTypeHandler = JsonTypeHandler.class)
    List<UserInfo> selectByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);
}
