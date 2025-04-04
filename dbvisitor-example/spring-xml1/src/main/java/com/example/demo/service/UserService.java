package com.example.demo.service;

import com.example.demo.dao.UserMapper;
import com.example.demo.dto.UserDTO;

import java.util.List;

public class UserService {
    private UserMapper userMapper;

    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<UserDTO> getAllUsers() {
        return this.userMapper.queryAll();
    }
}
