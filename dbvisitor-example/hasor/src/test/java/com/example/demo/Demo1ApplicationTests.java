package com.example.demo;

import com.example.demo.dto.UserDTO;
import com.example.demo.service.TestService;
import net.hasor.core.AppContext;
import net.hasor.core.Hasor;
import net.hasor.core.Inject;
import net.hasor.dbvisitor.DbVisitorModule;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class Demo1ApplicationTests {
    @Inject
    private TestService userService;

    @Before
    public void beforeTest() {
        AppContext injector = Hasor.create().mainSettingWith("single-ds.properties").build(binder -> {
            binder.installModule(new DbVisitorModule());
        });

        injector.justInject(this);
    }

    @Test
    public void contextLoads() {
        List<UserDTO> users = userService.queryUsers();

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
