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
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.dbvisitor.DbVisitorModule;
import net.hasor.dbvisitor.DefaultDataSource;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.dao.role.RoleMapper;
import net.hasor.dbvisitor.test.dao.user.UserMapper;
import net.hasor.dbvisitor.test.dto.UserDTO;
import org.junit.Test;

public class SimpleTest {
    private UserMapper userMapper;
    private RoleMapper roleMapper;
    private Session    dalSession;

    @Test
    public void getListTest() throws Exception {
        AppContext injector = Hasor.create().mainSettingWith("simple-ds.properties").build(new DbVisitorModule());
        DataSource dataSource = injector.getInstance(DataSource.class);
        if (dataSource instanceof DefaultDataSource) {
            DefaultDataSource defaultDataSource = (DefaultDataSource) dataSource;
            defaultDataSource.setJdbcUrl("jdbc:mysql://127.0.0.1:13306/devtester?allowMultiQueries=true");
            defaultDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            defaultDataSource.setUsername("root");
            defaultDataSource.setPassword("123456");
        }
        this.dalSession = injector.getInstance(Session.class);
        this.dalSession.jdbc().loadSQL("CreateDB.sql");
        this.userMapper = this.dalSession.createMapper(UserMapper.class);
        this.roleMapper = this.dalSession.createMapper(RoleMapper.class);

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
