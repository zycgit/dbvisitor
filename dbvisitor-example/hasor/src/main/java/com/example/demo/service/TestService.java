/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package com.example.demo.service;
import com.example.demo.dao.role.RoleMapper;
import com.example.demo.dto.UserDTO;
import net.hasor.dbvisitor.session.Session;
import com.example.demo.dao.user.UserMapper;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class TestService {
    @Inject
    private UserMapper userMapper;

    @Inject
    private RoleMapper roleMapper;

    @Inject
    private Session dalSession;

    @PostConstruct
    public void init() throws SQLException, IOException {
        this.dalSession.jdbc().loadSQL("CreateDB.sql");
    }

    public List<UserDTO> queryUsers() {
        return this.userMapper.queryAll();
    }
}
