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
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

@SimpleMapper
public interface MongoAttributeMapper extends AnnotationAttributesMapper {

    @Override
    @Query(value = "@{macro, mongoSource}.find({id: #{id}}, {_id: 0})", statementType = StatementType.Prepared)
    UserInfo selectByIdPrepared(@Param("id") Integer id);

    @Override
    @Query(value = "@{macro, mongoSource}.find({id: ${id}}, {_id: 0})", statementType = StatementType.Statement)
    UserInfo selectByIdStatement(@Param("id") Integer id);

    @Override
    @Query(value = "@{macro, mongoSource}.find({id: #{id}}, {_id: 0})", timeout = -1)
    UserInfo selectByIdDefaultTimeout(@Param("id") Integer id);

    @Override
    @Query(value = "@{macro, mongoSource}.find({id: #{id}}, {_id: 0})", timeout = 30)
    UserInfo selectByIdWithTimeout(@Param("id") Integer id);

    @Override
    @Query(value = "@{macro, mongoSource}.find({id: #{id}}, {_id: 0})", timeout = 3600)
    UserInfo selectWithMaxTimeout(@Param("id") Integer id);

    @Override
    @Query(value = "@{macro, mongoSource}.find({id: #{id}}, {_id: 0})")
    UserInfo selectWithAllDefaults(@Param("id") Integer id);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})", fetchSize = 256)
    List<UserInfo> selectWithDefaultFetchSize(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})", fetchSize = 10)
    List<UserInfo> selectWithSmallFetchSize(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})", fetchSize = 1000)
    List<UserInfo> selectWithLargeFetchSize(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})", fetchSize = 1)
    List<UserInfo> selectWithFetchSizeOne(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})", resultSetType = ResultSetType.DEFAULT)
    List<UserInfo> selectWithDefaultResultSetType(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})", resultSetType = ResultSetType.FORWARD_ONLY)
    List<UserInfo> selectWithForwardOnly(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})", resultSetType = ResultSetType.SCROLL_INSENSITIVE)
    List<UserInfo> selectWithScrollInsensitive(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})", resultSetType = ResultSetType.SCROLL_SENSITIVE)
    List<UserInfo> selectWithScrollSensitive(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})", statementType = StatementType.Prepared, timeout = 60, fetchSize = 100, resultSetType = ResultSetType.FORWARD_ONLY)
    List<UserInfo> selectWithCombinedAttributes(@Param("pattern") String pattern);

    @Override
    @Insert(value = "@{macro, mongoSource}.insert({id: #{id}, name: #{name}, age: #{age}, email: #{email}, create_time: #{createTime}})")
    int insertUserBasic(UserInfo user);

    @Override
    @Insert(value = "@{macro, mongoSource}.insert({id: #{id}, name: #{name}, age: #{age}, email: #{email}, create_time: #{createTime}})", timeout = 30)
    int insertWithTimeout(UserInfo user);

    @Override
    @Insert(value = "@{macro, mongoSource}.insert({id: #{id}, name: #{name}, age: #{age}, email: #{email}, create_time: #{createTime}})", useGeneratedKeys = false)
    int insertWithoutGeneratedKey(UserInfo user);

    @Override
    @Update(value = "@{macro, mongoSource}.update({id: #{id}}, {$set: {age: #{age}}})", timeout = 30)
    int updateWithTimeout(@Param("id") Integer id, @Param("age") Integer age);

    @Override
    @Delete(value = "@{macro, mongoSource}.remove({id: #{id}})", timeout = 30)
    int deleteWithTimeout(@Param("id") Integer id);

    @Override
    @Insert({ "@{macro, mongoSource}.insert(", "{id: #{id}, name: #{name}, age: #{age},", " email: #{email}, create_time: #{createTime}})" })
    int insertMultiLine(UserInfo user);
}
