package com.example.demo.dao;
import com.example.demo.dto.UserDTO;
import net.hasor.dbvisitor.mapper.Mapper;

import java.util.List;

public interface UserMapper extends Mapper {

    List<UserDTO> queryAll();
}
