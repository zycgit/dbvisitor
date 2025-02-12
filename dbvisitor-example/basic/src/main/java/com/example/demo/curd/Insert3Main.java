package com.example.demo.curd;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.cobble.DateFormatType;
import net.hasor.dbvisitor.wrapper.EntityInsertWrapper;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Insert3Main {
    // 纯 Map 模式，默认不开启驼峰转换。因此都是列名。
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        WrapperAdapter wrapper = new WrapperAdapter(dataSource);
        wrapper.jdbc().loadSQL("CreateDB.sql");
        wrapper.jdbc().execute("delete from test_user");

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("id", 20);
        newValue.put("name", "new name");
        newValue.put("age", 88);
        newValue.put("create_time", DateFormatType.s_yyyyMMdd_HHmmss.toDate("2000-01-01 12:12:12"));

        EntityInsertWrapper<Map<String, Object>> insert = wrapper.insert("test_user");
        int result = insert.applyMap(newValue).executeSumResult();

        PrintUtils.printObjectList(wrapper.jdbc().queryForList("select * from test_user"));
    }
}
