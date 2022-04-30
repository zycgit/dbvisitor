package net.hasor.db.example.quick.page1;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;
import net.hasor.db.lambda.LambdaTemplate;
import net.hasor.db.page.Page;
import net.hasor.db.page.PageObject;

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
