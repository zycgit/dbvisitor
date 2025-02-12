package com.example.demo.mapper;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileMapper2Main {
    public static void main(String[] args) throws Exception {
        Configuration config = new Configuration();
        config.loadMapper("/mapper/mapper_1/TestUserMapper.xml");

        DataSource dataSource = DsUtils.dsMySql();
        Session dalSession = config.newSession(dataSource);
        dalSession.jdbc().loadSQL("CreateDB.sql");

        Map<String, Object> ages = new HashMap<>();
        ages.put("age", 26);
        ages.put("name", "mali");
        ages.put("ids", Arrays.asList(1, 2, 3, 4));
        List<Object> listByAge4 = dalSession.queryStatement("queryByNameAndAge", ages);
        PrintUtils.printObjectList(listByAge4);
    }
}
