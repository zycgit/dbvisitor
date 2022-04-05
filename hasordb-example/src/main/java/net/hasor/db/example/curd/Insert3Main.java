package net.hasor.db.example.curd;
import net.hasor.cobble.DateFormatType;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;
import net.hasor.db.lambda.InsertOperation;
import net.hasor.db.lambda.LambdaTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Insert3Main {
    // 纯 Map 模式，默认不开启驼峰转换。因此都是列名。
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);
        lambdaTemplate.loadSQL("CreateDB.sql");
        lambdaTemplate.execute("delete from test_user");

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("id", 20);
        newValue.put("name", "new name");
        newValue.put("age", 88);
        newValue.put("create_time", DateFormatType.s_yyyyMMdd_HHmmss.toDate("2000-01-01 12:12:12"));

        InsertOperation<Map<String, Object>> insert = lambdaTemplate.lambdaInsert("test_user");
        int result = insert.applyMap(newValue).executeSumResult();

        PrintUtils.printObjectList(lambdaTemplate.queryForList("select * from test_user"));
    }
}
