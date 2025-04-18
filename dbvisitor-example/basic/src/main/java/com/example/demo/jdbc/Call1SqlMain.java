package com.example.demo.jdbc;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.SqlArg;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Call1SqlMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.loadSQL("CreateDB.sql");

        // 执行存储过程并接收所有返回的数据（输出参数、结果集、影响行数）
        List<SqlArg> parameters = new ArrayList<>();
        parameters.add(SqlArg.valueOf("dative", Types.VARCHAR));
        parameters.add(SqlArg.asOut("outName", Types.VARCHAR));
        Map<String, Object> resultMap = jdbcTemplate.call("{call proc_select_table(?,?)}", parameters);

        String outName = (String) resultMap.get("outName");
        List<Map<String, Object>> result1 = (List<Map<String, Object>>) resultMap.get("#result-set-1");
        List<Map<String, Object>> result2 = (List<Map<String, Object>>) resultMap.get("#result-set-2");

        System.out.println(outName);
        PrintUtils.printObjectList(result1);
        PrintUtils.printObjectList(result2);
    }
}
