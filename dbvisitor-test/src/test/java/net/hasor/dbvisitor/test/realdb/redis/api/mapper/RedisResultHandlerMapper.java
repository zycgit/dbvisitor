/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.mapper;

import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.ResultSetType;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.ResultHandlerMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

@SimpleMapper
public interface RedisResultHandlerMapper extends ResultHandlerMapper {
    @Override
    @Query("ZADD @{macro, redisHandlerKey} #{id} #{#{'id':id,'name':name,'age':age,'email':email,'createTime':createTime},typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}")
    int insertUser(UserInfo user);

    @Override
    @Query("@{macro, redisHandlerRows}")
    List<UserInfo> selectDefault(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, redisHandlerRows}", resultSetExtractor = RedisUserResultHandlers.Extractor.class)
    List<UserInfo> selectWithExtractor(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, redisHandlerRows}", resultSetExtractor = RedisUserResultHandlers.MapExtractor.class)
    Map<Integer, String> selectMapWithExtractor(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, redisHandlerRows}", resultSetExtractor = RedisUserResultHandlers.Extractor.class, fetchSize = 1, timeout = 30)
    List<UserInfo> selectWithExtractorAndFetchSize(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, redisHandlerRows}", resultSetExtractor = RedisUserResultHandlers.Extractor.class, fetchSize = 50, resultSetType = ResultSetType.SCROLL_INSENSITIVE)
    List<UserInfo> selectWithExtractorAndOptions(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, redisHandlerRows}", resultRowMapper = RedisUserResultHandlers.Rows.class)
    List<UserInfo> selectWithRowMapper(@Param("pattern") String pattern);

    @Override
    @Query(value = "ZRANGEBYSCORE @{macro, redisHandlerKey} #{id} #{id}", resultRowMapper = RedisUserResultHandlers.Rows.class)
    UserInfo selectSingleWithRowMapper(@Param("id") Integer id);

    @Override
    @Query(value = "@{macro, redisHandlerRows}", resultRowMapper = RedisUserResultHandlers.Rows.class, timeout = 30, fetchSize = 100)
    List<UserInfo> selectWithRowMapperAndOptions(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, redisHandlerRows}", resultRowCallback = RedisUserResultHandlers.Callback.class)
    void selectWithRowCallback(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, redisHandlerRows}", resultRowCallback = RedisUserResultHandlers.Callback.class, timeout = 30)
    void selectWithRowCallbackAndTimeout(@Param("pattern") String pattern);

    @Override
    @Query("ZRANGEBYSCORE @{macro, redisHandlerKey} #{id} #{id}")
    UserInfo selectById(@Param("id") Integer id);
}
