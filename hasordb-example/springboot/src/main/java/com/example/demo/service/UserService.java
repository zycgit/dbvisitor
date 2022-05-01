package com.example.demo.service;

import com.example.demo.dao.UserMapper;
import com.example.demo.dto.UserDTO;
import net.hasor.db.lambda.LambdaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class UserService {
    @Resource
    private UserMapper     userMapper;
    @Resource
    private LambdaTemplate lambdaTemplate;

    public List<UserDTO> getAllUsers() {
        return this.userMapper.queryAll();
    }
}
