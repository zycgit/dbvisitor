package com.example.demo.jdbc;
import com.example.demo.DsUtils;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ResultSetExtractorMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.loadSQL("CreateDB.sql");

        String queryString = "select * from test_user";
        Map<Integer, String> idMap = jdbcTemplate.query(queryString, new ResultSetExtractor<Map<Integer, String>>() {
            public Map<Integer, String> extractData(ResultSet rs) throws SQLException {
                Map<Integer, String> hashMap = new HashMap<>();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    hashMap.put(id, name);
                }
                return hashMap;
            }
        });
        System.out.println(idMap);
    }
}
