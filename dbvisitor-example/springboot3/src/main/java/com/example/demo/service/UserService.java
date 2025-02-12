package com.example.demo.service;

import com.example.demo.dao.UserMapper;
import com.example.demo.dto.UserDTO;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserService {
    @Autowired
    private UserMapper     userMapper;
    @Autowired
    private WrapperAdapter lambdaTemplate;

    public List<UserDTO> getAllUsers() {
        return this.userMapper.queryAll();
    }
}
