package com.example.demo.curd;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.wrapper.EntityDeleteWrapper;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class DeleteMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        WrapperAdapter wrapper = new WrapperAdapter(dataSource);
        wrapper.jdbc().loadSQL("CreateDB.sql");

        EntityDeleteWrapper<TestUser> update = wrapper.delete(TestUser.class);
        int result = update.eq(TestUser::getId, 1).doDelete();

        System.out.println("res = " + result);
        PrintUtils.printObjectList(wrapper.jdbc().queryForList("select * from test_user"));
    }
}
