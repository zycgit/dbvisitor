package com.example.demo.dao.user;
import com.example.demo.dto.UserDTO;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapper.Param;

import java.util.List;

public interface UserMapper extends BaseMapper<UserDTO> {

    int insertUser(@Param("name") String name, @Param("age") int age);

    List<UserDTO> queryAll();
}
