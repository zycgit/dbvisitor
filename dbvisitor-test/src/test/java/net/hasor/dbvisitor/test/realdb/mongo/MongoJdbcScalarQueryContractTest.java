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
import net.hasor.dbvisitor.test.contract.api.adapter.NativeDocumentQueryFixture;
import net.hasor.dbvisitor.test.contract.api.jdbc.JdbcScalarQueryContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import org.junit.After;
import org.junit.Before;

public class MongoJdbcScalarQueryContractTest extends JdbcScalarQueryContractTest {
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
    protected void insertUser(int id, String name, int age, String email, Date createTime) throws SQLException {
        this.fixture.insert(id, name, age, email, createTime);
    }

    @Override
    protected String selectById(String columns, String parameter) throws SQLException {
        return this.fixture.selectById(columns, parameter);
    }

    @Override
    protected String selectRange(String columns, String lower, String upper, boolean ordered) throws SQLException {
        return this.fixture.selectRange(columns, lower, upper, ordered);
    }

    @Override
    protected String countRange(String lower, String upper) throws SQLException {
        return this.fixture.countRange(lower, upper);
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }
}
