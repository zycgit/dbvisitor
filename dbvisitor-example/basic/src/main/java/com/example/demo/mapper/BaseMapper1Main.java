package com.example.demo.mapper;
import com.example.demo.DsUtils;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

public class BaseMapper1Main {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();

        Configuration config = new Configuration();
        Session session = config.newSession(dataSource);
        session.jdbc().loadSQL("CreateDB.sql");

        BaseMapper<TestUser> baseMapper = session.createBaseMapper(TestUser.class);
        baseMapper.query().queryForList();

        Iterator<TestUser> iterator = baseMapper.query().iteratorForLimit(200, 2, t -> t);
        while (iterator.hasNext()) {
            System.out.println(iterator.next().getName());
        }
    }
}
