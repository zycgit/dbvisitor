package net.hasor.db.example.jdbc;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;
import net.hasor.db.jdbc.core.JdbcTemplate;
import net.hasor.db.jdbc.extractor.RowMapperResultSetExtractor;
import net.hasor.db.jdbc.mapper.MappingRowMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CustomStatementMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.loadSQL("CreateDB.sql");

        List<TestUser> resultList = jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(//
                    "select * from test_user", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE);
            return ps;
        }, new RowMapperResultSetExtractor<>(new MappingRowMapper<>(TestUser.class)));

        PrintUtils.printObjectList(resultList);

    }
}
