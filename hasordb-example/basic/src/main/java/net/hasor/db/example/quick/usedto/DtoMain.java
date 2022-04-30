package net.hasor.db.example.quick.usedto;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;
import net.hasor.db.jdbc.core.JdbcTemplate;

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
