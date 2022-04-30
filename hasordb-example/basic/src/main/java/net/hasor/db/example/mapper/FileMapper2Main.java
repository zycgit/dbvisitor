package net.hasor.db.example.mapper;
import net.hasor.db.dal.session.DalSession;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileMapper2Main {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        DalSession dalSession = new DalSession(dataSource);
        dalSession.lambdaTemplate().loadSQL("CreateDB.sql");

        dalSession.getDalRegistry().loadMapper("/mapper/mapper_1/TestUserMapper.xml");

        Map<String, Object> ages = new HashMap<>();
        ages.put("age", 26);
        ages.put("name", "mali");
        ages.put("ids", Arrays.asList(1, 2, 3, 4));
        List<Object> listByAge4 = dalSession.queryStatement("queryByNameAndAge", ages);
        PrintUtils.printObjectList(listByAge4);

    }
}
