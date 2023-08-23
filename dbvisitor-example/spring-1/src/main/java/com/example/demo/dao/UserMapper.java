package com.example.demo.dao;
import com.example.demo.dto.UserDTO;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;

import java.util.List;

public interface UserMapper extends BaseMapper<UserDTO> {

    List<UserDTO> queryAll();
}
