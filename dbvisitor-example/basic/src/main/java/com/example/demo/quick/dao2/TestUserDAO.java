/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package com.example.demo.quick.dao2;
import net.hasor.dbvisitor.mapper.*;

import java.util.List;

@SimpleMapper
public interface TestUserDAO extends BaseMapper<TestUser> {

    @Insert("insert into `test_user` (name,age,create_time) values (#{name}, #{age}, now())")
    int insertUser(@Param("name") String name, @Param("age") int age);

    @Update("update `test_user` set age = #{age} where id = #{id}")
    int updateAge(@Param("id") int userId, @Param("age") int newAge);

    @Delete("delete from `test_user` where age > #{age}")
    int deleteByAge(@Param("age") int age);

    @Query(value = "select * from `test_user` where  #{beginAge} < age and age < #{endAge}")
    List<TestUser> queryByAge(@Param("beginAge") int beginAge, @Param("endAge") int endAge);
}
