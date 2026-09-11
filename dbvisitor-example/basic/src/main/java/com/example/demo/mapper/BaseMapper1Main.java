/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package com.example.demo.mapper;
import com.example.demo.DsUtils;
import net.hasor.dbvisitor.dialect.provider.MySqlDialect;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

public class BaseMapper1Main {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();

        Configuration config = new Configuration(Options.of().dialect(new MySqlDialect()));
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
