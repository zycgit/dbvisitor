package com.example.demo;

import com.example.demo.dto.UserDTO;
import com.example.demo.service.UserService;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest()
class DemoApplicationTests {

    @Autowired
    private UserService userService;
    @Autowired
    private DataSource  dataSource;

    @Test
    void contextLoads() throws SQLException, IOException {
        new JdbcTemplate(this.dataSource).loadSQL("CreateDB.sql");
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
