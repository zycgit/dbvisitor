package com.example.demo.dao;
import com.example.demo.dto.UserDTO;
import net.hasor.dbvisitor.dal.mapper.Mapper;

import java.util.List;

// extends Mapper or BaseMapper, or @DalMapper or @SimpleMapper or @RefMapper
public interface UserMapper extends Mapper {

    List<UserDTO> queryAll();
}
