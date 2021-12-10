package net.hasor.db.example.mapper;
import net.hasor.db.dal.repository.DalRegistry;
import net.hasor.db.dal.session.DalSession;
import net.hasor.db.example.DsUtils;
import net.hasor.db.page.PageObject;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileMapper5Main {
    public static void main(String[] args) throws SQLException, IOException {
        DalRegistry dalRegistry = new DalRegistry();
        dalRegistry.loadMapper("/mapper/mapper_1/TestUserMapper.xml");

        DataSource dataSource = DsUtils.dsMySql();

        DalSession dalSession = new DalSession(dataSource);
        dalSession.lambdaTemplate().loadSQL("CreateDB.sql");

        Map<String, Object> ages = new HashMap<>();
        ages.put("age", 26);

        PageObject page = new PageObject();
        page.setPageSize(20);
        List<Object> result = dalSession.queryStatement("queryListByAge", ages, page);

    }
}
