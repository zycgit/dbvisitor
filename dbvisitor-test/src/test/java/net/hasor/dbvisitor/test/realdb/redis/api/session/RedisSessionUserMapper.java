/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.session;

import java.util.List;
import net.hasor.dbvisitor.mapper.Delete;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.test.contract.material.dao.SessionUserMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

@SimpleMapper
public interface RedisSessionUserMapper extends SessionUserMapper {
    @Override
    @Query("ZADD @{macro, redisUsers} #{id} #{#{'id':id,'name':name,'age':age,'email':email},typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}")
    int insertUser(UserInfo user);

    @Override
    @Query(value = "ZRANGEBYSCORE @{macro, redisUsers} #{id} #{id}", resultTypeHandler = JsonTypeHandler.class)
    UserInfo selectById(@Param("id") Integer id);

    @Override
    @Query(value = "ZRANGE @{macro, redisUsers} 0 -1", resultTypeHandler = JsonTypeHandler.class)
    List<UserInfo> selectAll();

    @Override
    @Query("EVAL \"local r=redis.call('ZRANGEBYSCORE',KEYS[1],ARGV[1],ARGV[1]); if #r==0 then return 0 end; redis.call('ZREM',KEYS[1],r[1]); redis.call('ZADD',KEYS[1],ARGV[1],ARGV[2]); return 1\" 1 @{macro, redisUsers} #{id} #{#{'id':id,'name':name,'age':age,'email':email},typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}")
    int updateUser(UserInfo user);

    @Override
    @Delete("ZREMRANGEBYSCORE @{macro, redisUsers} #{id} #{id}")
    int deleteById(@Param("id") Integer id);

    @Override
    @Query("ZCARD @{macro, redisUsers}")
    int countAll();
}
