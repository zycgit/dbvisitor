/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.jdbc.FunctionCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import net.hasor.dbvisitor.test.realdb.mongo.material.MongoEntityFixture;
import org.junit.After;
import org.junit.Before;

/** Native aggregation functions preserve the shared parameter and result assertions. */
public class MongoFunctionTest extends FunctionCase {
    private final MongoEntityFixture fixture = new MongoEntityFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open();
    }

    @Override
    @Before
    public void createFunctionFixtures() throws SQLException {
        this.jdbcTemplate.executeUpdate(this.fixture.command("insertOne({id: 918101, name: 'FuncAlice'})"));
    }

    @After
    public void closeFixture() throws SQLException {
        this.fixture.close();
    }

    @Override
    protected String addNumbersQuerySql() {
        return this.fixture.command("aggregate([{$project: {_id: 0, value: {$add: [{$literal: ?}, {$literal: ?}]}}}])");
    }

    @Override
    protected String multiplyNamedQuerySql() {
        return this.fixture.command("aggregate([{$project: {_id: 0, value: {$multiply: [{$literal: :x}, {$literal: :y}]}}}])");
    }

    @Override
    protected String getUsernameQuerySql() {
        return this.fixture.command("aggregate([{$match: {id: ?}}, {$project: {_id: 0, value: {$concat: ['$name', '']}}}])");
    }

    @Override
    protected String transformStringQuerySql() {
        return this.fixture.command("aggregate([{$project: {_id: 0, text_value: {$concat: [{$toUpper: {$literal: ?}}, {$literal: ?}]}}}])");
    }
}
