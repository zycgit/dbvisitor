package com.example.demo.curd;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.wrapper.MapUpdateWrapper;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Update3Main {
    // 纯 Map 模式
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        WrapperAdapter wrapper = new WrapperAdapter(dataSource);
        wrapper.jdbc().loadSQL("CreateDB.sql");

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("name", "new name");
        newValue.put("age", 88);

        MapUpdateWrapper update = wrapper.updateFreedom("test_user");
        int result = update.eq("id", 1).updateRowUsingMap(newValue).doUpdate();

        System.out.println("res = " + result);
        PrintUtils.printObjectList(wrapper.jdbc().queryForList("select * from test_user"));

    }
}
