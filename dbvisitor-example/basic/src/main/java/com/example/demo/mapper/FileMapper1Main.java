package com.example.demo.mapper;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

import javax.sql.DataSource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileMapper1Main {
    public static void main(String[] args) throws Exception {
        Configuration config = new Configuration();
        config.loadMapper("/mapper/mapper_1/TestUserMapper.xml");

        DataSource dataSource = DsUtils.dsMySql();

        Session session = config.newSession(dataSource);
        session.jdbc().loadSQL("CreateDB.sql");

        Map<String, Object> ages = new HashMap<>();
        ages.put("age", 26);
        List<Object> listByAge1 = session.queryStatement("queryListByAge", ages);
        PrintUtils.printObjectList(listByAge1);

        Map<String, Object> listByAge2 = (Map<String, Object>) (session.queryStatement("multipleListByAge", ages).get(0));
        PrintUtils.printObjectList((List<?>) listByAge2.get("res1"));
        PrintUtils.printObjectList((List<?>) listByAge2.get("res2"));

        ages.put("age", 26);
        ages.put("name", "mali");
        List<Object> listByAge3 = session.queryStatement("queryByNameAndAge", ages);
        PrintUtils.printObjectList(listByAge3);

        Map<String, Object> params = new HashMap<>();
        params.put("id", null);
        params.put("name", "form app");
        params.put("age", 128);
        params.put("createTime", new Date());
        int result = (int) session.executeStatement("insertUser", params);
        PrintUtils.printObjectList(session.jdbc().queryForList("select * from `test_user`"));

    }
}
