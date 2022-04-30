package net.hasor.db.example.curd;
import net.hasor.cobble.DateFormatType;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;
import net.hasor.db.lambda.InsertOperation;
import net.hasor.db.lambda.LambdaTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class Insert1Main {
    // 使用实体映射 Table
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);
        lambdaTemplate.loadSQL("CreateDB.sql");
        lambdaTemplate.execute("delete from test_user");

        TestUser testUser = new TestUser();
        testUser.setId(20);
        testUser.setName("new name");
        testUser.setAge(88);
        testUser.setCreateTime(DateFormatType.s_yyyyMMdd_HHmmss.toDate("2000-01-01 12:12:12"));

        InsertOperation<TestUser> insert = lambdaTemplate.lambdaInsert(TestUser.class);
        int result = insert.applyEntity(testUser).executeSumResult();

        PrintUtils.printObjectList(lambdaTemplate.queryForList("select * from test_user"));
    }
}
