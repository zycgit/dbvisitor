package net.hasor.db.example.mapper;
import net.hasor.db.dal.session.BaseMapper;
import net.hasor.db.dal.session.DalSession;
import net.hasor.db.example.DsUtils;
import net.hasor.db.lambda.core.LambdaTemplate;

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
