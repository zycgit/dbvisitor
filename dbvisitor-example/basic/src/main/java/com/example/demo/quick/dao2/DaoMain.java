package com.example.demo.quick.dao2;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

import javax.sql.DataSource;
import java.util.List;

public class DaoMain {
    public static void main(String[] args) throws Exception {
        DataSource dataSource = DsUtils.dsMySql();

        // 创建 DalRegistry 并注册 TestUserDAO
        Configuration config = new Configuration();
        config.loadMapper(TestUserDAO.class);
        // 使用 DalRegistry 创建 Session
        Session session = config.newSession(dataSource);
        // 创建 DAO 接口
        TestUserDAO userDAO = session.createMapper(TestUserDAO.class);

        userDAO.jdbc().loadSQL("CreateDB.sql");

        int result1 = userDAO.insertUser("abc", 123);
        PrintUtils.printObjectList(userDAO.query().queryForList());

        int result2 = userDAO.updateAge(1, 223);
        PrintUtils.printObjectList(userDAO.query().queryForList());

        int result3 = userDAO.deleteByAge(100);
        PrintUtils.printObjectList(userDAO.query().queryForList());

        List<TestUser> testUsers = userDAO.queryByAge(20, 30);
        PrintUtils.printObjectList(testUsers);
    }
}
