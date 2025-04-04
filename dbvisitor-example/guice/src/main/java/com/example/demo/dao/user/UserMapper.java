package com.example.demo.dao.user;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapper.Param;
import com.example.demo.dto.UserDTO;

import java.util.List;

public interface UserMapper extends BaseMapper<UserDTO> {

    int insertUser(@Param("name") String name, @Param("age") int age);

    List<UserDTO> queryAll();
}
