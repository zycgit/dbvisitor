package com.example.demo.quick.page2;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

public class PageMain {
    public static void main(String[] args) throws SQLException, IOException {
        // 创建 Session 并初始化一些数据
        DataSource dataSource = DsUtils.dsMySql();
        Configuration config = new Configuration(Options.of().dialect(new MySqlDialect()));
        Session session = config.newSession(dataSource);
        session.jdbc().loadSQL("CreateDB.sql");

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
