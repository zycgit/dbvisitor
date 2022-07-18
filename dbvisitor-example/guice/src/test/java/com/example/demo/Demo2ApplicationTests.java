package com.example.demo;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.dbvisitor.guice.DbVisitorModule;
import net.hasor.dbvisitor.test.dto.UserDTO;
import net.hasor.dbvisitor.test.service.MultiDsService;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class Demo2ApplicationTests {
    @Inject
    private MultiDsService multiDsService;

    @Before
    public void beforeTest() throws IOException {
        Properties properties = new Properties();
        properties.load(ResourcesUtils.getResourceAsStream("multi-guice.properties"));

        Injector injector = Guice.createInjector(new DbVisitorModule(properties));
        injector.injectMembers(this);
    }

    @Test
    public void contextLoads() {
        List<UserDTO> users = this.multiDsService.queryUsers();

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