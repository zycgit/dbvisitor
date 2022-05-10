package com.example.demo.quick.page1;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PageMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();

        // 查询，所有数据
        LambdaTemplate lambdaTemplate = new LambdaTemplate(dataSource);

        lambdaTemplate.loadSQL("CreateDB.sql");

        // 查询，所有数据
        List<TestUser> dtoList = lambdaTemplate.lambdaQuery(TestUser.class).queryForList();
        PrintUtils.printObjectList(dtoList);

        // 插入新数据
        Page pageInfo = new PageObject();
        pageInfo.setPageSize(3);
        List<TestUser> pageData1 = lambdaTemplate.lambdaQuery(TestUser.class).usePage(pageInfo).queryForList();
        PrintUtils.printObjectList(pageData1);

        pageInfo.nextPage();
        List<TestUser> pageData2 = lambdaTemplate.lambdaQuery(TestUser.class).usePage(pageInfo).queryForList();
        PrintUtils.printObjectList(pageData2);

    }
}
