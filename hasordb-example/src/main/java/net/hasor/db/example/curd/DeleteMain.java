package net.hasor.db.example.curd;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;
import net.hasor.db.lambda.LambdaOperations.LambdaDelete;
import net.hasor.db.lambda.core.LambdaTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class DeleteMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);
        lambdaTemplate.loadSQL("CreateDB.sql");

        LambdaDelete<TestUser> update = lambdaTemplate.lambdaDelete(TestUser.class);
        int result = update.eq(TestUser::getId, 1).doDelete();

        PrintUtils.printObjectList(lambdaTemplate.queryForList("select * from test_user"));

    }
}
