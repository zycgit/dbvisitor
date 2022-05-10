package com.example.demo.mapper;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.dal.session.DalSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileMapper1Main {
    public static void main(String[] args) throws SQLException, IOException {
        DalRegistry dalRegistry = new DalRegistry();
        dalRegistry.loadMapper("/mapper/mapper_1/TestUserMapper.xml");

        DataSource dataSource = DsUtils.dsMySql();

        DalSession dalSession = new DalSession(dataSource, dalRegistry);
        dalSession.lambdaTemplate().loadSQL("CreateDB.sql");

        Map<String, Object> ages = new HashMap<>();
        ages.put("age", 26);
        List<Object> listByAge1 = dalSession.queryStatement("queryListByAge", ages);
        PrintUtils.printObjectList(listByAge1);

        List<Object> listByAge2 = dalSession.queryStatement("multipleListByAge", ages);
        PrintUtils.printObjectList(listByAge2);

        ages.put("age", 26);
        ages.put("name", "mali");
        List<Object> listByAge3 = dalSession.queryStatement("queryByNameAndAge", ages);
        PrintUtils.printObjectList(listByAge3);

        Map<String, Object> params = new HashMap<>();
        params.put("id", null);
        params.put("name", "form app");
        params.put("age", 128);
        params.put("createTime", new Date());
        int result = dalSession.executeStatement("insertUser", params);
        PrintUtils.printObjectList(dalSession.lambdaTemplate().queryForList("select * from `test_user`"));

    }
}
