/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package com.example.demo.service;
import com.example.demo.dao.role.RoleMapper;
import com.example.demo.dao.user.UserMapper;
import com.example.demo.dto.UserDTO;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;

@Component
public class TestService {
    @Inject
    private UserMapper userMapper;
    @Inject
    private RoleMapper roleMapper;

    public List<UserDTO> queryUsers() {
        return this.userMapper.queryAll();
    }
}
