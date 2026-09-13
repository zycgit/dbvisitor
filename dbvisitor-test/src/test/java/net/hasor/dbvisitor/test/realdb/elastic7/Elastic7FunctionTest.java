/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.feature.function.FunctionCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;
import org.junit.After;
import org.junit.Before;

public class Elastic7FunctionTest extends FunctionCase {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();

    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() throws SQLException {
        jdbcTemplate = fixture.open(profile().env());
    }

    @Override
    @Before
    public void createFunctionFixtures() throws SQLException {
        jdbcTemplate = fixture.open(profile().env());
        fixture.insert(918101, "FuncAlice", 25, "func-alice@test.com");
        fixture.insert(918102, "FuncBob", 30, "func-bob@test.com");
        fixture.insert(918103, "FuncCharlie", 35, "func-charlie@test.com");
    }

    @After
    public void closeFixture() throws SQLException {
        fixture.close();
    }

    private String scripted(String alias, String script, String parameters, String predicate) {
        return "POST /" + fixture.index() + "/_search {\"size\": 1,\"_source\": [\"" + alias
                + "\"],\"query\": " + predicate + ",\"script_fields\": {\"" + alias
                + "\": {\"script\": {\"lang\": \"painless\",\"source\": \"" + script
                + "\",\"params\": " + parameters + "}}}}";
    }

    @Override
    protected String addNumbersQuerySql() {
        return scripted("value", "params.x + params.y", "{\"x\": ?,\"y\": ?}", "{\"match_all\": {}}");
    }

    @Override
    protected String multiplyNamedQuerySql() {
        return scripted("value", "params.x * params.y", "{\"x\": :x,\"y\": :y}", "{\"match_all\": {}}");
    }

    @Override
    protected String getUsernameQuerySql() {
        return scripted("value", "doc['name'].value", "{}", "{\"term\": {\"id\": ?}}");
    }

    @Override
    protected String transformStringQuerySql() {
        return scripted("text_value", "params.text.toUpperCase() + params.suffix", "{\"text\": ?,\"suffix\": ?}", "{\"match_all\": {}}");
    }
}
