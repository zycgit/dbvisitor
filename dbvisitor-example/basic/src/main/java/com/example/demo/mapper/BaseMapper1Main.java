package com.example.demo.mapper;
import com.example.demo.DsUtils;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.dal.session.DalSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

public class BaseMapper1Main {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();

        DalSession dalSession = new DalSession(dataSource);
        dalSession.lambdaTemplate().loadSQL("CreateDB.sql");

        BaseMapper<TestUser> baseMapper = dalSession.createBaseMapper(TestUser.class);
        baseMapper.query().queryForList();

        Iterator<TestUser> iterator = baseMapper.query().queryForIterator(200, 2, t -> t);
        while (iterator.hasNext()) {
            System.out.println(iterator.next().getName());
        }
    }
}
