package com.example.demo.jdbc;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class RowMapper2Main {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.loadSQL("CreateDB.sql");

        String queryString = "select * from test_user where age > 40";
        List<TestUser> mapList = jdbcTemplate.queryForList(queryString, new RowMapper<TestUser>() {
            public TestUser mapRow(ResultSet rs, int rowNum) throws SQLException {
                TestUser testUser = new TestUser();
                testUser.setAge(rs.getInt("age"));
                testUser.setName(rs.getString("name"));
                return testUser;
            }
        });
        PrintUtils.printObjectList(mapList);

    }
}
