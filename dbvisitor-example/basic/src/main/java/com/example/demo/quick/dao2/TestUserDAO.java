package com.example.demo.quick.dao2;
import net.hasor.dbvisitor.dal.repository.*;
import net.hasor.dbvisitor.dal.session.BaseMapper;

import java.util.List;

@SimpleMapper
public interface TestUserDAO extends BaseMapper<TestUser> {

    @Insert("insert into `test_user` (name,age,create_time) values (#{name}, #{age}, now())")
    public int insertUser(@Param("name") String name, @Param("age") int age);

    @Update("update `test_user` set age = #{age} where id = #{id}")
    public int updateAge(@Param("id") int userId, @Param("age") int newAge);

    @Delete("delete from `test_user` where age > #{age}")
    public int deleteByAge(@Param("age") int age);

    @Query(value = "select * from `test_user` where  #{beginAge} < age and age < #{endAge}", resultType = TestUser.class)
    public List<TestUser> queryByAge(@Param("beginAge") int beginAge, @Param("endAge") int endAge);
}
