/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo.material;

import java.util.Date;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.ResultMappingMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

@SimpleMapper
public interface MongoResultMapper extends ResultMappingMapper {

    @Override
    @Insert("@{macro, mongoSource}.insert({id: #{id}, name: #{name}, age: #{age}, email: #{email}, create_time: #{createTime}})")
    int insertUser(UserInfo user);

    @Override
    @Query("@{macro, mongoSource}.find({id: #{id}}, {_id: 0})")
    UserInfo selectUserById(@Param("id") Integer id);

    @Override
    @Query("@{macro, mongoSource}.find({id: #{id}}, {_id: 0, id: 1, name: 1})")
    UserInfo selectUserPartial(@Param("id") Integer id);

    @Override
    @Query("@{macro, mongoSource}.find({id: #{id}}, {_id: 0})")
    Map<String, Object> selectUserAsMap(@Param("id") Integer id);

    @Override
    @Query("@{macro, mongoSource}.find({}, {_id: 0})")
    List<Map<String, Object>> selectUsersAsMapList();

    @Override
    @Query("@{macro, mongoSource}.find({id: #{id}}, {_id: 0, age: 1})")
    Integer selectAgeById(@Param("id") Integer id);

    @Override
    @Query("@{macro, mongoSource}.find({id: #{id}}, {_id: 0, name: 1})")
    String selectNameById(@Param("id") Integer id);

    @Override
    @Query("@{macro, mongoSource}.count({})")
    Long selectCount();

    @Override
    @Query("@{macro, mongoSource}.find({id: #{id}}, {_id: 0, create_time: 1})")
    Date selectCreateTimeById(@Param("id") Integer id);

    @Override
    @Query("@{macro, mongoSource}.find({age: {$gte: #{minAge}, $lte: #{maxAge}}}, {_id: 0})")
    List<UserInfo> selectUsersByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

    @Override
    @Query("@{macro, mongoSource}.find({id: {$gte: #{minId}, $lte: #{maxId}}}, {_id: 0, name: 1})")
    List<String> selectAllNames(@Param("minId") Integer minId, @Param("maxId") Integer maxId);

    @Override
    @Query("@{macro, mongoSource}.find({id: {$gte: #{minId}, $lte: #{maxId}}}, {_id: 0, id: 1})")
    List<Integer> selectIdRange(@Param("minId") Integer minId, @Param("maxId") Integer maxId);

    @Override
    @Query("@{macro, mongoSource}.aggregate([{$match: {$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}}, {$group: {_id: '$age'}}, {$project: {_id: 0, age: '$_id'}}])")
    List<Integer> selectDistinctAges(@Param("pattern") String pattern);

    @Override
    @Query("@{macro, mongoSource}.aggregate([{$group: {_id: null, maxAge: {$max: '$age'}}}, {$project: {_id: 0, maxAge: 1}}])")
    Integer selectMaxAge();

    @Override
    @Query("@{macro, mongoSource}.aggregate([{$match: {$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}}, {$group: {_id: null, minAge: {$min: '$age'}, maxAge: {$max: '$age'}, avgAge: {$avg: '$age'}}}, {$project: {_id: 0}}])")
    Map<String, Object> selectAgeStats(@Param("pattern") String pattern);

    @Override
    @Query("@{macro, mongoSource}.aggregate([{$match: {$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}}, {$group: {_id: '$age', cnt: {$sum: 1}}}, {$project: {_id: 0, age: '$_id', cnt: 1}}, {$sort: {age: 1}}])")
    List<Map<String, Object>> selectCountByAge(@Param("pattern") String pattern);

    @Override
    @Query("@{macro, mongoSource}.find({$expr: {$regexMatch: {input: '$name', regex: {$replaceAll: {input: #{pattern}, find: '%', replacement: '.*'}}}}}, {_id: 0}).sort({id: 1})")
    List<UserInfo> selectUsersWithPagination(@Param("pattern") String pattern, PageObject page);
}
