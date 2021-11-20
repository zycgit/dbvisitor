package net.hasor.db.example.jdbc;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;
import net.hasor.db.jdbc.RowMapper;
import net.hasor.db.jdbc.core.JdbcTemplate;

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
        List<TestUser> mapList = jdbcTemplate.query(queryString, new RowMapper<TestUser>() {
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
