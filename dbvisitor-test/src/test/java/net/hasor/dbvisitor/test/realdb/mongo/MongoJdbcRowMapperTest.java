/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import java.util.Date;

import org.junit.After;
import org.junit.Before;

import net.hasor.dbvisitor.test.contract.api.adapter.NativeDocumentQueryFixture;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcRowMapperCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;

public class MongoJdbcRowMapperTest extends JdbcRowMapperCase {
    private final NativeDocumentQueryFixture fixture = new NativeDocumentQueryFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open(profile().env());
    }

    @Override
    protected void insertUser(int id, String name, int age, String email) throws SQLException {
        this.fixture.insert(id, name, age, email, new Date());
    }

    @Override
    protected String selectSql(String columns, String predicate, boolean ordered) throws SQLException {
        return this.fixture.selectPredicate(columns, predicate, ordered);
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }
}
