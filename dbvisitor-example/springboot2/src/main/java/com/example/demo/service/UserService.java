/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package com.example.demo.service;

import com.example.demo.dao.UserMapper;
import com.example.demo.dto.UserDTO;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
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
