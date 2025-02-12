package com.example.demo.service;

import com.example.demo.dao.UserMapper;
import com.example.demo.dto.UserDTO;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class UserService {
    @Resource
    private UserMapper     userMapper;
    @Resource
    private WrapperAdapter lambdaTemplate;

    public List<UserDTO> getAllUsers() {
        return this.userMapper.queryAll();
    }
}
