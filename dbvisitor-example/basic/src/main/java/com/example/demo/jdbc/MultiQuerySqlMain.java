package com.example.demo.jdbc;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

public class MultiQuerySqlMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.loadSQL("CreateDB.sql");

        String querySql = "set @userName = convert(? USING utf8); select * from test_user where name = @userName;";
        Object[] queryArg = new Object[] { "dative" };
        Map<String, Object> resultMap = jdbcTemplate.multipleExecute(querySql, queryArg);

        PrintUtils.printObjectList(Arrays.asList(resultMap.values().toArray()));

    }
}
