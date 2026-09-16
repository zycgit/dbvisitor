/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.contract.feature.keygen.AssignedKeyCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoNamingFixture;
import org.junit.After;
import org.junit.Before;

public class MongoAssignedKeyTest extends AssignedKeyCase {
    private final MongoNamingFixture fixture = new MongoNamingFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
        this.lambdaTemplate = this.fixture.lambda(Options.of());
    }

    @Override
    protected void ensureStrictNoneTable() throws SQLException {
        this.fixture.createStrictKeyCollection();
    }

    @Override
    protected Integer countStrictKey(int id) throws SQLException {
        return this.jdbcTemplate.queryForObject("db.user_strict_none.count({id: ?})", new Object[] { id }, Integer.class);
    }

    @Override
    protected void deleteDuplicateKey(int id) throws SQLException {
        this.jdbcTemplate.executeUpdate("db.user_info.remove({id: ?})", new Object[] { id });
    }

    @Override
    protected boolean isNotNullMessage(Throwable error) {
        return super.isNotNullMessage(error) || lowerMessage(error).contains("document failed validation");
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }
}
