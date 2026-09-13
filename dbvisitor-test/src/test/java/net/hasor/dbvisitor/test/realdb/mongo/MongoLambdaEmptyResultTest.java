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
import net.hasor.dbvisitor.test.contract.api.lambda.LambdaEmptyResultCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;
import org.junit.After;
import org.junit.Before;

public class MongoLambdaEmptyResultTest extends LambdaEmptyResultCase {
    private final NativeDocumentQueryFixture fixture = new NativeDocumentQueryFixture();

    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        this.jdbcTemplate = this.fixture.open(profile().env());
        this.lambdaTemplate = this.fixture.lambdaTemplate();
    }

    @Override
    protected void insert(int id, String name, Integer age, String email) throws SQLException {
        this.fixture.insert(id, name, age, email, new Date());
    }

    @After
    public void cleanupFixture() throws SQLException {
        this.fixture.close();
    }

    @Override
    protected String groupCountSelect() {
        return "{cnt: {$sum: 1}}";
    }

    @Override
    protected String countSelect() {
        return "[{$facet: {rows: [{$count: 'value'}]}},"
                + " {$project: {_id: 0, value: {$ifNull: [{$arrayElemAt: ['$rows.value', 0]}, 0]}}}]";
    }

    @Override
    protected String maxAgeSelect() {
        return "[{$facet: {rows: [{$group: {_id: null, value: {$max: '$age'}}}]}},"
                + " {$project: {_id: 0, value: {$ifNull: [{$arrayElemAt: ['$rows.value', 0]}, null]}}}]";
    }

    @Override
    protected String distinctSelect() {
        return "[{$group: {_id: '$id'}}, {$project: {_id: 0, id: '$_id'}}]";
    }
}
