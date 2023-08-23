package com.example.demo;

import com.example.demo.dto.UserDTO;
import com.example.demo.service.UserService;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:application.xml" })
public class ApplicationTests {
    @Resource
    private UserService userService;
    @Resource
    private DataSource  dataSource;

    @Before
    public void beforeTest() throws SQLException, IOException {
        new JdbcTemplate(this.dataSource).loadSQL("CreateDB.sql");
    }

    @Test
    public void contextLoads() {
        List<UserDTO> users = userService.getAllUsers();

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
