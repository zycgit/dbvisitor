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
