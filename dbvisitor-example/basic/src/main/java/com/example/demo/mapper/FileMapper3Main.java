package com.example.demo.mapper;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.dialect.PageObject;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileMapper3Main {
    public static void main(String[] args) throws Exception {
        Configuration config = new Configuration(Options.of().dialect(new MySqlDialect()));
        config.loadMapper("/mapper/mapper_1/TestUserMapper.xml");

        DataSource dataSource = DsUtils.dsMySql();
        Session dalSession = config.newSession(dataSource);
        dalSession.jdbc().loadSQL("CreateDB.sql");

        Map<String, Object> ages = new HashMap<>();
        ages.put("age", 26);

        PageObject page = new PageObject();
        page.setPageSize(20);
        List<Object> result = dalSession.queryStatement("queryListByAge", ages, page);

        PrintUtils.printObjectList(result);
    }
}
