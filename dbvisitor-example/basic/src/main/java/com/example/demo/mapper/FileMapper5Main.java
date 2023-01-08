package com.example.demo.mapper;
import com.example.demo.DsUtils;
import net.hasor.dbvisitor.dal.repository.DalRegistry;
import net.hasor.dbvisitor.dal.session.DalSession;
import net.hasor.dbvisitor.page.PageObject;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileMapper5Main {
    public static void main(String[] args) throws Exception {
        DalRegistry dalRegistry = new DalRegistry();
        dalRegistry.loadMapper("/mapper/mapper_1/TestUserMapper.xml");

        DataSource dataSource = DsUtils.dsMySql();

        DalSession dalSession = new DalSession(dataSource, dalRegistry);
        dalSession.lambdaTemplate().loadSQL("CreateDB.sql");

        Map<String, Object> ages = new HashMap<>();
        ages.put("age", 26);

        PageObject page = new PageObject();
        page.setPageSize(20);
        List<Object> result = dalSession.queryStatement("queryListByAge", ages, page);

    }
}
