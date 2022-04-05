package net.hasor.db.example.curd;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;
import net.hasor.db.lambda.EntityUpdateOperation;
import net.hasor.db.lambda.LambdaTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Update2Main {
    // 使用实体映射 Table，Map 作为 update 值
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);
        lambdaTemplate.loadSQL("CreateDB.sql");

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("name", "new name");
        newValue.put("age", 88);

        EntityUpdateOperation<TestUser> update = lambdaTemplate.lambdaUpdate(TestUser.class);
        int result = update.eq(TestUser::getId, 1).updateByMap(newValue).doUpdate();

        PrintUtils.printObjectList(lambdaTemplate.queryForList("select * from test_user"));

    }
}
