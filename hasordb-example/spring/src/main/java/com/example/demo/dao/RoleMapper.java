package com.example.demo.dao;
import com.example.demo.dto.RoleDTO;
import com.example.demo.dto.UserDTO;
import net.hasor.db.dal.repository.Param;
import net.hasor.db.dal.session.BaseMapper;

import java.util.List;

public interface RoleMapper extends BaseMapper<RoleDTO> {

    int insertUser(@Param("name") String name, @Param("age") int age);

    int updateAge(@Param("id") int userId, @Param("age") int newAge);

    int deleteByAge(@Param("age") int age);

    List<UserDTO> queryByAge(@Param("beginAge") int beginAge, @Param("endAge") int endAge);

    List<UserDTO> queryAll();
}
