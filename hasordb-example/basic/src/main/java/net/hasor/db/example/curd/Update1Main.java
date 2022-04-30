package net.hasor.db.example.curd;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;
import net.hasor.db.lambda.EntityUpdateOperation;
import net.hasor.db.lambda.LambdaTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class Update1Main {
    // 使用实体映射 Table
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);
        lambdaTemplate.loadSQL("CreateDB.sql");

        TestUser testUser = new TestUser();
        testUser.setName("new name");
        testUser.setAge(88);

        EntityUpdateOperation<TestUser> update = lambdaTemplate.lambdaUpdate(TestUser.class);
        int result = update.eq(TestUser::getId, 1).updateBySample(testUser).doUpdate();

        PrintUtils.printObjectList(lambdaTemplate.queryForList("select * from test_user"));

    }
}
