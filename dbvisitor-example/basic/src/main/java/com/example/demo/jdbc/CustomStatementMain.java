package com.example.demo.jdbc;
import com.example.demo.DsUtils;
import net.hasor.dbvisitor.jdbc.PreparedStatementCreator;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.jdbc.mapper.MappingRowMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomStatementMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.loadSQL("CreateDB.sql");

        // 定制 PreparedStatement
        PreparedStatementCreator creator = con -> {
            PreparedStatement ps = con.prepareStatement(//
                    "select * from test_user", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE);
            return ps;
        };

        // 行读取工具
        MappingRowMapper<TestUser> rowMapper = new MappingRowMapper<>(TestUser.class);

        // 流式消费数据
        RowCallbackHandler handler = (rs, rowNum) -> {
            TestUser dto = rowMapper.mapRow(rs, rowNum);

        };

        // 执行流式处理
        jdbcTemplate.executeCreator(creator, handler);
    }
}
