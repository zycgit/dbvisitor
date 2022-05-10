package com.example.demo.curd;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.MapUpdateOperation;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Update3Main {
    // 纯 Map 模式
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);
        lambdaTemplate.loadSQL("CreateDB.sql");

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("name", "new name");
        newValue.put("age", 88);

        MapUpdateOperation update = lambdaTemplate.lambdaUpdate("test_user");
        int result = update.eq("id", 1).updateByMap(newValue).doUpdate();

        PrintUtils.printObjectList(lambdaTemplate.queryForList("select * from test_user"));

    }
}
