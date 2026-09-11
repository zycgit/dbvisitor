/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.inject.Inject;
import com.google.inject.Guice;
import com.google.inject.Injector;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.dbvisitor.guice.DbVisitorModule;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.dao.role.RoleMapper;
import net.hasor.dbvisitor.test.dao.user.UserMapper;
import net.hasor.dbvisitor.test.dto.UserDTO;
import org.junit.Test;

public class SimpleTest {
    @Inject
    private UserMapper userMapper;
    @Inject
    private RoleMapper roleMapper;
    @Inject
    private Session    session;

    @Test
    public void getListTest() throws SQLException, IOException {
        Properties properties = new Properties();
        properties.load(ResourcesUtils.getResourceAsStream("simple-ds.properties"));

        Injector injector = Guice.createInjector(new DbVisitorModule(properties));
        injector.injectMembers(this);
        this.session.jdbc().loadSQL("CreateDB.sql");

        assert this.userMapper != null;
        assert this.roleMapper != null;

        List<UserDTO> users = this.userMapper.queryAll();

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
