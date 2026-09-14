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
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.contract.feature.naming.IdentifierCasingCase;
import net.hasor.dbvisitor.test.contract.material.model.naming.CaseTestUpperCI;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.Before;
import org.junit.After;

public class Elastic7IdentifierCasingTest extends IdentifierCasingCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        jdbcTemplate = fixture.open(profile().env(), CaseTestUpperCI.class,
                "{\"properties\": {\"Id\": {\"type\": \"integer\"}, \"Name\": {\"type\": \"keyword\"}, \"Age\": {\"type\": \"integer\"}, \"Memo\": {\"type\": \"keyword\"}}}");
        lambdaTemplate = fixture.lambdaTemplate();
    }

    @Override
    protected void prepareMixedCaseFields() {
        // The physical index is lowercase; its field names retain their declared case.
    }

    @Override
    protected String mixedCaseTableName() {
        return fixture.index();
    }

    @Override
    protected String insertCommand(String table, String columns) {
        return "POST /" + fixture.index() + "/_doc {\"Id\": ?, \"Name\": ?, \"Age\": ?, \"Memo\": ?}";
    }

    @Override
    protected LambdaTemplate optionsLambda(Options options) throws SQLException {
        MappingRegistry registry = new MappingRegistry(null, options);
        registry.loadEntityAsTable(CaseTestUpperCI.class, fixture.index());
        return new LambdaTemplate(fixture.connection(), registry, null);
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }
}
