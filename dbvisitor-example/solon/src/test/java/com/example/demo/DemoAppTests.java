package com.example.demo;

import com.example.demo.dto.UserDTO;
import com.example.demo.service.TestService;
import net.hasor.dbvisitor.session.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit4ClassRunner;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Import(profiles = "classpath:app.yml")
@RunWith(SolonJUnit4ClassRunner.class)
public class DemoAppTests {
    @Inject
    private TestService userService;
    @Inject
    private Session     session;

    @Test
    public void contextLoads() throws SQLException, IOException {
        session.jdbc().loadSQL("CreateDB.sql");

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
