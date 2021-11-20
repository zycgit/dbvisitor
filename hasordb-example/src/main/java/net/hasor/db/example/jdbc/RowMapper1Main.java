package net.hasor.db.example.jdbc;
import net.hasor.cobble.StringUtils;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;
import net.hasor.db.jdbc.core.JdbcTemplate;
import net.hasor.db.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.db.jdbc.mapper.MappingRowMapper;
import net.hasor.db.jdbc.mapper.SingleColumnRowMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class RowMapper1Main {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.loadSQL("CreateDB.sql");

        List<Map<String, Object>> mapList = jdbcTemplate//
                .query("select * from test_user where age > 40", new ColumnMapRowMapper());
        PrintUtils.printObjectList(mapList);
        mapList = jdbcTemplate.queryForList("select * from test_user where age > 40");
        PrintUtils.printObjectList(mapList);

        List<TestUser> dtoList = jdbcTemplate//
                .query("select * from test_user where age > 40", new MappingRowMapper<>(TestUser.class));
        PrintUtils.printObjectList(dtoList);
        dtoList = jdbcTemplate.queryForList("select * from test_user where age > 40", TestUser.class);
        PrintUtils.printObjectList(dtoList);

        List<String> stringList = jdbcTemplate//
                .query("select name from test_user where age > 40", new SingleColumnRowMapper<>(String.class));
        System.out.println(StringUtils.join(stringList.toArray(), ", "));
        stringList = jdbcTemplate.queryForList("select name from test_user where age > 40", String.class);
        System.out.println(StringUtils.join(stringList.toArray(), ", "));

    }
}
