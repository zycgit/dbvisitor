package com.example.demo.curd;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.cobble.DateFormatType;
import net.hasor.dbvisitor.lambda.EntityInsert;
import net.hasor.dbvisitor.lambda.LambdaTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class Insert1Main {
    // 使用实体映射 Table
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        LambdaTemplate wrapper = new LambdaTemplate(dataSource);
        wrapper.jdbc().loadSQL("CreateDB.sql");
        wrapper.jdbc().execute("delete from test_user");

        TestUser testUser = new TestUser();
        testUser.setId(20);
        testUser.setName("new name");
        testUser.setAge(88);
        testUser.setCreateTime(DateFormatType.s_yyyyMMdd_HHmmss.toDate("2000-01-01 12:12:12"));

        EntityInsert<TestUser> insert = wrapper.insert(TestUser.class);
        int result = insert.applyEntity(testUser).executeSumResult();

        PrintUtils.printObjectList(wrapper.jdbc().queryForList("select * from test_user"));
    }
}
