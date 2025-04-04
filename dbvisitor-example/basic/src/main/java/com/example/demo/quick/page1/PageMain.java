package com.example.demo.quick.page1;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.dialect.PageObject;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PageMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();

        // 查询，所有数据
        WrapperAdapter wrapper = new WrapperAdapter(dataSource, Options.of().dialect(new MySqlDialect()));

        wrapper.jdbc().loadSQL("CreateDB.sql");

        // 查询，所有数据
        List<TestUser> dtoList = wrapper.query(TestUser.class).queryForList();
        PrintUtils.printObjectList(dtoList);

        // 插入新数据
        Page pageInfo = new PageObject();
        pageInfo.setPageSize(3);
        List<TestUser> pageData1 = wrapper.query(TestUser.class).usePage(pageInfo).queryForList();
        PrintUtils.printObjectList(pageData1);

        pageInfo.nextPage();
        List<TestUser> pageData2 = wrapper.query(TestUser.class).usePage(pageInfo).queryForList();
        PrintUtils.printObjectList(pageData2);

    }
}
