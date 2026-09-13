/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.scenario.mapper;

import net.hasor.dbvisitor.mapper.Delete;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.mapper.Update;
import net.hasor.dbvisitor.test.realdb.redis.scenario.model.LoginState;

@SimpleMapper
public interface LoginStateMapper {
    @Insert("SET #{key} #{state} EX #{ttlSeconds}")
    int save(@Param("key") String key, @Param("state") LoginState state,
             @Param("ttlSeconds") int ttlSeconds);

    @Query("GET #{key}")
    LoginState load(@Param("key") String key);

    @Update("EXPIRE #{key} #{ttlSeconds}")
    int renew(@Param("key") String key, @Param("ttlSeconds") int ttlSeconds);

    @Delete("DEL #{key}")
    int logout(@Param("key") String key);
}
