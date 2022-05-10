package com.example.demo.mapper;
import com.example.demo.DsUtils;
import net.hasor.dbvisitor.dal.session.BaseMapper;
import net.hasor.dbvisitor.dal.session.DalSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class BaseMapper1Main {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();

        DalSession dalSession = new DalSession(dataSource);
        dalSession.lambdaTemplate().loadSQL("CreateDB.sql");

        BaseMapper<TestUser> baseMapper = dalSession.createBaseMapper(TestUser.class);
        baseMapper.query().queryForList();

        //
        //        Map<String, Object> newValue = new HashMap<>();
        //        newValue.put("id", 20);
        //        newValue.put("name", "new name");
        //        newValue.put("age", 88);
        //        newValue.put("create_time", new Date());
        //
        //        LambdaInsert<TestUser> insert = lambdaTemplate.lambdaInsert(TestUser.class);
        //        int result = insert.applyMap(newValue).executeSumResult();
        //
        //        PrintUtils.printObjectList(lambdaTemplate.queryForList("select * from test_user"));
    }
}
