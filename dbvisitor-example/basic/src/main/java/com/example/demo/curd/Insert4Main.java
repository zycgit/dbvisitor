package com.example.demo.curd;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.cobble.DateFormatType;
import net.hasor.dbvisitor.lambda.InsertOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Insert4Main {
    // 纯 Map 模式，开启驼峰转换，因此都是属性名。
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);
        lambdaTemplate.loadSQL("CreateDB.sql");
        lambdaTemplate.execute("delete from test_user");

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("id", 20);
        newValue.put("name", "new name");
        newValue.put("age", 88);
        newValue.put("createTime", DateFormatType.s_yyyyMMdd_HHmmss.toDate("2000-01-01 12:12:12"));

        InsertOperation<Map<String, Object>> insert = lambdaTemplate.lambdaInsert(null, "test_user", MappingOptions.buildNew().mapUnderscoreToCamelCase(true));
        int result = insert.applyMap(newValue).executeSumResult();

        PrintUtils.printObjectList(lambdaTemplate.queryForList("select * from test_user"));
    }
}
