package com.example.demo.jdbc;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.jdbc.paramer.BeanSqlParameterSource;
import net.hasor.dbvisitor.jdbc.paramer.MapSqlParameterSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SqlParameterSourceMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.loadSQL("CreateDB.sql");

        String querySql1 = "select * from test_user where age > ?";
        Object[] queryArg1 = new Object[] { 40 };
        List<Map<String, Object>> mapList1 = jdbcTemplate.queryForList(querySql1, queryArg1);
        PrintUtils.printObjectList(mapList1);

        String querySql2 = "select * from test_user where age > :age";
        Map<String, Object> queryArg2 = Collections.singletonMap("age", 40);
        List<Map<String, Object>> mapList2 = jdbcTemplate.queryForList(querySql2, queryArg2);
        PrintUtils.printObjectList(mapList2);

        String querySql3 = "select * from test_user where age > :age";
        Map<String, Object> argMap = Collections.singletonMap("age", 40);
        MapSqlParameterSource queryArg3 = new MapSqlParameterSource(argMap);
        List<Map<String, Object>> mapList3 = jdbcTemplate.queryForList(querySql3, queryArg3);
        PrintUtils.printObjectList(mapList3);

        String querySql4 = "select * from test_user where age > :age";
        TestUser argDTO = new TestUser();
        argDTO.setAge(40);
        BeanSqlParameterSource queryArg4 = new BeanSqlParameterSource(argDTO);
        List<Map<String, Object>> mapList4 = jdbcTemplate.queryForList(querySql4, queryArg4);
        PrintUtils.printObjectList(mapList4);
    }
}
