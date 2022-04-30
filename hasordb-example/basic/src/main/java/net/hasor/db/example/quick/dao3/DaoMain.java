package net.hasor.db.example.quick.dao3;
import net.hasor.db.dal.repository.DalRegistry;
import net.hasor.db.dal.session.DalSession;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DaoMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();

        // 创建 DalRegistry 并注册 TestUserDAO
        DalRegistry dalRegistry = new DalRegistry();
        dalRegistry.loadMapper(TestUserDAO.class);
        // 使用 DalRegistry 创建 Session
        DalSession session = new DalSession(dataSource, dalRegistry);
        // 创建 DAO 接口
        TestUserDAO userDAO = session.createMapper(TestUserDAO.class);

        userDAO.template().loadSQL("CreateDB.sql");

        int result1 = userDAO.insertUser("abc", 123);
        PrintUtils.printObjectList(userDAO.queryAll());

        int result2 = userDAO.updateAge(1, 223);
        PrintUtils.printObjectList(userDAO.queryAll());

        int result3 = userDAO.deleteByAge(100);
        PrintUtils.printObjectList(userDAO.queryAll());

        List<TestUser> testUsers = userDAO.queryByAge(20, 30);
        PrintUtils.printObjectList(testUsers);
    }
}
