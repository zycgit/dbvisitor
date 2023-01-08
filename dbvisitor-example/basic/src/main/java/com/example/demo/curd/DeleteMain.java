package com.example.demo.curd;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.lambda.EntityDeleteOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class DeleteMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);
        lambdaTemplate.loadSQL("CreateDB.sql");

        EntityDeleteOperation<TestUser> update = lambdaTemplate.lambdaDelete(TestUser.class);
        int result = update.eq(TestUser::getId, 1).doDelete();

        System.out.println("res = " + result);
        PrintUtils.printObjectList(lambdaTemplate.queryForList("select * from test_user"));
    }
}
