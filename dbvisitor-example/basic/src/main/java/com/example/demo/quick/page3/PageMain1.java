package com.example.demo.quick.page3;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.dal.session.DalSession;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;

import javax.sql.DataSource;
import java.util.List;

public class PageMain1 {
    public static void main(String[] args) throws Exception {
        // 创建 DalRegistry 并注册 TestUserDAO
        DalRegistry dalRegistry = new DalRegistry();
        dalRegistry.loadMapper(TestUserDAO.class);
        // 创建 Session 并初始化一些数据
        DataSource dataSource = DsUtils.dsMySql();
        DalSession session = new DalSession(dataSource, dalRegistry);
        session.lambdaTemplate().loadSQL("CreateDB.sql");

        // 创建 DAO 接口
        TestUserDAO userDAO = session.createMapper(TestUserDAO.class);

        // 构建分页条件
        Page pageInfo = new PageObject();
        pageInfo.setPageSize(3);

        // 分页方式查询 mapper 中的查询
        List<TestUser> dtoList1 = userDAO.queryByAge1(25, 100, pageInfo);
        PrintUtils.printObjectList(dtoList1);

        // 分页方式查询 mapper 中的查询
        pageInfo.nextPage();
        List<TestUser> dtoList2 = userDAO.queryByAge1(25, 100, pageInfo);
        PrintUtils.printObjectList(dtoList2);
    }
}
