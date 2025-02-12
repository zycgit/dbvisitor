package com.example.demo.quick.page3;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.dialect.PageObject;
import net.hasor.dbvisitor.dialect.PageResult;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

import javax.sql.DataSource;

public class PageMain2 {
    public static void main(String[] args) throws Exception {
        // 创建 DalRegistry 并注册 TestUserDAO
        Configuration config = new Configuration();
        config.loadMapper(TestUserDAO.class);
        // 创建 Session 并初始化一些数据
        DataSource dataSource = DsUtils.dsMySql();
        Session session = config.newSession(dataSource);
        session.jdbc().loadSQL("CreateDB.sql");

        // 创建 DAO 接口
        TestUserDAO userDAO = session.createMapper(TestUserDAO.class);

        // 构建分页条件
        Page pageInfo = new PageObject();
        pageInfo.setPageSize(3);

        // 分页方式查询 mapper 中的查询
        PageResult<TestUser> dtoList1 = userDAO.queryByAge2(25, 100, pageInfo);
        PrintUtils.printObjectList(dtoList1.getData());

        // 分页方式查询 mapper 中的查询
        pageInfo.nextPage();
        PageResult<TestUser> dtoList2 = userDAO.queryByAge2(25, 100, pageInfo);
        PrintUtils.printObjectList(dtoList2.getData());
    }
}
