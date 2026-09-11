/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package com.example.demo.curd;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.MapInsert;

public class Insert3Main {
    // 纯 Map 模式，默认不开启驼峰转换。因此都是列名。
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        LambdaTemplate wrapper = new LambdaTemplate(dataSource);
        wrapper.jdbc().loadSQL("CreateDB.sql");
        wrapper.jdbc().execute("delete from test_user");

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("id", 20);
        newValue.put("name", "new name");
        newValue.put("age", 88);
        newValue.put("create_time", new Date());

        MapInsert insert = wrapper.insertFreedom("test_user");
        int result = insert.applyMap(newValue).executeSumResult();

        PrintUtils.printObjectList(wrapper.jdbc().queryForList("select * from test_user"));
    }
}
