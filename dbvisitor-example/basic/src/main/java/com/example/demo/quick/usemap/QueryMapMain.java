package com.example.demo.quick.usemap;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class QueryMapMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.loadSQL("CreateDB.sql");

        List<Map<String, Object>> mapList = jdbcTemplate.queryForList("select * from test_user");

        PrintUtils.printMapList(mapList);
    }
}
