/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo.material;

import java.util.List;
import net.hasor.dbvisitor.mapper.Delete;
import net.hasor.dbvisitor.mapper.Execute;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.mapper.Update;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationTestMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

@SimpleMapper
public interface MongoCommandMapper extends AnnotationTestMapper {

    @Override
    @Insert("@{macro, mongoSource}.insert({id: #{id}, name: #{name}, age: #{age}, email: #{email}, create_time: #{createTime}})")
    int insertUser(UserInfo user);

    @Override
    @Insert("@{macro, mongoSource}.insert({id: #{id}, name: #{name}, age: #{age}, email: #{email}})")
    int insertUserWithParams(@Param("id") Integer id, @Param("name") String name, @Param("age") Integer age, @Param("email") String email);

    @Override
    @Update("@{macro, mongoSource}.update({id: #{id}}, {$set: {age: #{age}}})")
    int updateUserAge(@Param("id") Integer id, @Param("age") Integer age);

    @Override
    @Update("@{macro, mongoSource}.update({id: #{id}}, {$set: {name: #{name}, age: #{age}}})")
    int updateUserInfo(@Param("id") Integer id, @Param("name") String name, @Param("age") Integer age);

    @Override
    @Update("@{macro, mongoSource}.update({age: #{oldAge}}, {$set: {age: #{newAge}}}, {multi: true})")
    int updateAgeByRange(@Param("oldAge") Integer oldAge, @Param("newAge") Integer newAge);

    @Override
    @Delete("@{macro, mongoSource}.remove({id: #{id}})")
    int deleteById(@Param("id") Integer id);

    @Override
    @Delete("@{macro, mongoSource}.remove({age: #{age}})")
    int deleteByAge(@Param("age") Integer age);

    @Override
    @Query("@{macro, mongoSource}.find({id: #{id}}, {_id: 0})")
    UserInfo selectById(@Param("id") Integer id);

    @Override
    @Query("@{macro, mongoSource}.find({age: #{age}}, {_id: 0})")
    List<UserInfo> selectByAge(@Param("age") Integer age);

    @Override
    @Query("@{macro, mongoSource}.count({age: #{age}})")
    int countByAge(@Param("age") Integer age);

    @Override
    @Execute("@{macro, mongoSource}.insert({id: #{id}, name: #{name}})")
    void insertTempData(@Param("id") Integer id, @Param("name") String name);

    @Override
    @Query("@{macro, mongoSource}.find({id: #{id}}, {_id: 0, name: 1})")
    String selectTempData(@Param("id") Integer id);

    @Override
    @Insert({
        "@{macro, mongoSource}.insert(",
        "{id: #{id}, name: #{name}, age: #{age},",
        " email: #{email}, create_time: #{createTime}})"
    })
    int insertUserMultiLine(UserInfo user);

    @Override
    default List<UserInfo> selectByNameLike(String pattern) {
        return selectByNameRegex(pattern.replace("%", ".*"));
    }

    @Query("@{macro, mongoSource}.find({name: {$regex: #{pattern}}}, {_id: 0})")
    List<UserInfo> selectByNameRegex(@Param("pattern") String pattern);
}

