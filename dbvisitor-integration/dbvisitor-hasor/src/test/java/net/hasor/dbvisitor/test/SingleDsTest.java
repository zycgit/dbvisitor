/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test;
import java.util.List;
import java.util.stream.Collectors;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.dbvisitor.hasor.autoconfig.AutoConfigModule;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.dao.role.RoleMapper;
import net.hasor.dbvisitor.test.dao.user.UserMapper;
import net.hasor.dbvisitor.test.dto.UserDTO;
import org.junit.Test;

public class SingleDsTest {
    private UserMapper userMapper;
    private RoleMapper roleMapper;
    private Session    dalSession;

    @Test
    public void getListTest() throws Exception {
        AppContext injector = Hasor.create().mainSettingWith("single-ds.properties").build(new AutoConfigModule());
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
