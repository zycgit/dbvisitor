/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.contract.feature.naming.NamingConversionCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticNamingFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic7NamingConversionTest extends NamingConversionCase {
    private final ElasticNamingFixture fixture = new ElasticNamingFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        jdbcTemplate = fixture.open(profile().env());
        lambdaTemplate = fixture.lambda(Options.of());
    }

    @Override
    protected String insertCommand(String table, String columns) {
        return fixture.insert(columns);
    }

    @Override
    protected String userTable() {
        return fixture.index();
    }

    @Override
    protected String rawIdCondition() {
        return "{\"term\": {\"id\": ?}}";
    }

    @Override
    protected LambdaTemplate optionsLambda(Options options) throws SQLException {
        return fixture.lambda(options);
    }

    @Override
    protected void ensurePlainUserTable() {
        // This fixture has already created the physical index shared by both entity mappings.
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }
}
