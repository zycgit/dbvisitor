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
package net.hasor.dbvisitor.test;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.dbvisitor.DbVisitorModule;
import net.hasor.dbvisitor.dal.session.DalSession;
import net.hasor.dbvisitor.test.dao.role.RoleMapper;
import net.hasor.dbvisitor.test.dao.user.UserMapper;
import net.hasor.dbvisitor.test.dto.UserDTO;
import org.junit.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleTest {
    @Inject
    private UserMapper userMapper;
    @Inject
    private RoleMapper roleMapper;
    @Inject
    private DalSession dalSession;

    @Test
    public void getListTest() throws SQLException, IOException {
        AppContext injector = Hasor.create().mainSettingWith("simple-ds.properties").build(new DbVisitorModule());
        injector.justInject(this);
        this.dalSession.lambdaTemplate().loadSQL("CreateDB.sql");

        assert userMapper != null;
        assert roleMapper != null;

        List<UserDTO> users = userMapper.queryAll();

        assert users.size() == 5;
        assert users.get(0).getId() == 1L;
        assert users.get(0).getName().equals("mali");
        assert users.get(0).getGender().equals("F");
        assert users.get(0).getEmail().equals("mali@hasor.net");
        assert users.get(0).getRoleId() == 1L;

        List<String> collect = users.stream().map(UserDTO::getName).collect(Collectors.toList());
        assert collect.contains("mali");
        assert collect.contains("dative");
        assert collect.contains("jon wes");
        assert collect.contains("mary");
        assert collect.contains("matt");
    }
}
