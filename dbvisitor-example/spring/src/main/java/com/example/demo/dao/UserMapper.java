package com.example.demo.dao;
import com.example.demo.dto.UserDTO;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.dal.repository.Param;

import java.util.List;

public interface UserMapper extends BaseMapper<UserDTO> {

    int insertUser(@Param("name") String name, @Param("age") int age);

    int updateAge(@Param("id") int userId, @Param("age") int newAge);

    int deleteByAge(@Param("age") int age);

    List<UserDTO> queryByAge(@Param("beginAge") int beginAge, @Param("endAge") int endAge);

    List<UserDTO> queryAll();
}
