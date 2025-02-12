package com.example.demo.curd;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.wrapper.EntityUpdateWrapper;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class Update1Main {
    // 使用实体映射 Table
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        WrapperAdapter wrapper = new WrapperAdapter(dataSource);
        wrapper.jdbc().loadSQL("CreateDB.sql");

        TestUser testUser = new TestUser();
        testUser.setName("new name");
        testUser.setAge(88);

        EntityUpdateWrapper<TestUser> update = wrapper.update(TestUser.class);
        int result = update.eq(TestUser::getId, 1).updateToSample(testUser).doUpdate();

        System.out.println("res = " + result);
        PrintUtils.printObjectList(wrapper.jdbc().queryForList("select * from test_user"));
    }
}
