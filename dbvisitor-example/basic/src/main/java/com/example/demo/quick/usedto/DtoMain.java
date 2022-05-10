package com.example.demo.quick.usedto;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DtoMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.loadSQL("CreateDB.sql");

        List<TestUser> dtoList = jdbcTemplate.queryForList("select * from test_user", TestUser.class);
        PrintUtils.printObjectList(dtoList);
    }
}
