package net.hasor.db.example.jdbc;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;
import net.hasor.db.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class Execute3SqlMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.loadSQL("CreateDB.sql");

        String querySql1 = "select * from test_user where age > :age";
        Map<String, Object> queryArg1 = Collections.singletonMap("age", 40);
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(querySql1, queryArg1);
        PrintUtils.printObjectList(mapList);

        String querySql2 = "select * from test_user where age > :age";
        Map<String, Object> queryArg2 = Collections.singletonMap("age", 40);
        List<TestUser> dtoList = jdbcTemplate.queryForList(querySql2, queryArg2, TestUser.class);
        PrintUtils.printObjectList(dtoList);

        String querySql3 = "select * from test_user where age > :age order by age limit 1";
        Map<String, Object> queryArg3 = Collections.singletonMap("age", 40);
        Map<String, Object> map = jdbcTemplate.queryForMap(querySql3, queryArg3);
        PrintUtils.printObjectList(Collections.singletonList(map));

        String querySql4 = "select * from test_user where age > :age order by age limit 1";
        Map<String, Object> queryArg4 = Collections.singletonMap("age", 40);
        TestUser dto = jdbcTemplate.queryForObject(querySql4, queryArg4, TestUser.class);
        PrintUtils.printObjectList(Collections.singletonList(dto));

        String querySql5 = "select count(*) from test_user where age > :age";
        Map<String, Object> queryArg5 = Collections.singletonMap("age", 40);
        int queryForInt = jdbcTemplate.queryForInt(querySql5, queryArg5);
        System.out.println(queryForInt);

        String querySql6 = "select count(*) from test_user where age > :age";
        Map<String, Object> queryArg6 = Collections.singletonMap("age", 40);
        long queryForLong = jdbcTemplate.queryForLong(querySql6, queryArg6);
        System.out.println(queryForLong);

        String querySql7 = "select name from test_user where id = :id";
        Map<String, Object> queryArg7 = Collections.singletonMap("id", 1);
        String queryForString = jdbcTemplate.queryForString(querySql7, queryArg7);
        System.out.println(queryForString);

        String querySql8 = "update test_user set name = :name where id = :id";
        Map<String, Object> queryArg8 = new HashMap<>();
        queryArg8.put("name", "mala");
        queryArg8.put("id", 1);
        int result1 = jdbcTemplate.executeUpdate(querySql8, queryArg8);
        System.out.println(result1);

        String querySql9 = "delete from test_user where id = :id";
        Map<String, Object> queryArg9 = Collections.singletonMap("id", 1);
        int result2 = jdbcTemplate.executeUpdate(querySql9, queryArg9);
        System.out.println(result2);

        String querySql10 = "insert into `test_user` values (:id , :name , :age , :create )";
        Map<String, Object> queryArg10 = new HashMap<>();
        queryArg10.put("id", 10);
        queryArg10.put("name", "david");
        queryArg10.put("age", 26);
        queryArg10.put("create", new Date());
        int result3 = jdbcTemplate.executeUpdate(querySql10, queryArg10);
        System.out.println(result3);
    }
}
