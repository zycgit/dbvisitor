package com.example.demo.quick.page2;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.dal.session.DalSession;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PageMain {
    public static void main(String[] args) throws SQLException, IOException {
        // 创建 Session 并初始化一些数据
        DataSource dataSource = DsUtils.dsMySql();
        DalSession session = new DalSession(dataSource);
        session.lambdaTemplate().loadSQL("CreateDB.sql");

        BaseMapper<TestUser> baseMapper = session.createBaseMapper(TestUser.class);

        // 查询，所有数据
        List<TestUser> dtoList = baseMapper.query().queryForList();
        PrintUtils.printObjectList(dtoList);

        Page pageInfo = new PageObject();
        pageInfo.setPageSize(3);
        PageResult<TestUser> pageData1 = baseMapper.pageBySample(null, pageInfo);
        PrintUtils.printObjectList(pageData1.getData());

        pageInfo.nextPage();
        PageResult<TestUser> pageData2 = baseMapper.pageBySample(null, pageInfo);
        PrintUtils.printObjectList(pageData2.getData());

    }
}
