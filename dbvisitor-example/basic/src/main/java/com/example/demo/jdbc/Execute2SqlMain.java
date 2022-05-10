package com.example.demo.jdbc;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Execute2SqlMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.loadSQL("CreateDB.sql");

        String querySql1 = "select * from test_user where age > ?";
        Object[] queryArg1 = new Object[] { 40 };
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(querySql1, queryArg1);
        PrintUtils.printObjectList(mapList);

        String querySql2 = "select * from test_user where age > ?";
        Object[] queryArg2 = new Object[] { 40 };
        List<TestUser> dtoList = jdbcTemplate.queryForList(querySql2, queryArg2, TestUser.class);
        PrintUtils.printObjectList(dtoList);

        String querySql3 = "select * from test_user where age > ? order by age limit 1";
        Object[] queryArg3 = new Object[] { 40 };
        Map<String, Object> map = jdbcTemplate.queryForMap(querySql3, queryArg3);
        PrintUtils.printObjectList(Collections.singletonList(map));

        String querySql4 = "select * from test_user where age > ? order by age limit 1";
        Object[] queryArg4 = new Object[] { 40 };
        TestUser dto = jdbcTemplate.queryForObject(querySql4, queryArg4, TestUser.class);
        PrintUtils.printObjectList(Collections.singletonList(dto));

        String querySql5 = "select count(*) from test_user where age > ?";
        Object[] queryArg5 = new Object[] { 40 };
        int queryForInt = jdbcTemplate.queryForInt(querySql5, queryArg5);
        System.out.println(queryForInt);

        String querySql6 = "select count(*) from test_user where age > ?";
        Object[] queryArg6 = new Object[] { 40 };
        long queryForLong = jdbcTemplate.queryForLong(querySql6, queryArg6);
        System.out.println(queryForLong);

        String querySql7 = "select name from test_user where id = ?";
        Object[] queryArg7 = new Object[] { 1 };
        String queryForString = jdbcTemplate.queryForString(querySql7, queryArg7);
        System.out.println(queryForString);

        String querySql8 = "update test_user set name = ? where id = ?";
        Object[] queryArg8 = new Object[] { "mala", 1 };
        int result1 = jdbcTemplate.executeUpdate(querySql8, queryArg8);
        System.out.println(result1);

        String querySql9 = "delete from test_user where id = ?";
        Object[] queryArg9 = new Object[] { 1 };
        int result2 = jdbcTemplate.executeUpdate(querySql9, queryArg9);
        System.out.println(result2);

        String querySql10 = "insert into `test_user` values (?,?,?,?)";
        Object[] queryArg10 = new Object[] { 10, "'david'", 26, new Date() };
        int result3 = jdbcTemplate.executeUpdate(querySql10, queryArg10);
        System.out.println(result3);
    }
}