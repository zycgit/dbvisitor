/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.demo.service;
import net.hasor.dbvisitor.session.Session;
import com.example.demo.dao.role.RoleMapper;
import com.example.demo.dao.user.UserMapper;
import com.example.demo.dto.UserDTO;

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
