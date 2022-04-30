package net.hasor.db.example.jdbc;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;
import net.hasor.db.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Execute1SqlMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.loadSQL("CreateDB.sql");

        List<Map<String, Object>> mapList = jdbcTemplate//
                .queryForList("select * from test_user where age > 40");
        PrintUtils.printObjectList(mapList);

        List<TestUser> dtoList = jdbcTemplate//
                .queryForList("select * from test_user where age > 40", TestUser.class);
        PrintUtils.printObjectList(dtoList);

        Map<String, Object> map = jdbcTemplate//
                .queryForMap("select * from test_user where age > 40 order by age limit 1");
        PrintUtils.printObjectList(Collections.singletonList(map));

        TestUser dto = jdbcTemplate//
                .queryForObject("select * from test_user where age > 40 order by age limit 1", TestUser.class);
        PrintUtils.printObjectList(Collections.singletonList(dto));

        int queryForInt = jdbcTemplate.queryForInt("select count(*) from test_user where age > 40");
        System.out.println(queryForInt);

        long queryForLong = jdbcTemplate.queryForLong("select count(*) from test_user where age > 40");
        System.out.println(queryForLong);

        String queryForString = jdbcTemplate.queryForString("select name from test_user where id = 1");
        System.out.println(queryForString);

        int result1 = jdbcTemplate.executeUpdate("update test_user set name = 'mala' where id = 1");
        System.out.println(result1);

        int result2 = jdbcTemplate.executeUpdate("delete from test_user where id = 1");
        System.out.println(result2);

        int result3 = jdbcTemplate.executeUpdate("insert into `test_user` values (10, 'david', 26, now())");
        System.out.println(result3);
    }
}