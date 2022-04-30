package net.hasor.db.example.jdbc;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;
import net.hasor.db.jdbc.BatchPreparedStatementSetter;
import net.hasor.db.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BatchSqlMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.loadSQL("CreateDB.sql");

        int[] result1 = jdbcTemplate.executeBatch(new String[] {//
                "insert into `test_user` values (11, 'david', 26, now())",//
                "insert into `test_user` values (12, 'kevin', 26, now())"//
        });
        PrintUtils.printObjectList(jdbcTemplate.queryForList("select * from test_user"));

        String querySql2 = "insert into `test_user` values (?,?,?,?)";
        Object[][] queryArg2 = new Object[][] {//
                new Object[] { 20, "david", 26, new Date() },//
                new Object[] { 22, "kevin", 26, new Date() } //
        };
        int[] result2 = jdbcTemplate.executeBatch(querySql2, queryArg2);
        PrintUtils.printObjectList(jdbcTemplate.queryForList("select * from test_user"));

        Map<String, Object> record1 = new HashMap<>();
        record1.put("name", "jack");
        record1.put("id", 1);
        Map<String, Object> record2 = new HashMap<>();
        record2.put("name", "steve");
        record2.put("id", 2);
        String querySql3 = "update test_user set name = :name where id = :id";
        Map<String, Object>[] queryArg3 = new Map[] { record1, record2 };
        int[] result3 = jdbcTemplate.executeBatch(querySql3, queryArg3);
        PrintUtils.printObjectList(jdbcTemplate.queryForList("select * from test_user"));

        String querySql4 = "delete from test_user where id = ?";
        Object[][] queryArg4 = new Object[][] { new Object[] { 1 }, new Object[] { 2 } };
        int[] result4 = jdbcTemplate.executeBatch(querySql4, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setObject(1, queryArg4[i][0]);
            }

            public int getBatchSize() {
                return queryArg4.length;
            }
        });
        PrintUtils.printObjectList(jdbcTemplate.queryForList("select * from test_user"));
    }
}
