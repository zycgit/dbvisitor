package net.hasor.db.example.quick.page2;
import net.hasor.db.dal.session.BaseMapper;
import net.hasor.db.dal.session.DalSession;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;
import net.hasor.db.page.Page;
import net.hasor.db.page.PageObject;
import net.hasor.db.page.PageResult;

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
        PageResult<TestUser> pageData1 = baseMapper.queryByPage(pageInfo);
        PrintUtils.printObjectList(pageData1.getData());

        pageInfo.nextPage();
        PageResult<TestUser> pageData2 = baseMapper.queryByPage(pageInfo);
        PrintUtils.printObjectList(pageData2.getData());

    }
}
