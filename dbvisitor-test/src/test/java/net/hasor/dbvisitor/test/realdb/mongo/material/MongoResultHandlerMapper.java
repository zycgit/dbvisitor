/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo.material;

import java.util.List;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.ResultSetType;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.ResultHandlerMapper;
import net.hasor.dbvisitor.test.contract.material.handler.CustomResultSetExtractor;
import net.hasor.dbvisitor.test.contract.material.handler.RecordingRowCallbackHandler;
import net.hasor.dbvisitor.test.contract.material.handler.CustomRowMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

@SimpleMapper
public interface MongoResultHandlerMapper extends ResultHandlerMapper {
    @Override
    @Insert("@{macro, mongoSource}.insert({id: #{id}, name: #{name}, age: #{age}, email: #{email}, create_time: #{createTime}})")
    int insertUser(UserInfo user);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})")
    List<UserInfo> selectDefault(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})", resultSetExtractor = CustomResultSetExtractor.class)
    List<UserInfo> selectWithExtractor(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})", resultSetExtractor = CustomResultSetExtractor.class, fetchSize = 1, timeout = 30)
    List<UserInfo> selectWithExtractorAndFetchSize(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})", resultSetExtractor = CustomResultSetExtractor.class, fetchSize = 50, resultSetType = ResultSetType.SCROLL_INSENSITIVE)
    List<UserInfo> selectWithExtractorAndOptions(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})", resultRowMapper = CustomRowMapper.class)
    List<UserInfo> selectWithRowMapper(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})", resultRowMapper = CustomRowMapper.class, timeout = 30, fetchSize = 100)
    List<UserInfo> selectWithRowMapperAndOptions(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})", resultRowCallback = RecordingRowCallbackHandler.class)
    void selectWithRowCallback(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})", resultRowCallback = RecordingRowCallbackHandler.class, timeout = 30)
    void selectWithRowCallbackAndTimeout(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, mongoSource}.find({id: #{id}}, {_id: 0})", resultRowMapper = CustomRowMapper.class)
    UserInfo selectSingleWithRowMapper(@Param("id") Integer id);

    @Override
    @Query("@{macro, mongoSource}.find({id: #{id}}, {_id: 0})")
    UserInfo selectById(@Param("id") Integer id);
}
