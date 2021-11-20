package net.hasor.db.example.jdbc;
import net.hasor.db.example.DsUtils;
import net.hasor.db.jdbc.SqlParameter;
import net.hasor.db.jdbc.SqlParameterUtils;
import net.hasor.db.jdbc.core.JdbcTemplate;

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
        List<SqlParameter> parameters = new ArrayList<>();
        parameters.add(SqlParameterUtils.withInput("dative", Types.VARCHAR));
        parameters.add(SqlParameterUtils.withOutputName("outName", Types.VARCHAR));
        Map<String, Object> resultMap = jdbcTemplate.call("{call proc_select_table(?,?)}", parameters);

        String outName = (String) resultMap.get("outName");
        List<Map<String, Object>> result1 = (List<Map<String, Object>>) resultMap.get("#result-set-1");
        List<Map<String, Object>> result2 = (List<Map<String, Object>>) resultMap.get("#result-set-2");

    }
}
