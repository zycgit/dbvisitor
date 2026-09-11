/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package com.example.demo.curd;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.lambda.EntityDelete;
import net.hasor.dbvisitor.lambda.LambdaTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class DeleteMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        LambdaTemplate wrapper = new LambdaTemplate(dataSource);
        wrapper.jdbc().loadSQL("CreateDB.sql");

        EntityDelete<TestUser> update = wrapper.delete(TestUser.class);
        int result = update.eq(TestUser::getId, 1).doDelete();

        System.out.println("res = " + result);
        PrintUtils.printObjectList(wrapper.jdbc().queryForList("select * from test_user"));
    }
}
