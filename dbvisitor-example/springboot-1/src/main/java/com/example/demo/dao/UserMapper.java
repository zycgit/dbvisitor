package com.example.demo.dao;
import com.example.demo.dto.UserDTO;
import net.hasor.dbvisitor.dal.repository.SimpleMapper;

import java.util.List;

@SimpleMapper
public interface UserMapper {

    List<UserDTO> queryAll();
}
